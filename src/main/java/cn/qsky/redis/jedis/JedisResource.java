package cn.qsky.redis.jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("jedis")
public class JedisResource {

  private static final Logger LOG = LoggerFactory.getLogger(JedisResource.class);
  @Resource
  private JedisService jedisService;

  @GetMapping("/setnx/{key}/{val}")
  public boolean setnx(@PathVariable String key, @PathVariable String val) {

    LOG.debug("key: " + key + ", value: " + val);
    return jedisService.setnx(key, val);
  }

  @GetMapping("/delnx/{key}/{val}")
  public int delnx(@PathVariable String key, @PathVariable String val) {
    return jedisService.delnx(key, val);
  }


  //总库存
  private long inventory = 0;
  //商品key名字
  private String productKey = "computer_key";
  //获取锁的超时时间 秒
  private int timeout = 30 * 1000;

  @GetMapping("/qiangdan")
  public List<String> qiangdan() {

    //抢到商品的用户
    List<String> shopUsers = new ArrayList<>();

    //构造很多用户
    List<String> users = new ArrayList<>();
    IntStream.range(0, 10).parallel().forEach(b -> users.add("黄牛-" + b));

    //初始化库存
    inventory = 2;

    //模拟开抢
    users.parallelStream().forEach(b -> {
      String shopUser = qiang(b);
      if (!StringUtils.isEmpty(shopUser)) {
        shopUsers.add(shopUser);
      }
    });
    LOG.debug("redis中记录的成功抢到的用户：");
    jedisService.consumeOrder("success").forEach(user -> LOG.debug(user));

    return shopUsers;
  }

  /**
   * 模拟抢单动作
   */
  private String qiang(String b) {
    //用户开抢时间
    long startTime = System.currentTimeMillis();

    //未抢到的情况下，30秒内继续获取锁
    while ((startTime + timeout) >= System.currentTimeMillis()) {
      //商品是否剩余
      LOG.info("{}申请锁...", b);
      if (inventory <= 0) {
        break;
      }
      if (jedisService.setnx(productKey, b)) {
        //用户b拿到锁
        LOG.info("{}拿到锁...", b);
        try {
          //商品是否剩余
          if (inventory <= 0) {
            break;
          }

          //模拟生成订单耗时操作，方便查看：神牛-50 多次获取锁记录
          try {
            TimeUnit.MILLISECONDS.sleep(100);
            jedisService.lpush("success", b);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }

          //抢购成功，商品递减，记录用户
          inventory -= 1;

          //抢单成功跳出
          LOG.info("{}抢单成功释放锁...所剩库存：{}", b, inventory);

          return b + "抢单成功，所剩库存：" + inventory;
        } finally {
          LOG.info("{}释放锁...", b);
          //释放锁
          jedisService.delnx(productKey, b);
        }
      } else {
        //用户b没拿到锁，在超时范围内继续请求锁，不需要处理
        LOG.debug("{}没抢到锁，继续无限申请锁", b);
      }
    }
    return "";
  }
}
