package cn.xuanyuanli.core.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 变量获取工具
 *
 * @author John Li
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
        if (StringUtils.isBlank(value)) {
            value = System.getenv(key);
        }
        return value;
    }
}
