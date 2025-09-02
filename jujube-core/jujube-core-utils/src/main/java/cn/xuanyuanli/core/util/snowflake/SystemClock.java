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
     * 时钟更新周期，单位毫秒
     * 默认为1毫秒，表示每毫秒更新一次缓存的时间戳
     */
    final long period;

    /**
     * 缓存的当前时间戳
     * 使用原子变量保证线程安全性，避免多线程并发访问时的数据不一致
     */
    final AtomicLong now;
    
    /**
     * 定时任务线程池
     * 用于定期更新缓存的时间戳，采用单线程池避免资源浪费
     */
    static ScheduledExecutorService SCHEDULEDPOOL = new ScheduledThreadPoolExecutor(1, new BasicThreadFactory.Builder().namingPattern("SystemClock-scheduled-pool-%d").build());

    /**
     * 构造系统时钟实例
     * 初始化时钟周期和当前时间戳，并启动定时更新任务
     *
     * @param period 时钟更新周期，单位为毫秒，决定了时间缓存的更新频率
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
     * 获取系统时钟的单例实例
     * 使用懒加载的单例模式，保证线程安全且延迟初始化
     *
     * @return SystemClock 系统时钟实例，使用1毫秒的更新周期
     */
    static SystemClock instance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 启动定时任务来更新缓存的时间戳
     * 使用固定频率的调度任务，每隔指定周期更新一次时间缓存
     * 这样可以避免频繁调用System.currentTimeMillis()带来的性能开销
     */
    void scheduleClockUpdating() {
        SCHEDULEDPOOL.scheduleAtFixedRate(() -> now.set(System.currentTimeMillis()), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取缓存的当前时间戳
     * 返回预先缓存的时间戳，避免每次都调用系统时间函数
     * 在高并发场景下可以显著提升性能
     *
     * @return long 当前时间的毫秒时间戳
     */
    long currentTimeMillis() {
        return now.get();
    }

    /**
     * 获取当前时间戳的静态方法
     * 这是对外提供的主要接口，用于替代System.currentTimeMillis()
     * 在高并发场景下具有更好的性能表现
     *
     * @return long 当前时间的毫秒时间戳，精度取决于时钟更新周期
     */
    public static long now() {
        return instance().currentTimeMillis();
    }

}
