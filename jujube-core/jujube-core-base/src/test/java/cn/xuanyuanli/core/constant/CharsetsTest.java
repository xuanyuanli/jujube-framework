package cn.xuanyuanli.core.constant;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Charsets 常量类测试
 * 验证字符集常量的正确性
 */
@DisplayName("Charsets 常量类测试")
class CharsetsTest {

    @Nested
    @DisplayName("标准字符集常量测试")
    class StandardCharsetsTests {

        @Test
        @DisplayName("UTF_8_应该等于标准UTF8字符集_当获取UTF8字符集时")
        void UTF_8_shouldEqualStandardUTF8_whenGettingUTF8Charset() {
            // Act
            Charset actual = Charsets.UTF_8;

            // Assert
            assertThat(actual).isEqualTo(StandardCharsets.UTF_8);
        }

        @Test
        @DisplayName("ISO_8859_1_应该等于标准ISO88591字符集_当获取ISO88591字符集时")
        void ISO_8859_1_shouldEqualStandardISO88591_whenGettingISO88591Charset() {
            // Act
            Charset actual = Charsets.ISO_8859_1;

            // Assert
            assertThat(actual).isEqualTo(StandardCharsets.ISO_8859_1);
        }

        @Test
        @DisplayName("US_ASCII_应该等于标准ASCII字符集_当获取ASCII字符集时")
        void US_ASCII_shouldEqualStandardASCII_whenGettingASCIICharset() {
            // Act
            Charset actual = Charsets.US_ASCII;

            // Assert
            assertThat(actual).isEqualTo(StandardCharsets.US_ASCII);
        }

        @Test
        @DisplayName("UTF_16_应该等于标准UTF16字符集_当获取UTF16字符集时")
        void UTF_16_shouldEqualStandardUTF16_whenGettingUTF16Charset() {
            // Act
            Charset actual = Charsets.UTF_16;

            // Assert
            assertThat(actual).isEqualTo(StandardCharsets.UTF_16);
        }

        @Test
        @DisplayName("UTF_16BE_应该等于标准UTF16BE字符集_当获取UTF16BE字符集时")
        void UTF_16BE_shouldEqualStandardUTF16BE_whenGettingUTF16BECharset() {
            // Act
            Charset actual = Charsets.UTF_16BE;

            // Assert
            assertThat(actual).isEqualTo(StandardCharsets.UTF_16BE);
        }

        @Test
        @DisplayName("UTF_16LE_应该等于标准UTF16LE字符集_当获取UTF16LE字符集时")
        void UTF_16LE_shouldEqualStandardUTF16LE_whenGettingUTF16LECharset() {
            // Act
            Charset actual = Charsets.UTF_16LE;

            // Assert
            assertThat(actual).isEqualTo(StandardCharsets.UTF_16LE);
        }
    }

    @Nested
    @DisplayName("扩展字符集常量测试")
    class ExtendedCharsetsTests {

        @Test
        @DisplayName("GBK_应该等于GBK字符集_当获取GBK字符集时")
        void GBK_shouldEqualGBKCharset_whenGettingGBKCharset() {
            // Arrange
            Charset expectedGBK = Charset.forName("GBK");

            // Act
            Charset actual = Charsets.GBK;

            // Assert
            assertThat(actual).isEqualTo(expectedGBK);
            assertThat(actual.name()).isEqualTo("GBK");
        }
    }
}
