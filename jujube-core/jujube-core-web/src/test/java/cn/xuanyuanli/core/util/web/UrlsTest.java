package cn.xuanyuanli.core.util.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.util.UriComponents;

/**
 * 测试 Urls 工具类
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
class UrlsTest {

    @Nested
    @DisplayName("parse 方法测试")
    class ParseMethodTest {

        @Test
        @DisplayName("解析复杂URL并验证各部分")
        void parse_ComplexUrl_ValidatesAllParts() {
            // Arrange
            String url = "https://a.example.com/path with spaces/and?query=中文#abc你好";

            // Act
            UriComponents uriComponents = Urls.parse(url);

            // Assert
            assertThat(uriComponents).isNotNull();
            assertThat(uriComponents.getPath()).isEqualTo("/path with spaces/and");
            assertThat(uriComponents.getScheme()).isEqualTo("https");
            assertThat(uriComponents.getHost()).isEqualTo("a.example.com");
            assertThat(uriComponents.getPort()).isEqualTo(-1);
            assertThat(uriComponents.getFragment()).isEqualTo("abc你好");
            assertThat(uriComponents.getQuery()).isEqualTo("query=中文");
            assertThat(uriComponents.getPathSegments()).isEqualTo(List.of("path with spaces", "and"));
            assertThat(uriComponents.getQueryParams()).containsOnly(Map.entry("query", List.of("中文")));
            assertThat(uriComponents.toUriString()).isEqualTo("https://a.example.com/path with spaces/and?query=中文#abc你好");
            assertThat(uriComponents.encode().toUriString()).isEqualTo(
                    "https://a.example.com/path%20with%20spaces/and?query=%E4%B8%AD%E6%96%87#abc%E4%BD%A0%E5%A5%BD");
        }
    }

    @Nested
    @DisplayName("encodeURIComponent 方法测试")
    class EncodeURIComponentMethodTest {

        @DisplayName("编码合法输入")
        @ParameterizedTest(name = "编码输入: {0}")
        @CsvSource({"https://wsaddev.csdn.com/, https%3A%2F%2Fwsaddev.csdn.com%2F",
                "https://wsaddev.csdn.com/getMatchesList?id=23295, https%3A%2F%2Fwsaddev.csdn.com%2FgetMatchesList%3Fid%3D23295",
                "hello world!@#$%^&*(), hello%20world!%40%23%24%25%5E%26*()"})
        void encodeURIComponent_ValidInput_ReturnsEncodedString(String input, String expectedOutput) {
            // Act
            String result = Urls.encodeURIComponent(input);

            // Assert
            assertThat(result).isEqualTo(expectedOutput);
        }

        @DisplayName("编码空白或空输入")
        @ParameterizedTest(name = "编码空白或空输入: {0}")
        @ValueSource(strings = {"", " "})
        @NullSource
        void encodeURIComponent_BlankOrNullInput_ReturnsEmptyString(String input) {
            // Act
            String result = Urls.encodeURIComponent(input);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("decodeURIComponent 方法测试")
    class DecodeURIComponentMethodTest {

        @ParameterizedTest(name = "解码输入: {0}")
        @DisplayName("解码合法输入")
        @CsvSource({"https%3A%2F%2Fwsaddev.csdn.com%2F, https://wsaddev.csdn.com/",
                "https%3A%2F%2Fwsaddev.csdn.com%2FgetMatchesList%3Fid%3D23295, https://wsaddev.csdn.com/getMatchesList?id=23295",
                "hello%20world!%40%23%24%25%5E%26*(), hello world!@#$%^&*()"})
        void decodeURIComponent_ValidInput_ReturnsDecodedString(String input, String expectedOutput) {
            // Act
            String result = Urls.decodeURIComponent(input);

            // Assert
            assertThat(result).isEqualTo(expectedOutput);
        }

        @ParameterizedTest(name = "解码空白或空输入: {0}")
        @DisplayName("解码空白或空输入")
        @ValueSource(strings = {"", " "})
        @NullSource
        void decodeURIComponent_BlankOrNullInput_ReturnsEmptyString(String input) {
            // Act
            String result = Urls.decodeURIComponent(input);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("解码包含加号的字符串")
        void decodeURIComponent_PlusSymbol_HandlesAsLiteralPlus() {
            // Arrange
            String input = "hello+world";

            // Act
            String result = Urls.decodeURIComponent(input);

            // Assert
            assertThat(result).isEqualTo("hello+world");
        }
    }

    @Nested
    @DisplayName("encodeURI 方法测试")
    class EncodeURIMethodTest {

        @ParameterizedTest(name = "编码输入: {0}")
        @DisplayName("编码输入")
        @CsvSource({"你好 world, %E4%BD%A0%E5%A5%BD%20world"})
        void encodeURI_ValidInput_EncodesCorrectly(String input, String expectedOutput) {
            // Act
            String result = Urls.encodeURI(input);

            // Assert
            assertThat(result).isEqualTo(expectedOutput);
        }

        @Test
        @DisplayName("编码特殊字符集")
        void encodeURI_SpecialCharacters_EncodesCorrectly() {
            // Arrange
            String input = "!#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
            String expectedOutput = "!#!%23$%25&'()*+,-./:;%3C=%3E?@%5B%5C%5D%5E_%60%7B%7C%7D~";

            // Act
            String result = Urls.encodeURI(input);

            // Assert
            assertThat(result).isEqualTo(expectedOutput);
        }


        @ParameterizedTest(name = "编码空白或空输入: {0}")
        @DisplayName("编码空白或空输入")
        @ValueSource(strings = {"", " "})
        @NullSource
        void encodeURI_BlankOrNullInput_ReturnsEmptyString(String input) {
            // Act
            String result = Urls.encodeURI(input);

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("isChinese 方法测试")
    class IsChineseMethodTest {

        @Test
        @DisplayName("验证中文字符返回 true")
        void isChinese_ValidChineseCharacter_ReturnsTrue() {
            // Arrange
            char chineseChar = '一';

            // Act
            boolean result = Urls.isChinese(chineseChar);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("验证非中文字符返回 false")
        void isChinese_NonChineseCharacter_ReturnsFalse() {
            // Arrange
            char nonChineseChar = 'a';

            // Act
            boolean result = Urls.isChinese(nonChineseChar);

            // Assert
            assertThat(result).isFalse();
        }
    }
}
