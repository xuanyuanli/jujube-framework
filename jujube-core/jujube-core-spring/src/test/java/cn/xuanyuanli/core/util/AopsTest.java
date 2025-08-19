package cn.xuanyuanli.core.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import lombok.Cleanup;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.aop.framework.AopContext;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

class AopsTest {

    static ProxyImpl cglibProxyImpl;

    @BeforeAll
    static void setup() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ProxyImpl.class);
        enhancer.setCallback(new CglibProxy());
        cglibProxyImpl = (ProxyImpl) enhancer.create();
    }

    @Test
    void testGetSelfJavaProxy() {
        JavaProxy mapperProxy = new JavaProxy();
        ProxyApi t = (ProxyApi) java.lang.reflect.Proxy.newProxyInstance(ProxyApi.class.getClassLoader(), new Class[]{ProxyApi.class},
                mapperProxy);
        @Cleanup MockedStatic<AopContext> utilities = Mockito.mockStatic(AopContext.class);
        utilities.when(AopContext::currentProxy).thenReturn(t);
        // 这里不能使用equals方法，因为equals方法会调用JDK代理的equals方法，导致死循环。应该用==比较
        org.junit.jupiter.api.Assertions.assertSame(Aops.getSelf(t), t);
        Assertions.assertThat(Aops.getSelf(new ProxyImpl())).isNotEqualTo(t);
    }

    @Test
    void testGetSelfCglibProxy() {
        try (MockedStatic<AopContext> utilities = Mockito.mockStatic(AopContext.class)) {
            utilities.when(AopContext::currentProxy).thenReturn(cglibProxyImpl);
            new ProxyImpl().sayHello();
            utilities.verify(AopContext::currentProxy);
        }
    }

    public interface ProxyApi {

    }

    public static class ProxyImpl implements ProxyApi {

        void sayHello() {
            Assertions.assertThat(Aops.getSelf(this).toString()).isEqualTo(cglibProxyImpl.toString());
        }
    }

    public static class CglibProxy implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            return method.invoke(proxy, args);
        }
    }

    public static class JavaProxy implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(proxy, args);
        }
    }
}
