package cn.xuanyuanli.core.spring;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.aop.framework.ProxyFactory;

@SuppressWarnings("unchecked")
@DisplayName("基础方法参数校验Bean后置处理器测试")
class BaseMethodParamCheckBeanPostProcessorTest {

    private BaseMethodParamCheckBeanPostProcessor processor;
    private BiFunction<Method, Object[], Object> consume;
    private Function<Method, Boolean> check;

    @BeforeEach
    void setUp() {
        processor = Mockito.spy(BaseMethodParamCheckBeanPostProcessor.class);
        consume = mock(BiFunction.class);
        check = mock(Function.class);

        OngoingStubbing<Class<? extends Annotation>> when = when(processor.getAnnotationClass());
        when.thenReturn(CustomAnnotation.class);
        when(processor.getConsume()).thenReturn(consume);
        when(processor.isCheck()).thenReturn(check);
    }

    @Nested
    @DisplayName("初始化后处理")
    class PostProcessAfterInitialization {

        @Test
        @DisplayName("应该调用消费函数一次 - 当Bean为非AOP代理且检查通过时")
        void postProcessAfterInitialization_shouldCallConsumeOnce_whenBeanIsNonAopProxyAndCheckPasses() throws Exception {
            // Arrange
            Object bean = new CustomBean();
            Method method = bean.getClass().getMethod("annotatedMethod");
            when(check.apply(method)).thenReturn(true);

            // Act
            processor.postProcessAfterInitialization(bean, "beanName");

            // Assert
            verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
        }

        @Test
        @DisplayName("应该调用消费函数一次 - 当Bean为AOP代理且检查通过时")
        void postProcessAfterInitialization_shouldCallConsumeOnce_whenBeanIsAopProxyAndCheckPasses() throws Exception {
            // Arrange
            Object bean = new CustomBean();
            ProxyFactory proxyFactory = new ProxyFactory(bean);
            Object proxy = proxyFactory.getProxy();
            Method method = bean.getClass().getMethod("annotatedMethod");
            when(check.apply(method)).thenReturn(true);

            // Act
            processor.postProcessAfterInitialization(proxy, "beanName");

            // Assert
            verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
        }

        @Test
        @DisplayName("应该抛出运行时异常 - 当校验失败时")
        void postProcessAfterInitialization_shouldThrowRuntimeException_whenValidationFails() throws Exception {
            // Arrange
            Object bean = new CustomBean();
            Method method = bean.getClass().getMethod("annotatedMethod");
            when(check.apply(method)).thenReturn(true);
            when(consume.apply(any(Method.class), any(Object[].class)))
                    .thenThrow(new RuntimeException("Validation failed"));

            // Act & Assert
            assertThatThrownBy(() -> processor.postProcessAfterInitialization(bean, "beanName"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("方法的")
                    .hasMessageContaining("注解相关参数映射有误")
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("应该不调用消费函数 - 当检查不通过时")
        void postProcessAfterInitialization_shouldNotCallConsume_whenCheckFails() throws Exception {
            // Arrange
            Object bean = new CustomBean();
            Method method = bean.getClass().getMethod("annotatedMethod");
            when(check.apply(method)).thenReturn(false);

            // Act
            processor.postProcessAfterInitialization(bean, "beanName");

            // Assert
            verify(consume, never()).apply(any(Method.class), any(Object[].class));
        }
    }

    @Nested
    @DisplayName("不同参数类型处理")
    class ParameterTypeHandling {

        @Test
        @DisplayName("应该调用消费函数一次 - 当方法有List参数且检查通过时")
        void postProcessAfterInitialization_shouldCallConsumeOnce_whenMethodHasListParameterAndCheckPasses() throws Exception {
            // Arrange
            Object bean = new CustomBeanWithListParam();
            Method method = bean.getClass().getMethod("methodWithListParam", List.class);
            when(check.apply(method)).thenReturn(true);

            // Act
            processor.postProcessAfterInitialization(bean, "beanName");

            // Assert
            verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
        }

        @Test
        @DisplayName("应该调用消费函数一次 - 当方法有非List参数且检查通过时")
        void postProcessAfterInitialization_shouldCallConsumeOnce_whenMethodHasNonListParameterAndCheckPasses() throws Exception {
            // Arrange
            Object bean = new CustomBeanWithNonListParam();
            Method method = bean.getClass().getMethod("methodWithNonListParam", String.class);
            when(check.apply(method)).thenReturn(true);

            // Act
            processor.postProcessAfterInitialization(bean, "beanName");

            // Assert
            verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomAnnotation {
    }

    @SuppressWarnings("EmptyMethod")
    static class CustomBean {
        @CustomAnnotation
        public void annotatedMethod() {
        }
    }

    @SuppressWarnings("EmptyMethod")
    static class CustomBeanWithListParam {
        @SuppressWarnings("unused")
        @CustomAnnotation
        public void methodWithListParam(List<String> list) {
        }
    }

    static class CustomBeanWithNonListParam {
        @SuppressWarnings({"unused", "EmptyMethod"})
        @CustomAnnotation
        public void methodWithNonListParam(String param) {
        }
    }
}
