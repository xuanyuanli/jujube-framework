package cn.xuanyuanli.core.util.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cn.xuanyuanli.core.util.Dates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Cookies 测试")
class CookiesTest {

    @Nested
    @DisplayName("Cookie获取测试")
    class CookieRetrievalTests {

        @Test
        @DisplayName("getCookie_应该返回正确的Cookie值_当指定名称的Cookie存在时")
        void getCookie_shouldReturnCorrectValue_whenCookieWithNameExists() {
            // Arrange
            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie[] cookies = {new Cookie("key1", "val1"), new Cookie("key2", "val2")};
            given(request.getCookies()).willReturn(cookies);

            // Act
            String result = Cookies.getCookie(request, "key1");

            // Assert
            assertThat(result).isEqualTo("val1");
            verify(request).getCookies();
        }

        @Test
        @DisplayName("getCookie_应该返回null_当指定名称的Cookie不存在时")
        void getCookie_shouldReturnNull_whenCookieWithNameDoesNotExist() {
            // Arrange
            HttpServletRequest request = mock(HttpServletRequest.class);
            Cookie[] cookies = {new Cookie("key1", "val1")};
            given(request.getCookies()).willReturn(cookies);

            // Act
            String result = Cookies.getCookie(request, "nonexistent");

            // Assert
            assertThat(result).isNull();
            verify(request).getCookies();
        }
    }

    @Nested
    @DisplayName("Cookie添加测试")
    class CookieAdditionTests {

        @Test
        @DisplayName("addCookie_应该添加带有指定属性的Cookie_当提供有效参数时")
        void addCookie_shouldAddCookieWithSpecifiedAttributes_whenValidParametersProvided() {
            // Arrange
            HttpServletResponse response = mock(HttpServletResponse.class);
            doNothing().when(response).addCookie(any(Cookie.class));

            // Act
            Cookies.addCookie(response, "key", "value", 1000);

            // Assert
            verify(response).addCookie(argThat(cookie ->
                    "key".equals(cookie.getName()) &&
                    "value".equals(cookie.getValue()) &&
                    cookie.getSecure() &&
                    cookie.getMaxAge() == 1000
            ));
        }

        @Test
        @DisplayName("addCookieForSession_应该添加会话Cookie_当调用时")
        void addCookieForSession_shouldAddSessionCookie_whenCalled() {
            // Arrange
            HttpServletResponse response = mock(HttpServletResponse.class);
            doNothing().when(response).addCookie(any(Cookie.class));

            // Act
            Cookies.addCookieForSession(response, "sessionKey", "sessionValue");

            // Assert
            verify(response).addCookie(argThat(cookie ->
                    "sessionKey".equals(cookie.getName()) &&
                    "sessionValue".equals(cookie.getValue()) &&
                    cookie.getSecure() &&
                    cookie.getMaxAge() == -1
            ));
        }

        @Test
        @DisplayName("addCookieForDayEnded_应该添加当天结束到期的Cookie_当调用时")
        void addCookieForDayEnded_shouldAddCookieExpiringAtEndOfDay_whenCalled() {
            // Arrange
            HttpServletResponse response = mock(HttpServletResponse.class);
            doNothing().when(response).addCookie(any(Cookie.class));

            // Act
            Cookies.addCookieForDayEnded(response, "dayKey", "dayValue");

            // Assert
            verify(response).addCookie(argThat(cookie ->
                    "dayKey".equals(cookie.getName()) &&
                    "dayValue".equals(cookie.getValue()) &&
                    cookie.getSecure() &&
                    cookie.getMaxAge() == Dates.endOfToday()
            ));
        }
    }

    @Nested
    @DisplayName("Cookie删除测试")
    class CookieDeletionTests {

        @Test
        @DisplayName("delCookie_应该添加空值且MaxAge为0的Cookie_当删除指定Cookie时")
        void delCookie_shouldAddEmptyValueCookieWithZeroMaxAge_whenDeletingSpecificCookie() {
            // Arrange
            HttpServletResponse response = mock(HttpServletResponse.class);
            doNothing().when(response).addCookie(any(Cookie.class));

            // Act
            Cookies.delCookie(response, "keyToDelete");

            // Assert
            verify(response).addCookie(argThat(cookie ->
                    "keyToDelete".equals(cookie.getName()) &&
                    cookie.getValue().isEmpty() &&
                    cookie.getSecure() &&
                    cookie.getMaxAge() == 0
            ));
        }
    }
}