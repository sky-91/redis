package cn.qsky.redis.cglib;

public class TestCglibProxy {

  public static void main(String[] args) {
    MyCglibProxyFactory cglib = new MyCglibProxyFactory();
    CookService cook = (CookService) cglib.getInstance(new CookService().getClass());
    cook.cookFish();
    System.out.println(cook.getClass());
  }
}
