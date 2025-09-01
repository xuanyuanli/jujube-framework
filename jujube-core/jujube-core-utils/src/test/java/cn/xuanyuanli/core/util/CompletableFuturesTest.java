package cn.xuanyuanli.core.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@DisplayName("CompletableFutures工具类测试")

public class CompletableFuturesTest {

    @Test
    @DisplayName("combine: 两个任务都成功执行，返回组合结果")
    public void combine_BothTasksComplete_ReturnsCombinedResult() {
        AtomicInteger  counter = new AtomicInteger(0);
        Supplier<Integer> task1 = () -> {
            counter.incrementAndGet();
            return 10;
        };
        Supplier<Integer> task2 = () -> {
            counter.incrementAndGet();
            return 20;
        };
        BiFunction<Integer, Integer, Integer> fn = Integer::sum;

        Integer result = CompletableFutures.combine(task1, task2, fn);

        Assertions.assertEquals(30, result);
        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("combine: 任务并发执行验证")
    @Timeout(value = 250, unit = TimeUnit.MILLISECONDS)
    public void combine_TasksRunConcurrently_Performance() {
        long start = System.currentTimeMillis();
        
        Supplier<Integer> task1 = () -> {
            try {
                Thread.sleep(100);
                return 1;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
        
        Supplier<Integer> task2 = () -> {
            try {
                Thread.sleep(100);
                return 2;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };
        
        Integer result = CompletableFutures.combine(task1, task2, Integer::sum);
        
        long duration = System.currentTimeMillis() - start;
        Assertions.assertEquals(3, result);
        Assertions.assertTrue(duration < 200, "任务应该并行执行，总时间应小于200ms，实际:" + duration + "ms");
    }

    @Test
    @DisplayName("combine: task1抛出异常")
    public void combine_Task1ThrowsException_ThrowsRuntimeException() {
        Supplier<Integer> task1 = () -> {
            throw new RuntimeException("Task 1 failed");
        };
        Supplier<Integer> task2 = () -> 20;
        BiFunction<Integer, Integer, Integer> fn = Integer::sum;

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            CompletableFutures.combine(task1, task2, fn)
        );

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("Task 1 failed"));
    }

    @Test
    @DisplayName("combine: task2抛出异常")
    public void combine_Task2ThrowsException_ThrowsRuntimeException() {
        Supplier<Integer> task1 = () -> 10;
        Supplier<Integer> task2 = () -> {
            throw new RuntimeException("Task 2 failed");
        };
        BiFunction<Integer, Integer, Integer> fn = Integer::sum;

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            CompletableFutures.combine(task1, task2, fn)
        );

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("Task 2 failed"));
    }

    @Test
    @DisplayName("combine: 两个任务都抛出异常")
    public void combine_BothTasksThrowException_ThrowsRuntimeException() {
        Supplier<Integer> task1 = () -> {
            throw new RuntimeException("Task 1 failed");
        };
        Supplier<Integer> task2 = () -> {
            throw new RuntimeException("Task 2 failed");
        };
        BiFunction<Integer, Integer, Integer> fn = Integer::sum;

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            CompletableFutures.combine(task1, task2, fn)
        );

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("Task 1 failed"));
    }

    @Test
    @DisplayName("combine: 组合函数抛出异常")
    public void combine_CombinerThrowsException_ThrowsRuntimeException() {
        Supplier<Integer> task1 = () -> 10;
        Supplier<Integer> task2 = () -> 20;
        BiFunction<Integer, Integer, Integer> fn = (a, b) -> {
            throw new RuntimeException("Combiner failed");
        };

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            CompletableFutures.combine(task1, task2, fn)
        );

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("Combiner failed"));
    }

    @Test
    @DisplayName("combineAll: 多个任务都执行完成")
    public void combineAll_BothTasksComplete_NoExceptionThrown() {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable task1 = counter::incrementAndGet;
        Runnable task2 = counter::incrementAndGet;

        CompletableFutures.combineAll(task1, task2);

        Assertions.assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("combineAll: 其中一个任务失败")
    public void combineAll_OneTaskFails_ThrowsException() {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable task1 = counter::incrementAndGet;
        Runnable task2 = () -> {
            throw new RuntimeException("Task failed");
        };

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            CompletableFutures.combineAll(task1, task2));

        Assertions.assertNotNull(exception.getCause());
        Assertions.assertTrue(exception.getCause().getMessage().contains("Task failed"));
    }

    @Test
    @DisplayName("combineAny: 单个任务执行")
    public void combineAny_SingleTask_CompletesSuccessfully() {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable task = counter::incrementAndGet;

        CompletableFutures.combineAny(task);

        Assertions.assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("combineAny: 多个任务执行，最快的完成")
    public void combineAny_MultipleTasks_FastestCompletes() {
        AtomicBoolean fastTaskCompleted = new AtomicBoolean(false);
        AtomicBoolean slowTaskCompleted = new AtomicBoolean(false);
        
        Runnable fastTask = () -> fastTaskCompleted.set(true);
        
        Runnable slowTask = () -> {
            try {
                Thread.sleep(200);
                slowTaskCompleted.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        };

        CompletableFutures.combineAny(fastTask, slowTask);

        Assertions.assertTrue(fastTaskCompleted.get(), "快速任务应该已完成");
        // 给慢任务一些时间检查状态
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void combineAny_EmptyTasks_NoExecution() {
        AtomicInteger counter = new AtomicInteger(0);

        CompletableFutures.combineAny();

        Assertions.assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("combineAny: 所有任务都失败")
    public void combineAny_AllTasksFail_ThrowsException() {
        Runnable task1 = () -> {
            throw new RuntimeException("Task 1 failed");
        };
        Runnable task2 = () -> {
            throw new RuntimeException("Task 2 failed");
        };

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            CompletableFutures.combineAny(task1, task2));

        Assertions.assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("combineAny: 部分任务失败，成功任务先完成")
    public void combineAny_PartialTasksFail_SuccessfulTaskCompletes() {
        AtomicBoolean successTaskCompleted = new AtomicBoolean(false);

        Runnable successTask = () -> successTaskCompleted.set(true);
        Runnable failTask = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Task failed");
        };

        Assertions.assertDoesNotThrow(() -> CompletableFutures.combineAny(successTask, failTask));
        Assertions.assertTrue(successTaskCompleted.get(), "成功任务应该已完成");
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("combine: null参数测试")
    public void combine_NullParameters_ThrowsException() {
        Supplier<Integer> validTask = () -> 1;
        BiFunction<Integer, Integer, Integer> validFn = Integer::sum;

        Assertions.assertThrows(NullPointerException.class, () ->
            CompletableFutures.combine(null, validTask, validFn));

        Assertions.assertThrows(NullPointerException.class, () ->
            CompletableFutures.combine(validTask, null, validFn));

        Assertions.assertThrows(NullPointerException.class, () ->
            CompletableFutures.combine(validTask, validTask, null));
    }

    @Test
    @DisplayName("combineAll: null数组参数")
    public void combineAll_NullArray_NoExecution() {
        Assertions.assertDoesNotThrow(() -> CompletableFutures.combineAll((Runnable[]) null));
    }

    @Test
    @DisplayName("combineAny: null数组参数")
    public void combineAny_NullArray_NoExecution() {
        Assertions.assertDoesNotThrow(() -> CompletableFutures.combineAny((Runnable[]) null));
    }

    // ==================== 线程中断处理测试 ====================

    @Test
    @DisplayName("combine: 任务中抛出InterruptedException")
    public void combine_TaskInterrupted_HandlesInterruption() {
        Supplier<Integer> interruptedTask = () -> {
            try {
                Thread.sleep(1000); // 模拟长时间操作
                return 1;
            } catch (InterruptedException e) {
                throw new RuntimeException("Task was interrupted", e);
            }
        };

        Supplier<Integer> normalTask = () -> 2;

        // 在新线程中执行并中断
        AtomicReference<RuntimeException> caughtException = new AtomicReference<>();
        Thread testThread = new Thread(() -> {
            try {
                // 启动任务后立即中断当前线程
                Thread.currentThread().interrupt();
                CompletableFutures.combine(interruptedTask, normalTask, Integer::sum);
            } catch (RuntimeException e) {
                caughtException.set(e);
            }
        });

        testThread.start();
        try {
            testThread.join(5000); // 最多等待5秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        RuntimeException exception = caughtException.get();
        Assertions.assertNotNull(exception, "应该捕获到RuntimeException");
        Assertions.assertInstanceOf(InterruptedException.class, exception.getCause(), "异常原因应该是InterruptedException");
    }

    // ==================== 压力测试 ====================

    @Test
    @DisplayName("combineAll: 大量任务压力测试")
    public void combineAll_ManyTasks_CompletesSuccessfully() {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable[] tasks = new Runnable[50];
        for (int i = 0; i < 50; i++) {
            tasks[i] = counter::incrementAndGet;
        }

        CompletableFutures.combineAll(tasks);

        Assertions.assertEquals(50, counter.get());
    }

    @Test
    @DisplayName("combineAny: 大量任务测试")
    public void combineAny_ManyTasks_CompletesSuccessfully() {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable[] tasks = new Runnable[10];
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            tasks[i] = () -> {
                try {
                    // 让第一个任务最快完成
                    Thread.sleep(taskId * 10);
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            };
        }

        CompletableFutures.combineAny(tasks);

        // 至少有一个任务完成了
        Assertions.assertTrue(counter.get() >= 1, "至少应有一个任务完成");
    }
}
