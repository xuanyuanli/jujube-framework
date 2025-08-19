package cn.xuanyuanli.core.util.snowflake;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

/**
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * <p>
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（参考：<a href="https://www.jianshu.com/p/3fbe607600a5">System.currentTimeMillis()性能分析</a>）
 * <p>
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道
 * <p>
 * 后台定时更新时钟，JVM退出时，线程自动回收
 * <p>
 * 10亿：43410,206,210.72815533980582%
 * <p>
 * 1亿：4699,29,162.0344827586207%
 * <p>
 * 1000万：480,12,40.0%
 * <p>
 * 100万：50,10,5.0%
 * <p>
 *
 * @author lry
 * @date 2021/09/01
 */
public class SystemClock {

    /**
     * 周期
     */
    final long period;

    /**
     * 当前
     */
    final AtomicLong now;
    /**
     * scheduledpool
     */
    static ScheduledExecutorService SCHEDULEDPOOL = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("SystemClock-scheduled-pool-%d").build());

    /**
     * 系统时钟
     *
     * @param period 期
     */
    SystemClock(long period) {
        this.period = period;
        this.now = new AtomicLong(System.currentTimeMillis());
        scheduleClockUpdating();
    }

    private static class InstanceHolder {
        public static final SystemClock INSTANCE = new SystemClock(1);
    }

    /**
     * 实例
     *
     * @return {@link SystemClock}
     */
    static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 安排时间更新
     */
    void scheduleClockUpdating() {
        SCHEDULEDPOOL.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 当前时间
     *
     * @return long
     */
    long currentTimeMillis() {
        return now.get();
    }

    /**
     * 当前
     *
     * @return long
     */
    public static long now() {
        return instance().currentTimeMillis();
    }

}
