package cn.xuanyuanli.core.constant;

import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.core.spring.ApplicationContextHolder;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Envs;
import org.springframework.core.env.Environment;

/**
 * Spring 环境配置文件管理工具类
 * <p>
 * 提供全面的 Spring Profiles 环境管理功能，支持：
 * <ul>
 * <li>自动检测当前运行环境（开发、测试、生产）</li>
 * <li>动态设置和获取 Spring Profile 配置</li>
 * <li>智能识别测试环境（基于 JUnit 类路径检测）</li>
 * <li>提供环境判断的便捷方法</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>支持的环境类型：</strong>
 * <ul>
 * <li><strong>开发环境 (dev):</strong> 用于本地开发和调试</li>
 * <li><strong>测试环境 (test):</strong> 自动检测 JUnit 测试执行环境</li>
 * <li><strong>生产环境 (prod):</strong> 线上正式环境</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 设置环境配置
 * Profiles.setSpringProfileToSystemProperty("dev");
 * 
 * // 获取当前环境
 * String currentProfile = Profiles.getSpringProfileFromSystemProperty();
 * 
 * // 环境判断
 * if (Profiles.isDevProfile()) {
 *     // 开发环境特定逻辑
 *     logger.debug("开发环境启动");
 * } else if (Profiles.isProdProfile()) {
 *     // 生产环境特定逻辑
 *     logger.info("生产环境启动");
 * }
 * 
 * // 测试环境自动检测
 * if (Profiles.isTestProfile()) {
 *     // 测试环境配置
 *     mockExternalServices();
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>技术特点：</strong>
 * <ul>
 * <li><strong>智能检测：</strong>通过类路径检测自动识别测试环境</li>
 * <li><strong>多层级获取：</strong>优先从 Spring Environment，fallback 到系统环境变量</li>
 * <li><strong>线程安全：</strong>所有方法都是静态的且线程安全</li>
 * <li><strong>容错处理：</strong>异常情况下不影响应用启动</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class Profiles {

    /**
     * JUnit 测试类的全限定名称
     * <p>
     * 用于检测当前环境是否为测试环境。通过尝试加载此类来判断
     * 是否在测试环境中运行（即类路径中是否包含 JUnit 5）。
     * </p>
     */
    private static final String JUNIT_TEST_CLASS_NAME = "org.junit.jupiter.api.Test";

    /**
     * Spring 活跃配置文件系统属性键
     * <p>
     * 标准的 Spring Boot 配置属性，用于指定当前激活的 Profile。
     * 可通过系统属性、环境变量或 application.properties 设置。
     * </p>
     */
    public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    /**
     * 测试环境标识符
     * <p>
     * 静态初始化时通过检测 JUnit 类是否存在来确定当前是否为测试环境。
     * 一旦确定后不会改变，确保环境检测的一致性。
     * </p>
     */
    private static boolean IS_TEST = false;

    static {
        try {
            Beans.forName(JUNIT_TEST_CLASS_NAME);
            IS_TEST = true;
        } catch (Throwable ignored) {
        }
    }

    /**
     * 私有构造函数，防止实例化
     * <p>
     * 该类为纯静态工具类，不需要创建实例。
     * </p>
     */
    private Profiles() {
    }

    /**
     * 设置 Spring Profile 到系统属性
     * <p>
     * 将指定的 Profile 设置为系统属性，使 Spring Boot 能够识别并激活相应的配置。
     * 通常在应用启动前调用，或者在单元测试中设置测试环境。
     * </p>
     *
     * @param profile 要激活的 Profile 名称（如 "dev"、"prod"、"test"等）
     * @see #DEVELOPMENT
     * @see #PRODUCTION
     */
    public static void setSpringProfileToSystemProperty(String profile) {
        System.setProperty(SPRING_PROFILES_ACTIVE, profile);
    }

    /**
     * 获取当前激活的 Spring Profile
     * <p>
     * 按优先级顺序获取当前环境的 Profile：
     * <ol>
     * <li>优先从 Spring Environment 获取激活的 Profile</li>
     * <li>如果 Spring 上下文不可用，则从系统环境变量获取</li>
     * <li>异常情况下返回空字符串</li>
     * </ol>
     * </p>
     *
     * @return 当前激活的 Profile 名称，如果未设置则返回空字符串
     * @see ApplicationContextHolder#getEnvironment()
     * @see #SPRING_PROFILES_ACTIVE
     */
    public static String getSpringProfileFromSystemProperty() {
        String profile = "";
        try {
            Environment environment = ApplicationContextHolder.getEnvironment();
            if (environment != null) {
                profile = environment.getActiveProfiles()[0];
            }
        } catch (Throwable ignored) {
        }
        if (StringUtils.isBlank(profile)) {
            profile = Envs.getEnv(SPRING_PROFILES_ACTIVE);
        }
        return profile;
    }

    /**
     * 检查当前是否为测试环境
     * <p>
     * 通过检测类路径中是否包含 JUnit 5 的 Test 注解类来判断。
     * 该检测在类加载时执行，结果缓存且不可变。
     * </p>
     *
     * <p>
     * <strong>注意：</strong>这里的“测试环境”指的是 JUnit 测试执行环境，
     * 与 Spring Profile 的 "test" 环境不同。
     * </p>
     *
     * @return 如果当前在 JUnit 测试环境中运行则返回 true，否则返回 false
     * @see #JUNIT_TEST_CLASS_NAME
     */
    public static boolean isTestProfile() {
        return IS_TEST;
    }

    /**
     * 检查当前是否为生产环境
     * <p>
     * 通过比较当前激活的 Profile 与生产环境标识来判断。
     * 比较过程不区分大小写，提高配置的灵活性。
     * </p>
     *
     * @return 如果当前 Profile 为生产环境（"prod"）则返回 true，否则返回 false
     * @see #PRODUCTION
     * @see #getSpringProfileFromSystemProperty()
     */
    public static boolean isProdProfile() {
        return PRODUCTION.equalsIgnoreCase(getSpringProfileFromSystemProperty());
    }

    /**
     * 检查当前是否为开发环境
     * <p>
     * 通过比较当前激活的 Profile 与开发环境标识来判断。
     * 比较过程不区分大小写，提高配置的灵活性。
     * </p>
     *
     * @return 如果当前 Profile 为开发环境（"dev"）则返回 true，否则返回 false
     * @see #DEVELOPMENT
     * @see #getSpringProfileFromSystemProperty()
     */
    public static boolean isDevProfile() {
        return DEVELOPMENT.equalsIgnoreCase(getSpringProfileFromSystemProperty());
    }


    /**
     * 生产环境 Profile 标识
     * <p>
     * 用于标识正式的生产环境。在此环境下，应用会启用最优化的配置，
     * 包括性能调优、安全加固、缓存策略等。
     * </p>
     */
    public static final String PRODUCTION = "prod";
    /**
     * 开发环境 Profile 标识
     * <p>
     * 用于标识本地开发环境。在此环境下，应用会启用便于调试的配置，
     * 包括详细日志、热重载、开发工具等。
     * </p>
     */
    public static final String DEVELOPMENT = "dev";
}
