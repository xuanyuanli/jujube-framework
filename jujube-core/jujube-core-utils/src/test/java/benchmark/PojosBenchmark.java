package benchmark;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Pojos;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.cglib.beans.BeanCopier;

/**
 * --add-opens java.base/java.lang=ALL-UNNAMED
 */
@State(Scope.Benchmark)
@BenchmarkMode(value = Mode.Throughput)
@Warmup(iterations = 1)
@Measurement(iterations = 2, time = 2)
@Threads(10)
@Fork(2)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class PojosBenchmark {

    @Data
    public static class Person {

        private Long id;
        private String code;
        private String name;
        private String country;
        private String city;
        private String email;
        private String mobile;
        private String position;
        private Integer userType;
        private Date birthday;
    }

    @Data
    public static class Person2 {

        private Long id;
        private String code;
        private String name;
        private String country;
        private String city;
        private String email;
        private String mobile;
        private String position;
        private Integer userType;
        private Date birthday;
    }

    private final Person sourcePerson = new Person();
    private final net.sf.cglib.beans.BeanCopier beanCopier2 = net.sf.cglib.beans.BeanCopier.create(Person.class, Person2.class, false);
    private final BeanCopier beanCopier = BeanCopier.create(Person.class, Person2.class, false);
    private final cn.xuanyuanli.core.util.beancopy.BeanCopier jujubeBeanCopier = Pojos.getBeanCopierFromCache(sourcePerson, Person2.class, null, true);

    @Setup
    public void init() {
        sourcePerson.setId(1000021L);
        sourcePerson.setCode("IM-Test-1001");
        sourcePerson.setName("小明");
        sourcePerson.setCountry("中国");
        sourcePerson.setCity("上海");
        sourcePerson.setEmail("xiaoming.wang@demo.com");
        sourcePerson.setMobile("18810088888");
        sourcePerson.setPosition("闲杂人员");
        sourcePerson.setUserType(2);
        sourcePerson.setBirthday(new GregorianCalendar(1990, Calendar.NOVEMBER, 24).getTime());
    }

    @Benchmark
    public Person2 pojosMappingTest() {
        return Pojos.mapping(sourcePerson, Person2.class);
    }

    @Benchmark
    public Person2 beanCopierTest() {
        Person2 target = new Person2();
        jujubeBeanCopier.copyBean(sourcePerson, target, false);
        return target;
    }

    @Benchmark
    public cn.xuanyuanli.core.util.beancopy.BeanCopier beanCopierFromCacheTest() {
        return Pojos.getBeanCopierFromCache(sourcePerson, Person2.class, null, true);
    }

    @Benchmark
    public Person2 springBeanUtilsCopyTest() {
        Person2 target = new Person2();
        org.springframework.beans.BeanUtils.copyProperties(sourcePerson, target);
        return target;
    }

    @Benchmark
    public Person2 cglibBeanCopierTest() {
        Person2 target = new Person2();
        beanCopier2.copy(sourcePerson, target, null);
        return target;
    }

    @Benchmark
    public Person2 springBeanCopierTest() {
        Person2 target = new Person2();
        beanCopier.copy(sourcePerson, target, null);
        return target;
    }

    @Benchmark
    public Person2 getSetTest() {
        Person2 target = new Person2();
        target.setId(sourcePerson.getId());
        target.setBirthday(sourcePerson.getBirthday());
        target.setCity(sourcePerson.getCity());
        target.setCode(sourcePerson.getCode());
        target.setCountry(sourcePerson.getCountry());
        target.setEmail(sourcePerson.getEmail());
        target.setMobile(sourcePerson.getMobile());
        target.setUserType(sourcePerson.getUserType());
        target.setPosition(sourcePerson.getPosition());
        target.setName(sourcePerson.getName());
        return target;
    }

    @Benchmark
    public Person2 getSetOneFieldTest() {
        Person2 p = new Person2();
        p.setId(sourcePerson.getId());
        return p;
    }

    @Benchmark
    public Person2 reflectMethodOneFieldTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Long id = (Long) Person.class.getDeclaredMethod("getId").invoke(sourcePerson);
        Person2 p = new Person2();
        Person2.class.getDeclaredMethod("setId", Long.class).invoke(p, id);
        return p;
    }

    @Benchmark
    public Person2 propertyOneFieldTest() {
        Long id = (Long) Beans.getProperty(sourcePerson, "id");
        Person2 p = new Person2();
        Beans.setProperty(p, "id", id);
        return p;
    }

    /*
      2025-1-22

        Benchmark                                  Mode  Cnt       Score        Error   Units
        PojosBenchmark.beanCopierFromCacheTest    thrpt    4  132656.312 ±  58747.620  ops/ms
        PojosBenchmark.beanCopierTest             thrpt    4  441148.612 ±  72508.520  ops/ms
        PojosBenchmark.cglibBeanCopierTest        thrpt    4  425981.922 ±  90454.644  ops/ms
        PojosBenchmark.getSetOneFieldTest         thrpt    4  490886.432 ±  48085.412  ops/ms
        PojosBenchmark.getSetTest                 thrpt    4  436109.135 ±  90438.222  ops/ms
        PojosBenchmark.pojosMappingTest           thrpt    4   82043.625 ±   4123.364  ops/ms
        PojosBenchmark.propertyOneFieldTest       thrpt    4    6478.111 ±   2064.210  ops/ms
        PojosBenchmark.reflectMethodOneFieldTest  thrpt    4   48872.354 ±  27767.701  ops/ms
        PojosBenchmark.springBeanCopierTest       thrpt    4  420513.687 ± 117425.907  ops/ms
        PojosBenchmark.springBeanUtilsCopyTest    thrpt    4     971.822 ±     75.722  ops/ms
     */
}
