package cn.qsky.redis.jedis;

import junit.framework.TestCase;
import redis.clients.jedis.Jedis;

public class JedisTest extends TestCase {
  public void testJedis()
  {
    Jedis jedis = new Jedis("192.168.56.103", 30036);
    System.out.println(jedis.ping());
  }
}
