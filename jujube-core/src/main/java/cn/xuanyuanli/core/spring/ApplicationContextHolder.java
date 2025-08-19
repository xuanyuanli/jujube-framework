package cn.xuanyuanli.core.spring;

import java.util.Optional;
import lombok.Getter;
import cn.xuanyuanli.core.util.Envs;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * ApplicationContext持有者，可以静态获取到ApplicationContext
 *
 * @author John Li
 * @date 2021/09/01
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    /**
     * 应用程序上下文
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
     *
     * @param applicationContext 应用程序上下文
     */
    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        manualSetApplicationContext(applicationContext);
    }

    /**
     * 手册设置应用程序上下文
     *
     * @param applicationContext 应用程序上下文
     */
    public static void manualSetApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    /**
     * 获得环境
     *
     * @return {@link Environment}
     */
    public static Environment getEnvironment() {
        return applicationContext != null ? applicationContext.getEnvironment() : null;
    }

    /**
     * 获得属性
     *
     * @param name 名字
     * @return {@link String}
     */
    public static String getProperty(String name) {
        return Optional.ofNullable(ApplicationContextHolder.getEnvironment()).map(e -> e.getProperty(name)).orElse(Envs.getEnv(name));
    }

    /**
     * 获得属性
     *
     * @param name         名字
     * @param defaultValue 默认值
     * @return {@link String}
     */
    public static String getProperty(String name, String defaultValue) {
        return Optional.ofNullable(ApplicationContextHolder.getEnvironment()).map(e -> e.getProperty(name)).orElse(defaultValue);
    }
}
