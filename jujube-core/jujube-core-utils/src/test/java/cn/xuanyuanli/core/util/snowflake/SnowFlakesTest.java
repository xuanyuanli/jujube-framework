package cn.xuanyuanli.core.util.snowflake;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SnowFlakes 雪花算法工具类测试")
class SnowFlakesTest {

    @Nested
    @DisplayName("ID生成测试")
    class IdGenerationTests {

        @Test
        @DisplayName("nextId_应该返回大于0的ID_当调用生成ID时")
        void nextId_shouldReturnPositiveId_whenGeneratingId() {
            // Act & Assert
            assertThat(SnowFlakes.nextId()).isGreaterThan(0);
        }
    }
}
