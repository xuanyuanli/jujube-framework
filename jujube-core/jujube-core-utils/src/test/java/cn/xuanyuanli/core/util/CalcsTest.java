package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Calcs 数学计算工具测试")
class CalcsTest {

    @Nested
    @DisplayName("数值比较测试")
    class ComparisonTests {

        @Test
        @DisplayName("isLow_应该正确比较数值_当使用数字类型时")
        void isLow_shouldCompareNumbersCorrectly_whenUsingNumberType() {
            // Act & Assert
            assertThat(Calcs.isLow(0.0, 1)).isTrue();
            assertThat(Calcs.isLow(0.0, 0)).isFalse();
            assertThat(Calcs.isLow(1.01, 1)).isFalse();
            assertThat(Calcs.isLow(1.0000000001, 1)).isFalse();
        }

        @Test
        @DisplayName("isLow_应该正确比较数值_当使用字符串类型时")
        void isLow_shouldCompareNumbersCorrectly_whenUsingStringType() {
            // Act & Assert
            assertThat(Calcs.isLow("0.0", "1")).isTrue();
            assertThat(Calcs.isLow("0.0", "0")).isFalse();
            assertThat(Calcs.isLow("1.01", "1")).isFalse();
            assertThat(Calcs.isLow("1.0000000001", "1")).isFalse();
        }

        @Test
        @DisplayName("isLte_应该正确比较小于等于_当使用数字类型时")
        void isLte_shouldCompareLessThanOrEqualCorrectly_whenUsingNumberType() {
            // Act & Assert
            assertThat(Calcs.isLte(0.0, 1)).isTrue();
            assertThat(Calcs.isLte(0.0, 0)).isTrue();
            assertThat(Calcs.isLte(1.01, 1)).isFalse();
            assertThat(Calcs.isLte(1.0000000001, 1)).isFalse();
        }

        @Test
        @DisplayName("equ_应该正确判断相等_当比较数值时")
        void equ_shouldCompareEqualityCorrectly_whenComparingNumbers() {
            // Act & Assert
            assertThat(Calcs.equ(0.0, 0)).isTrue();
            assertThat(Calcs.equ(0.000001, 0)).isFalse();
            assertThat(Calcs.equ(0.0f, 0)).isTrue();
        }
    }

    @Nested
    @DisplayName("加法运算测试")
    class AdditionTests {

        @Test
        @DisplayName("add_应该正确处理浮点精度_当使用指定精度时")
        void add_shouldHandleFloatingPointPrecision_whenUsingSpecifiedScale() {
            // Arrange
            double floatingPointIssue = 0.1 + 0.2;

            // Act & Assert
            assertThat(floatingPointIssue).isEqualTo(0.30000000000000004);
            assertThat(Calcs.add(0.0, 0, 2)).isEqualTo(0.0);
            assertThat(Calcs.add(0.1 + 0.2, 0.2, 18)).isEqualTo(0.50000000000000004);
            assertThat(Calcs.add(0.1 + 0.2, 0.2, 18)).isEqualTo(0.5);
            assertThat(Calcs.add(0.01, 0, 2)).isEqualTo(0.01);
            assertThat(Calcs.add(0.005, 0, 2)).isEqualTo(0.01);
            assertThat(Calcs.add(0.004, 0, 2)).isEqualTo(0.0);
            assertThat(Calcs.add(0.004, 0, 3)).isEqualTo(0.004);
            assertThat(Calcs.add(12000000000.2536D, 100000000, 5)).isEqualTo(12100000000.2536);
            assertThat(Calcs.add(12000000000.2536D, 100000000, 3)).isEqualTo(12100000000.254);
        }

        @Test
        @DisplayName("add_应该正确计算加法_当使用默认精度时")
        void add_shouldCalculateAdditionCorrectly_whenUsingDefaultScale() {
            // Act & Assert
            assertThat(Calcs.add(0.0, 0.0)).isEqualTo(0.0);
            assertThat(Calcs.add(0.01, 0)).isEqualTo(0.01);
            assertThat(Calcs.add(0.005, 0)).isEqualTo(0.01);
            assertThat(Calcs.add(0.004, 0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("add_应该正确计算字符串加法_当使用指定精度时")
        void add_shouldCalculateStringAdditionCorrectly_whenUsingSpecifiedScale() {
            // Act & Assert
            assertThat(Calcs.add("0.0", "0", 2)).isEqualTo("0.00");
            assertThat(Calcs.add("0.0", "0", 3)).isEqualTo("0.000");
            assertThat(Calcs.add("0.01", "0", 2)).isEqualTo("0.01");
            assertThat(Calcs.add("0.005", "0", 2)).isEqualTo("0.01");
            assertThat(Calcs.add("0.004", "0", 3)).isEqualTo("0.004");
        }

        @Test
        @DisplayName("add_应该正确计算字符串加法_当使用默认精度时")
        void add_shouldCalculateStringAdditionCorrectly_whenUsingDefaultScale() {
            // Act & Assert
            assertThat(Calcs.add("0.0", "0")).isEqualTo("0.00");
            assertThat(Calcs.add("0.01", "0")).isEqualTo("0.01");
            assertThat(Calcs.add("0.005", "0")).isEqualTo("0.01");
            assertThat(Calcs.add("0.004", "0")).isEqualTo("0.00");
        }

        @Test
        @DisplayName("getSum_应该正确计算多个数的和_当包含null值时")
        void getSum_shouldCalculateSumOfMultipleNumbers_whenContainingNullValues() {
            // Act & Assert
            assertThat(Calcs.getSum(0.0, 0)).isEqualTo(0.0);
            assertThat(Calcs.getSum(0.0, null)).isEqualTo(0.0);
            assertThat(Calcs.getSum(0.01, 0)).isEqualTo(0.01);
            assertThat(Calcs.getSum(0.01, null)).isEqualTo(0.01);
            assertThat(Calcs.getSum(0.005, 0)).isEqualTo(0.01);
            assertThat(Calcs.getSum(0.004, 0)).isEqualTo(0.0);
            assertThat(Calcs.getSum(0.004, 0, 0.006)).isEqualTo(0.01);
            assertThat(Calcs.getSum(0.04, 0, 2)).isEqualTo(2.04);
            assertThat(Calcs.getSum(0.004, 0, 2, 0.01)).isEqualTo(2.01);
            assertThat(Calcs.getSum(0.004, 0, 2, 0.001, 0.005)).isEqualTo(2.01);
            assertThat(Calcs.getSum(0.004, 0, 2, 0.001, 0.005, null)).isEqualTo(2.01);
        }
    }

    @Nested
    @DisplayName("乘法运算测试")
    class MultiplicationTests {

        @Test
        @DisplayName("mul_应该正确计算乘法_当使用数字类型时")
        void mul_shouldCalculateMultiplicationCorrectly_whenUsingNumberType() {
            // Act & Assert
            assertThat(Calcs.mul(1.0, 0.996)).isEqualTo(1.0);
            assertThat(Calcs.mul(123.55, 0.996)).isEqualTo(123.06);
        }

        @Test
        @DisplayName("mul_应该正确计算乘法_当使用数字类型和指定精度时")
        void mul_shouldCalculateMultiplicationCorrectly_whenUsingNumberTypeWithScale() {
            // Act & Assert
            assertThat(Calcs.mul(1.0, 0.996, 3)).isEqualTo(0.996);
            assertThat(Calcs.mul(123.55, 0.996, 4)).isEqualTo(123.0558);
        }

        @Test
        @DisplayName("mul_应该正确计算乘法_当使用字符串类型时")
        void mul_shouldCalculateMultiplicationCorrectly_whenUsingStringType() {
            // Act & Assert
            assertThat(Calcs.mul("1.0", "0.996")).isEqualTo("1.00");
            assertThat(Calcs.mul("123.55", "0.996")).isEqualTo("123.06");
        }

        @Test
        @DisplayName("mul_应该正确计算乘法_当使用字符串类型和指定精度时")
        void mul_shouldCalculateMultiplicationCorrectly_whenUsingStringTypeWithScale() {
            // Act & Assert
            assertThat(Calcs.mul("1.0", "0.996", 3)).isEqualTo("0.996");
            assertThat(Calcs.mul("123.55", "0.996", 4)).isEqualTo("123.0558");
        }
    }

    @Nested
    @DisplayName("减法运算测试")
    class SubtractionTests {

        @Test
        @DisplayName("sub_应该正确计算减法_当使用字符串类型时")
        void sub_shouldCalculateSubtractionCorrectly_whenUsingStringType() {
            // Act & Assert
            assertThat(Calcs.sub("1.0", "0.996")).isEqualTo("0.00");
            assertThat(Calcs.sub("123.55", "0.996")).isEqualTo("122.55");
        }

        @Test
        @DisplayName("sub_应该正确计算减法_当使用字符串类型和指定精度时")
        void sub_shouldCalculateSubtractionCorrectly_whenUsingStringTypeWithScale() {
            // Act & Assert
            assertThat(Calcs.sub("1.0", "0.996", 3)).isEqualTo("0.004");
            assertThat(Calcs.sub("123.55", "0.996", 4)).isEqualTo("122.5540");
        }

        @Test
        @DisplayName("sub_应该正确计算减法_当使用数字类型时")
        void sub_shouldCalculateSubtractionCorrectly_whenUsingNumberType() {
            // Act & Assert
            assertThat(Calcs.sub(1.0, 0.996)).isEqualTo(0);
            assertThat(Calcs.sub(123.55, 0.996)).isEqualTo(122.55);
        }

        @Test
        @DisplayName("sub_应该正确计算减法_当使用数字类型和指定精度时")
        void sub_shouldCalculateSubtractionCorrectly_whenUsingNumberTypeWithScale() {
            // Act & Assert
            assertThat(Calcs.sub(1.0, 0.996, 3)).isEqualTo(0.004);
            assertThat(Calcs.sub(123.55, 0.996, 4)).isEqualTo(122.554);
        }
    }

    @Nested
    @DisplayName("除法运算测试")
    class DivisionTests {

        @Test
        @DisplayName("div_应该正确计算除法_当使用字符串类型时")
        void div_shouldCalculateDivisionCorrectly_whenUsingStringType() {
            // Act & Assert
            assertThat(Calcs.div("12", "1")).isEqualTo("12.00");
            assertThat(Calcs.div("12", "0")).isEqualTo("12.00");
            assertThat(Calcs.div("1", "0.996")).isEqualTo("1.00");
            assertThat(Calcs.div("123.55", "0.996")).isEqualTo("124.05");
        }

        @Test
        @DisplayName("div_应该正确计算除法_当使用字符串类型和指定精度时")
        void div_shouldCalculateDivisionCorrectly_whenUsingStringTypeWithScale() {
            // Act & Assert
            assertThat(Calcs.div("12", "1", 3)).isEqualTo("12.000");
            assertThat(Calcs.div("1", "0.996", 3)).isEqualTo("1.004");
            assertThat(Calcs.div("123.55", "0.996", 5)).isEqualTo("124.04618");
        }

        @Test
        @DisplayName("div_应该正确计算除法_当使用数字类型时")
        void div_shouldCalculateDivisionCorrectly_whenUsingNumberType() {
            // Act & Assert
            assertThat(Calcs.div(12, 1)).isEqualTo(12.0);
            assertThat(Calcs.div(12, 0)).isEqualTo(12.0);
            assertThat(Calcs.div(1, 0.996)).isEqualTo(1.0);
            assertThat(Calcs.div(123.55, 0.996)).isEqualTo(124.05);
        }

        @Test
        @DisplayName("div_应该正确计算除法_当使用数字类型和指定精度时")
        void div_shouldCalculateDivisionCorrectly_whenUsingNumberTypeWithScale() {
            // Act & Assert
            assertThat(Calcs.div(12, 1, 3)).isEqualTo(12.0);
            assertThat(Calcs.div(1, 0.996, 3)).isEqualTo(1.004);
            assertThat(Calcs.div(123.55, 0.996, 5)).isEqualTo(124.04618);
        }
    }

    @Nested
    @DisplayName("统计运算测试")
    class StatisticsTests {

        @Test
        @DisplayName("getAverage_应该正确计算平均值_当输入数值列表时")
        void getAverage_shouldCalculateAverageCorrectly_whenGivenNumberList() {
            // Arrange
            ArrayList<Number> numbers = new ArrayList<>(Arrays.asList(12, 13, 14, 11, 10));

            // Act
            double result = Calcs.getAverage(numbers);

            // Assert
            assertThat(result).isEqualTo(12);
        }

        @Test
        @DisplayName("getMedian_应该正确计算中位数_当输入数值列表时")
        void getMedian_shouldCalculateMedianCorrectly_whenGivenNumberList() {
            // Arrange
            ArrayList<Number> numbers = new ArrayList<>(Arrays.asList(12, 13, 14, 11, 10));

            // Act
            double result = Calcs.getMedian(numbers);

            // Assert
            assertThat(result).isEqualTo(13);
        }
    }
}