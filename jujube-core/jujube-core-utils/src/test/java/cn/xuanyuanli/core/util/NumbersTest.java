package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Numbers 数字工具类测试")
class NumbersTest {

    @Nested
    @DisplayName("数字格式化测试")
    class NumberFormatTests {

        @Test
        @DisplayName("numberFormat_应该按格式输出_当输入有效数字和格式时")
        void numberFormat_shouldFormatCorrectly_whenInputValidNumberAndFormat() {
            // Act & Assert
            assertThat(Numbers.numberFormat(123.344, "##.####")).isEqualTo("123.344");
            assertThat(Numbers.numberFormat(123.3444, "##.####")).isEqualTo("123.3444");
        }

        @Test
        @DisplayName("numberFormat_应该截断多余小数位_当小数位超过格式限制时")
        void numberFormat_shouldTruncateExtraDecimals_whenDecimalsExceedFormatLimit() {
            // Act & Assert
            assertThat(Numbers.numberFormat(123.34444, "##.####")).isEqualTo("123.3444");
        }

        @Test
        @DisplayName("numberFormat_应该忽略小数部分_当格式不包含小数位时")
        void numberFormat_shouldIgnoreDecimals_whenFormatHasNoDecimals() {
            // Act & Assert
            assertThat(Numbers.numberFormat(13.34444, "###")).isEqualTo("13");
            assertThat(Numbers.numberFormat(123000000.34444, "##")).isEqualTo("123000000");
        }

        @Test
        @DisplayName("numberFormat_应该处理零和null值_当输入特殊值时")
        void numberFormat_shouldHandleZeroAndNull_whenInputSpecialValues() {
            // Act & Assert
            assertThat(Numbers.numberFormat(0, "##.##")).isEqualTo("0");
            assertThat(Numbers.numberFormat(null, "##.##")).isNull();
        }
    }

    @Nested
    @DisplayName("货币格式化测试")
    class MoneyFormatTests {

        @Test
        @DisplayName("moneyFormat_应该按货币格式输出_当输入有效金额时")
        void moneyFormat_shouldFormatAsCurrency_whenInputValidAmount() {
            // Act & Assert
            assertThat(Numbers.moneyFormat(700000000.224)).isEqualTo("700,000,000.22");
            assertThat(Numbers.moneyFormat(700000000.225)).isEqualTo("700,000,000.23");
        }

        @Test
        @DisplayName("moneyFormat_应该省略小数位_当小数部分为零时")
        void moneyFormat_shouldOmitDecimals_whenDecimalPartIsZero() {
            // Act & Assert
            assertThat(Numbers.moneyFormat(7000000000.0011)).isEqualTo("7,000,000,000");
        }

        @Test
        @DisplayName("moneyFormat_应该处理零和null值_当输入特殊值时")
        void moneyFormat_shouldHandleZeroAndNull_whenInputSpecialValues() {
            // Act & Assert
            assertThat(Numbers.moneyFormat(0.0)).isEqualTo("0");
            assertThat(Numbers.moneyFormat(null)).isNull();
        }
    }

    @Nested
    @DisplayName("中文前缀货币格式化测试")
    class ChinesePrefixMoneyFormatTests {

        @Test
        @DisplayName("moneyFormatOfZhPrefix_应该使用中文万单位_当启用中文前缀时")
        void moneyFormatOfZhPrefix_shouldUseChineseWanUnit_whenChinesePrefixEnabled() {
            // Act & Assert
            assertThat(Numbers.moneyFormatOfZhPrefix(7000000000.0011, true)).isEqualTo("700,000万");
        }

        @Test
        @DisplayName("moneyFormatOfZhPrefix_应该使用普通格式_当禁用中文前缀时")
        void moneyFormatOfZhPrefix_shouldUseNormalFormat_whenChinesePrefixDisabled() {
            // Act & Assert
            assertThat(Numbers.moneyFormatOfZhPrefix(7000000000.0011, false)).isEqualTo("7,000,000,000");
        }

        @Test
        @DisplayName("moneyFormatOfZhPrefix_应该处理零和null值_当输入特殊值时")
        void moneyFormatOfZhPrefix_shouldHandleZeroAndNull_whenInputSpecialValues() {
            // Act & Assert
            assertThat(Numbers.moneyFormatOfZhPrefix(0.0, false)).isEqualTo("0");
            assertThat(Numbers.moneyFormatOfZhPrefix(null, false)).isNull();
            assertThat(Numbers.moneyFormatOfZhPrefix(null, true)).isNull();
        }
    }

    @Nested
    @DisplayName("数字转字符串测试")
    class NumberToStringTests {

        @Test
        @DisplayName("numberToString_应该转换为字符串_当输入各种数字类型时")
        void numberToString_shouldConvertToString_whenInputVariousNumberTypes() {
            // Act & Assert
            assertThat(Numbers.numberToString(890000000000.8)).isEqualTo("890000000000.8");
            assertThat(Numbers.numberToString(89.898012555D)).isEqualTo("89.898012555");
            assertThat(Numbers.numberToString(89.898F)).isEqualTo("89.898");
            assertThat(Numbers.numberToString(898)).isEqualTo("898");
            assertThat(Numbers.numberToString(80000000000000L)).isEqualTo("80000000000000");
        }

        @Test
        @DisplayName("numberToString_应该返回null_当输入null时")
        void numberToString_shouldReturnNull_whenInputIsNull() {
            // Act & Assert
            assertThat(Numbers.numberToString(null)).isNull();
        }
    }
}
