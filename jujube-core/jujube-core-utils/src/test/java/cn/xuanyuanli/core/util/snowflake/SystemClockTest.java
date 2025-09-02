package cn.xuanyuanli.core.util.snowflake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("SystemClock 系统时钟测试")
@ExtendWith(MockitoExtension.class)
class SystemClockTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("constructor_应该正确初始化时钟_当提供周期参数时")
        void constructor_shouldInitializeClockCorrectly_whenPeriodProvided() {
            // Arrange
            long period = 100;

            // Act
            SystemClock systemClock = new SystemClock(period);

            // Assert
            assertThat(systemClock.period).isEqualTo(period);
            assertThat(systemClock.now.get()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("单例模式测试")
    class SingletonTests {

        @Test
        @DisplayName("instance_应该返回相同实例_当多次调用时")
        void instance_shouldReturnSameInstance_whenCalledMultipleTimes() {
            // Act
            SystemClock instance1 = SystemClock.instance();
            SystemClock instance2 = SystemClock.instance();

            // Assert
            assertThat(instance1).isSameAs(instance2);
        }
    }

    @Nested
    @DisplayName("调度任务测试")
    class SchedulingTests {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Test
        @DisplayName("scheduleClockUpdating_应该正确调度时钟更新任务_当调用时")
        void scheduleClockUpdating_shouldScheduleClockUpdateCorrectly_whenCalled() {
            // Arrange
            long period = 10;
            SystemClock systemClock = new SystemClock(period);
            ScheduledExecutorService mockSchedPool = mock(ScheduledExecutorService.class);
            ScheduledFuture mockFuture = mock(ScheduledFuture.class);
            when(mockSchedPool.scheduleAtFixedRate(
                    any(Runnable.class), 
                    eq(period), 
                    eq(period), 
                    eq(TimeUnit.MILLISECONDS)
            )).thenReturn(mockFuture);
            SystemClock.SCHEDULEDPOOL = mockSchedPool;

            // Act
            systemClock.scheduleClockUpdating();

            // Assert
            verify(mockSchedPool, times(1)).scheduleAtFixedRate(
                    any(Runnable.class), 
                    eq(period), 
                    eq(period), 
                    eq(TimeUnit.MILLISECONDS)
            );
        }
    }

    @Nested
    @DisplayName("时间获取测试")
    class TimeRetrievalTests {

        @Test
        @DisplayName("currentTimeMillis_应该返回正数时间戳_当调用时")
        void currentTimeMillis_shouldReturnPositiveTimestamp_whenCalled() {
            // Arrange
            SystemClock systemClock = new SystemClock(1);

            // Act
            long currentTime = systemClock.currentTimeMillis();

            // Assert
            assertThat(currentTime).isGreaterThan(0);
        }

        @Test
        @DisplayName("now_应该返回正数时间戳_当调用静态方法时")
        void now_shouldReturnPositiveTimestamp_whenCallingStaticMethod() {
            // Act
            long now = SystemClock.now();

            // Assert
            assertThat(now).isGreaterThan(0);
        }
    }
}
