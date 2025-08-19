package cn.xuanyuanli.core.util.web;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * url处理工具类(仅限于http和https协议的解析，防止SSRF攻击)
 * <br>
 * 参考：UriComponentsBuilder
 *
 * @author John Li
 * @date 2021/09/01
 */
@NoArgsConstructor
public class Urls {

    /**
     * 解析url
     *
     * @param url url
     * @return {@link UriComponents}
     */
    public static UriComponents parse(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        return UriComponentsBuilder.fromUriString(url).build();
    }

    /**
     * java版的encodeURIComponent
     *
     * @param s 字符串
     * @return {@link String}
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static String encodeURIComponent(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        String result;
        result = URLEncoder.encode(s, StandardCharsets.UTF_8).replaceAll("\\+", "%20").replaceAll("%21", "!").replaceAll("%27", "'").replaceAll("%28", "(")
                .replaceAll("%29", ")").replaceAll("%7E", "~");
        return result;
    }

    /**
     * java版的decodeURIComponent（和js逻辑完全一致）
     *
     * @param url url
     * @return {@link String}
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static String decodeURIComponent(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        // 对于URLDecoder.decode中+表空格的问题进行处理
        url = url.replace("+", "%2B");
        return URLDecoder.decode(url, StandardCharsets.UTF_8);
    }

    /**
     * java版的encodeURI
     *
     * @param url url
     * @return {@link String}
     */
    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @SneakyThrows
    public static String encodeURI(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return UriComponentsBuilder.fromUriString(url).build().encode().toUriString();
    }

    /**
     * 是否是中文字符
     *
     * @param c c
     * @return boolean
     */
    static boolean isChinese(char c) {
        return String.valueOf(c).matches("[一-龥]");
    }
}
