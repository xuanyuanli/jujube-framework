package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Algorithms 算法工具测试")
class AlgorithmsTest {

    @Nested
    @DisplayName("贪心算法测试")
    class GreedyTests {

        @Test
        @DisplayName("greedy_应该返回最优组合_当给定数值列表和目标值时")
        void greedy_shouldReturnOptimalCombination_whenGivenNumberListAndTarget() {
            // Arrange
            List<Double> numbers = Arrays.asList(1d, 3d, 4d, 3d, 4d, 30d, 41d);
            double target = 75d;
            List<Double> expected = new ArrayList<>(Arrays.asList(41.0d, 30.0d, 4.0d));

            // Act
            List<Double> result = Algorithms.greedy(numbers, target);

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }
}

