package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("ChineseNumbers 中文数字转换测试")
class ChineseNumbersTest {

    @Nested
    @DisplayName("中文转数字测试")
    class ChineseToNumberTests {

        @Test
        @DisplayName("chineseNumberToEnglish_应该正确转换中文数字_当输入各种中文数字格式时")
        void chineseNumberToEnglish_shouldConvertChineseNumbersCorrectly_whenGivenVariousChineseFormats() {
            // Act & Assert
            assertThat(ChineseNumbers.chineseNumberToEnglish("捌玖")).isEqualTo(89);
            assertThat(ChineseNumbers.chineseNumberToEnglish("十五")).isEqualTo(15);
            assertThat(ChineseNumbers.chineseNumberToEnglish("一点五")).isEqualTo(1.5);
            assertThat(ChineseNumbers.chineseNumberToEnglish("一百五")).isEqualTo(150);
            assertThat(ChineseNumbers.chineseNumberToEnglish("一百五十")).isEqualTo(150);
            assertThat(ChineseNumbers.chineseNumberToEnglish("一千两百万")).isEqualTo(12000000);
        }
    }

    @Nested
    @DisplayName("数字转中文测试")
    class NumberToChineseTests {

        @Test
        @DisplayName("englishNumberToChinese_应该正确转换数字为中文_当输入各种数字格式时")
        void englishNumberToChinese_shouldConvertNumbersToChineseCorrectly_whenGivenVariousNumberFormats() {
            // Act & Assert
            assertThat(ChineseNumbers.englishNumberToChinese("15")).isEqualTo("十五");
            assertThat(ChineseNumbers.englishNumberToChinese("1.5")).isEqualTo("一点五");
            assertThat(ChineseNumbers.englishNumberToChinese("150")).isEqualTo("一百五十");
            assertThat(ChineseNumbers.englishNumberToChinese("1500")).isEqualTo("一千五百");
            assertThat(ChineseNumbers.englishNumberToChinese("15000")).isEqualTo("一万五千");
        }
    }
}
