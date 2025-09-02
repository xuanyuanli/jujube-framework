package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import cn.xuanyuanli.core.util.ListMutilThreadExecutor.ExecResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
@DisplayName("ListMutilThreadExecutor 多线程列表执行器测试")
class ListMutilThreadExecutorTest {

    @Nested
    @DisplayName("构造器和空列表处理测试")
    class ConstructorAndEmptyListTests {

        @Test
        @DisplayName("start_应该不执行任何操作_当列表为null时")
        void start_shouldNotExecuteAnyOperation_whenListIsNull() {
            // Arrange
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(null, 2);
            Consumer<String> execConsumer = mock(Consumer.class);
            Consumer<ExecResult> resultConsumer = mock(Consumer.class);
            
            // Act & Assert
            assertThatNoException().isThrownBy(() -> executor.start(execConsumer, resultConsumer));
            verify(execConsumer, times(0)).accept(any());
            verify(resultConsumer, times(0)).accept(any());
        }

        @Test
        @DisplayName("start_应该不执行任何操作_当列表为空时")
        void start_shouldNotExecuteAnyOperation_whenListIsEmpty() {
            // Arrange
            List<String> emptyList = new ArrayList<>();
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(emptyList, 2);
            Consumer<String> execConsumer = mock(Consumer.class);
            Consumer<ExecResult> resultConsumer = mock(Consumer.class);
            
            // Act & Assert
            assertThatNoException().isThrownBy(() -> executor.start(execConsumer, resultConsumer));
            verify(execConsumer, times(0)).accept(any());
            verify(resultConsumer, times(0)).accept(any());
        }
    }

    @Nested
    @DisplayName("正常执行测试")
    class NormalExecutionTests {

        @Test
        @DisplayName("start_应该处理所有元素_当正常执行时")
        void start_shouldProcessAllElements_whenExecutingNormally() {
            // Arrange
            List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
            ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(numbers, 2);
            AtomicInteger sum = new AtomicInteger(0);
            AtomicInteger resultCount = new AtomicInteger(0);
            Consumer<Integer> execConsumer = sum::addAndGet;
            Consumer<ExecResult> resultConsumer = result -> {
                resultCount.set(result.getCapacity());
                assertThat(result.getCapacity()).isEqualTo(5);
                assertThat(result.getUseTime()).isGreaterThanOrEqualTo(0.0);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            assertThat(sum.get()).isEqualTo(15);
            assertThat(resultCount.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("start_应该处理单个元素_当列表只有一个元素时")
        void start_shouldProcessSingleElement_whenListHasSingleElement() {
            // Arrange
            List<String> singleItem = Collections.singletonList("test");
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(singleItem, 1);
            AtomicInteger executedCount = new AtomicInteger(0);
            Consumer<String> execConsumer = item -> {
                assertThat(item).isEqualTo("test");
                executedCount.incrementAndGet();
            };
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(1);
                assertThat(result.getUseTime()).isGreaterThanOrEqualTo(0);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            assertThat(executedCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("start_应该正确处理不同数据类型_当使用泛型时")
        void start_shouldHandleDifferentDataTypes_whenUsingGenerics() {
            // Arrange
            List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);
            ListMutilThreadExecutor<Double> executor = new ListMutilThreadExecutor<>(doubles, 2);
            AtomicInteger count = new AtomicInteger(0);
            Consumer<Double> execConsumer = d -> {
                assertThat(d).isInstanceOf(Double.class);
                count.incrementAndGet();
            };
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(3);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            assertThat(count.get()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("start_应该继续处理其他元素_当某个元素处理抛出异常时")
        void start_shouldContinueProcessingOtherElements_whenOneElementThrowsException() {
            // Arrange
            List<String> items = Arrays.asList("item1", "item2", "error", "item4");
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(items, 2);
            AtomicInteger processedCount = new AtomicInteger(0);
            Consumer<String> execConsumer = item -> {
                if ("error".equals(item)) {
                    throw new RuntimeException("Test exception");
                }
                processedCount.incrementAndGet();
            };
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(4);
                assertThat(result.getUseTime()).isGreaterThan(0);
            };
            
            // Act & Assert
            assertThatNoException().isThrownBy(() -> executor.start(execConsumer, resultConsumer));
            assertThat(processedCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("start_应该正常执行_当消费者为空操作时")
        void start_shouldExecuteNormally_whenConsumerIsEmptyOperation() {
            // Arrange
            List<String> items = Arrays.asList("a", "b");
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(items, 1);
            AtomicInteger count = new AtomicInteger(0);
            Consumer<ExecResult> resultConsumer = result -> {
                count.set(result.getCapacity());
                assertThat(result.getCapacity()).isEqualTo(2);
            };
            
            // Act & Assert
            assertThatNoException().isThrownBy(() -> 
                executor.start(item -> {}, resultConsumer)
            );
            assertThat(count.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("多线程并发测试")
    class MultiThreadConcurrencyTests {

        @Test
        @DisplayName("start_应该并发执行任务_当使用多线程时")
        void start_shouldExecuteTasksConcurrently_whenUsingMultipleThreads() {
            // Arrange
            List<Integer> items = Collections.nCopies(6, 1);
            ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(items, 3);
            AtomicInteger concurrentCount = new AtomicInteger(0);
            AtomicInteger maxConcurrent = new AtomicInteger(0);
            Consumer<Integer> execConsumer = item -> {
                int current = concurrentCount.incrementAndGet();
                maxConcurrent.updateAndGet(max -> Math.max(max, current));
                Runtimes.sleep(50);
                concurrentCount.decrementAndGet();
            };
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(6);
                assertThat(result.getUseTime()).isGreaterThan(0);
            };
            
            // Act
            long startTime = System.currentTimeMillis();
            executor.start(execConsumer, resultConsumer);
            long endTime = System.currentTimeMillis();
            
            // Assert
            assertThat(maxConcurrent.get()).isGreaterThan(1).isLessThanOrEqualTo(3);
            assertThat(endTime - startTime).isLessThan(6 * 50);
        }

        @SuppressWarnings("ConstantValue")
        @Test
        @DisplayName("start_应该安全处理并发修改_当原列表被修改时")
        void start_shouldSafelyHandleConcurrentModification_whenOriginalListIsModified() {
            // Arrange
            List<String> items = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e"));
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(items, 3);
            AtomicInteger processedCount = new AtomicInteger(0);
            Consumer<String> execConsumer = item -> {
                processedCount.incrementAndGet();
                Runtimes.sleep(10);
            };
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(5);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            assertThat(processedCount.get()).isEqualTo(5);
            
            // 验证原列表修改不影响执行结果
            items.clear();
            assertThat(items.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("执行时间和性能测试")
    class ExecutionTimeAndPerformanceTests {

        @Test
        @DisplayName("start_应该正确计算执行时间_当任务有固定延时时")
        void start_shouldCalculateExecutionTimeCorrectly_whenTasksHaveFixedDelay() {
            // Arrange
            List<Integer> items = Arrays.asList(1, 2, 3);
            ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(items, 3);
            int sleepTime = 50;
            Consumer<Integer> execConsumer = item -> Runtimes.sleep(sleepTime);
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(3);
                assertThat(result.getUseTime()).isGreaterThanOrEqualTo(sleepTime / 1000.0);
            };
            
            // Act
            long startTime = System.currentTimeMillis();
            executor.start(execConsumer, resultConsumer);
            long actualTime = System.currentTimeMillis() - startTime;
            
            // Assert
            assertThat(actualTime).isGreaterThanOrEqualTo(sleepTime);
        }

        @Test
        @DisplayName("start_应该处理大数据集_当数据量较大时")
        void start_shouldProcessLargeDataSet_whenDataSetIsBig() {
            // Arrange
            List<Integer> largeList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largeList.add(i);
            }
            ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(largeList, 5);
            AtomicInteger sum = new AtomicInteger(0);
            Consumer<Integer> execConsumer = sum::addAndGet;
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(100);
                assertThat(result.getUseTime()).isGreaterThanOrEqualTo(0.0);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            int expectedSum = (99 * 100) / 2;
            assertThat(sum.get()).isEqualTo(expectedSum);
        }

        @Test
        @DisplayName("start_应该完成所有任务并关闭线程池_当正常执行时")
        void start_shouldCompleteAllTasksAndShutdownThreadPool_whenExecutingNormally() {
            // Arrange
            List<String> items = Arrays.asList("a", "b", "c");
            ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(items, 2);
            AtomicInteger count = new AtomicInteger(0);
            Consumer<String> execConsumer = item -> count.incrementAndGet();
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(3);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            assertThat(count.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("start_应该快速处理简单任务_当执行基本操作时")
        void start_shouldProcessSimpleTasksQuickly_whenExecutingBasicOperations() {
            // Arrange
            List<Integer> items = Arrays.asList(1, 2, 3);
            ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(items, 2);
            AtomicInteger sum = new AtomicInteger(0);
            Consumer<Integer> execConsumer = sum::addAndGet;
            Consumer<ExecResult> resultConsumer = result -> {
                assertThat(result.getCapacity()).isEqualTo(3);
                assertThat(result.getUseTime()).isGreaterThanOrEqualTo(0.0);
            };
            
            // Act
            executor.start(execConsumer, resultConsumer);
            
            // Assert
            assertThat(sum.get()).isEqualTo(6);
        }
    }

    @Nested
    @DisplayName("ExecResult结果对象测试")
    class ExecResultTests {

        @Test
        @DisplayName("getCapacity_应该返回正确容量_当创建ExecResult时")
        void getCapacity_shouldReturnCorrectCapacity_whenCreatingExecResult() {
            // Act
            ExecResult result = new ExecResult(10, 1.5);
            
            // Assert
            assertThat(result.getCapacity()).isEqualTo(10);
            assertThat(result.getUseTime()).isEqualTo(1.5);
        }

        @Test
        @DisplayName("equals_应该正确比较对象_当比较ExecResult实例时")
        void equals_shouldCompareObjectsCorrectly_whenComparingExecResultInstances() {
            // Arrange
            ExecResult result1 = new ExecResult(10, 1.5);
            ExecResult result2 = new ExecResult(10, 1.5);
            ExecResult result3 = new ExecResult(5, 2.0);
            
            // Act & Assert
            assertThat(result1).isEqualTo(result2);
            assertThat(result1).isNotEqualTo(result3);
            assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
        }
    }
}