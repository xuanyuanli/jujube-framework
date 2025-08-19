package cn.xuanyuanli.core.util.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cn.xuanyuanli.core.util.Dates;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mockito;

class CookiesTest {

    @Test
    void testGetCookie() {
        HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
        BDDMockito.given(httpServletRequest.getCookies()).willReturn(new Cookie[]{new Cookie("key1", "val1")});
        String result = Cookies.getCookie(httpServletRequest, "key1");
        Assertions.assertEquals("val1", result);
        BDDMockito.verify(httpServletRequest).getCookies();
    }

    @Test
    void testAddCookie() {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        BDDMockito.doNothing().when(httpServletResponse).addCookie(Mockito.any(Cookie.class));
        Cookies.addCookie(httpServletResponse, "key", "value", 1000);
        BDDMockito.verify(httpServletResponse)
                .addCookie(Mockito.argThat(e -> e.getName().equals("key") && e.getValue().equals("value") && e.getSecure() && e.getMaxAge() == 1000));
    }

    @Test
    void testAddCookieForSession() {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        BDDMockito.doNothing().when(httpServletResponse).addCookie(Mockito.any(Cookie.class));
        Cookies.addCookieForSession(httpServletResponse, "key", "value");
        BDDMockito.verify(httpServletResponse)
                .addCookie(Mockito.argThat(e -> e.getName().equals("key") && e.getValue().equals("value") && e.getSecure() && e.getMaxAge() == -1));
    }

    @Test
    void testAddCookieForDayEnded() {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        BDDMockito.doNothing().when(httpServletResponse).addCookie(Mockito.any(Cookie.class));
        Cookies.addCookieForDayEnded(httpServletResponse, "key", "value");
        BDDMockito.verify(httpServletResponse).addCookie(
                Mockito.argThat(e -> e.getName().equals("key") && e.getValue().equals("value") && e.getSecure() && e.getMaxAge() == Dates.endOfToday()));
    }

    @Test
    void testDelCookie() {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        BDDMockito.doNothing().when(httpServletResponse).addCookie(Mockito.any(Cookie.class));
        Cookies.delCookie(httpServletResponse, "key");
        BDDMockito.verify(httpServletResponse)
                .addCookie(Mockito.argThat(e -> e.getName().equals("key") && e.getValue().isEmpty() && e.getSecure() && e.getMaxAge() == 0));
    }
}


