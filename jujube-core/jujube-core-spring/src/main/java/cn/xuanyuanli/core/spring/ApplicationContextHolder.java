package cn.xuanyuanli.core.spring;

import java.util.Optional;
import lombok.Getter;
import cn.xuanyuanli.core.util.Envs;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * Spring 应用上下文持有者工具类
 * <p>
 * 提供静态访问 Spring ApplicationContext 的功能，解决在非 Spring 管理的类中
 * 无法直接注入 ApplicationContext 的问题。支持：
 * <ul>
 * <li>静态获取 ApplicationContext 实例</li>
 * <li>便捷获取 Spring Environment 和配置属性</li>
 * <li>支持手动设置上下文（用于测试或特殊场景）</li>
 * <li>优雅的降级处理（Spring 上下文不可用时回退到系统环境变量）</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用方式：</strong>
 * <ol>
 * <li>将此类注册为 Spring Bean（通过 @Component 或配置类）</li>
 * <li>Spring 容器启动时会自动调用 {@link #setApplicationContext(ApplicationContext)}</li>
 * <li>在任何地方使用静态方法访问 Spring 上下文和配置</li>
 * </ol>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 获取 ApplicationContext
 * ApplicationContext context = ApplicationContextHolder.getApplicationContext();
 * MyService service = context.getBean(MyService.class);
 * 
 * // 获取配置属性
 * String dbUrl = ApplicationContextHolder.getProperty("spring.datasource.url");
 * String appName = ApplicationContextHolder.getProperty("app.name", "DefaultApp");
 * 
 * // 获取 Environment
 * Environment env = ApplicationContextHolder.getEnvironment();
 * String[] activeProfiles = env.getActiveProfiles();
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>线程安全性：</strong>
 * <ul>
 * <li>ApplicationContext 设置发生在容器初始化阶段，之后只读访问，因此是线程安全的</li>
 * <li>所有静态方法都是线程安全的</li>
 * <li>支持在多线程环境下并发访问</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>降级策略：</strong>当 Spring 上下文不可用时，{@link #getProperty(String)} 
 * 会自动回退到系统环境变量，确保配置获取的可用性。
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class ApplicationContextHolder implements ApplicationContextAware {

    /**
     * Spring 应用上下文实例
     * <p>
     * 由 Spring 容器启动时通过 {@link ApplicationContextAware} 接口自动注入。
     * 一旦设置后不会变更，保证线程安全性。
     * </p>
     */
    @Getter
    private static ApplicationContext applicationContext;

    /**
     * Spring 容器自动调用的上下文注入方法
     * <p>
     * 实现 {@link ApplicationContextAware} 接口的方法，Spring 容器启动时
     * 会自动调用此方法，将 ApplicationContext 实例注入到静态变量中。
     * </p>
     *
     * @param applicationContext Spring 应用上下文实例，不可为 null
     * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        manualSetApplicationContext(applicationContext);
    }

    /**
     * 手动设置 Spring 应用上下文
     * <p>
     * 主要用于测试场景或特殊情况下的手动设置。
     * 正常情况下，Spring 容器会自动通过 {@link #setApplicationContext(ApplicationContext)} 设置。
     * </p>
     * 
     * <p>
     * <strong>注意：</strong>此方法不是线程安全的，应避免在并发环境中使用。
     * </p>
     *
     * @param applicationContext 要设置的 Spring 应用上下文实例
     * @see #setApplicationContext(ApplicationContext)
     */
    public static void manualSetApplicationContext(ApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }

    /**
     * 获取 Spring Environment 实例
     * <p>
     * 从当前的 ApplicationContext 中获取 Environment，用于访问配置属性、
     * Profile 信息等。如果 ApplicationContext 不可用，则返回 null。
     * </p>
     *
     * @return Spring Environment 实例，如果 ApplicationContext 未初始化则返回 null
     * @see Environment
     * @see ApplicationContext#getEnvironment()
     */
    public static Environment getEnvironment() {
        return applicationContext != null ? applicationContext.getEnvironment() : null;
    }

    /**
     * 获取配置属性值（带降级处理）
     * <p>
     * 按优先级顺序获取配置属性：
     * <ol>
     * <li>优先从 Spring Environment 获取属性值</li>
     * <li>如果 Spring 上下文不可用，则从系统环境变量获取</li>
     * </ol>
     * 这种设计确保了在 Spring 容器未启动或不可用时仍然能获取配置。
     * </p>
     *
     * @param name 配置属性名称，不可为 null
     * @return 配置属性值，如果未找到则返回 null
     * @see Environment#getProperty(String)
     * @see Envs#getEnv(String)
     */
    public static String getProperty(String name) {
        return Optional.ofNullable(ApplicationContextHolder.getEnvironment()).map(e -> e.getProperty(name)).orElse(Envs.getEnv(name));
    }

    /**
     * 获取配置属性值（带默认值）
     * <p>
     * 从 Spring Environment 获取配置属性值，如果未找到或 Spring 上下文
     * 不可用，则返回指定的默认值。
     * </p>
     * 
     * <p>
     * <strong>注意：</strong>此方法不会回退到系统环境变量，与 
     * {@link #getProperty(String)} 的行为不同。
     * </p>
     *
     * @param name         配置属性名称，不可为 null
     * @param defaultValue 默认值，当属性不存在或 Spring 上下文不可用时返回
     * @return 配置属性值或默认值
     * @see Environment#getProperty(String)
     */
    public static String getProperty(String name, String defaultValue) {
        return Optional.ofNullable(ApplicationContextHolder.getEnvironment()).map(e -> e.getProperty(name)).orElse(defaultValue);
    }
}
