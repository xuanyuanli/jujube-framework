package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@DisplayName("CompletableFutures 并行任务工具测试")
class CompletableFuturesTest {

    @Nested
    @DisplayName("combine方法测试")
    class CombineTests {

        @Test
        @DisplayName("combine_应该返回组合结果_当两个任务都成功执行时")
        void combine_shouldReturnCombinedResult_whenBothTasksComplete() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Supplier<Integer> task1 = () -> {
                counter.incrementAndGet();
                return 10;
            };
            Supplier<Integer> task2 = () -> {
                counter.incrementAndGet();
                return 20;
            };
            BiFunction<Integer, Integer, Integer> fn = Integer::sum;

            // Act
            Integer result = CompletableFutures.combine(task1, task2, fn);

            // Assert
            assertThat(result).isEqualTo(30);
            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("combine_应该并行执行任务_当验证执行性能时")
        @Timeout(value = 250, unit = TimeUnit.MILLISECONDS)
        void combine_shouldRunTasksConcurrently_whenVerifyingPerformance() {
            // Arrange
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
            
            // Act
            Integer result = CompletableFutures.combine(task1, task2, Integer::sum);
            
            // Assert
            long duration = System.currentTimeMillis() - start;
            assertThat(result).isEqualTo(3);
            assertThat(duration).isLessThan(200L);
        }

        @Test
        @DisplayName("combine_应该抛出运行时异常_当task1抛出异常时")
        void combine_shouldThrowRuntimeException_whenTask1ThrowsException() {
            // Arrange
            Supplier<Integer> task1 = () -> {
                throw new RuntimeException("Task 1 failed");
            };
            Supplier<Integer> task2 = () -> 20;
            BiFunction<Integer, Integer, Integer> fn = Integer::sum;

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combine(task1, task2, fn))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(ExecutionException.class);
        }

        @Test
        @DisplayName("combine_应该抛出运行时异常_当task2抛出异常时")
        void combine_shouldThrowRuntimeException_whenTask2ThrowsException() {
            // Arrange
            Supplier<Integer> task1 = () -> 10;
            Supplier<Integer> task2 = () -> {
                throw new RuntimeException("Task 2 failed");
            };
            BiFunction<Integer, Integer, Integer> fn = Integer::sum;

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combine(task1, task2, fn))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(ExecutionException.class);
        }

        @Test
        @DisplayName("combine_应该抛出运行时异常_当两个任务都抛出异常时")
        void combine_shouldThrowRuntimeException_whenBothTasksThrowException() {
            // Arrange
            Supplier<Integer> task1 = () -> {
                throw new RuntimeException("Task 1 failed");
            };
            Supplier<Integer> task2 = () -> {
                throw new RuntimeException("Task 2 failed");
            };
            BiFunction<Integer, Integer, Integer> fn = Integer::sum;

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combine(task1, task2, fn))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(ExecutionException.class);
        }

        @Test
        @DisplayName("combine_应该抛出运行时异常_当组合函数抛出异常时")
        void combine_shouldThrowRuntimeException_whenCombinerThrowsException() {
            // Arrange
            Supplier<Integer> task1 = () -> 10;
            Supplier<Integer> task2 = () -> 20;
            BiFunction<Integer, Integer, Integer> fn = (a, b) -> {
                throw new RuntimeException("Combiner failed");
            };

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combine(task1, task2, fn))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(ExecutionException.class);
        }

        @Test
        @DisplayName("combine_应该抛出空指针异常_当参数为null时")
        void combine_shouldThrowNullPointerException_whenParametersAreNull() {
            // Arrange
            Supplier<Integer> validTask = () -> 1;
            BiFunction<Integer, Integer, Integer> validFn = Integer::sum;

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combine(null, validTask, validFn))
                .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> CompletableFutures.combine(validTask, null, validFn))
                .isInstanceOf(NullPointerException.class);

            assertThatThrownBy(() -> CompletableFutures.combine(validTask, validTask, null))
                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("combine_应该处理线程中断_当任务被中断时")
        void combine_shouldHandleInterruption_whenTaskIsInterrupted() {
            // Arrange
            Supplier<Integer> interruptedTask = () -> {
                try {
                    Thread.sleep(1000); // 模拟长时间操作
                    return 1;
                } catch (InterruptedException e) {
                    throw new RuntimeException("Task was interrupted", e);
                }
            };
            Supplier<Integer> normalTask = () -> 2;
            AtomicReference<RuntimeException> caughtException = new AtomicReference<>();

            // Act
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

            // Assert
            RuntimeException exception = caughtException.get();
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isInstanceOf(InterruptedException.class);
        }
    }

    @Nested
    @DisplayName("combineAll方法测试")
    class CombineAllTests {

        @Test
        @DisplayName("combineAll_应该执行完成所有任务_当所有任务正常时")
        void combineAll_shouldCompleteAllTasks_whenAllTasksAreNormal() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task1 = counter::incrementAndGet;
            Runnable task2 = counter::incrementAndGet;

            // Act
            CompletableFutures.combineAll(task1, task2);

            // Assert
            assertThat(counter.get()).isEqualTo(2);
        }

        @Test
        @DisplayName("combineAll_应该抛出运行时异常_当其中一个任务失败时")
        void combineAll_shouldThrowRuntimeException_whenOneTaskFails() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task1 = counter::incrementAndGet;
            Runnable task2 = () -> {
                throw new RuntimeException("Task failed");
            };

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combineAll(task1, task2))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(ExecutionException.class);
        }

        @Test
        @DisplayName("combineAll_应该执行大量任务_当进行压力测试时")
        void combineAll_shouldExecuteManyTasks_whenPerformingStressTest() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Runnable[] tasks = new Runnable[50];
            for (int i = 0; i < 50; i++) {
                tasks[i] = counter::incrementAndGet;
            }

            // Act
            CompletableFutures.combineAll(tasks);

            // Assert
            assertThat(counter.get()).isEqualTo(50);
        }

        @Test
        @DisplayName("combineAll_应该正常执行_当参数为null数组时")
        void combineAll_shouldExecuteNormally_whenNullArray() {
            // Act & Assert
            assertThatNoException().isThrownBy(() -> CompletableFutures.combineAll((Runnable[]) null));
        }
    }

    @Nested
    @DisplayName("combineAny方法测试")
    class CombineAnyTests {

        @Test
        @DisplayName("combineAny_应该成功完成_当执行单个任务时")
        void combineAny_shouldCompleteSuccessfully_whenExecutingSingleTask() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);
            Runnable task = counter::incrementAndGet;

            // Act
            CompletableFutures.combineAny(task);

            // Assert
            assertThat(counter.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("combineAny_应该完成最快的任务_当执行多个任务时")
        void combineAny_shouldCompleteFastestTask_whenExecutingMultipleTasks() {
            // Arrange
            AtomicBoolean fastTaskCompleted = new AtomicBoolean(false);

            Runnable fastTask = () -> fastTaskCompleted.set(true);
            Runnable slowTask = () -> {
                try {
                    Thread.sleep(200);
                    // 慢任务完成（在快任务之后）
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            };

            // Act
            CompletableFutures.combineAny(fastTask, slowTask);

            // Assert
            assertThat(fastTaskCompleted.get()).isTrue();
            // 给慢任务一些时间检查状态
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Test
        @DisplayName("combineAny_应该不执行任何任务_当任务为空时")
        void combineAny_shouldNotExecuteAnyTask_whenTasksAreEmpty() {
            // Arrange
            AtomicInteger counter = new AtomicInteger(0);

            // Act
            CompletableFutures.combineAny();

            // Assert
            assertThat(counter.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("combineAny_应该抛出运行时异常_当所有任务都失败时")
        void combineAny_shouldThrowRuntimeException_whenAllTasksFail() {
            // Arrange
            Runnable task1 = () -> {
                throw new RuntimeException("Task 1 failed");
            };
            Runnable task2 = () -> {
                throw new RuntimeException("Task 2 failed");
            };

            // Act & Assert
            assertThatThrownBy(() -> CompletableFutures.combineAny(task1, task2))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("combineAny_应该完成成功的任务_当部分任务失败且成功任务先完成时")
        void combineAny_shouldCompleteSuccessfulTask_whenPartialTasksFailAndSuccessfulTaskCompletesFirst() {
            // Arrange
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

            // Act & Assert
            assertThatNoException().isThrownBy(() -> CompletableFutures.combineAny(successTask, failTask));
            assertThat(successTaskCompleted.get()).isTrue();
        }

        @Test
        @DisplayName("combineAny_应该成功完成_当进行大量任务测试时")
        void combineAny_shouldCompleteSuccessfully_whenPerformingManyTasksTest() {
            // Arrange
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

            // Act
            CompletableFutures.combineAny(tasks);

            // Assert
            assertThat(counter.get()).isGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("combineAny_应该正常执行_当参数为null数组时")
        void combineAny_shouldExecuteNormally_whenNullArray() {
            // Act & Assert
            assertThatNoException().isThrownBy(() -> CompletableFutures.combineAny((Runnable[]) null));
        }
    }
}
