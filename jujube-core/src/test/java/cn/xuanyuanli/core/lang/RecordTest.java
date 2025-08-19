package cn.xuanyuanli.core.lang;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import lombok.Data;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Dates;
import org.junit.jupiter.api.Test;

public class RecordTest {

    @Test
    public void constructor() {
        assertThat(new Record(3)).isEmpty();
    }

    @Test
    public void getStr() {
        Record record = new Record().set("key", "abc").set("k2", 1);
        assertThat(record.getStr("key")).isEqualTo("abc");
        assertThat(record.getStr("key","")).isEqualTo("abc");
        assertThat(record.getStr("k2")).isEqualTo("1");
        assertThat(record.getStr("key2")).isNull();
        assertThat(record.getStr("key2", "")).isEmpty();
        assertThat(record.getStr("key3", "abc")).isEqualTo("abc");
    }

    @Test
    public void getInt() {
        Record record = new Record().set("key", "123");
        assertThat(record.getInt("key")).isEqualTo(123);
        assertThat(record.getInt("key2", 1)).isEqualTo(1);
    }

    @Test
    public void getLong() {
        Record record = new Record().set("key", "123");
        assertThat(record.getLong("key")).isEqualTo(123L);
        assertThat(record.getLong("key2", 1L)).isEqualTo(1L);
    }

    @Test
    public void getBigInteger() {
        Record record = new Record().set("key", BigInteger.valueOf(123));
        assertThat(record.getBigInteger("key")).isEqualTo(123).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
    }

    @Test
    public void getDate() {
        Record record = new Record().set("key", Dates.parse("2017-06-28"));
        assertThat(record.getDate("key")).isEqualTo("2017-06-28").isAfter("2017-06-27").isBefore("2017-06-29");
        assertThat(record.getDate("key2")).isNull();
    }

    @Test
    public void getDate2() {
        Record record = new Record().set("key", new Date(1498572800000L));
        assertThat(record.getDate("key")).isEqualTo("2017-06-27T22:13:20.000");
        assertThat(record.getDate("key2")).isNull();
    }

    @Test
    public void getDouble() {
        Record record = new Record().set("key", "123.456");
        assertThat(record.getDouble("key")).isEqualTo(123.456);
    }

    @Test
    public void getFloat() {
        Record record = new Record().set("key", "123.456");
        assertThat(record.getFloat("key")).isEqualTo(123.456f);
    }

    @Test
    public void getBoolean() {
        Record record = new Record().set("key", "true").set("key1", "0").set("key2", "false").set("key3", "1").set("key4", "").set("key5", null);
        assertThat(record.getBoolean("key")).isEqualTo(true);
        assertThat(record.getBoolean("key1")).isEqualTo(false);
        assertThat(record.getBoolean("key2")).isEqualTo(false);
        assertThat(record.getBoolean("key3")).isEqualTo(true);
        assertThat(record.getBoolean("key4")).isEqualTo(false);
        assertThat(record.getBoolean("key5")).isEqualTo(false);
        assertThat(record.getBoolean("key6")).isEqualTo(false);
    }

    @Test
    public void getBigDecimal() {
        Record record = new Record().set("key", new BigDecimal("1.22"));
        assertThat(record.getBigDecimal("key")).isEqualTo(new BigDecimal("1.22"));
    }

    @Test
    public void getBytes() {
        Record record = new Record().set("key", "123".getBytes());
        assertThat(record.getBytes("key")).isEqualTo("123".getBytes());
    }

    @Test
    public void getNumber() {
        Record record = new Record().set("key", new BigDecimal("1.22"));
        assertThat(record.getNumber("key")).isEqualTo(new BigDecimal("1.22"));
    }

    @Test
    public void getId() {
        Record record = new Record().set("id", "123");
        assertThat(record.getId()).isEqualTo(123);
    }

    @Test
    public void getListRecord() {
        Record record = new Record().set("ids", "123");
        assertThat(record.getListRecord("ids")).isNull();

        record = new Record().set("ids", Sets.newHashSet(1, 2, 3));
        assertThat(record.getListRecord("ids")).hasSize(3).containsSequence(new Record(), new Record(), new Record());

        Record r1 = new Record().set("a", "a");
        Record r2 = new Record().set("b", "b");
        record = new Record().set("ids", Sets.newHashSet(r1, r2));
        assertThat(record.getListRecord("ids")).hasSize(2).containsSequence(r1, r2);

        record = new Record().set("ids", Lists.newArrayList(r1, r2));
        assertThat(record.getListRecord("ids")).hasSize(2).containsSequence(r1, r2);
    }

    @Test
    public void getListString() {
        Record record = new Record().set("ids", "123");
        assertThat(record.getListString("ids")).isNull();

        record = new Record().set("ids", Sets.newHashSet(1, 2, 3));
        assertThat(record.getListString("ids")).containsSequence("1", "2", "3");

        record = new Record().set("ids", Lists.newArrayList("1", "2"));
        assertThat(record.getListString("ids")).containsSequence("1", "2");
    }

    @Test
    public void valueOf() {
        Record record = Record.valueOf(new FromEntity().setAge(10).setUserName("jack"));
        assertThat(record.getInt("age")).isEqualTo(10);
        assertThat(record.getStr("user_name")).isEqualTo("jack");

        assertThat(Record.valueOf(null)).isEmpty();
    }

    @Test
    public void toEntity() {
        Record record = new Record().set("age", "1").set("user_name", "a");
        FromEntity entity = record.toEntity(FromEntity.class);
        assertThat(entity.getAge()).isEqualTo(1);
        assertThat(entity.getUserName()).isEqualTo("a");
    }

    @Test
    public void getRecord() {
        Record record = new Record();
        Record record2 = new Record();
        record.set("key", record2);
        assertThat(record.getRecord("key")).isEqualTo(record2);
    }

    @Test
    public void valueOfNullable() {
        assertThat(Record.valueOfNullable(null)).isNull();
        Record record = Record.valueOfNullable(new FromEntity().setAge(10).setUserName("jack"));
        assertThat(record.getInt("age")).isEqualTo(10);
        assertThat(record.getStr("user_name")).isEqualTo("jack");
    }

    @Test
    void testHashCode() {
        Record record = new Record();
        record.set("key", 1);
        assertThat(record.hashCode()).isEqualTo(106078);
    }

    @Data
    @Accessors(chain = true)
    public static class FromEntity implements BaseEntity {

        private String userName;
        private Integer age;
    }
}
