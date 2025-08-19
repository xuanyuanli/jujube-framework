package benchmark;

import java.util.concurrent.TimeUnit;
import lombok.Data;
import cn.xuanyuanli.core.util.Beans;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 1)
@Measurement(iterations = 3, time = 5)
@Threads(10)
@Fork(3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class BeanBenchmark {

    @Benchmark
    public Person newBase() {
        return new Person();
    }

    @Benchmark
    public Person newReflect() {
        return Beans.getInstance(Person.class);
    }

    @Data
    public static class Person {

        private Long id;
        private String code;
        private String name;
    }
}
