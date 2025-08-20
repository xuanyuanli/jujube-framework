package cn.xuanyuanli.core.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.StringUtils;

/**
 * 驼峰命名法转换工具
 *
 * <pre>
 *  CamelCaseUtils.toCamelCase("orderId") == "orderId"
 *  CamelCaseUtils.toCamelCase("hello_world") == "helloWorld"
 *  CamelCaseUtils.toCapitalizeCamelCase("hello_world") == "HelloWorld"
 *  CamelCaseUtils.toUnderScoreCase("helloWorld") = "hello_world"
 * </pre>
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 */
public class CamelCase {

    /**
     * 驼峰式大小写
     */
    private CamelCase() {
    }

    /**
     * 字段转换的缓存
     */
    private static final ConcurrentMap<String, String> CACHE = new ConcurrentHashMap<>();

    /**
     * 分隔符
     */
    private static final char SEPARATOR = '_';

    /**
     * 遇大写，则转换为下划线形式+小写
     *
     * @param input 输入
     * @return {@link String}
     */
    public static String toUnderlineName(String input) {
        if (input == null) {
            return "";
        }
        String key = input + "@toUnderlineName";
        return CACHE.computeIfAbsent(key, k -> {
            StringBuilder resultSb = new StringBuilder();
            boolean upperCase = false;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                boolean nextUpperCase = true;
                if (i < (input.length() - 1)) {
                    nextUpperCase = Character.isUpperCase(input.charAt(i + 1));
                }
                if (Character.isUpperCase(c)) {
                    if (!upperCase || !nextUpperCase) {
                        if (i > 0) {
                            resultSb.append(SEPARATOR);
                        }
                    }
                    upperCase = true;
                } else {
                    upperCase = false;
                }
                resultSb.append(Character.toLowerCase(c));
            }
            return resultSb.toString();
        });


    }

    /**
     * 下划线写法转换为驼峰写法
     *
     * @param input 输入
     * @return {@link String}
     */
    public static String toCamelCase(String input) {
        if (input == null) {
            return "";
        }
        if (!input.contains(String.valueOf(SEPARATOR))) {
            return input;
        }
        String key = input + "@toUnderlineName";
        return CACHE.computeIfAbsent(key, k -> {
            StringBuilder resultSb = new StringBuilder(input.length());
            boolean upperCase = false;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == SEPARATOR) {
                    upperCase = true;
                } else if (upperCase) {
                    resultSb.append(Character.toUpperCase(c));
                    upperCase = false;
                } else {
                    resultSb.append(c);
                }
            }
            return resultSb.toString();
        });
    }

    /**
     * 特殊的转换：前两个字母不能大写
     *
     * @param input 输入
     * @return {@link String}
     */
    public static String toSpecilCamelCase(String input) {
        String temp = toCamelCase(input);
        return temp.substring(0, 2).toLowerCase() + temp.substring(2);
    }

    /**
     * 下划线写法转换为驼峰写法,并首字母大写
     *
     * @param input 输入
     * @return {@link String}
     */
    public static String toCapitalizeCamelCase(String input) {
        String str = toCamelCase(input);
        return StringUtils.capitalize(str);
    }

}
