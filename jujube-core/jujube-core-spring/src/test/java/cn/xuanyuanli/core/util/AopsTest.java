package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import lombok.Cleanup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.aop.framework.AopContext;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@DisplayName("AOP工具类测试")
class AopsTest {

    static ProxyImpl cglibProxyImpl;

    @BeforeAll
    static void setup() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ProxyImpl.class);
        enhancer.setCallback(new CglibProxy());
        cglibProxyImpl = (ProxyImpl) enhancer.create();
    }

    @Nested
    @DisplayName("获取自身对象")
    class GetSelf {

        @Test
        @DisplayName("应该返回相同的代理对象 - 当使用JDK动态代理时")
        void getSelf_shouldReturnSameProxyObject_whenUsingJdkDynamicProxy() {
            // Arrange
            JavaProxy mapperProxy = new JavaProxy();
            ProxyApi jdkProxy = (ProxyApi) java.lang.reflect.Proxy.newProxyInstance(
                    ProxyApi.class.getClassLoader(), 
                    new Class[]{ProxyApi.class}, 
                    mapperProxy);
            
            @Cleanup MockedStatic<AopContext> utilities = Mockito.mockStatic(AopContext.class);
            utilities.when(AopContext::currentProxy).thenReturn(jdkProxy);

            // Act & Assert
            // 这里不能使用equals方法，因为equals方法会调用JDK代理的equals方法，导致死循环。应该用==比较
            assertThat(Aops.getSelf(jdkProxy)).isSameAs(jdkProxy);
            assertThat(Aops.getSelf(new ProxyImpl())).isNotEqualTo(jdkProxy);
        }

        @Test
        @DisplayName("应该验证AopContext调用 - 当使用CGLIB代理时")
        void getSelf_shouldVerifyAopContextCall_whenUsingCglibProxy() {
            try (MockedStatic<AopContext> utilities = Mockito.mockStatic(AopContext.class)) {
                // Arrange
                utilities.when(AopContext::currentProxy).thenReturn(cglibProxyImpl);

                // Act
                new ProxyImpl().sayHello();

                // Assert
                utilities.verify(AopContext::currentProxy);
            }
        }
    }

    public interface ProxyApi {
    }

    public static class ProxyImpl implements ProxyApi {

        void sayHello() {
            assertThat(Aops.getSelf(this).toString()).isEqualTo(cglibProxyImpl.toString());
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
