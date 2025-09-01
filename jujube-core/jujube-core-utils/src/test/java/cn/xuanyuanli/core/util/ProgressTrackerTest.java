package cn.xuanyuanli.core.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProgressTracker 单元测试
 *
 * @author xuanyuanli
 * @date 2025/09/01
 */
@DisplayName("ProgressTracker 测试")
class ProgressTrackerTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("仅提供总数时应创建默认小数位数的实例")
        void constructor_shouldCreateWithDefaultDecimalPlaces_whenOnlyTotalProvided() {
            // Arrange & Act
            ProgressTracker tracker = new ProgressTracker(100);

            // Assert
            assertThat(tracker.getTotal()).isEqualTo(100);
            assertThat(tracker.getDecimalPlaces()).isEqualTo(2);
            assertThat(tracker.getCurrent()).isZero();
        }

        @Test
        @DisplayName("提供两个参数时应创建自定义小数位数的实例")
        void constructor_shouldCreateWithCustomDecimalPlaces_whenBothParametersProvided() {
            // Arrange & Act
            ProgressTracker tracker = new ProgressTracker(100, 3);

            // Assert
            assertThat(tracker.getTotal()).isEqualTo(100);
            assertThat(tracker.getDecimalPlaces()).isEqualTo(3);
            assertThat(tracker.getCurrent()).isZero();
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -100})
        @DisplayName("总数为 [{0}] 时应抛出异常")
        void constructor_shouldThrowException_whenTotalIsZeroOrNegative(long invalidTotal) {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new ProgressTracker(invalidTotal))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("总数必须大于0");
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, -5})
        @DisplayName("小数位数为 [{0}] 时应抛出异常")
        void constructor_shouldThrowException_whenDecimalPlacesIsNegative(int invalidDecimalPlaces) {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new ProgressTracker(100, invalidDecimalPlaces))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("小数点保留位数不能为负数");
        }

        @Test
        @DisplayName("小数位数为零时应正常接受")
        void constructor_shouldAcceptZeroDecimalPlaces_whenValid() {
            // Arrange & Act
            ProgressTracker tracker = new ProgressTracker(100, 0);

            // Assert
            assertThat(tracker.getDecimalPlaces()).isZero();
        }
    }

    @Nested
    @DisplayName("Step 方法测试")
    class StepTests {

        @Test
        @DisplayName("无参数调用时应递增1")
        void step_shouldIncrementByOne_whenNoParameterProvided() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(10);

            // Act
            tracker.step();

            // Assert
            assertThat(tracker.getCurrent()).isEqualTo(1);
        }

        @Test
        @DisplayName("提供计数参数时应按指定数量递增")
        void step_shouldIncrementBySpecifiedAmount_whenCountProvided() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);

            // Act
            tracker.step(25);

            // Assert
            assertThat(tracker.getCurrent()).isEqualTo(25);
        }

        @Test
        @DisplayName("步进超出限制时不应超过总数")
        void step_shouldNotExceedTotal_whenStepBeyondLimit() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(10);
            tracker.step(8);

            // Act
            tracker.step(5);

            // Assert
            assertThat(tracker.getCurrent()).isEqualTo(10);
        }

        @Test
        @DisplayName("多次调用时应正确累加")
        void step_shouldAccumulateCorrectly_whenCalledMultipleTimes() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);

            // Act
            tracker.step(10);
            tracker.step(20);
            tracker.step();

            // Assert
            assertThat(tracker.getCurrent()).isEqualTo(31);
        }

        @Test
        @DisplayName("计数为负数时应抛出异常")
        void step_shouldThrowException_whenCountIsNegative() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);

            // Act & Assert
            assertThatThrownBy(() -> tracker.step(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("步进数量不能为负数");
        }

        @Test
        @DisplayName("计数为零时应正常接受")
        void step_shouldAcceptZeroCount_whenValid() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(5);

            // Act
            tracker.step(0);

            // Assert
            assertThat(tracker.getCurrent()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("进度百分比测试")
    class ProgressPercentageTests {

        @Test
        @DisplayName("正常进度时应返回格式化的百分比")
        void getProgressPercentage_shouldReturnFormattedPercentage_whenNormalProgress() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100, 2);
            tracker.step(25);

            // Act
            String percentage = tracker.getProgressPercentage();

            // Assert
            assertThat(percentage).isEqualTo("25.00 %");
        }

        @Test
        @DisplayName("正常进度时应返回数值")
        void getProgressValue_shouldReturnNumericValue_whenNormalProgress() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100, 2);
            tracker.step(25);

            // Act
            double value = tracker.getProgressValue();

            // Assert
            assertThat(value).isEqualTo(25.0);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 3, 5})
        @DisplayName("小数位数为 [{0}] 时应遵循精度设置")
        void getProgressPercentage_shouldRespectDecimalPlaces_whenDifferentPrecision(int decimalPlaces) {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(3, decimalPlaces);
            tracker.step(1);

            // Act
            String percentage = tracker.getProgressPercentage();
            double value = tracker.getProgressValue();

            // Assert
            String expectedFormat = String.format("%%.%df %%%%", decimalPlaces);
            String expected = String.format(expectedFormat, 33.333333);
            assertThat(percentage).isEqualTo(expected);
            assertThat(value).isCloseTo(33.33, within(0.01));
        }

        @Test
        @DisplayName("当前等于总数时应处理完成进度")
        void getProgressPercentage_shouldHandleCompleteProgress_whenCurrentEqualsTotal() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(50);
            tracker.step(50);

            // Act
            String percentage = tracker.getProgressPercentage();
            double value = tracker.getProgressValue();

            // Assert
            assertThat(percentage).isEqualTo("100.00 %");
            assertThat(value).isEqualTo(100.0);
        }

        @Test
        @DisplayName("当前为零时应处理零进度")
        void getProgressPercentage_shouldHandleZeroProgress_whenCurrentIsZero() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);

            // Act
            String percentage = tracker.getProgressPercentage();
            double value = tracker.getProgressValue();

            // Assert
            assertThat(percentage).isEqualTo("0.00 %");
            assertThat(value).isEqualTo(0.0);
        }

        @Test
        @DisplayName("复杂分数时应正确处理精度")
        void getProgressPercentage_shouldHandlePrecisionCorrectly_whenComplexFraction() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(7, 3);
            tracker.step(2);

            // Act
            String percentage = tracker.getProgressPercentage();
            double value = tracker.getProgressValue();

            // Assert
            assertThat(percentage).isEqualTo("28.571 %");
            assertThat(value).isCloseTo(28.571, within(0.001));
        }
    }

    @Nested
    @DisplayName("里程碑检查测试")
    class MilestoneTests {

        @Test
        @DisplayName("首次调用百分比里程碑时应返回false")
        void isPercentMilestone_shouldReturnFalse_whenFirstCall() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(5);

            // Act
            boolean isMilestone = tracker.isPercentMilestone();

            // Assert
            assertThat(isMilestone).isFalse();
        }

        @Test
        @DisplayName("整数百分比变化时应返回true")
        void isPercentMilestone_shouldReturnTrue_whenIntegerPercentageChanges() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(5);
            tracker.isPercentMilestone(); // 首次调用，初始化状态

            // Act
            tracker.step(5); // 从5%到10%
            boolean isMilestone = tracker.isPercentMilestone();

            // Assert
            assertThat(isMilestone).isTrue();
        }

        @Test
        @DisplayName("整数百分比未变化时应返回false")
        void isPercentMilestone_shouldReturnFalse_whenIntegerPercentageNotChanged() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(1000);
            tracker.step(51); // 5.1%
            tracker.isPercentMilestone(); // 初始化

            // Act
            tracker.step(8); // 5.9%，整数部分仍然是5
            boolean isMilestone = tracker.isPercentMilestone();

            // Assert
            assertThat(isMilestone).isFalse();
        }

        @Test
        @DisplayName("首次调用千分比里程碑时应返回false")
        void isPermilleMilestone_shouldReturnFalse_whenFirstCall() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(1000);
            tracker.step(15); // 1.5%

            // Act
            boolean isMilestone = tracker.isPermilleMilestone();

            // Assert
            assertThat(isMilestone).isFalse();
        }

        @Test
        @DisplayName("千分比变化时应返回true")
        void isPermilleMilestone_shouldReturnTrue_whenPermilleChanges() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(1000);
            tracker.step(15); // 1.5%
            tracker.isPermilleMilestone(); // 初始化

            // Act
            tracker.step(5); // 2.0%，千分位从15变为20
            boolean isMilestone = tracker.isPermilleMilestone();

            // Assert
            assertThat(isMilestone).isTrue();
        }

        @Test
        @DisplayName("千分比未变化时应返回false")
        void isPermilleMilestone_shouldReturnFalse_whenPermilleNotChanged() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(10000);
            tracker.step(150); // 1.50%
            tracker.isPermilleMilestone(); // 初始化

            // Act
            tracker.step(2); // 1.52%，千分位仍然是15
            boolean isMilestone = tracker.isPermilleMilestone();

            // Assert
            assertThat(isMilestone).isFalse();
        }

        @Test
        @DisplayName("同时调用两个里程碑方法时应独立工作")
        void milestones_shouldWorkIndependently_whenBothCalled() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(5); // 5%
            tracker.isPercentMilestone(); // 初始化百分比
            tracker.isPermilleMilestone(); // 初始化千分比

            // Act
            tracker.step(5); // 10%
            boolean percentMilestone = tracker.isPercentMilestone();
            boolean permilleMilestone = tracker.isPermilleMilestone();

            // Assert
            assertThat(percentMilestone).isTrue();
            assertThat(permilleMilestone).isTrue();
        }
    }

    @Nested
    @DisplayName("辅助方法测试")
    class UtilityTests {

        @Test
        @DisplayName("当前小于总数时完成状态应返回false")
        void isCompleted_shouldReturnFalse_whenCurrentLessThanTotal() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(99);

            // Act
            boolean completed = tracker.isCompleted();

            // Assert
            assertThat(completed).isFalse();
        }

        @Test
        @DisplayName("当前等于总数时完成状态应返回true")
        void isCompleted_shouldReturnTrue_whenCurrentEqualsTotal() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(100);

            // Act
            boolean completed = tracker.isCompleted();

            // Assert
            assertThat(completed).isTrue();
        }

        @Test
        @DisplayName("当前超过总数时完成状态应返回true")
        void isCompleted_shouldReturnTrue_whenCurrentExceedsTotal() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(10);
            tracker.step(15); // 会被限制为10

            // Act
            boolean completed = tracker.isCompleted();

            // Assert
            assertThat(completed).isTrue();
        }

        @Test
        @DisplayName("调用重置时应将当前值重置为零")
        void reset_shouldResetCurrentToZero_whenCalled() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(100);
            tracker.step(50);
            tracker.isPercentMilestone(); // 初始化里程碑状态

            // Act
            tracker.reset();

            // Assert
            assertThat(tracker.getCurrent()).isZero();
            assertThat(tracker.isPercentMilestone()).isFalse(); // 重置后首次调用应该返回false
        }

        @Test
        @DisplayName("获取总用时应返回可读时间格式")
        void getTotalDuration_shouldReturnHumanReadableTime_whenCalled() {
            try (MockedStatic<Dates> mockedDates = mockStatic(Dates.class)) {
                // Arrange
                mockedDates.when(() -> Dates.humanReadableMillis(anyLong()))
                          .thenReturn("5s123ms");
                
                ProgressTracker tracker = new ProgressTracker(100);

                // Act
                String duration = tracker.getTotalDuration();

                // Assert
                assertThat(duration).isEqualTo("5s123ms");
                mockedDates.verify(() -> Dates.humanReadableMillis(anyLong()));
            }
        }

        @Test
        @DisplayName("转换为字符串时应返回格式化字符串")
        void toString_shouldReturnFormattedString_whenCalled() {
            try (MockedStatic<Dates> mockedDates = mockStatic(Dates.class)) {
                // Arrange
                mockedDates.when(() -> Dates.humanReadableMillis(anyLong()))
                          .thenReturn("2s500ms");
                
                ProgressTracker tracker = new ProgressTracker(100);
                tracker.step(25);

                // Act
                String result = tracker.toString();

                // Assert
                assertThat(result).matches("ProgressTracker\\[25/100, 25\\.00 %, 2s500ms\\]");
            }
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("大数值时应正确处理")
        void largeNumbers_shouldHandleCorrectly_whenMaxValues() {
            // Arrange
            long largeTotal = Long.MAX_VALUE / 2;
            ProgressTracker tracker = new ProgressTracker(largeTotal);

            // Act
            tracker.step(largeTotal / 4); // 步进 1/4，应该是 25%
            double percentage = tracker.getProgressValue();

            // Assert
            assertThat(percentage).isCloseTo(25.0, within(0.01));
        }

        @Test
        @DisplayName("需要舍入时应正确处理精度边界情况")
        void precisionEdgeCases_shouldHandleCorrectly_whenRoundingRequired() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(3, 2);
            tracker.step(1);

            // Act
            String percentage = tracker.getProgressPercentage();
            double value = tracker.getProgressValue();

            // Assert
            assertThat(percentage).isEqualTo("33.33 %");
            assertThat(value).isCloseTo(33.33, within(0.01));
        }

        @Test
        @DisplayName("最小总数为1时应正确处理")
        void totalOne_shouldHandleCorrectly_whenMinimumTotal() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(1);

            // Act & Assert
            assertThat(tracker.getProgressPercentage()).isEqualTo("0.00 %");
            assertThat(tracker.isCompleted()).isFalse();

            tracker.step();
            assertThat(tracker.getProgressPercentage()).isEqualTo("100.00 %");
            assertThat(tracker.isCompleted()).isTrue();
        }

        @Test
        @DisplayName("高精度小数位数时应正确处理")
        void highPrecision_shouldHandleCorrectly_whenManyDecimalPlaces() {
            // Arrange
            ProgressTracker tracker = new ProgressTracker(7, 5);
            tracker.step(3);

            // Act
            String percentage = tracker.getProgressPercentage();
            double value = tracker.getProgressValue();

            // Assert
            assertThat(percentage).isEqualTo("42.85714 %");
            assertThat(value).isCloseTo(42.85714, within(0.00001));
        }
    }
}