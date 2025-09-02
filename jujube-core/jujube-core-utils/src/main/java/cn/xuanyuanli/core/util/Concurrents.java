package cn.xuanyuanli.core.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 并发工具类
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Concurrents {

    /**
     * 日志记录器，用于记录并发操作过程中的日志信息
     */
    protected final static Logger logger = LoggerFactory.getLogger(Concurrents.class);

    /**
     * 任务执行结果缓存，用于防止多线程环境下相同任务的重复执行
     * <p>key为任务的唯一标识，value为任务执行的结果</p>
     */
    private final static ConcurrentMap<String, Object> FUTURE_CACHE = new ConcurrentHashMap<>();

    /**
     * 带超时设置的执行任务，如果任务在指定时间内未完成则取消执行
     *
     * @param execBody             执行主体，包含具体业务逻辑的供应商函数
     * @param timeout              超时时间，单位为毫秒，超过此时间将取消任务执行
     * @param timeoutExceptionCall 超时异常的回调函数，当任务超时时会调用此回调，传入null则使用默认日志记录
     * @param <T>                  返回值的泛型类型
     * @return {@link T} 任务执行的结果，如果超时或异常则返回null
     */
    public static <T> T execOfTimeout(Supplier<T> execBody, long timeout, Consumer<Exception> timeoutExceptionCall) {
        final AtomicReference<T> temp = new AtomicReference<>();
        @Cleanup ExecutorService executor = createThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, 50, "concurrents-execoftimeout-");
        T result = null;
        Future<Boolean> future = executor.submit(() -> {
            temp.set(execBody.get());
            return true;
        });
        try {
            future.get(timeout, TimeUnit.MILLISECONDS);
            result = temp.get();
        } catch (TimeoutException e) {// 超时异常
            future.cancel(true);
            if (timeoutExceptionCall != null) {
                timeoutExceptionCall.accept(e);
            } else {
                logger.error("execOfTimeout()-TimeoutException", e);
            }
        } catch (Exception e) {
            logger.error("execOfTimeout()-Exception", e);
            future.cancel(true);
        } finally {
            executor.shutdown();
        }
        return result;
    }

    /**
     * 创建一个通用的线程池
     *
     * @param corePoolSize     核心池大小，线程池中保持活跃的线程数量
     * @param maximumPoolSize  最大池大小，线程池允许的最大线程数量
     * @param keepAliveTime    维持时间，非核心线程的最大空闲时间
     * @param unit             时间单位，keepAliveTime参数的时间单位
     * @param workQueueNum     工作队列大小，用于存储待执行任务的队列容量
     * @param threadPrefixName 线程前缀名称，用于标识线程池中线程的名称前缀
     * @return {@link ThreadPoolExecutor} 创建的线程池执行器
     */
    public static ThreadPoolExecutor createThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueNum,
            String threadPrefixName) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<>(workQueueNum),
                new BasicThreadFactory.Builder().namingPattern(threadPrefixName + "%d").build());
    }

    /**
     * 等待某个任务执行完毕，通过轮询方式检查任务状态
     *
     * @param supplier     任务状态检查函数，返回true表示任务已完成，false表示任务还在执行中
     * @param intervalTime 轮询间隔时间，单位为毫秒，每次检查任务状态之间的等待时间
     */
    public static void await(Supplier<Boolean> supplier, int intervalTime) {
        while (!supplier.get()) {
            Runtimes.sleep(intervalTime);
        }
    }

    /**
     * 等待某个任务执行完毕，通过轮询方式检查任务状态，支持最大重试次数限制
     *
     * @param supplier     任务状态检查函数，返回true表示任务已完成，false表示任务还在执行中
     * @param intervalTime 轮询间隔时间，单位为毫秒，每次检查任务状态之间的等待时间
     * @param maxRetryTime 最大重试次数，超过此次数将停止等待，避免无限循环
     */
    public static void await(Supplier<Boolean> supplier, int intervalTime, int maxRetryTime) {
        int num = 0;
        while (!supplier.get() && num++ < maxRetryTime) {
            Runtimes.sleep(intervalTime);
        }
    }

    /**
     * 多线程环境下执行相同任务时的防重复执行机制，确保同一个key对应的任务只执行一次
     * 
     * <p>此方法使用ConcurrentHashMap的computeIfAbsent特性，在多线程环境下确保相同key的任务
     * 只会被执行一次，其他线程会等待并获取已执行任务的结果。适用于缓存初始化、资源加载等场景。</p>
     *
     * @param key      任务的唯一标识键，用于区分不同的任务
     * @param supplier 任务执行的供应商函数，包含具体的业务逻辑
     * @param <T>      返回值的泛型类型
     * @return {@link T} 任务执行的结果
     */
    @SuppressWarnings("UnusedReturnValue")
    public static <T> T executeTask(String key, Supplier<?> supplier) {
        Object o = FUTURE_CACHE.computeIfAbsent(key, k -> supplier.get());
        FUTURE_CACHE.remove(key);
        return (T) o;
    }
}
