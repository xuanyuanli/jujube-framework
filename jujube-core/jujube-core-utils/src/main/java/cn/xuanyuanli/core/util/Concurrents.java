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
     * 日志记录器
     */
    protected final static Logger logger = LoggerFactory.getLogger(Concurrents.class);

    /**
     * 未来缓存
     */
    private final static ConcurrentMap<String, Object> FUTURE_CACHE = new ConcurrentHashMap<>();

    /**
     * 带超时设置的执行
     *
     * @param execBody             执行主体
     * @param timeout              超时时间,单位为毫秒
     * @param timeoutExceptionCall 超时异常的回调
     * @param <T>                  泛型
     * @return {@link T}
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
     * @param corePoolSize     核心池大小
     * @param maximumPoolSize  最大池大小
     * @param keepAliveTime    维持时间
     * @param unit             货币
     * @param workQueueNum     全国矿工工会工作队列
     * @param threadPrefixName 线程前缀名字
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor createThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int workQueueNum,
            String threadPrefixName) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, new ArrayBlockingQueue<>(workQueueNum),
                new BasicThreadFactory.Builder().namingPattern(threadPrefixName + "%d").build());
    }

    /**
     * 等待某个任务执行完毕
     *
     * @param supplier     任务是否完成，完成为true
     * @param intervalTime 任务结果获取的间隔时间
     */
    public static void await(Supplier<Boolean> supplier, int intervalTime) {
        while (!supplier.get()) {
            Runtimes.sleep(intervalTime);
        }
    }

    /**
     * 等待某个任务执行完毕
     *
     * @param supplier     任务是否完成，完成为true
     * @param intervalTime 任务结果获取的间隔时间
     * @param maxRetryTime 最大重试时间
     */
    public static void await(Supplier<Boolean> supplier, int intervalTime, int maxRetryTime) {
        int num = 0;
        while (!supplier.get() && num++ < maxRetryTime) {
            Runtimes.sleep(intervalTime);
        }
    }

    /**
     * 多线程下执行相同任务，用此方法防止同一任务重复执行
     *
     * @param key      键
     * @param supplier 供应商
     * @param <T>      泛型
     * @return {@link T}
     */
    @SuppressWarnings("UnusedReturnValue")
    public static <T> T executeTask(String key, Supplier<?> supplier) {
        Object o = FUTURE_CACHE.computeIfAbsent(key, k -> supplier.get());
        FUTURE_CACHE.remove(key);
        return (T) o;
    }
}
