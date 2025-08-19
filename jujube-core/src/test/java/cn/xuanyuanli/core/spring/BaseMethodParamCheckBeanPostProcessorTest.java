package cn.xuanyuanli.core.spring;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.aop.framework.ProxyFactory;

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

    @Test
    void testPostProcessAfterInitialization_NonAopProxy() throws Exception {
        Object bean = new CustomBean();
        Method method = bean.getClass().getMethod("annotatedMethod");
        when(check.apply(method)).thenReturn(true);

        processor.postProcessAfterInitialization(bean, "beanName");

        verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
    }

    @Test
    void testPostProcessAfterInitialization_AopProxy() throws Exception {
        Object bean = new CustomBean();
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        Object proxy = proxyFactory.getProxy();

        Method method = bean.getClass().getMethod("annotatedMethod");
        when(check.apply(method)).thenReturn(true);

        processor.postProcessAfterInitialization(proxy, "beanName");

        verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
    }

    @Test
    void testPostProcessAfterInitialization_ValidationFailure() throws Exception {
        Object bean = new CustomBean();
        Method method = bean.getClass().getMethod("annotatedMethod");
        when(check.apply(method)).thenReturn(true);
        when(consume.apply(any(Method.class), any(Object[].class))).thenThrow(new RuntimeException("Validation failed"));

        assertThrows(RuntimeException.class, () -> processor.postProcessAfterInitialization(bean, "beanName"));
    }

    @Test
    void testPostProcessAfterInitialization_ListParameter() throws Exception {
        Object bean = new CustomBeanWithListParam();
        Method method = bean.getClass().getMethod("methodWithListParam", List.class);
        when(check.apply(method)).thenReturn(true);

        processor.postProcessAfterInitialization(bean, "beanName");

        verify(consume, times(1)).apply(any(Method.class), any(Object[].class));
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

    @Test
    void testPostProcessAfterInitialization_IsCheckFalse() throws Exception {
        Object bean = new CustomBean();
        Method method = bean.getClass().getMethod("annotatedMethod");
        when(check.apply(method)).thenReturn(false); // isCheck() 返回 false

        processor.postProcessAfterInitialization(bean, "beanName");

        verify(consume, never()).apply(any(Method.class), any(Object[].class)); // 确保 consume 未被调用
    }

    @Test
    void testPostProcessAfterInitialization_NonListParameter() throws Exception {
        Object bean = new CustomBeanWithNonListParam();
        Method method = bean.getClass().getMethod("methodWithNonListParam", String.class);
        when(check.apply(method)).thenReturn(true);

        processor.postProcessAfterInitialization(bean, "beanName");

        verify(consume, times(1)).apply(any(Method.class), any(Object[].class)); // 确保 consume 被调用
        // 这里可以进一步验证参数生成逻辑，确保调用了 DataGenerator.fullObject()
    }

    static class CustomBeanWithNonListParam {

        @SuppressWarnings({"unused", "EmptyMethod"})
        @CustomAnnotation
        public void methodWithNonListParam(String param) {
        }
    }
}
