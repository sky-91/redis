package cn.qsky.redis.jedis;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

@Service
public class JedisService {

  public boolean setnx(String key, String val) {
    Jedis jedis = null;
    try {
      jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
      if (jedis == null) {
        return false;
      }
      return jedis.set(key, val, new SetParams().nx().px(30000)).
          equalsIgnoreCase("ok");
    } catch (Exception ex) {
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    return false;
  }

  public int delnx(String key, String val) {
    Jedis jedis = null;
    try {
      jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
      if (jedis == null) {
        return 0;
      }

      //if redis.call('get','orderkey')=='1111' then return redis.call('del','orderkey') else return 0 end
      StringBuilder sbScript = new StringBuilder();
      sbScript.append("if redis.call('get','").append(key).append("')").append("=='").append(val)
          .append("'")
          .append(" then ")
          .append("    return redis.call('del','").append(key).append("')")
          .append(" else ")
          .append("    return 0")
          .append(" end");

      return Integer.valueOf(jedis.eval(sbScript.toString()).toString());
    } catch (Exception ex) {
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    return 0;
  }

  public int lpush(String key, String val) {
    Jedis jedis = null;
    try {
      jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
      if (jedis == null) {
        return 0;
      }
      return jedis.lpush(key, val).intValue();
    } catch (Exception ex) {
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    return 0;
  }

  public List<String> consumeOrder(String key) {
    Jedis jedis = null;
    List<String> result = new ArrayList<>();
    try {
      jedis = JedisPoolUtil.getJedisPoolInstance().getResource();
      if (jedis == null) {
        return result;
      }
      if (jedis.llen(key) > 0) {
        for (int i = 0; i <= jedis.llen(key); i++) {
          result.add(jedis.rpop(key));
        }
      }
    } catch (Exception ex) {
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    return result;
  }
}
