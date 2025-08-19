package benchmark;

import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.digest.DigestUtils;
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

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 3)
@Threads(3)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class DigestBenchmark {

    @Benchmark
    public static String testDateFormat() {
        return DigestUtils.md5Hex("45612345486212");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder().include(DigestBenchmark.class.getSimpleName()).build();
        new Runner(opt).run();
    }
}
