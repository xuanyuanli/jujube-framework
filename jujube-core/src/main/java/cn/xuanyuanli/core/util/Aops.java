package cn.xuanyuanli.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;

/**
 * spring aop的辅助工具类<br> 对于Spring Boot来说，需要在配置文件中配置：spring.aop.auto=false<br> 还需要在启动类上加上注解：@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
 *
 * @author John Li
 * @date 2021/09/01
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Aops {

    /**
     * 为了解决循环aop调用,要使用这个方法 用法：
     *
     * <pre>
     * Aops.getSelf(this)
     * </pre>
     *
     * @param t t一般入参为this，而this只能是类对象，不可能是代理类，这一点要注意
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T> T getSelf(T t) {
        try {
            T currentProxy = (T) AopContext.currentProxy();
            // 有时出现currentProxy和t类型不一致，这里做一下判断
            if (currentProxy.getClass().isInterface() || currentProxy.getClass().getSuperclass().equals(t.getClass())) {
                return currentProxy;
            }
        } catch (IllegalStateException e) {
            // 一般会报错：Cannot find current proxy: Set 'exposeProxy' property on
            // Advised to 'true' to make it available.
            // 此时表明这个类中没有aop方法，直接返回t即可
            log.error("Aop获取自身代理对象失败,对象类型：{}", t.getClass());
        }
        return t;
    }
}
