package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import lombok.Cleanup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

@DisplayName("Concurrents 并发工具测试")
class ConcurrentsTest {

    @Nested
    @DisplayName("执行超时测试")
    class ExecuteTimeoutTests {

        @Test
        @DisplayName("execOfTimeout_应该执行超时回调_当任务执行超时时")
        void execOfTimeout_shouldExecuteTimeoutCallback_whenTaskTimesOut() {
            // Arrange
            AtomicInteger result = new AtomicInteger();
            
            // Act
            Integer code = Concurrents.execOfTimeout(() -> {
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return 1;
            }, 100, t -> result.set(-1));
            
            if (code != null) {
                result.set(code);
            }
            
            // Assert
            assertThat(result.get()).isEqualTo(-1);
        }
    }

    @Nested
    @DisplayName("等待条件测试")
    class AwaitTests {

        @Test
        @DisplayName("await_应该等待直到条件满足_当使用默认重试次数时")
        void await_shouldWaitUntilConditionMet_whenUsingDefaultRetryCount() {
            // Arrange
            AtomicInteger i = new AtomicInteger();
            long begin = System.currentTimeMillis();
            int max = 5;
            int intervalTime = 100;
            
            // Act
            Concurrents.await(() -> i.getAndIncrement() > max, intervalTime);
            
            // Assert
            assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(max * intervalTime);
        }

        @Test
        @DisplayName("await_应该等待指定重试次数_当使用自定义重试次数时")
        void await_shouldWaitSpecifiedRetryCount_whenUsingCustomRetryCount() {
            // Arrange
            AtomicInteger i = new AtomicInteger();
            long begin = System.currentTimeMillis();
            int max = 5;
            int intervalTime = 100;
            
            // Act
            Concurrents.await(() -> i.getAndIncrement() > max, intervalTime, max - 1);
            
            // Assert
            assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo((max - 1) * intervalTime);
        }
    }

    @Nested
    @DisplayName("任务执行测试")
    class TaskExecutionTests {

        @Test
        @DisplayName("executeTask_应该同步执行同名任务_当多线程执行相同任务时")
        void executeTask_shouldSynchronizeTasksWithSameName_whenMultipleThreadsExecuteSameTask() throws InterruptedException {
            // Arrange
            long begin = System.currentTimeMillis();
            int millis = 500;
            Supplier<Object> supplier = () -> {
                Runtimes.sleep(millis);
                return 1;
            };
            
            // Act
            Thread thread1 = new Thread(() -> Concurrents.executeTask("1", supplier));
            thread1.start();
            Thread thread2 = new Thread(() -> Concurrents.executeTask("1", supplier));
            thread2.start();
            thread1.join();
            thread2.join();
            
            // Assert
            assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(millis).isLessThan(millis * 3);
        }
    }

    @Nested
    @DisplayName("线程池创建测试")
    class ThreadPoolCreationTests {


        @Test
        @Disabled("仅用于手动验证线程池执行性能")
        @DisplayName("createThreadPoolExecutor_应该创建线程池_当使用基本参数时")
        void createThreadPoolExecutor_shouldCreateThreadPool_whenUsingBasicParameters() {
            // Arrange & Act
            ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, 3, "test-");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            // Execute task
            threadPoolExecutor.execute(() -> Runtimes.sleep(2000));
            System.out.println(stopWatch.prettyPrint());
        }

        @Test
        @Disabled("仅用于手动验证线程池关闭")
        @DisplayName("createThreadPoolExecutor_应该优雅关闭线程池_当调用shutdown时")
        void createThreadPoolExecutor_shouldShutdownGracefully_whenShutdownCalled() {
            // Arrange & Act
            ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.DAYS, 3, "test-");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            // Submit task and shutdown
            threadPoolExecutor.submit(() -> Runtimes.sleep(2000));
            threadPoolExecutor.shutdown();
            
            while (true) {
                if (threadPoolExecutor.isTerminated()) {
                    stopWatch.stop();
                    System.out.println(stopWatch.lastTaskInfo().getTimeMillis() / 1000.0);
                    break;
                }
            }
        }

        @Test
        @Disabled("仅用于手动验证线程池强制关闭")
        @DisplayName("createThreadPoolExecutor_应该强制关闭线程池_当调用shutdownNow时")
        void createThreadPoolExecutor_shouldForceShutdown_whenShutdownNowCalled() {
            // Arrange & Act
            @Cleanup ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.DAYS, 3, "test-");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            // Submit tasks and force shutdown
            threadPoolExecutor.submit(() -> Runtimes.sleep(2000));
            threadPoolExecutor.submit(() -> Runtimes.sleep(20000));
            threadPoolExecutor.shutdownNow();
            
            while (true) {
                if (threadPoolExecutor.isTerminated()) {
                    stopWatch.stop();
                    System.out.println(stopWatch.lastTaskInfo().getTimeMillis() / 1000.0);
                    break;
                }
            }
        }

        @Test
        @Disabled("仅用于手动验证线程池等待终止")
        @DisplayName("createThreadPoolExecutor_应该等待任务完成或超时_当调用awaitTermination时")
        void createThreadPoolExecutor_shouldAwaitTasksCompletionOrTimeout_whenAwaitTerminationCalled() throws InterruptedException {
            // Arrange & Act
            ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.DAYS, 3, "test-");
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            
            // Submit tasks and wait for termination
            threadPoolExecutor.submit(() -> Runtimes.sleep(20000));
            threadPoolExecutor.submit(() -> Runtimes.sleep(1000));
            threadPoolExecutor.shutdown();
            
            // awaitTermination会阻塞，直到任务都结束或者发送超时
            //noinspection ResultOfMethodCallIgnored
            threadPoolExecutor.awaitTermination(3, TimeUnit.SECONDS);
            stopWatch.stop();
            System.out.println(stopWatch.lastTaskInfo().getTimeMillis() / 1000.0);
        }
    }
}
