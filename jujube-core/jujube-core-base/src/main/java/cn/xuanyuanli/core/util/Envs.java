package cn.xuanyuanli.core.util;


/**
 * 变量获取工具
 *
 * @author xuanyuanli
 */
public class Envs {

    /**
     * 获得系统变量的值
     *
     * @param key 键
     * @return {@link String}
     */
    public static String getEnv(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        return value;
    }
}
