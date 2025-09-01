package cn.xuanyuanli.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 集合多线程执行器，提供对集合元素的并发处理能力
 * <p>
 * 该执行器允许将一个集合的元素分配到多个线程中并发处理，提高处理效率。
 * 特别适用于需要对大量数据进行相同操作的场景，如批量数据处理、并发网络请求等。
 * </p>
 * 
 * <p>主要特性：</p>
 * <ul>
 *     <li>支持自定义线程池大小</li>
 *     <li>提供执行结果统计（处理元素数量、执行时间）</li>
 *     <li>支持异常处理，不会因单个元素处理失败而影响整体执行</li>
 *     <li>自动管理线程池生命周期</li>
 * </ul>
 * 
 * <p>示例用法：</p>
 * <pre>{@code
 * List<String> urls = Arrays.asList("url1", "url2", "url3");
 * ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(urls, 5);
 * 
 * executor.start(
 *     // 处理每个元素的逻辑
 *     url -> {
 *         // 执行HTTP请求或其他耗时操作
 *         processUrl(url);
 *     },
 *     // 处理执行结果
 *     result -> {
 *         System.out.println("处理了 " + result.getCapacity() + " 个元素");
 *         System.out.println("耗时 " + result.getUseTime() + " 秒");
 *     }
 * );
 * }</pre>
 *
 * @param <T> 集合元素的类型
 * @author xuanyuanli
 */
@Slf4j
public class ListMutilThreadExecutor<T> {

    /**
     * 线程池执行器
     */
    private ThreadPoolExecutor threadPoolExecutor;
    
    /**
     * 待处理的数据集合
     */
    private List<T> data;

    /**
     * 构造函数，创建集合多线程执行器
     * <p>
     * 根据指定的线程数量创建线程池，线程池大小等于传入的threadSize。
     * 线程池的队列大小等于集合大小，确保所有任务都能被接受。
     * </p>
     *
     * @param list 待处理的数据集合，如果为null或空集合，则不创建线程池
     * @param threadSize 线程池大小，建议根据CPU核心数和任务特性来设置
     */
    public ListMutilThreadExecutor(List<T> list, int threadSize) {
        if (list == null || list.isEmpty()) {
            return;
        }
        this.data = list;
        threadPoolExecutor = Concurrents.createThreadPoolExecutor(threadSize, threadSize, 30, TimeUnit.SECONDS, list.size(),
                "ListMutilThreadExecutor-" + threadSize + "-");
    }

    /**
     * 启动多线程执行
     * <p>
     * 该方法会将集合中的每个元素提交到线程池中并发处理，使用CountDownLatch确保等待所有任务完成。
     * 执行过程中如果某个元素处理失败，会记录错误日志但不影响其他元素的处理。
     * 所有任务完成后，会自动关闭线程池并调用结果回调函数。
     * </p>
     *
     * @param execConsumer 元素处理函数，对集合中的每个元素执行的操作
     * @param resultConsumer 结果处理函数，用于处理执行统计结果
     */
    @SneakyThrows
    public void start(Consumer<T> execConsumer, Consumer<ExecResult> resultConsumer) {
        if (data == null || data.isEmpty()) {
            return;
        }
        long begin = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(data.size());
        data.forEach(d -> threadPoolExecutor.execute(() -> {
            try {
                execConsumer.accept(d);
            } catch (Exception e) {
                log.error("execConsumer", e);
            } finally {
                latch.countDown();
            }
        }));
        latch.await();
        threadPoolExecutor.shutdownNow();
        resultConsumer.accept(new ExecResult(data.size(), (System.currentTimeMillis() - begin) / 1000.0));
    }

    /**
     * 执行结果统计类
     * <p>
     * 封装多线程执行完成后的统计信息，包括处理的元素数量和总耗时。
     * 可用于性能监控、日志记录或业务统计等场景。
     * </p>
     */
    @Data
    @AllArgsConstructor
    public static class ExecResult {

        /**
         * 处理的元素数量
         * <p>
         * 表示本次多线程执行处理的集合元素总数
         * </p>
         */
        private int capacity;
        
        /**
         * 执行总耗时（秒）
         * <p>
         * 从开始执行到所有任务完成的总时间，以秒为单位（支持小数）
         * </p>
         */
        private double useTime;
    }
}
