package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import lombok.Cleanup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

public class ConcurrentsTest {

    @Test
    public void testExecOfTimeout() {
        AtomicInteger result = new AtomicInteger();
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
        assertThat(result.get()).isEqualTo(-1);
    }

    @Test
    public void await() {
        AtomicInteger i = new AtomicInteger();
        long begin = System.currentTimeMillis();
        int max = 5;
        int intervalTime = 100;
        Concurrents.await(() -> i.getAndIncrement() > max, intervalTime);
        assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(max * intervalTime);
    }

    @Test
    void executeTask() throws InterruptedException {
        long begin = System.currentTimeMillis();
        int millis = 500;
        Supplier<Object> supplier = () -> {
            Runtimes.sleep(millis);
            return 1;
        };
        Thread thread1 = new Thread(() -> Concurrents.executeTask("1", supplier));
        thread1.start();
        Thread thread2 = new Thread(() -> Concurrents.executeTask("1", supplier));
        thread2.start();
        thread1.join();
        thread2.join();
        assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo(millis).isLessThan(millis * 3);
    }

    @Test
    public void await2() {
        AtomicInteger i = new AtomicInteger();
        long begin = System.currentTimeMillis();
        int max = 5;
        int intervalTime = 100;
        Concurrents.await(() -> i.getAndIncrement() > max, intervalTime, max - 1);
        assertThat(System.currentTimeMillis() - begin).isGreaterThanOrEqualTo((max - 1) * intervalTime);
    }

    @Test
    @Disabled
    void createThreadPoolExecutor0() {
        ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, 3, "test-");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        threadPoolExecutor.execute(() -> Runtimes.sleep(2000));
        System.out.println(stopWatch.prettyPrint());
    }

    @Test
    @Disabled
    void createThreadPoolExecutor1() {
        ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.DAYS, 3, "test-");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
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
    @Disabled
    void createThreadPoolExecutor2() {
        @Cleanup ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.DAYS, 3, "test-");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
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
    @Disabled
    void createThreadPoolExecutor3() throws InterruptedException {
        ThreadPoolExecutor threadPoolExecutor = Concurrents.createThreadPoolExecutor(1, 1, 30, TimeUnit.DAYS, 3, "test-");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
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
