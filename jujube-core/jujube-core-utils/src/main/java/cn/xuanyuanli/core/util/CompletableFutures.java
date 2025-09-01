package cn.xuanyuanli.core.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * CompletableFuture工具类，提供异步任务组合和执行的便捷方法
 * <p>
 * 该工具类封装了CompletableFuture的常见使用场景：
 * <ul>
 *     <li>组合两个异步任务并合并结果</li>
 *     <li>并发执行多个任务并等待全部完成</li>
 *     <li>并发执行多个任务并等待任意一个完成</li>
 * </ul>
 * 
 * <p>示例用法：
 * <pre>{@code
 * // 组合两个异步任务
 * String result = CompletableFutures.combine(
 *     () -> "Hello",
 *     () -> "World",
 *     (s1, s2) -> s1 + " " + s2
 * );
 * 
 * // 等待所有任务完成
 * CompletableFutures.combineAll(
 *     () -> System.out.println("Task 1"),
 *     () -> System.out.println("Task 2"),
 *     () -> System.out.println("Task 3")
 * );
 * 
 * // 等待任意一个任务完成
 * CompletableFutures.combineAny(
 *     () -> slowTask(),
 *     () -> fastTask()
 * );
 * }</pre>
 *
 * @author xuanyuanli
 */
public class CompletableFutures {

    /**
     * 组合两个异步任务的执行结果。 该方法启动两个异步任务，并在两个任务都完成后，使用一个组合函数对它们的结果进行处理
     *
     * @param <U>   第一个任务的结果类型
     * @param <K>   第二个任务的结果类型
     * @param <T>   组合函数的返回类型
     * @param task1 第一个异步任务，提供类型为 U 的结果
     * @param task2 第二个异步任务，提供类型为 K 的结果
     * @param fn    组合函数，接受两个任务的结果并返回类型为 T 的结果
     * @return 组合函数的结果
     * @throws RuntimeException 如果任务的执行被中断或执行失败，则抛出运行时异常
     */
    public static <U, K, T> T combine(Supplier<U> task1, Supplier<K> task2, BiFunction<U, K, T> fn) {
        try {
            // 使用CompletableFuture启动两个异步任务，并在两个任务都完成后，使用fn函数对结果进行组合
            return CompletableFuture.supplyAsync(task1, new ThreadPerTaskExecutor())
                    .thenCombine(CompletableFuture.supplyAsync(task2, new ThreadPerTaskExecutor()), fn).get();
        } catch (InterruptedException | ExecutionException e) {
            // 如果任务执行过程中出现中断或执行异常，则将其包装为RuntimeException并抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 结合多个任务并异步执行它们，并在这些任务中的全部完成后返回
     *
     * @param task 可变参数，代表要执行的多个Runnable任务
     */
    public static void combineAll(Runnable... task) {
        try {
            // 检查任务数组是否为null，如果为null则直接返回
            if (task == null || task.length == 0) {
                return;
            }
            // 使用Stream API将每个任务映射到一个CompletableFuture，并将其转换为数组
            CompletableFuture<?>[] array = Arrays.stream(task).map(e -> CompletableFuture.runAsync(e, new ThreadPerTaskExecutor()))
                    .toArray(CompletableFuture[]::new);
            // 使用CompletableFuture.allOf等待所有任务完成，如果任何任务失败，整个调用将抛出异常
            CompletableFuture.allOf(array).get();
        } catch (InterruptedException | ExecutionException e) {
            // 如果任务执行过程中出现中断或执行异常，则将其包装为RuntimeException并抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 该方法允许传入多个任务，并在这些任务中的任意一个完成后返回
     *
     * @param task 可变参数，代表多个Runnable任务，这些任务将被并行执行
     */
    public static void combineAny(Runnable... task) {
        try {
            // 检查任务数组是否为null，如果为null则直接返回
            if (task == null || task.length == 0) {
                return;
            }
            // 使用Stream API将Runnable任务转换为CompletableFuture对象数组
            // 每个任务都将使用ThreadPerTaskExecutor执行
            CompletableFuture<?>[] array = Arrays.stream(task).map(e -> CompletableFuture.runAsync(e, new ThreadPerTaskExecutor()))
                    .toArray(CompletableFuture[]::new);
            // 使用CompletableFuture的anyOf方法执行转换后的任务数组
            // 这将返回一个新的CompletableFuture，当原任务数组中的任意一个完成时，它也会完成
            // 调用get方法等待这个新的CompletableFuture完成
            CompletableFuture.anyOf(array).get();
        } catch (InterruptedException | ExecutionException e) {
            // 如果任务执行过程中出现中断或执行异常，则将其包装为RuntimeException并抛出
            throw new RuntimeException(e);
        }
    }

    /**
     * 每任务一线程的执行器实现
     * <p>
     * 该执行器为每个任务创建一个新的线程来执行，适用于短期任务或需要完全并行执行的场景。
     * 注意：该实现不进行线程复用，对于大量任务建议使用线程池。
     */
    static final class ThreadPerTaskExecutor implements Executor {

        /**
         * 执行给定的任务
         * <p>
         * 为每个任务创建一个新的线程并启动执行
         *
         * @param r 要执行的任务，不能为null
         * @throws NullPointerException 如果参数r为null
         */
        @Override
        public void execute(Runnable r) {
            Objects.requireNonNull(r);
            new Thread(r).start();
        }
    }
}
