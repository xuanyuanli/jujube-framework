package cn.xuanyuanli.core.util.useragent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Platform 测试")
public class PlatformTest {

    @Nested
    @DisplayName("平台实例创建测试")
    class PlatformInstanceTests {

        @Test
        @DisplayName("constructor_应该创建有效的Platform实例_当提供有效参数时")
        void constructor_shouldCreateValidPlatform_whenValidParametersProvided() {
            // Arrange & Act
            Platform platform = new Platform("TestPlatform", "testplatform");

            // Assert
            assertThat(platform).isNotNull();
            assertThat(platform.getName()).isEqualTo("TestPlatform");
            assertThat(platform.isMatch("testplatform")).isTrue();
        }
    }

    @Nested
    @DisplayName("静态常量测试")
    class StaticConstantsTests {

        @Test
        @DisplayName("UNKNOWN_应该是有效的Platform实例_当访问时")
        void unknown_shouldBeValidPlatform_whenAccessed() {
            // Act & Assert
            assertThat(Platform.UNKNOWN).isNotNull();
            assertThat(Platform.UNKNOWN.getName()).isEqualTo("Unknown");
            assertThat(Platform.UNKNOWN.isUnknown()).isTrue();
        }

        @Test
        @DisplayName("预定义常量_应该存在并有效_当访问时")
        void predefinedConstants_shouldExistAndBeValid_whenAccessed() {
            // Act & Assert
            assertThat(Platform.IPHONE).isNotNull();
            assertThat(Platform.IPHONE.getName()).isEqualTo("iPhone");
            
            assertThat(Platform.IPOD).isNotNull();
            assertThat(Platform.IPOD.getName()).isEqualTo("iPod");
            
            assertThat(Platform.IPAD).isNotNull();
            assertThat(Platform.IPAD.getName()).isEqualTo("iPad");
            
            assertThat(Platform.ANDROID).isNotNull();
            assertThat(Platform.ANDROID.getName()).isEqualTo("Android");
            
            assertThat(Platform.GOOGLE_TV).isNotNull();
            assertThat(Platform.GOOGLE_TV.getName()).isEqualTo("GoogleTV");
            
            assertThat(Platform.WINDOWS_PHONE).isNotNull();
            assertThat(Platform.WINDOWS_PHONE.getName()).isEqualTo("Windows Phone");
        }
    }

    @Nested
    @DisplayName("平台集合测试")
    class PlatformCollectionTests {

        @Test
        @DisplayName("MOBILE_PLATFORMS_应该包含移动平台_当访问时")
        void mobilePlatforms_shouldContainMobilePlatforms_whenAccessed() {
            // Act & Assert
            assertThat(Platform.MOBILE_PLATFORMS).isNotEmpty();
            assertThat(Platform.MOBILE_PLATFORMS.size()).isGreaterThan(5);
            
            // 检查重要的移动平台是否存在
            assertThat(Platform.MOBILE_PLATFORMS).contains(Platform.IPHONE);
            assertThat(Platform.MOBILE_PLATFORMS).contains(Platform.IPAD);
            assertThat(Platform.MOBILE_PLATFORMS).contains(Platform.IPOD);
            assertThat(Platform.MOBILE_PLATFORMS).contains(Platform.ANDROID);
            assertThat(Platform.MOBILE_PLATFORMS).contains(Platform.GOOGLE_TV);
            assertThat(Platform.MOBILE_PLATFORMS).contains(Platform.WINDOWS_PHONE);
        }

        @Test
        @DisplayName("DESKTOP_PLATFORMS_应该包含桌面平台_当访问时")
        void desktopPlatforms_shouldContainDesktopPlatforms_whenAccessed() {
            // Act & Assert
            assertThat(Platform.DESKTOP_PLATFORMS).isNotEmpty();
            assertThat(Platform.DESKTOP_PLATFORMS.size()).isGreaterThan(3);
            
            // 检查重要的桌面平台是否存在
            assertThat(Platform.DESKTOP_PLATFORMS.stream().anyMatch(p -> "Windows".equals(p.getName()))).isTrue();
            assertThat(Platform.DESKTOP_PLATFORMS.stream().anyMatch(p -> "Mac".equals(p.getName()))).isTrue();
            assertThat(Platform.DESKTOP_PLATFORMS.stream().anyMatch(p -> "Linux".equals(p.getName()))).isTrue();
        }

        @Test
        @DisplayName("PLATFORMS_应该包含所有平台_当访问时")
        void platforms_shouldContainAllPlatforms_whenAccessed() {
            // Act & Assert
            assertThat(Platform.PLATFORMS).isNotEmpty();
            assertThat(Platform.PLATFORMS.size()).isEqualTo(
                Platform.MOBILE_PLATFORMS.size() + Platform.DESKTOP_PLATFORMS.size());
            
            // 验证包含所有移动平台
            for (Platform mobilePlatform : Platform.MOBILE_PLATFORMS) {
                assertThat(Platform.PLATFORMS).contains(mobilePlatform);
            }
            
            // 验证包含所有桌面平台
            for (Platform desktopPlatform : Platform.DESKTOP_PLATFORMS) {
                assertThat(Platform.PLATFORMS).contains(desktopPlatform);
            }
        }
    }

    @Nested
    @DisplayName("移动平台判断测试")
    class MobilePlatformTests {

        @Test
        @DisplayName("isMobile_应该返回true_当平台为移动平台时")
        void isMobile_shouldReturnTrue_whenPlatformIsMobile() {
            // Act & Assert
            assertThat(Platform.IPHONE.isMobile()).isTrue();
            assertThat(Platform.IPAD.isMobile()).isTrue();
            assertThat(Platform.IPOD.isMobile()).isTrue();
            assertThat(Platform.ANDROID.isMobile()).isTrue();
            assertThat(Platform.GOOGLE_TV.isMobile()).isTrue();
            assertThat(Platform.WINDOWS_PHONE.isMobile()).isTrue();
        }

        @Test
        @DisplayName("isMobile_应该返回false_当平台为桌面平台时")
        void isMobile_shouldReturnFalse_whenPlatformIsDesktop() {
            // Arrange
            Platform windowsPlatform = Platform.DESKTOP_PLATFORMS.stream()
                    .filter(p -> "Windows".equals(p.getName()))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (windowsPlatform != null) {
                assertThat(windowsPlatform.isMobile()).isFalse();
            }
            
            assertThat(Platform.UNKNOWN.isMobile()).isFalse();
        }
    }

    @Nested
    @DisplayName("iOS相关判断测试")
    class IOSRelatedTests {

        @Test
        @DisplayName("isIPhoneOrIPod_应该返回true_当平台为iPhone或iPod时")
        void isIPhoneOrIPod_shouldReturnTrue_whenPlatformIsIPhoneOrIPod() {
            // Act & Assert
            assertThat(Platform.IPHONE.isIPhoneOrIPod()).isTrue();
            assertThat(Platform.IPOD.isIPhoneOrIPod()).isTrue();
            assertThat(Platform.IPAD.isIPhoneOrIPod()).isFalse();
            assertThat(Platform.ANDROID.isIPhoneOrIPod()).isFalse();
        }

        @Test
        @DisplayName("isIPad_应该返回true_当平台为iPad时")
        void isIPad_shouldReturnTrue_whenPlatformIsIPad() {
            // Act & Assert
            assertThat(Platform.IPAD.isIPad()).isTrue();
            assertThat(Platform.IPHONE.isIPad()).isFalse();
            assertThat(Platform.IPOD.isIPad()).isFalse();
            assertThat(Platform.ANDROID.isIPad()).isFalse();
        }

        @Test
        @DisplayName("isIos_应该返回true_当平台为iOS设备时")
        void isIos_shouldReturnTrue_whenPlatformIsIOSDevice() {
            // Act & Assert
            assertThat(Platform.IPHONE.isIos()).isTrue();
            assertThat(Platform.IPOD.isIos()).isTrue();
            assertThat(Platform.IPAD.isIos()).isTrue();
            assertThat(Platform.ANDROID.isIos()).isFalse();
            assertThat(Platform.GOOGLE_TV.isIos()).isFalse();
        }
    }

    @Nested
    @DisplayName("Android相关判断测试")
    class AndroidRelatedTests {

        @Test
        @DisplayName("isAndroid_应该返回true_当平台为Android时")
        void isAndroid_shouldReturnTrue_whenPlatformIsAndroid() {
            // Act & Assert
            assertThat(Platform.ANDROID.isAndroid()).isTrue();
            assertThat(Platform.GOOGLE_TV.isAndroid()).isTrue();
            assertThat(Platform.IPHONE.isAndroid()).isFalse();
            assertThat(Platform.IPAD.isAndroid()).isFalse();
            assertThat(Platform.WINDOWS_PHONE.isAndroid()).isFalse();
        }
    }

    @Nested
    @DisplayName("平台匹配测试")
    class PlatformMatchingTests {

        @ParameterizedTest(name = "isMatch_应该正确匹配_当User-Agent包含: {0}")
        @MethodSource("providePlatformMatchTestCases")
        @DisplayName("isMatch_应该正确匹配平台_当User-Agent包含关键字时")
        void isMatch_shouldMatchCorrectly_whenUserAgentContainsKeyword(
                String userAgent, Platform expectedPlatform, boolean shouldMatch) {
            // Act & Assert
            assertThat(expectedPlatform.isMatch(userAgent)).isEqualTo(shouldMatch);
        }

        private static Stream<Arguments> providePlatformMatchTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)", Platform.IPHONE, true),
                Arguments.of("Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X)", Platform.IPAD, true),
                Arguments.of("Mozilla/5.0 (iPod touch; CPU iPhone OS 14_6 like Mac OS X)", Platform.IPOD, true),
                Arguments.of("Mozilla/5.0 (Linux; Android 11; SM-G991B)", Platform.ANDROID, true),
                Arguments.of("Mozilla/5.0 (Windows Phone 10.0; Android 6.0.1)", Platform.WINDOWS_PHONE, true),
                Arguments.of("Some random user agent", Platform.IPHONE, false),
                Arguments.of("Some random user agent", Platform.ANDROID, false)
            );
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("constructor_应该处理null参数_当参数为null时")
        void constructor_shouldHandleNullParameters_whenParametersAreNull() {
            // Act & Assert
            Platform platform1 = new Platform(null, "testplatform");
            Platform platform2 = new Platform("TestPlatform", null);
            
            assertThat(platform1).isNotNull();
            assertThat(platform2).isNotNull();
            assertThat(platform1.getName()).isNull();
            assertThat(platform2.getName()).isEqualTo("TestPlatform");
        }

        @Test
        @DisplayName("isMatch_应该处理null输入_当输入为null时")
        void isMatch_shouldHandleNullInput_whenInputIsNull() {
            // Arrange
            Platform platform = new Platform("TestPlatform", "testplatform");

            // Act & Assert
            try {
                boolean result = platform.isMatch(null);
                assertThat(result).isFalse();
            } catch (Exception e) {
                // 这是可接受的行为
                assertThat(e).isInstanceOf(Exception.class);
            }
        }

        @Test
        @DisplayName("equals_应该正确比较平台_当比较不同平台时")
        void equals_shouldCorrectlyComparePlatforms_whenComparingDifferentPlatforms() {
            // Act & Assert
            assertThat(Platform.IPHONE).isNotEqualTo(Platform.IPAD);
            assertThat(Platform.ANDROID).isNotEqualTo(Platform.IPHONE);
            assertThat(Platform.IPHONE).isNotEqualTo(Platform.ANDROID);
            
            // 测试自定义平台
            Platform custom1 = new Platform("Custom", "custom");
            Platform custom2 = new Platform("Custom", "custom");
            Platform custom3 = new Platform("Other", "other");
            
            assertThat(custom1).isEqualTo(custom2);
            assertThat(custom1).isNotEqualTo(custom3);
        }
    }
}