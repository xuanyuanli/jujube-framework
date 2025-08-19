package benchmark;

import java.util.concurrent.TimeUnit;
import cn.xuanyuanli.core.util.Dates;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author John Li
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 3)
@Threads(3)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DatesBenchmark {

    private static final Long time1 = 1585123364L;
    private static final Long time2 = 1585123364L;

    /**
     * 两个时间戳是否为同一天
     */
    static boolean isSameDay() {
        String pattern = "yyyyMMdd";
        String strDay1 = Dates.formatTimeMillis(DatesBenchmark.time1, pattern);
        String strDay2 = Dates.formatTimeMillis(DatesBenchmark.time2, pattern);
        return strDay1.equals(strDay2);
    }

    @Benchmark
    public static boolean testDateFormat() {
        return isSameDay();
    }

    @Benchmark
    public static boolean testLocalDate() {
        return Dates.isSameDay(time1, time2);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(DatesBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }

}
