package cn.qsky.redis.jedis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtil {

  private static volatile JedisPool jedisPool = null;

  private JedisPoolUtil() {
  }

  public static JedisPool getJedisPoolInstance() {
    if (null == jedisPool) {
      synchronized (JedisPoolUtil.class) {
        if (null == jedisPool) {//双端检索机制
          JedisPoolConfig poolConfig = new JedisPoolConfig();
          poolConfig.setMaxTotal(1000);
          poolConfig.setMaxIdle(32);
          poolConfig.setMaxWaitMillis(100 * 1000);
          poolConfig.setTestOnBorrow(true);
          jedisPool = new JedisPool(poolConfig, "192.168.56.103", 30036);
        }
      }
    }
    return jedisPool;
  }

  public static void release(final Jedis jedis) {//释放资源
    if (null != jedis) {
      jedis.close();
    }
  }
}
