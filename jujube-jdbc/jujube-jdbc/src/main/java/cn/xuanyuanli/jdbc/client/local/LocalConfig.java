package cn.xuanyuanli.jdbc.client.local;

import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.core.constant.Profiles;
import cn.xuanyuanli.core.util.Resources;

/**
 * 本地application.properties配置文件映射（只适用于Windows本地环境）
 *
 * @author xuanyuanli
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalConfig {

    /**
     * 全局配置文件
     */
    private static final Properties P = wrapApplicationConfig(Resources.getCurrentClasspathProperties("application.properties"));

    /**
     * jdbc驱动程序类名
     */
    public static final String JDBC_DRIVER_CLASS_NAME = P.getProperty(Name.JDBC_DRIVER_CLASS_NAME);
    /**
     * jdbc url
     */
    public static final String JDBC_URL = P.getProperty(Name.JDBC_URL);
    /**
     * jdbc用户名
     */
    public static final String JDBC_USERNAME = P.getProperty(Name.JDBC_USERNAME);
    /**
     * jdbc密码
     */
    public static final String JDBC_PASSWORD = P.getProperty(Name.JDBC_PASSWORD);

    private static Properties wrapApplicationConfig(Properties properties) {
        if (properties != null) {
            properties.putAll(Resources.getCurrentClasspathProperties("application-" + Profiles.getSpringProfileFromSystemProperty() + ".properties"));

            if (StringUtils.isBlank(properties.getProperty(Name.JDBC_DRIVER_CLASS_NAME))) {
                properties.setProperty(Name.JDBC_DRIVER_CLASS_NAME, "com.mysql.cj.jdbc.Driver");
            }
        }
        return properties;
    }

    /**
     * 配置文件中的name值
     *
     * @author xuanyuanli
     * @date 2022/07/16
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private static class Name {

        /**
         * jdbc驱动程序类名
         */
        public static final String JDBC_DRIVER_CLASS_NAME = "spring.datasource.driver-class-name";
        /**
         * jdbc url
         */
        public static final String JDBC_URL = "spring.datasource.url";
        /**
         * jdbc用户名
         */
        public static final String JDBC_USERNAME = "spring.datasource.username";
        /**
         * jdbc密码
         */
        public static final String JDBC_PASSWORD = "spring.datasource.password";

    }
}
