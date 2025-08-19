package cn.xuanyuanli.core.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import cn.xuanyuanli.core.util.DataGenerator;
import cn.xuanyuanli.core.util.Randoms;
import cn.xuanyuanli.core.util.Texts;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 相关注解方法参数的正确性检测
 *
 * @author John Li
 */
public abstract class BaseMethodParamCheckBeanPostProcessor implements BeanPostProcessor {

    /**
     * 在Bean初始化之后进行处理，检查带有特定注解的方法参数是否正确
     *
     * @param bean     要处理的Bean对象
     * @param beanName Bean的名称
     * @return 处理后的Bean对象
     * @throws BeansException 如果处理过程中发生错误
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        // 如果Bean是AOP代理，获取目标类
        if (AopUtils.isAopProxy(bean)) {
            aClass = AopUtils.getTargetClass(bean);
        }
        // 遍历类中的所有方法，检查是否带有特定注解
        Arrays.stream(aClass.getMethods()).filter(m -> m.isAnnotationPresent(getAnnotationClass())).forEach(m -> {
            // 如果满足检查条件，则进行参数验证
            if (isCheck().apply(m)) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                Object[] objs = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    // 对于参数中的泛型进行处理
                    if (List.class.isAssignableFrom(parameterType)) {
                        objs[i] = DataGenerator.fullListBean((Class<?>) ((ParameterizedType) m.getGenericParameterTypes()[i]).getActualTypeArguments()[0],
                                Randoms.randomInt(1, 5));
                    } else {
                        objs[i] = DataGenerator.fullObject(parameterType);
                    }
                }
                // 如果验证失败，则会抛出异常
                try {
                    getConsume().apply(m, objs);
                } catch (Exception e) {
                    throw new RuntimeException(Texts.format("{}方法的{}注解相关参数映射有误", m.toString(), getAnnotationClass()), e);
                }
            }
        });
        return bean;
    }

    /**
     * 获得方法上的注解类型
     *
     * @return {@link Class}<{@link ?} {@link extends} {@link Annotation}>
     */
    public abstract Class<? extends Annotation> getAnnotationClass();

    /**
     * 获得方法及实参消费者
     *
     * @return {@link BiFunction}<{@link Method}, {@link Object[]}, {@link Object}>
     */
    public abstract BiFunction<Method, Object[], Object> getConsume();

    /**
     * 是否执行检测
     *
     * @return {@link Function}<{@link Method}, {@link Boolean}>
     */
    public abstract Function<Method, Boolean> isCheck();

}
