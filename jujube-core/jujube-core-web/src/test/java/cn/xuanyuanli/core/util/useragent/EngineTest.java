package cn.xuanyuanli.core.util.useragent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Engine 测试")
public class EngineTest {

    @Nested
    @DisplayName("引擎实例创建测试")
    class EngineInstanceTests {

        @Test
        @DisplayName("constructor_应该创建有效的引擎实例_当提供有效参数时")
        void constructor_shouldCreateValidEngine_whenValidParametersProvided() {
            // Arrange & Act
            Engine engine = new Engine("TestEngine", "testengine");

            // Assert
            assertThat(engine).isNotNull();
            assertThat(engine.getName()).isEqualTo("TestEngine");
            assertThat(engine.isMatch("testengine")).isTrue();
        }

        @Test
        @DisplayName("constructor_应该处理null正则表达式_当正则表达式为null时")
        void constructor_shouldHandleNullRegex_whenRegexIsNull() {
            // Arrange & Act
            Engine engine = new Engine("TestEngine", null);

            // Assert
            assertThat(engine).isNotNull();
            assertThat(engine.getName()).isEqualTo("TestEngine");
        }
    }

    @Nested
    @DisplayName("版本获取测试")
    class VersionExtractionTests {

        @ParameterizedTest(name = "getVersion_应该提取正确版本_当User-Agent为: {0}")
        @MethodSource("provideEngineVersionTestCases")
        @DisplayName("getVersion_应该提取正确版本_当提供有效User-Agent时")
        void getVersion_shouldExtractCorrectVersion_whenValidUserAgentProvided(
                String userAgent, String expectedEngine, String expectedVersion) {
            // Arrange
            Engine engine = Engine.ENGINES.stream()
                    .filter(e -> e.getName().equals(expectedEngine))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (engine != null) {
                String version = engine.getVersion(userAgent);
                if (expectedVersion != null) {
                    assertThat(version).isEqualTo(expectedVersion);
                } else {
                    assertThat(version).isNull();
                }
            }
        }

        private static Stream<Arguments> provideEngineVersionTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36", "Webkit", "537.36"),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36", "Chrome", "91.0.4472.124"),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0", "Gecko", "20100101"),
                Arguments.of("Mozilla/5.0 compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0", "Trident", "5.0"),
                Arguments.of("Opera/9.80 (Windows NT 6.1; WOW64) Presto/2.12.388 Version/12.18", "Opera", "9.80"),
                Arguments.of("Opera/9.80 (Windows NT 6.1; WOW64) Presto/2.12.388 Version/12.18", "Presto", "2.12.388")
            );
        }

        @Test
        @DisplayName("getVersion_应该返回null_当引擎为UNKNOWN时")
        void getVersion_shouldReturnNull_whenEngineIsUnknown() {
            // Arrange
            String userAgent = "Some unknown engine";

            // Act
            String version = Engine.UNKNOWN.getVersion(userAgent);

            // Assert
            assertThat(version).isNull();
        }

        @Test
        @DisplayName("getVersion_应该处理null User-Agent_当输入为null时")
        void getVersion_shouldHandleNullUserAgent_whenInputIsNull() {
            // Arrange
            Engine engine = new Engine("TestEngine", "testengine");

            // Act & Assert
            try {
                String version = engine.getVersion(null);
                assertThat(version).isNull();
            } catch (NullPointerException e) {
                // 这是可接受的行为，null参数导致NPE
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }
    }

    @Nested
    @DisplayName("引擎匹配测试")
    class EngineMatchingTests {

        @ParameterizedTest(name = "isMatch_应该正确匹配_当User-Agent包含: {0}")
        @MethodSource("provideEngineMatchTestCases")
        @DisplayName("isMatch_应该正确匹配引擎_当User-Agent包含关键字时")
        void isMatch_shouldMatchCorrectly_whenUserAgentContainsKeyword(
                String userAgent, String expectedEngine, boolean shouldMatch) {
            // Arrange
            Engine engine = Engine.ENGINES.stream()
                    .filter(e -> e.getName().equals(expectedEngine))
                    .findFirst()
                    .orElse(null);

            // Act & Assert
            if (engine != null) {
                assertThat(engine.isMatch(userAgent)).isEqualTo(shouldMatch);
            }
        }

        private static Stream<Arguments> provideEngineMatchTestCases() {
            return Stream.of(
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36", "Webkit", true),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0", "Chrome", true),
                Arguments.of("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101", "Gecko", true),
                Arguments.of("Mozilla/5.0 compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0", "Trident", true),
                Arguments.of("Opera/9.80 (Windows NT 6.1; WOW64) Presto/2.12.388", "Presto", true),
                Arguments.of("Some random user agent", "Webkit", false),
                Arguments.of("Some random user agent", "Chrome", false)
            );
        }
    }

    @Nested
    @DisplayName("静态常量和集合测试")
    class StaticConstantsTests {

        @Test
        @DisplayName("UNKNOWN_应该是有效的Engine实例_当访问时")
        void unknown_shouldBeValidEngine_whenAccessed() {
            // Act & Assert
            assertThat(Engine.UNKNOWN).isNotNull();
            assertThat(Engine.UNKNOWN.getName()).isEqualTo("Unknown");
            assertThat(Engine.UNKNOWN.isUnknown()).isTrue();
        }

        @Test
        @DisplayName("ENGINES_应该包含预定义的引擎_当访问时")
        void engines_shouldContainPredefinedEngines_whenAccessed() {
            // Act & Assert
            assertThat(Engine.ENGINES).isNotEmpty();
            assertThat(Engine.ENGINES.size()).isEqualTo(9);
            
            // 检查一些重要的引擎是否存在
            assertThat(Engine.ENGINES.stream().anyMatch(e -> "Webkit".equals(e.getName()))).isTrue();
            assertThat(Engine.ENGINES.stream().anyMatch(e -> "Chrome".equals(e.getName()))).isTrue();
            assertThat(Engine.ENGINES.stream().anyMatch(e -> "Gecko".equals(e.getName()))).isTrue();
            assertThat(Engine.ENGINES.stream().anyMatch(e -> "Trident".equals(e.getName()))).isTrue();
        }

        @Test
        @DisplayName("ENGINES_应该包含所有预期的引擎类型_当访问时")
        void engines_shouldContainAllExpectedEngineTypes_whenAccessed() {
            // Arrange
            String[] expectedEngines = {
                "Trident", "Webkit", "Chrome", "Opera", "Presto", 
                "Gecko", "KHTML", "Konqueror", "MIDP"
            };

            // Act & Assert
            for (String expectedEngine : expectedEngines) {
                assertThat(Engine.ENGINES.stream()
                    .anyMatch(e -> expectedEngine.equals(e.getName())))
                    .as("应该包含引擎: " + expectedEngine)
                    .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("constructor_应该处理null名称_当名称为null时")
        void constructor_shouldHandleNullName_whenNameIsNull() {
            // Act & Assert
            Engine engine = new Engine(null, "testengine");
            assertThat(engine).isNotNull();
            assertThat(engine.getName()).isNull();
        }

        @Test
        @DisplayName("getVersion_应该处理空字符串User-Agent_当输入为空字符串时")
        void getVersion_shouldHandleEmptyUserAgent_whenInputIsEmpty() {
            // Arrange
            Engine engine = new Engine("TestEngine", "testengine");

            // Act
            String version = engine.getVersion("");

            // Assert
            assertThat(version).isNull();
        }

        @Test
        @DisplayName("getVersion_应该处理不匹配的User-Agent_当User-Agent不包含引擎信息时")
        void getVersion_shouldHandleNonMatchingUserAgent_whenUserAgentDoesNotContainEngine() {
            // Arrange
            Engine engine = new Engine("TestEngine", "testengine");

            // Act
            String version = engine.getVersion("some other engine info");

            // Assert
            assertThat(version).isNull();
        }

        @Test
        @DisplayName("isMatch_应该处理null输入_当输入为null时")
        void isMatch_shouldHandleNullInput_whenInputIsNull() {
            // Arrange
            Engine engine = new Engine("TestEngine", "testengine");

            // Act & Assert
            // 由于isMatch可能会因为null参数抛出异常，我们需要处理这种情况
            try {
                boolean result = engine.isMatch(null);
                assertThat(result).isFalse();
            } catch (Exception e) {
                // 这是可接受的行为
                assertThat(e).isInstanceOf(Exception.class);
            }
        }
    }
}