package cn.xuanyuanli.core.util.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServletsTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletRequestAttributes servletRequestAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        RequestContextHolder.setRequestAttributes(servletRequestAttributes);
        when(servletRequestAttributes.getRequest()).thenReturn(request);
    }

    @Test
    void testSetFileDownloadHeader() {
        // 模拟 request 对象并设置 User-Agent 请求头
        when(request.getHeader("User-Agent")).thenReturn(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        String filename = "test file.txt";
        Servlets.setFileDownloadHeader(response, filename);

        // 验证响应头是否正确设置
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"test_file.txt\"");
        verify(response).setContentType("application/octet-stream");
    }

    @Test
    void testSetFileDownloadHeaderWithSafari() {
        // 模拟 request 对象并设置 Safari 的 User-Agent 请求头
        when(request.getHeader("User-Agent")).thenReturn(
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15");

        String filename = "test file.txt";
        Servlets.setFileDownloadHeader(response, filename);

        // 验证响应头是否正确设置（Safari 特殊处理）
        verify(response).setHeader("Content-Disposition", "attachment; filename=\"test_file.txt\"");
        verify(response).setContentType("application/octet-stream");
    }

    @Test
    void testEncodeHttpBasic() {
        String encoded = Servlets.encodeHttpBasic("user", "pass");
        assertEquals("Basic dXNlcjpwYXNz", encoded);
    }

    @Test
    void testGetBrowser() {
        when(request.getHeader("User-Agent")).thenReturn(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
        assertEquals("Safari 13.0.3", Servlets.getBrowser(request));
    }

    @Test
    void testGetParametersStartingWith() {
        // 模拟 ServletRequest 对象
        ServletRequest servletRequest = mock(ServletRequest.class);

        // 设置参数映射
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("prefix_param1", new String[]{"value1"});
        parameterMap.put("prefix_param2", new String[]{"value2"});
        parameterMap.put("other_param", new String[]{"value3"});

        // 模拟 getParameterMap 方法返回参数映射
        when(servletRequest.getParameterMap()).thenReturn(parameterMap);
        when(servletRequest.getParameterNames()).thenReturn(Collections.enumeration(parameterMap.keySet()));
        parameterMap.forEach((k, v) -> when(servletRequest.getParameterValues(k)).thenReturn(v));

        // 调用方法并验证结果
        Map<String, Object> result = Servlets.getParametersStartingWith(servletRequest, "prefix_");
        assertEquals(2, result.size()); // 预期有 2 个以 "prefix_" 开头的参数
        assertEquals("value1", result.get("param1")); // 验证去除了前缀的键值对
        assertEquals("value2", result.get("param2"));
    }

    @Test
    void testGetParametersEndingWith() {
        ServletRequest servletRequest = mock(ServletRequest.class);
        Enumeration<String> paramNames = Collections.enumeration(Arrays.asList("param1_suffix", "param2_suffix", "param3"));

        when(servletRequest.getParameterNames()).thenReturn(paramNames);
        when(servletRequest.getParameterValues("param1_suffix")).thenReturn(new String[]{"value1"});
        when(servletRequest.getParameterValues("param2_suffix")).thenReturn(new String[]{"value2"});
        when(servletRequest.getParameterValues("param3")).thenReturn(new String[]{"value3"});

        Map<String, Object> result = Servlets.getParametersEndingWith(servletRequest, "_suffix");
        assertEquals(2, result.size());
        assertEquals("value1", result.get("param1"));
        assertEquals("value2", result.get("param2"));
    }

    @Test
    void testGetFullUrl() {
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com/path"));
        when(request.getQueryString()).thenReturn("param=value");

        String fullUrl = Servlets.getFullUrl(request);
        assertEquals("http://example.com/path?param=value", fullUrl);
    }

    @Test
    void testIsWebEnviroument() {
        assertTrue(Servlets.isWebEnviroument());
    }

    @Test
    void testGetCurrentHttpServletRequest() {
        when(servletRequestAttributes.getRequest()).thenReturn(request);
        assertEquals(request, Servlets.getCurrentHttpServletRequest());
    }

    @Test
    void testGetFormatHeader() {
        when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("Header1", "Header2")));
        when(request.getHeader("Header1")).thenReturn("Value1");
        when(request.getHeader("Header2")).thenReturn("Value2");

        Map<String, String> headers = Servlets.getFormatHeader(request);
        assertEquals(2, headers.size());
        assertEquals("Value1", headers.get("Header1"));
        assertEquals("Value2", headers.get("Header2"));
    }

    @Test
    void testGetFormatParamter() {
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("param1", new String[]{"value1"});
        parameterMap.put("param2", new String[]{"value2", "value3"});

        when(request.getParameterMap()).thenReturn(parameterMap);

        Map<String, String> result = Servlets.getFormatParamter(request);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("param1"));
        assertEquals("value2,value3", result.get("param2"));
    }

    @Test
    void testGetCurrentLocale() {
        when(request.getLocale()).thenReturn(Locale.US);
        assertEquals(Locale.US, Servlets.getCurrentLocale());
    }

    @Test
    void testGetHeader() {
        when(request.getHeader("headerKey")).thenReturn("Value");
        assertEquals("Value", Servlets.getHeader(request, "headerKey"));
    }

    @Test
    void testGetIpAddr() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        assertEquals("", Servlets.getIpAddr(request));

        when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn("192.168.1.2,123.23.2.3");
        assertEquals("123.23.2.3", Servlets.getIpAddr(request));
    }

    @Test
    void testGetIpAddrForwardedFor() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        BDDMockito.given(httpServletRequest.getHeader("x-forwarded-for")).willReturn("123.23.2.3");
        String result = Servlets.getIpAddr(httpServletRequest);
        Assertions.assertEquals("123.23.2.3", result);
        Mockito.verify(httpServletRequest).getHeader("x-forwarded-for");
    }

    @Test
    void testGetIpAddrRealIp() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        BDDMockito.given(httpServletRequest.getHeader("x-real-ip")).willReturn("123.23.2.3,127.0.0.1");
        String result = Servlets.getIpAddr(httpServletRequest);
        Assertions.assertEquals("123.23.2.3", result);
        Mockito.verify(httpServletRequest).getHeader("x-real-ip");
    }

    @Test
    void testGetIpAddrRealIpMulti() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        BDDMockito.given(httpServletRequest.getHeader("x-real-ip")).willReturn("127.0.0.1,192.168.1.1, 10.0.0.1,  172.16.0.1,123.23.2.3");
        String result = Servlets.getIpAddr(httpServletRequest);
        Assertions.assertEquals("123.23.2.3", result);
        Mockito.verify(httpServletRequest).getHeader("x-real-ip");
    }

    @Test
    void testGetIpAddrRealIpEmpty() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        BDDMockito.given(httpServletRequest.getHeader("x-real-ip")).willReturn("127.0.0.1,192.168.1.1, 10.0.0.1,  172.16.0.1");
        String result = Servlets.getIpAddr(httpServletRequest);
        Assertions.assertEquals("", result);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        Mockito.verify(httpServletRequest).getHeader("x-real-ip");
    }

    @Test
    void testGetIpAddrRealIpEmpty2() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        String result = Servlets.getIpAddr(httpServletRequest);
        Assertions.assertEquals("", result);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testGetIpAddrHttp() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        BDDMockito.given(httpServletRequest.getHeader("WL-Proxy-Client-IP")).willReturn("192.168.1.10");
        BDDMockito.given(httpServletRequest.getHeader("x-real-ip")).willReturn("172.18.0.1");
        BDDMockito.given(httpServletRequest.getHeader("X-Forwarded-For")).willReturn("10.0.0.10");
        BDDMockito.given(httpServletRequest.getHeader("Proxy-Client-IP")).willReturn("192.168.1.10");
        BDDMockito.given(httpServletRequest.getHeader("http_client_ip")).willReturn("169.254.10.0");
        BDDMockito.given(httpServletRequest.getHeader("HTTP_X_FORWARDED_FOR")).willReturn("123.230.200.123");
        String result = Servlets.getIpAddr(httpServletRequest);
        Assertions.assertEquals("123.230.200.123", result);
        Mockito.verify(httpServletRequest).getHeader("http_client_ip");
    }

}

