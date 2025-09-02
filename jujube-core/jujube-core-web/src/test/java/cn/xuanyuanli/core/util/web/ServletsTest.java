package cn.xuanyuanli.core.util.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@DisplayName("Servlets 测试")
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

    @Nested
    @DisplayName("文件下载头设置测试")
    class FileDownloadHeaderTests {

        @Test
        @DisplayName("setFileDownloadHeader_应该设置正确的下载头_当使用Chrome浏览器时")
        void setFileDownloadHeader_shouldSetCorrectDownloadHeader_whenUsingChrome() {
            // Arrange
            when(request.getHeader("User-Agent")).thenReturn(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            String filename = "test file.txt";

            // Act
            Servlets.setFileDownloadHeader(response, filename);

            // Assert
            verify(response).setHeader("Content-Disposition", "attachment; filename=\"test_file.txt\"");
            verify(response).setContentType("application/octet-stream");
        }

        @Test
        @DisplayName("setFileDownloadHeader_应该设置正确的下载头_当使用Safari浏览器时")
        void setFileDownloadHeader_shouldSetCorrectDownloadHeader_whenUsingSafari() {
            // Arrange
            when(request.getHeader("User-Agent")).thenReturn(
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15");
            String filename = "test file.txt";

            // Act
            Servlets.setFileDownloadHeader(response, filename);

            // Assert
            verify(response).setHeader("Content-Disposition", "attachment; filename=\"test_file.txt\"");
            verify(response).setContentType("application/octet-stream");
        }
    }

    @Nested
    @DisplayName("HTTP Basic编码测试")
    class HttpBasicEncodingTests {

        @Test
        @DisplayName("encodeHttpBasic_应该返回正确的Basic认证头_当提供用户名和密码时")
        void encodeHttpBasic_shouldReturnCorrectBasicAuthHeader_whenUsernameAndPasswordProvided() {
            // Act
            String encoded = Servlets.encodeHttpBasic("user", "pass");

            // Assert
            assertThat(encoded).isEqualTo("Basic dXNlcjpwYXNz");
        }
    }

    @Nested
    @DisplayName("浏览器信息获取测试")
    class BrowserInfoTests {

        @Test
        @DisplayName("getBrowser_应该返回正确的浏览器信息_当提供iPhone Safari User-Agent时")
        void getBrowser_shouldReturnCorrectBrowserInfo_whenIPhoneSafariUserAgentProvided() {
            // Arrange
            when(request.getHeader("User-Agent")).thenReturn(
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");

            // Act
            String browser = Servlets.getBrowser(request);

            // Assert
            assertThat(browser).isEqualTo("Safari 13.0.3");
        }
    }

    @Nested
    @DisplayName("参数获取测试")
    class ParameterRetrievalTests {

        @Test
        @DisplayName("getParametersStartingWith_应该返回指定前缀的参数_当存在匹配参数时")
        void getParametersStartingWith_shouldReturnParametersWithPrefix_whenMatchingParametersExist() {
            // Arrange
            ServletRequest servletRequest = mock(ServletRequest.class);
            Map<String, String[]> parameterMap = new HashMap<>();
            parameterMap.put("prefix_param1", new String[]{"value1"});
            parameterMap.put("prefix_param2", new String[]{"value2"});
            parameterMap.put("other_param", new String[]{"value3"});

            when(servletRequest.getParameterMap()).thenReturn(parameterMap);
            when(servletRequest.getParameterNames()).thenReturn(Collections.enumeration(parameterMap.keySet()));
            parameterMap.forEach((k, v) -> when(servletRequest.getParameterValues(k)).thenReturn(v));

            // Act
            Map<String, Object> result = Servlets.getParametersStartingWith(servletRequest, "prefix_");

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get("param1")).isEqualTo("value1");
            assertThat(result.get("param2")).isEqualTo("value2");
        }

        @Test
        @DisplayName("getParametersEndingWith_应该返回指定后缀的参数_当存在匹配参数时")
        void getParametersEndingWith_shouldReturnParametersWithSuffix_whenMatchingParametersExist() {
            // Arrange
            ServletRequest servletRequest = mock(ServletRequest.class);
            Enumeration<String> paramNames = Collections.enumeration(
                    Arrays.asList("param1_suffix", "param2_suffix", "param3"));

            when(servletRequest.getParameterNames()).thenReturn(paramNames);
            when(servletRequest.getParameterValues("param1_suffix")).thenReturn(new String[]{"value1"});
            when(servletRequest.getParameterValues("param2_suffix")).thenReturn(new String[]{"value2"});
            when(servletRequest.getParameterValues("param3")).thenReturn(new String[]{"value3"});

            // Act
            Map<String, Object> result = Servlets.getParametersEndingWith(servletRequest, "_suffix");

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get("param1")).isEqualTo("value1");
            assertThat(result.get("param2")).isEqualTo("value2");
        }
    }

    @Nested
    @DisplayName("URL获取测试")
    class UrlRetrievalTests {

        @Test
        @DisplayName("getFullUrl_应该返回完整URL_当包含查询参数时")
        void getFullUrl_shouldReturnFullUrl_whenQueryParametersIncluded() {
            // Arrange
            when(request.getRequestURL()).thenReturn(new StringBuffer("http://example.com/path"));
            when(request.getQueryString()).thenReturn("param=value");

            // Act
            String fullUrl = Servlets.getFullUrl(request);

            // Assert
            assertThat(fullUrl).isEqualTo("http://example.com/path?param=value");
        }
    }

    @Nested
    @DisplayName("Web环境检测测试")
    class WebEnvironmentTests {

        @Test
        @DisplayName("isWebEnvironment_应该返回true_当在Web环境中时")
        void isWebEnvironment_shouldReturnTrue_whenInWebEnvironment() {
            // Act
            boolean result = Servlets.isWebEnviroument();

            // Assert
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("请求获取测试")
    class RequestRetrievalTests {

        @Test
        @DisplayName("getCurrentHttpServletRequest_应该返回当前请求_当请求上下文存在时")
        void getCurrentHttpServletRequest_shouldReturnCurrentRequest_whenRequestContextExists() {
            // Arrange
            when(servletRequestAttributes.getRequest()).thenReturn(request);

            // Act
            HttpServletRequest result = Servlets.getCurrentHttpServletRequest();

            // Assert
            assertThat(result).isEqualTo(request);
        }
    }

    @Nested
    @DisplayName("请求头格式化测试")
    class HeaderFormattingTests {

        @Test
        @DisplayName("getFormatHeader_应该返回格式化的请求头_当存在请求头时")
        void getFormatHeader_shouldReturnFormattedHeaders_whenHeadersExist() {
            // Arrange
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("Header1", "Header2")));
            when(request.getHeader("Header1")).thenReturn("Value1");
            when(request.getHeader("Header2")).thenReturn("Value2");

            // Act
            Map<String, String> headers = Servlets.getFormatHeader(request);

            // Assert
            assertThat(headers).hasSize(2);
            assertThat(headers.get("Header1")).isEqualTo("Value1");
            assertThat(headers.get("Header2")).isEqualTo("Value2");
        }
    }

    @Nested
    @DisplayName("参数格式化测试")
    class ParameterFormattingTests {

        @Test
        @DisplayName("getFormatParameter_应该返回格式化的参数_当存在参数时")
        void getFormatParameter_shouldReturnFormattedParameters_whenParametersExist() {
            // Arrange
            Map<String, String[]> parameterMap = new HashMap<>();
            parameterMap.put("param1", new String[]{"value1"});
            parameterMap.put("param2", new String[]{"value2", "value3"});

            when(request.getParameterMap()).thenReturn(parameterMap);

            // Act
            Map<String, String> result = Servlets.getFormatParamter(request);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result.get("param1")).isEqualTo("value1");
            assertThat(result.get("param2")).isEqualTo("value2,value3");
        }
    }

    @Nested
    @DisplayName("本地化获取测试")
    class LocaleRetrievalTests {

        @Test
        @DisplayName("getCurrentLocale_应该返回当前区域设置_当请求包含区域设置时")
        void getCurrentLocale_shouldReturnCurrentLocale_whenRequestContainsLocale() {
            // Arrange
            when(request.getLocale()).thenReturn(Locale.US);

            // Act
            Locale result = Servlets.getCurrentLocale();

            // Assert
            assertThat(result).isEqualTo(Locale.US);
        }
    }

    @Nested
    @DisplayName("请求头获取测试")
    class HeaderRetrievalTests {

        @Test
        @DisplayName("getHeader_应该返回指定请求头值_当请求头存在时")
        void getHeader_shouldReturnHeaderValue_whenHeaderExists() {
            // Arrange
            when(request.getHeader("headerKey")).thenReturn("Value");

            // Act
            String result = Servlets.getHeader(request, "headerKey");

            // Assert
            assertThat(result).isEqualTo("Value");
        }
    }

    @Nested
    @DisplayName("IP地址获取测试")
    class IpAddressTests {

        @Test
        @DisplayName("getIpAddr_应该返回正确IP地址_当X-Forwarded-For头存在时")
        void getIpAddr_shouldReturnCorrectIpAddress_whenXForwardedForHeaderExists() {
            // Arrange
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            given(httpServletRequest.getHeader("x-forwarded-for")).willReturn("123.23.2.3");

            // Act
            String result = Servlets.getIpAddr(httpServletRequest);

            // Assert
            assertThat(result).isEqualTo("123.23.2.3");
            verify(httpServletRequest).getHeader("x-forwarded-for");
        }

        @Test
        @DisplayName("getIpAddr_应该返回第一个有效IP_当X-Real-IP包含多个IP时")
        void getIpAddr_shouldReturnFirstValidIp_whenXRealIpContainsMultipleIps() {
            // Arrange
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            given(httpServletRequest.getHeader("x-real-ip")).willReturn("123.23.2.3,127.0.0.1");

            // Act
            String result = Servlets.getIpAddr(httpServletRequest);

            // Assert
            assertThat(result).isEqualTo("123.23.2.3");
            verify(httpServletRequest).getHeader("x-real-ip");
        }

        @Test
        @DisplayName("getIpAddr_应该过滤内网IP_当IP列表包含内网地址时")
        void getIpAddr_shouldFilterPrivateIps_whenIpListContainsPrivateAddresses() {
            // Arrange
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            given(httpServletRequest.getHeader("x-real-ip"))
                    .willReturn("127.0.0.1,192.168.1.1, 10.0.0.1,  172.16.0.1,123.23.2.3");

            // Act
            String result = Servlets.getIpAddr(httpServletRequest);

            // Assert
            assertThat(result).isEqualTo("123.23.2.3");
            verify(httpServletRequest).getHeader("x-real-ip");
        }

        @Test
        @DisplayName("getIpAddr_应该返回空字符串_当只有内网IP时")
        void getIpAddr_shouldReturnEmptyString_whenOnlyPrivateIpsExist() {
            // Arrange
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            given(httpServletRequest.getHeader("x-real-ip"))
                    .willReturn("127.0.0.1,192.168.1.1, 10.0.0.1,  172.16.0.1");

            // Act
            String result = Servlets.getIpAddr(httpServletRequest);

            // Assert
            assertThat(result).isEqualTo("");
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(httpServletRequest).getHeader("x-real-ip");
        }

        @Test
        @DisplayName("getIpAddr_应该返回空字符串_当没有IP头时")
        void getIpAddr_shouldReturnEmptyString_whenNoIpHeadersExist() {
            // Arrange
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

            // Act
            String result = Servlets.getIpAddr(httpServletRequest);

            // Assert
            assertThat(result).isEqualTo("");
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getIpAddr_应该按优先级获取IP_当多个IP头存在时")
        void getIpAddr_shouldGetIpByPriority_whenMultipleIpHeadersExist() {
            // Arrange
            HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
            given(httpServletRequest.getHeader("WL-Proxy-Client-IP")).willReturn("192.168.1.10");
            given(httpServletRequest.getHeader("x-real-ip")).willReturn("172.18.0.1");
            given(httpServletRequest.getHeader("X-Forwarded-For")).willReturn("10.0.0.10");
            given(httpServletRequest.getHeader("Proxy-Client-IP")).willReturn("192.168.1.10");
            given(httpServletRequest.getHeader("http_client_ip")).willReturn("169.254.10.0");
            given(httpServletRequest.getHeader("HTTP_X_FORWARDED_FOR")).willReturn("123.230.200.123");

            // Act
            String result = Servlets.getIpAddr(httpServletRequest);

            // Assert
            assertThat(result).isEqualTo("123.230.200.123");
            verify(httpServletRequest).getHeader("http_client_ip");
        }
    }
}