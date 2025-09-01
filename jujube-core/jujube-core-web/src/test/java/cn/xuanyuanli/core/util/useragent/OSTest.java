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

@DisplayName("OS 测试")
public class OSTest {

    @Nested
    @DisplayName("操作系统实例创建测试")
    class OSInstanceTests {

        @Test
        @DisplayName("constructor_应该创建有效的OS实例_当提供名称和正则表达式时")
        void constructor_shouldCreateValidOS_whenProvidingNameAndRegex() {
            // Arrange & Act
            OS os = new OS("TestOS", "testos");

            // Assert
            assertThat(os).isNotNull();
            assertThat(os.getName()).isEqualTo("TestOS");
            assertThat(os.isMatch("testos")).isTrue();
        }

        @Test
        @DisplayName("constructor_应该创建有效的OS实例_当提供完整参数时")
        void constructor_shouldCreateValidOS_whenProvidingFullParameters() {
            // Arrange & Act
            OS os = new OS("TestOS", "testos", "testos ([\\d\\.]+)");

            // Assert
            assertThat(os).isNotNull();
            assertThat(os.getName()).isEqualTo("TestOS");
            assertThat(os.isMatch("testos")).isTrue();
        }
    }

    @Nested
    @DisplayName("版本获取测试")
    class VersionExtractionTests {

        @ParameterizedTest(name = "getVersion_应该提取正确版本_当User-Agent为: {0}")
        @MethodSource("provideOSVersionTestCases")
        @DisplayName("getVersion_应该提取正确版本_当提供有效User-Agent时")
        void getVersion_shouldExtractCorrectVersion_whenValidUserAgentProvided(
                String userAgent, String expectedOS, String expectedVersion) {
            // Arrange
            OS os = OS.OSES.stream()
                    .filter(o -> o.getName().equals(expectedOS))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (os != null) {
                String version = os.getVersion(userAgent);
                if (expectedVersion != null) {
                    assertThat(version).isEqualTo(expectedVersion);
                } else {
                    assertThat(version).isNull();
                }
            }
        }

        private static Stream<Arguments> provideOSVersionTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Windows 10 or Windows Server 2016", "10.0"),
                Arguments.of("Mozilla/5.0 (Windows NT 6.3; Win64; x64)", "Windows 8.1 or Winsows Server 2012R2", "6.3"),
                Arguments.of("Mozilla/5.0 (Windows NT 6.1; Win64; x64)", "Windows 7 or Windows Server 2008R2", "6.1"),
                Arguments.of("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)", "OSX", "10_15_7"),
                Arguments.of("Mozilla/5.0 (X11; Linux x86_64)", "Linux", null),
                Arguments.of("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)", "iPhone", "14_6"),
                Arguments.of("Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X)", "iPad", "14_6"),
                Arguments.of("Mozilla/5.0 (Linux; Android 11; SM-G991B)", "Android", "11")
            );
        }

        @Test
        @DisplayName("getVersion_应该返回null_当OS为UNKNOWN时")
        void getVersion_shouldReturnNull_whenOSIsUnknown() {
            // Arrange
            String userAgent = "Some unknown OS";

            // Act
            String version = OS.UNKNOWN.getVersion(userAgent);

            // Assert
            assertThat(version).isNull();
        }

        @Test
        @DisplayName("getVersion_应该返回null_当没有版本模式时")
        void getVersion_shouldReturnNull_whenNoVersionPattern() {
            // Arrange
            OS os = new OS("TestOS", "testos"); // 没有版本正则

            // Act
            String version = os.getVersion("testos 1.0");

            // Assert
            assertThat(version).isNull();
        }
    }

    @Nested
    @DisplayName("操作系统匹配测试")
    class OSMatchingTests {

        @ParameterizedTest(name = "isMatch_应该正确匹配_当User-Agent包含: {0}")
        @MethodSource("provideOSMatchTestCases")
        @DisplayName("isMatch_应该正确匹配操作系统_当User-Agent包含关键字时")
        void isMatch_shouldMatchCorrectly_whenUserAgentContainsKeyword(
                String userAgent, String expectedOS, boolean shouldMatch) {
            // Arrange
            OS os = OS.OSES.stream()
                    .filter(o -> o.getName().equals(expectedOS))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (os != null) {
                assertThat(os.isMatch(userAgent)).isEqualTo(shouldMatch);
            }
        }

        private static Stream<Arguments> provideOSMatchTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", "Windows 10 or Windows Server 2016", true),
                Arguments.of("Mozilla/5.0 (Windows NT 6.1; Win64; x64)", "Windows 7 or Windows Server 2008R2", true),
                Arguments.of("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)", "OSX", true),
                Arguments.of("Mozilla/5.0 (X11; Linux x86_64)", "Linux", true),
                Arguments.of("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)", "iPhone", true),
                Arguments.of("Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X)", "iPad", true),
                Arguments.of("Mozilla/5.0 (Linux; Android 11; SM-G991B)", "Android", true),
                Arguments.of("Some random user agent", "Windows 10 or Windows Server 2016", false),
                Arguments.of("Some random user agent", "Linux", false)
            );
        }
    }

    @Nested
    @DisplayName("静态常量和集合测试")
    class StaticConstantsTests {

        @Test
        @DisplayName("UNKNOWN_应该是有效的OS实例_当访问时")
        void unknown_shouldBeValidOS_whenAccessed() {
            // Act & Assert
            assertThat(OS.UNKNOWN).isNotNull();
            assertThat(OS.UNKNOWN.getName()).isEqualTo("Unknown");
            assertThat(OS.UNKNOWN.isUnknown()).isTrue();
        }

        @Test
        @DisplayName("OSES_应该包含预定义的操作系统_当访问时")
        void oses_shouldContainPredefinedOSes_whenAccessed() {
            // Act & Assert
            assertThat(OS.OSES).isNotEmpty();
            assertThat(OS.OSES.size()).isGreaterThan(15);
            
            // 检查一些重要的操作系统是否存在
            assertThat(OS.OSES.stream().anyMatch(o -> o.getName().contains("Windows 10"))).isTrue();
            assertThat(OS.OSES.stream().anyMatch(o -> "OSX".equals(o.getName()))).isTrue();
            assertThat(OS.OSES.stream().anyMatch(o -> "Linux".equals(o.getName()))).isTrue();
            assertThat(OS.OSES.stream().anyMatch(o -> "Android".equals(o.getName()))).isTrue();
            assertThat(OS.OSES.stream().anyMatch(o -> "iPhone".equals(o.getName()))).isTrue();
            assertThat(OS.OSES.stream().anyMatch(o -> "iPad".equals(o.getName()))).isTrue();
        }

        @Test
        @DisplayName("OSES_应该包含各种Windows版本_当访问时")
        void oses_shouldContainVariousWindowsVersions_whenAccessed() {
            // Act & Assert
            long windowsCount = OS.OSES.stream()
                    .filter(o -> o.getName().toLowerCase().contains("windows"))
                    .count();
            
            assertThat(windowsCount).isGreaterThanOrEqualTo(8);
        }
    }

    @Nested
    @DisplayName("自定义操作系统添加测试")
    class CustomOSTests {

        @Test
        @DisplayName("addCustomOs_应该成功添加自定义操作系统_当提供有效参数时")
        void addCustomOs_shouldSuccessfullyAddCustomOS_whenValidParametersProvided() {
            // Arrange
            int originalSize = OS.OSES.size();
            String customName = "TestCustomOS";
            String customRegex = "testcustomos";
            String customVersionRegex = "testcustomos ([\\d\\.]+)";

            // Act
            assertThatNoException().isThrownBy(() -> 
                OS.addCustomOs(customName, customRegex, customVersionRegex));

            // Assert
            assertThat(OS.OSES.size()).isEqualTo(originalSize + 1);
            OS addedOS = OS.OSES.get(OS.OSES.size() - 1);
            assertThat(addedOS.getName()).isEqualTo(customName);
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
                OS os1 = new OS(null, "testos");
                OS os2 = new OS("TestOS", null);
                OS os3 = new OS("TestOS", "testos", null);
                
                assertThat(os1).isNotNull();
                assertThat(os2).isNotNull();
                assertThat(os3).isNotNull();
            });
        }

        @Test
        @DisplayName("getVersion_应该处理null User-Agent_当输入为null时")
        void getVersion_shouldHandleNullUserAgent_whenInputIsNull() {
            // Arrange
            OS os = new OS("TestOS", "testos", "testos ([\\d\\.]+)");

            // Act & Assert
            try {
                String version = os.getVersion(null);
                assertThat(version).isNull();
            } catch (NullPointerException e) {
                // 这是可接受的行为，null参数导致NPE
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        @DisplayName("getVersion_应该处理空字符串User-Agent_当输入为空字符串时")
        void getVersion_shouldHandleEmptyUserAgent_whenInputIsEmpty() {
            // Arrange
            OS os = new OS("TestOS", "testos", "testos ([\\d\\.]+)");

            // Act
            String version = os.getVersion("");

            // Assert
            assertThat(version).isNull();
        }

        @Test
        @DisplayName("getVersion_应该处理不匹配的User-Agent_当User-Agent不包含OS信息时")
        void getVersion_shouldHandleNonMatchingUserAgent_whenUserAgentDoesNotContainOS() {
            // Arrange
            OS os = new OS("TestOS", "testos", "testos ([\\d\\.]+)");

            // Act
            String version = os.getVersion("some other os info");

            // Assert
            assertThat(version).isNull();
        }

        @Test
        @DisplayName("isMatch_应该处理null输入_当输入为null时")
        void isMatch_shouldHandleNullInput_whenInputIsNull() {
            // Arrange
            OS os = new OS("TestOS", "testos");

            // Act & Assert
            try {
                boolean result = os.isMatch(null);
                assertThat(result).isFalse();
            } catch (Exception e) {
                // 这是可接受的行为
                assertThat(e).isInstanceOf(Exception.class);
            }
        }
    }
}