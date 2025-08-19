package cn.xuanyuanli.core.constant;

import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.core.spring.ApplicationContextHolder;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Envs;
import org.springframework.core.env.Environment;

/**
 * Spring Profiles
 *
 * @author John Li
 * @date 2021/09/01
 */
public class Profiles {

    /**
     * Junit的Test类
     */
    private static final String JUNIT_TEST_CLASS_NAME = "org.junit.jupiter.api.Test";

    /**
     * Spring 切面
     */
    public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    /**
     * 是否测试
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
     * 配置文件
     */
    private Profiles() {
    }

    /**
     * 设置Spring Profile到系统变量
     *
     * @param profile 配置文件
     */
    public static void setSpringProfileToSystemProperty(String profile) {
        System.setProperty(SPRING_PROFILES_ACTIVE, profile);
    }

    /**
     * 从系统变量中获取Spring Profile
     *
     * @return {@link String}
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
     * 是否是测试环境
     *
     * @return boolean
     */
    public static boolean isTestProfile() {
        return IS_TEST;
    }

    /**
     * 是否是正式环境
     *
     * @return boolean
     */
    public static boolean isProdProfile() {
        return PRODUCTION.equalsIgnoreCase(getSpringProfileFromSystemProperty());
    }

    /**
     * 是否是dev环境
     *
     * @return boolean
     */
    public static boolean isDevProfile() {
        return DEVELOPMENT.equalsIgnoreCase(getSpringProfileFromSystemProperty());
    }


    /**
     * 生产
     */
    public static final String PRODUCTION = "prod";
    /**
     * 开发
     */
    public static final String DEVELOPMENT = "dev";
}
