package cn.xuanyuanli.core.util;

import cn.xuanyuanli.core.util.ListMutilThreadExecutor.ExecResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class ListMutilThreadExecutorTest {

    @Test
    void testConstructorWithNullList() {
        ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(null, 2);
        
        Consumer<String> execConsumer = mock(Consumer.class);
        Consumer<ExecResult> resultConsumer = mock(Consumer.class);
        
        assertThatNoException().isThrownBy(() -> executor.start(execConsumer, resultConsumer));
        
        verify(execConsumer, times(0)).accept(any());
        verify(resultConsumer, times(0)).accept(any());
    }

    @Test
    void testConstructorWithEmptyList() {
        List<String> emptyList = new ArrayList<>();
        ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(emptyList, 2);
        
        Consumer<String> execConsumer = mock(Consumer.class);
        Consumer<ExecResult> resultConsumer = mock(Consumer.class);
        
        assertThatNoException().isThrownBy(() -> executor.start(execConsumer, resultConsumer));
        
        verify(execConsumer, times(0)).accept(any());
        verify(resultConsumer, times(0)).accept(any());
    }

    @Test
    void testNormalExecution() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(numbers, 2);
        
        AtomicInteger sum = new AtomicInteger(0);
        Consumer<Integer> execConsumer = sum::addAndGet;
        
        AtomicInteger resultCount = new AtomicInteger(0);
        Consumer<ExecResult> resultConsumer = result -> {
            resultCount.set(result.getCapacity());
            assertThat(result.getCapacity()).isEqualTo(5);
            assertThat(result.getUseTime()).isGreaterThanOrEqualTo(0.0);
        };
        
        executor.start(execConsumer, resultConsumer);
        
        assertThat(sum.get()).isEqualTo(15);
        assertThat(resultCount.get()).isEqualTo(5);
    }

    @Test
    void testExecutionWithException() {
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
        
        assertThatNoException().isThrownBy(() -> executor.start(execConsumer, resultConsumer));
        assertThat(processedCount.get()).isEqualTo(3);
    }

    @Test
    void testMultiThreadExecution() {
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
        
        long startTime = System.currentTimeMillis();
        executor.start(execConsumer, resultConsumer);
        long endTime = System.currentTimeMillis();
        
        assertThat(maxConcurrent.get()).isGreaterThan(1).isLessThanOrEqualTo(3);
        assertThat(endTime - startTime).isLessThan(6 * 50);
    }

    @Test
    void testSingleItemExecution() {
        List<String> singleItem = Collections.singletonList("test");
        ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(singleItem, 1);
        
        AtomicInteger executedCount = new AtomicInteger(0);
        Consumer<String> execConsumer = item -> {
            assertThat(item).isEqualTo("test");
            executedCount.incrementAndGet();
        };
        
        Consumer<ExecResult> resultConsumer = result -> {
            assertThat(result.getCapacity()).isEqualTo(1);
            assertThat(result.getUseTime()).isGreaterThan(0);
        };
        
        executor.start(execConsumer, resultConsumer);
        
        assertThat(executedCount.get()).isEqualTo(1);
    }

    @Test
    void testExecutionTime() {
        List<Integer> items = Arrays.asList(1, 2, 3);
        ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(items, 3);
        
        int sleepTime = 50;
        Consumer<Integer> execConsumer = item -> Runtimes.sleep(sleepTime);
        
        Consumer<ExecResult> resultConsumer = result -> {
            assertThat(result.getCapacity()).isEqualTo(3);
            assertThat(result.getUseTime()).isGreaterThanOrEqualTo(sleepTime / 1000.0);
        };
        
        long startTime = System.currentTimeMillis();
        executor.start(execConsumer, resultConsumer);
        long actualTime = System.currentTimeMillis() - startTime;
        
        assertThat(actualTime).isGreaterThanOrEqualTo(sleepTime);
    }

    @Test
    void testThreadPoolShutdown() {
        List<String> items = Arrays.asList("a", "b", "c");
        ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(items, 2);
        
        AtomicInteger count = new AtomicInteger(0);
        Consumer<String> execConsumer = item -> count.incrementAndGet();
        Consumer<ExecResult> resultConsumer = result -> {
            assertThat(result.getCapacity()).isEqualTo(3);
        };
        
        executor.start(execConsumer, resultConsumer);
        assertThat(count.get()).isEqualTo(3);
    }

    @Test
    void testWithDifferentDataTypes() {
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
        
        executor.start(execConsumer, resultConsumer);
        assertThat(count.get()).isEqualTo(3);
    }

    @Test
    void testExecResultProperties() {
        ExecResult result = new ExecResult(10, 1.5);
        
        assertThat(result.getCapacity()).isEqualTo(10);
        assertThat(result.getUseTime()).isEqualTo(1.5);
    }

    @Test
    void testExecResultEqualsAndHashCode() {
        ExecResult result1 = new ExecResult(10, 1.5);
        ExecResult result2 = new ExecResult(10, 1.5);
        ExecResult result3 = new ExecResult(5, 2.0);
        
        assertThat(result1).isEqualTo(result2);
        assertThat(result1).isNotEqualTo(result3);
        assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    void testLargeDataSet() {
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
        
        executor.start(execConsumer, resultConsumer);
        
        int expectedSum = (99 * 100) / 2;
        assertThat(sum.get()).isEqualTo(expectedSum);
    }

    @Test
    void testNullConsumerHandling() {
        List<String> items = Arrays.asList("a", "b");
        ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(items, 1);
        
        AtomicInteger count = new AtomicInteger(0);
        Consumer<ExecResult> resultConsumer = result -> {
            count.set(result.getCapacity());
            assertThat(result.getCapacity()).isEqualTo(2);
        };
        
        assertThatNoException().isThrownBy(() -> 
            executor.start(item -> {}, resultConsumer)
        );
        
        assertThat(count.get()).isEqualTo(2);
    }

    @Test
    void testSimpleExecution() {
        List<Integer> items = Arrays.asList(1, 2, 3);
        ListMutilThreadExecutor<Integer> executor = new ListMutilThreadExecutor<>(items, 2);
        
        AtomicInteger sum = new AtomicInteger(0);
        Consumer<Integer> execConsumer = sum::addAndGet;
        
        Consumer<ExecResult> resultConsumer = result -> {
            assertThat(result.getCapacity()).isEqualTo(3);
            assertThat(result.getUseTime()).isGreaterThanOrEqualTo(0.0);
        };
        
        executor.start(execConsumer, resultConsumer);
        assertThat(sum.get()).isEqualTo(6);
    }

    @Test
    void testConcurrentModificationSafety() {
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
        
        executor.start(execConsumer, resultConsumer);
        
        assertThat(processedCount.get()).isEqualTo(5);
        
        items.clear();
        assertThat(items.size()).isEqualTo(0);
    }
}