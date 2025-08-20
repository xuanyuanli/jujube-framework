package cn.xuanyuanli.core.util.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Collections3;
import cn.xuanyuanli.core.util.useragent.UserAgent;
import cn.xuanyuanli.core.util.useragent.UserAgentParser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

/**
 * Servlet相关工具类
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class Servlets {

    /**
     * 未知
     */
    private static final String UNKNOWN = "unknown";

    /**
     * servlet
     */
    private Servlets() {
    }

    /**
     * 设置让浏览器弹出下载对话框的Header
     *
     * @param filename       下载后的文件名.
     * @param ignoredRequest 忽略请求
     * @param response       响应
     */
    @SneakyThrows
    @Deprecated
    public static void setFileDownloadHeader(HttpServletRequest ignoredRequest, HttpServletResponse response, String filename) {
        setFileDownloadHeader(response, filename);
    }

    /**
     * 设置让浏览器弹出下载对话框的Header
     *
     * @param filename 下载后的文件名.
     * @param response 响应
     */
    @SneakyThrows
    public static void setFileDownloadHeader(HttpServletResponse response, String filename) {
        // 替换空格，否则firefox下有空格文件名会被截断,其他浏览器会将空格替换成+号
        filename = filename.trim().replaceAll(" ", "_");
        HttpServletRequest request = getCurrentHttpServletRequest();
        String agent = "";
        if (request != null) {
            agent = Servlets.getHeader(request, "User-Agent");
        }
        // 兼容一下safari浏览器
        if (agent.contains("Safari")) {
            filename = new String(filename.getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        } else {
            response.setHeader("Content-Disposition", "attachment; filename*=utf-8'zh_cn'" + Urls.encodeURIComponent(filename));
        }
        // 中文文件名支持
        response.setContentType("application/octet-stream");
    }

    /**
     * 客户端对Http Basic验证的 Header进行编码.
     *
     * @param userName 用户名
     * @param password 密码
     * @return {@link String}
     */
    public static String encodeHttpBasic(String userName, String password) {
        String encode = userName + ":" + password;
        return "Basic " + java.util.Base64.getEncoder().encodeToString(encode.getBytes());
    }

    /**
     * 获得真实ip地址
     *
     * @param request 请求
     * @return {@link String}
     */
    public static String getIpAddr(HttpServletRequest request) {
        // 定义可能的 IP Header 列表
        String[] ipHeaders = {"X-Real-IP", "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};

        // 收集所有有效的 IP 值
        StringBuilder ipBuilder = new StringBuilder();
        for (String header : ipHeaders) {
            String ipValue = getHeader(request, header);
            if (ipValue != null && !ipValue.isEmpty() && !UNKNOWN.equalsIgnoreCase(ipValue)) {
                if (!ipBuilder.isEmpty()) {
                    ipBuilder.append(",");
                }
                ipBuilder.append(ipValue);
            }
        }
        String ip = getFirstPublicIp(ipBuilder.toString());
        return Optional.ofNullable(ip).orElse("");
    }

    /**
     * 从 X-Forwarded-For 获取第一个非内网 IP
     */
    static String getFirstPublicIp(String xForwardedFor) {
        if (xForwardedFor == null || xForwardedFor.trim().isEmpty()) {
            return null;
        }

        // 分割 IP 列表（去除空格）
        List<String> ips = Arrays.stream(xForwardedFor.split(",")).map(String::trim).toList();

        // 遍历 IP，返回第一个非内网 IP
        for (String ip : ips) {
            if (!isPrivateIp(ip)) {
                return ip;
            }
        }

        // 全是内网 IP
        return null;
    }

    /**
     * 判断是否为内网 IP
     */
    private static boolean isPrivateIp(String ip) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ip);
            return inetAddress.isSiteLocalAddress() || inetAddress.isLoopbackAddress() || isLinkLocalIp(ip);
        } catch (UnknownHostException e) {
            // 无效 IP 视为非内网
            return false;
        }
    }

    /**
     * 检查是否是链路本地地址（169.254.0.0/16）
     */
    private static boolean isLinkLocalIp(String ip) {
        return ip.startsWith("169.254.");
    }

    /**
     * 获得客户端浏览器
     *
     * @param request 请求
     * @return {@link String}
     */
    public static String getBrowser(final HttpServletRequest request) {
        String userAgent = getHeader(request, "User-Agent");
        UserAgent parse = UserAgentParser.parse(userAgent);
        if (parse != null) {
            return parse.getBrowser().getName() + " " + parse.getVersion();
        } else {
            return "";
        }
    }

    /**
     * 取得带相同前缀的Request Parameters, copy from spring WebUtils.
     *
     * @param request 请求
     * @param prefix  前缀
     * @return 返回的结果的Parameter名已去除前缀
     */
    public static Map<String, Object> getParametersStartingWith(ServletRequest request, String prefix) {
        return WebUtils.getParametersStartingWith(request, prefix);
    }

    /**
     * 取得带相同后缀的Request Parameters
     *
     * @param request 请求
     * @param suffix  后缀
     * @return 返回的结果的Parameter名已去除后缀
     */
    public static Map<String, Object> getParametersEndingWith(ServletRequest request, String suffix) {
        Enumeration<String> paramNames = request.getParameterNames();
        Map<String, Object> params = new TreeMap<>();
        if (suffix == null) {
            suffix = "";
        }
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            if (suffix.isEmpty() || paramName.endsWith(suffix)) {
                String[] values = request.getParameterValues(paramName);
                String name = paramName.substring(0, paramName.length() - paramName.lastIndexOf(suffix) - 1);
                //noinspection StatementWithEmptyBody
                if (values == null || values.length == 0) {
                    // ignored
                } else if (values.length > 1) {
                    params.put(name, values);
                } else {
                    params.put(name, values[0]);
                }
            }
        }
        return params;
    }

    /**
     * 获得完整的访问Url
     *
     * @param request 请求
     * @return {@link String}
     */
    public static String getFullUrl(HttpServletRequest request) {
        return request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }


    /**
     * 是否是web环境
     *
     * @return boolean
     */
    public static boolean isWebEnviroument() {
        try {
            Beans.forName("jakarta.servlet.http.HttpServletRequest");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获得当前线程的Request
     *
     * @return {@link HttpServletRequest}
     */
    public static HttpServletRequest getCurrentHttpServletRequest() {
        ServletRequestAttributes servletRequestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (servletRequestAttributes != null) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    /**
     * 获得Map版Header
     *
     * @param request 请求
     * @return {@link Map}<{@link String}, {@link String}>
     */
    public static Map<String, String> getFormatHeader(HttpServletRequest request) {
        return Collections3.enumerationToList(request.getHeaderNames()).stream().collect(Collectors.toMap(t -> t, request::getHeader));
    }

    /**
     * 获得Map版Paramter
     *
     * @param request 请求
     * @return 如果是多个值，用逗号隔开
     */
    public static Map<String, String> getFormatParamter(HttpServletRequest request) {
        Map<String, String[]> pMap = request.getParameterMap();
        Map<String, String> result = new HashMap<>(pMap.size());
        for (String key : pMap.keySet()) {
            String[] values = pMap.get(key);
            result.put(key, StringUtils.join(values, ","));
        }
        return result;
    }

    /**
     * 获得资源国际化的当前Locale对象
     *
     * @return {@link Locale}
     */
    public static Locale getCurrentLocale() {
        if (isWebEnviroument()) {
            HttpServletRequest request = getCurrentHttpServletRequest();
            Locale locale = Locale.CHINA;
            if (request != null) {
                locale = RequestContextUtils.getLocale(request);
                if (!locale.equals(Locale.CHINA) && !locale.equals(Locale.US)) {
                    locale = Locale.US;
                }
            }
            return locale;
        } else {
            return Locale.CHINA;
        }
    }

    /**
     * 从request中获得对应的header（兼容大小写）
     *
     * @param request   request
     * @param headerKey header key
     * @return {@link String}
     */
    public static String getHeader(HttpServletRequest request, String headerKey) {
        return Optional.ofNullable(request.getHeader(headerKey.toLowerCase())).orElse(request.getHeader(headerKey));
    }
}
