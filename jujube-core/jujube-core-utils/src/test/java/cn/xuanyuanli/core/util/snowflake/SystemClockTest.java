package cn.xuanyuanli.core.util.snowflake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SystemClockTest {

    @Test
    public void testConstructor() {
        long period = 100;
        SystemClock systemClock = new SystemClock(period);
        assertEquals(period, systemClock.period);
        assertTrue(systemClock.now.get() > 0);
    }

    @Test
    public void testInstance() {
        SystemClock instance1 = SystemClock.instance();
        SystemClock instance2 = SystemClock.instance();
        assertSame(instance1, instance2);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testScheduleClockUpdating() {
        long period = 10;
        SystemClock systemClock = new SystemClock(period);
        ScheduledExecutorService mockSchedPool = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture mockFuture = Mockito.mock(ScheduledFuture.class);
        Mockito.when(mockSchedPool.scheduleAtFixedRate(Mockito.any(Runnable.class), Mockito.eq(period), Mockito.eq(period), Mockito.eq(TimeUnit.MILLISECONDS))).thenReturn(mockFuture);
        SystemClock.SCHEDULEDPOOL = mockSchedPool;
        systemClock.scheduleClockUpdating();
        Mockito.verify(mockSchedPool, Mockito.times(1)).scheduleAtFixedRate(Mockito.any(Runnable.class), Mockito.eq(period), Mockito.eq(period), Mockito.eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void testCurrentTimeMillis() {
        SystemClock systemClock = new SystemClock(1);
        long currentTime = systemClock.currentTimeMillis();
        assertTrue(currentTime > 0);
    }

    @Test
    public void testNow() {
        long now = SystemClock.now();
        assertTrue(now > 0);
    }
}
