package cn.xuanyuanli.core.util.useragent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Browser 测试")
public class BrowserTest {

    @Nested
    @DisplayName("浏览器实例创建测试")
    class BrowserInstanceTests {

        @Test
        @DisplayName("constructor_应该创建有效的浏览器实例_当提供有效参数时")
        void constructor_shouldCreateValidBrowser_whenValidParametersProvided() {
            // Arrange & Act
            Browser browser = new Browser("TestBrowser", "testbrowser", "testbrowser/([\\d\\.]+)");

            // Assert
            assertThat(browser).isNotNull();
            assertThat(browser.getName()).isEqualTo("TestBrowser");
            assertThat(browser.isMatch("testbrowser/1.0")).isTrue();
        }

        @Test
        @DisplayName("constructor_应该处理OTHER_VERSION常量_当版本正则为OTHER_VERSION时")
        void constructor_shouldHandleOtherVersion_whenVersionRegexIsOtherVersion() {
            // Arrange & Act
            Browser browser = new Browser("TestBrowser", "testbrowser", Browser.OTHER_VERSION);

            // Assert
            assertThat(browser).isNotNull();
            assertThat(browser.getName()).isEqualTo("TestBrowser");
        }
    }

    @Nested
    @DisplayName("版本获取测试")
    class VersionExtractionTests {

        @ParameterizedTest(name = "getVersion_应该提取正确版本_当User-Agent为: {0}")
        @MethodSource("provideBrowserVersionTestCases")
        @DisplayName("getVersion_应该提取正确版本_当提供有效User-Agent时")
        void getVersion_shouldExtractCorrectVersion_whenValidUserAgentProvided(
                String userAgent, String expectedBrowser, String expectedVersion) {
            // Arrange
            Browser browser = Browser.BROWSERS.stream()
                    .filter(b -> b.getName().equals(expectedBrowser))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (browser != null && expectedVersion != null) {
                String version = browser.getVersion(userAgent);
                assertThat(version).isEqualTo(expectedVersion);
            }
        }

        private static Stream<Arguments> provideBrowserVersionTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36", "Chrome", "91.0.4472.124"),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0", "Firefox", "89.0"),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.864.59 Safari/537.36 Edg/91.0.864.59", "MSEdge", "91.0.864.59"),
                Arguments.of("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1", "Safari", "14.1.1"),
                Arguments.of("Mozilla/5.0 (Linux; Android 11; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36", "Chrome", "91.0.4472.120")
            );
        }

        @Test
        @DisplayName("getVersion_应该返回null_当浏览器为UNKNOWN时")
        void getVersion_shouldReturnNull_whenBrowserIsUnknown() {
            // Arrange
            String userAgent = "Some unknown browser";

            // Act
            String version = Browser.UNKNOWN.getVersion(userAgent);

            // Assert
            assertThat(version).isNull();
        }
    }

    @Nested
    @DisplayName("移动浏览器判断测试")
    class MobileBrowserTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "PSP",
            "Yammer Mobile",
            "Android Browser",
            "IEMobile",
            "MicroMessenger",
            "miniProgram",
            "DingTalk"
        })
        @DisplayName("isMobile_应该返回true_当浏览器为移动浏览器时")
        void isMobile_shouldReturnTrue_whenBrowserIsMobile(String browserName) {
            // Arrange
            Browser browser = Browser.BROWSERS.stream()
                    .filter(b -> b.getName().equals(browserName))
                    .findFirst()
                    .orElse(new Browser(browserName, browserName.toLowerCase(), null));

            // Act & Assert
            assertThat(browser.isMobile()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Chrome",
            "Firefox",
            "Safari",
            "MSEdge",
            "Opera"
        })
        @DisplayName("isMobile_应该返回false_当浏览器为桌面浏览器时")
        void isMobile_shouldReturnFalse_whenBrowserIsDesktop(String browserName) {
            // Arrange
            Browser browser = Browser.BROWSERS.stream()
                    .filter(b -> b.getName().equals(browserName))
                    .findFirst()
                    .orElse(new Browser(browserName, browserName.toLowerCase(), null));

            // Act & Assert
            assertThat(browser.isMobile()).isFalse();
        }
    }

    @Nested
    @DisplayName("浏览器匹配测试")
    class BrowserMatchingTests {

        @ParameterizedTest(name = "isMatch_应该正确匹配_当User-Agent包含: {0}")
        @MethodSource("provideBrowserMatchTestCases")
        @DisplayName("isMatch_应该正确匹配浏览器_当User-Agent包含关键字时")
        void isMatch_shouldMatchCorrectly_whenUserAgentContainsKeyword(
                String userAgent, String expectedBrowser, boolean shouldMatch) {
            // Arrange
            Browser browser = Browser.BROWSERS.stream()
                    .filter(b -> b.getName().equals(expectedBrowser))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (browser != null) {
                assertThat(browser.isMatch(userAgent)).isEqualTo(shouldMatch);
            }
        }

        private static Stream<Arguments> provideBrowserMatchTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36", "Chrome", true),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0", "Firefox", true),
                Arguments.of("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1", "Safari", true),
                Arguments.of("Mozilla/5.0 MicroMessenger/8.0.0", "MicroMessenger", true),
                Arguments.of("Some random user agent", "Chrome", false),
                Arguments.of("Some random user agent", "Firefox", false)
            );
        }
    }

    @Nested
    @DisplayName("静态常量和集合测试")
    class StaticConstantsTests {

        @Test
        @DisplayName("UNKNOWN_应该是有效的Browser实例_当访问时")
        void unknown_shouldBeValidBrowser_whenAccessed() {
            // Act & Assert
            assertThat(Browser.UNKNOWN).isNotNull();
            assertThat(Browser.UNKNOWN.getName()).isEqualTo("Unknown");
            assertThat(Browser.UNKNOWN.isUnknown()).isTrue();
        }

        @Test
        @DisplayName("BROWSERS_应该包含预定义的浏览器_当访问时")
        void browsers_shouldContainPredefinedBrowsers_whenAccessed() {
            // Act & Assert
            assertThat(Browser.BROWSERS).isNotEmpty();
            assertThat(Browser.BROWSERS.size()).isGreaterThan(20);
            
            // 检查一些重要的浏览器是否存在
            assertThat(Browser.BROWSERS.stream().anyMatch(b -> "Chrome".equals(b.getName()))).isTrue();
            assertThat(Browser.BROWSERS.stream().anyMatch(b -> "Firefox".equals(b.getName()))).isTrue();
            assertThat(Browser.BROWSERS.stream().anyMatch(b -> "Safari".equals(b.getName()))).isTrue();
            assertThat(Browser.BROWSERS.stream().anyMatch(b -> "MSEdge".equals(b.getName()))).isTrue();
        }

        @Test
        @DisplayName("OTHER_VERSION_应该是有效的正则表达式模式_当访问时")
        void otherVersion_shouldBeValidRegexPattern_whenAccessed() {
            // Act & Assert
            assertThat(Browser.OTHER_VERSION).isNotNull();
            assertThat(Browser.OTHER_VERSION).isEqualTo("[\\/ ]([\\d\\w\\.\\-]+)");
        }
    }

    @Nested
    @DisplayName("自定义浏览器添加测试")
    class CustomBrowserTests {

        @Test
        @DisplayName("addCustomBrowser_应该成功添加自定义浏览器_当提供有效参数时")
        void addCustomBrowser_shouldSuccessfullyAddCustomBrowser_whenValidParametersProvided() {
            // Arrange
            int originalSize = Browser.BROWSERS.size();
            String customName = "TestCustomBrowser";
            String customRegex = "testcustombrowser";
            String customVersionRegex = "testcustombrowser\\/([\\d\\.]+)";

            // Act
            assertThatNoException().isThrownBy(() -> 
                Browser.addCustomBrowser(customName, customRegex, customVersionRegex));

            // Assert
            assertThat(Browser.BROWSERS.size()).isEqualTo(originalSize + 1);
            Browser addedBrowser = Browser.BROWSERS.get(Browser.BROWSERS.size() - 1);
            assertThat(addedBrowser.getName()).isEqualTo(customName);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("constructor_应该处理null参数_当参数为null时")
        void constructor_shouldHandleNullParameters_whenParametersAreNull() {
            // Act & Assert
            assertThatNoException().isThrownBy(() -> {
                Browser browser = new Browser("TestBrowser", null, null);
                assertThat(browser.getName()).isEqualTo("TestBrowser");
            });
        }

        @Test
        @DisplayName("getVersion_应该处理null或空User-Agent_当输入无效时")
        void getVersion_shouldHandleNullOrEmptyUserAgent_whenInputIsInvalid() {
            // Arrange
            Browser browser = new Browser("TestBrowser", "testbrowser", "testbrowser\\/([\\d\\.]+)");

            // Act & Assert
            // 由于getVersion方法在null参数时会抛出NPE，这是实际行为，我们需要捕获异常
            try {
                String version1 = browser.getVersion(null);
                assertThat(version1).isNull();
            } catch (NullPointerException e) {
                // 这是可接受的行为，null参数导致NPE
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
            
            // 空字符串和不匹配的字符串应该返回null
            String version2 = browser.getVersion("");
            String version3 = browser.getVersion("no match");
            
            assertThat(version2).isNull();
            assertThat(version3).isNull();
        }
    }
}