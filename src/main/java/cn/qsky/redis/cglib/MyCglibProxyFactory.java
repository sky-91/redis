package cn.qsky.redis.cglib;

import java.lang.reflect.Method;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

public class MyCglibProxyFactory implements MethodInterceptor {

  public Object getInstance(Class<?> c) {
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(c);
    enhancer.setCallback(this);
    return enhancer.create();
  }

  public Object intercept(Object obj, Method method, Object[] args,
      MethodProxy proxy) throws Throwable {
    System.out.println("before cook!");
    proxy.invokeSuper(obj, args);
    System.out.println("after cook!");
    return null;
  }
}
