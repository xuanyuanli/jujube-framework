package cn.xuanyuanli.jdbc;

import java.util.concurrent.TimeUnit;
import cn.xuanyuanli.jdbc.base.util.Sqls;
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
public class SqlBenchmark {

    @Benchmark
    public void getCountSql() {
        Sqls.getCountSql("select * from a left join b on b.id = a.id where a.age = 4 and b.id > 6 order by a.id");
    }
}
