package cn.xuanyuanli.core.util.net;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import cn.xuanyuanli.core.util.Texts;

/**
 * http相关工具类
 * <pre>
 *     <a href="http://kong.github.io/unirest-java/">官网</a>
 *     独立的Unirest要使用Unirest.spawnInstance()，比如要定制化超时时间，并发数，代理，则推荐使用独立的Unirest。不要污染默认全局的Unirest
 * </pre>
 *
 * @author John Li
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Https {

    /**
     * 获得页面返回码
     *
     * @param href href
     * @return int
     */
    public static int getStatusCode(String href) {
        try {
            return Unirest.head(href).asString().getStatus();
        } catch (UnirestException e) {
            return 500;
        }
    }

    /**
     * get请求获得页面内容.如果报错，则返回空字符串
     *
     * @param href href
     * @return {@link String}
     */
    public static String getAsString(String href) {
        try {
            return Unirest.get(href).asString().getBody();
        } catch (UnirestException e) {
            return "";
        }
    }

    /**
     * post请求获得页面内容.如果报错，则返回空字符串
     *
     * @param href href
     * @return {@link String}
     */
    public static String postAsString(String href) {
        try {
            return Unirest.post(href).asString().getBody();
        } catch (UnirestException e) {
            return "";
        }
    }

    /**
     * 根据url获得stream流
     *
     * @param url url
     * @return {@link InputStream}
     */
    public static InputStream getAsStream(String url) {
        try {
            HttpResponse<byte[]> response = Unirest.get(url).asBytes();
            return new ByteArrayInputStream(response.getBody());
        } catch (UnirestException e) {
            throw new RuntimeException(Texts.format("http getAsStream error. url:{}", url), e);
        }
    }
}
