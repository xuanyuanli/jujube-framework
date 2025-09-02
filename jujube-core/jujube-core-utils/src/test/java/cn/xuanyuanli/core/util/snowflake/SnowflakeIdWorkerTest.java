package cn.xuanyuanli.core.util.snowflake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@DisplayName("SnowflakeIdWorker 雪花算法ID生成器测试")
class SnowflakeIdWorkerTest {

    private SnowflakeIdWorker idWorker;

    @BeforeEach
    void setUp() {
        idWorker = new SnowflakeIdWorker(1, 1);
    }

    @Nested
    @DisplayName("ID生成测试")
    class IdGenerationTests {

        @Test
        @DisplayName("nextId_应该生成不同的ID_当连续调用时")
        void nextId_shouldGenerateDifferentIds_whenCalledConsecutively() {
            // Act
            long id1 = idWorker.nextId();
            long id2 = idWorker.nextId();

            // Assert
            assertThat(id1).isNotEqualTo(id2);
        }

        @Test
        @DisplayName("nextId_应该抛出运行时异常_当时钟回拨时")
        void nextId_shouldThrowRuntimeException_whenClockIsBack() {
            // Arrange
            idWorker.setClock(true);

            // Act & Assert
            try (MockedStatic<SystemClock> mockedClock = mockStatic(SystemClock.class)) {
                mockedClock.when(SystemClock::now).thenReturn(-2L);
                assertThatThrownBy(() -> idWorker.nextId()).isInstanceOf(RuntimeException.class);
            }
        }
    }

    @Nested
    @DisplayName("UID解析测试")
    class UidParsingTests {

        @Test
        @DisplayName("parseUid_应该返回包含UID字段的JSON字符串_当传入长整型ID时")
        void parseUid_shouldReturnJsonWithUidField_whenPassingLongId() {
            // Arrange
            long id = idWorker.nextId();

            // Act
            String parsedUid = idWorker.parseUid(id);

            // Assert
            assertThat(parsedUid).isNotNull();
            assertThat(parsedUid).contains("\"UID\":");
        }

        @Test
        @DisplayName("parseUid_应该返回包含UID字段的JSON字符串_当传入字符串ID时")
        void parseUid_shouldReturnJsonWithUidField_whenPassingStringId() {
            // Arrange
            long id = idWorker.nextId();

            // Act
            String parsedUid = idWorker.parseUid(String.valueOf(id));

            // Assert
            assertThat(parsedUid).isNotNull();
            assertThat(parsedUid).contains("\"UID\":");
        }

        @Test
        @DisplayName("parseUid_应该返回正确的JSON格式_当传入特定长整型ID时")
        void parseUid_shouldReturnCorrectJsonFormat_whenPassingSpecificLongId() {
            // Arrange
            SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);

            // Act & Assert
            assertThat(worker.parseUid(583659091574915072L))
                    .isEqualTo("{\"UID\":\"583659091574915072\",\"timestamp\":\"2022-05-23 14:12:51\",\"workerId\":\"0\",\"dataCenterId\":\"0\",\"sequence\":\"0\"}");
        }

        @Test
        @DisplayName("parseUid_应该返回正确的JSON格式_当传入特定字符串ID时")
        void parseUid_shouldReturnCorrectJsonFormat_whenPassingSpecificStringId() {
            // Arrange
            SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);

            // Act & Assert
            assertThat(worker.parseUid("583659091574915072"))
                    .isEqualTo("{\"UID\":\"100000011001100100101101100111110111010000000000000000000000\",\"timestamp\":\"2022-05-23 14:12:51\",\"workerId\":\"0\",\"dataCenterId\":\"0\",\"sequence\":\"0\"}");
        }
    }

    @Nested
    @DisplayName("时间处理测试")
    class TimeHandlingTests {

        @Test
        @DisplayName("tilNextMillis_应该返回下一毫秒时间戳_当传入当前时间戳时")
        void tilNextMillis_shouldReturnNextMillisTimestamp_whenPassingCurrentTimestamp() {
            // Arrange
            long lastTimestamp = System.currentTimeMillis();

            // Act
            long nextTimestamp = idWorker.tilNextMillis(lastTimestamp);

            // Assert
            assertThat(nextTimestamp).isGreaterThan(lastTimestamp);
        }

        @Test
        @DisplayName("timeGen_应该返回系统时钟时间_当启用时钟时")
        void timeGen_shouldReturnSystemClockTime_whenClockEnabled() {
            // Arrange
            idWorker.setClock(true);

            // Act & Assert
            try (MockedStatic<SystemClock> mockedClock = mockStatic(SystemClock.class)) {
                mockedClock.when(SystemClock::now).thenReturn(1000L);
                assertThat(idWorker.timeGen()).isEqualTo(1000L);
            }
        }
    }

    @Nested
    @DisplayName("参数验证测试")
    class ParameterValidationTests {

        @Test
        @DisplayName("constructor_应该抛出IllegalArgumentException_当workerId为负数时")
        void constructor_shouldThrowIllegalArgumentException_whenWorkerIdIsNegative() {
            // Act & Assert
            assertThatThrownBy(() -> new SnowflakeIdWorker(-1, 1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("constructor_应该抛出IllegalArgumentException_当datacenterId为负数时")
        void constructor_shouldThrowIllegalArgumentException_whenDatacenterIdIsNegative() {
            // Act & Assert
            assertThatThrownBy(() -> new SnowflakeIdWorker(1, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
