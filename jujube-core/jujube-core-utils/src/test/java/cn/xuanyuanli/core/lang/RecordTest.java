package cn.xuanyuanli.core.lang;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import lombok.Data;
import lombok.experimental.Accessors;
import cn.xuanyuanli.core.util.Dates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Record 记录对象测试")
class RecordTest {

    @Nested
    @DisplayName("构造器测试")
    class ConstructorTests {

        @Test
        @DisplayName("constructor_应该返回空Record_当使用整数参数构造时")
        void constructor_shouldReturnEmptyRecord_whenConstructedWithIntParameter() {
            // Act
            Record record = new Record(3);
            
            // Assert
            assertThat(record).isEmpty();
        }
    }

    @Nested
    @DisplayName("基础类型获取测试")
    class BasicTypeGetterTests {

        @Test
        @DisplayName("getStr_应该返回正确字符串值_当键存在时")
        void getStr_shouldReturnCorrectStringValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", "abc").set("k2", 1);
            
            // Act & Assert
            assertThat(record.getStr("key")).isEqualTo("abc");
            assertThat(record.getStr("key", "")).isEqualTo("abc");
            assertThat(record.getStr("k2")).isEqualTo("1");
            assertThat(record.getStr("key2")).isNull();
            assertThat(record.getStr("key2", "")).isEmpty();
            assertThat(record.getStr("key3", "abc")).isEqualTo("abc");
        }

        @Test
        @DisplayName("getInt_应该返回正确整数值_当键存在时")
        void getInt_shouldReturnCorrectIntValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", "123");
            
            // Act & Assert
            assertThat(record.getInt("key")).isEqualTo(123);
            assertThat(record.getInt("key2", 1)).isEqualTo(1);
        }

        @Test
        @DisplayName("getLong_应该返回正确长整数值_当键存在时")
        void getLong_shouldReturnCorrectLongValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", "123");
            
            // Act & Assert
            assertThat(record.getLong("key")).isEqualTo(123L);
            assertThat(record.getLong("key2", 1L)).isEqualTo(1L);
        }

        @Test
        @DisplayName("getDouble_应该返回正确双精度值_当键存在时")
        void getDouble_shouldReturnCorrectDoubleValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", "123.456");
            
            // Act & Assert
            assertThat(record.getDouble("key")).isEqualTo(123.456);
        }

        @Test
        @DisplayName("getFloat_应该返回正确单精度值_当键存在时")
        void getFloat_shouldReturnCorrectFloatValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", "123.456");
            
            // Act & Assert
            assertThat(record.getFloat("key")).isEqualTo(123.456f);
        }

        @Test
        @DisplayName("getBoolean_应该返回正确布尔值_当使用不同值时")
        void getBoolean_shouldReturnCorrectBooleanValue_whenUsingDifferentValues() {
            // Arrange
            Record record = new Record()
                .set("key", "true")
                .set("key1", "0")
                .set("key2", "false")
                .set("key3", "1")
                .set("key4", "")
                .set("key5", null);
            
            // Act & Assert
            assertThat(record.getBoolean("key")).isEqualTo(true);
            assertThat(record.getBoolean("key1")).isEqualTo(false);
            assertThat(record.getBoolean("key2")).isEqualTo(false);
            assertThat(record.getBoolean("key3")).isEqualTo(true);
            assertThat(record.getBoolean("key4")).isEqualTo(false);
            assertThat(record.getBoolean("key5")).isEqualTo(false);
            assertThat(record.getBoolean("key6")).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("数值类型获取测试")
    class NumberTypeGetterTests {

        @Test
        @DisplayName("getBigInteger_应该返回正确BigInteger值_当键存在时")
        void getBigInteger_shouldReturnCorrectBigIntegerValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", BigInteger.valueOf(123));
            
            // Act & Assert
            assertThat(record.getBigInteger("key")).isEqualTo(123).isGreaterThanOrEqualTo(BigInteger.valueOf(100));
        }

        @Test
        @DisplayName("getBigDecimal_应该返回正确BigDecimal值_当键存在时")
        void getBigDecimal_shouldReturnCorrectBigDecimalValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", new BigDecimal("1.22"));
            
            // Act & Assert
            assertThat(record.getBigDecimal("key")).isEqualTo(new BigDecimal("1.22"));
        }

        @Test
        @DisplayName("getNumber_应该返回正确Number值_当键存在时")
        void getNumber_shouldReturnCorrectNumberValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", new BigDecimal("1.22"));
            
            // Act & Assert
            assertThat(record.getNumber("key")).isEqualTo(new BigDecimal("1.22"));
        }
    }

    @Nested
    @DisplayName("日期类型获取测试")
    class DateTypeGetterTests {

        @Test
        @DisplayName("getDate_应该返回正确日期值_当使用LocalDateTime时")
        void getDate_shouldReturnCorrectDateValue_whenUsingLocalDateTime() {
            // Arrange
            Record record = new Record().set("key", Dates.parse("2017-06-28"));
            
            // Act & Assert
            assertThat(record.getDate("key")).isEqualTo("2017-06-28").isAfter("2017-06-27").isBefore("2017-06-29");
            assertThat(record.getDate("key2")).isNull();
        }

        @Test
        @DisplayName("getDate_应该返回正确日期值_当使用SqlDate时")
        void getDate_shouldReturnCorrectDateValue_whenUsingSqlDate() {
            // Arrange
            Record record = new Record().set("key", new Date(1498572800000L));
            
            // Act & Assert
            assertThat(record.getDate("key")).isEqualTo("2017-06-27T22:13:20.000");
            assertThat(record.getDate("key2")).isNull();
        }
    }

    @Nested
    @DisplayName("其他类型获取测试")
    class OtherTypeGetterTests {

        @Test
        @DisplayName("getBytes_应该返回正确字节数组_当键存在时")
        void getBytes_shouldReturnCorrectByteArray_whenKeyExists() {
            // Arrange
            Record record = new Record().set("key", "123".getBytes());
            
            // Act & Assert
            assertThat(record.getBytes("key")).isEqualTo("123".getBytes());
        }

        @Test
        @DisplayName("getId_应该返回正确ID值_当键存在时")
        void getId_shouldReturnCorrectIdValue_whenKeyExists() {
            // Arrange
            Record record = new Record().set("id", "123");
            
            // Act & Assert
            assertThat(record.getId()).isEqualTo(123);
        }

        @Test
        @DisplayName("getRecord_应该返回正确Record值_当键存在时")
        void getRecord_shouldReturnCorrectRecordValue_whenKeyExists() {
            // Arrange
            Record record = new Record();
            Record record2 = new Record();
            record.set("key", record2);
            
            // Act & Assert
            assertThat(record.getRecord("key")).isEqualTo(record2);
        }
    }

    @Nested
    @DisplayName("集合类型获取测试")
    class CollectionTypeGetterTests {

        @Test
        @DisplayName("getListRecord_应该返回正确Record列表_当使用不同数据类型时")
        void getListRecord_shouldReturnCorrectRecordList_whenUsingDifferentDataTypes() {
            // Arrange & Act & Assert
            Record record = new Record().set("ids", "123");
            assertThat(record.getListRecord("ids")).isNull();

            record = new Record().set("ids", new HashSet<>(Arrays.asList(1, 2, 3)));
            assertThat(record.getListRecord("ids")).hasSize(3).containsSequence(new Record(), new Record(), new Record());

            Record r1 = new Record().set("a", "a");
            Record r2 = new Record().set("b", "b");
            record = new Record().set("ids", new HashSet<>(Arrays.asList(r1, r2)));
            assertThat(record.getListRecord("ids")).hasSize(2).containsSequence(r1, r2);

            record = new Record().set("ids", new ArrayList<>(Arrays.asList(r1, r2)));
            assertThat(record.getListRecord("ids")).hasSize(2).containsSequence(r1, r2);
        }

        @Test
        @DisplayName("getListString_应该返回正确字符串列表_当使用不同数据类型时")
        void getListString_shouldReturnCorrectStringList_whenUsingDifferentDataTypes() {
            // Arrange & Act & Assert
            Record record = new Record().set("ids", "123");
            assertThat(record.getListString("ids")).isNull();

            record = new Record().set("ids", new HashSet<>(Arrays.asList(1, 2, 3)));
            assertThat(record.getListString("ids")).containsSequence("1", "2", "3");

            record = new Record().set("ids", new ArrayList<>(Arrays.asList("1", "2")));
            assertThat(record.getListString("ids")).containsSequence("1", "2");
        }
    }

    @Nested
    @DisplayName("对象转换测试")
    class ObjectConversionTests {

        @Test
        @DisplayName("valueOf_应该返回正确Record_当从实体对象转换时")
        void valueOf_shouldReturnCorrectRecord_whenConvertingFromEntity() {
            // Arrange
            FromEntity entity = new FromEntity().setAge(10).setUserName("jack");
            
            // Act
            Record record = Record.valueOf(entity);
            
            // Assert
            assertThat(record.getInt("age")).isEqualTo(10);
            assertThat(record.getStr("user_name")).isEqualTo("jack");
            assertThat(Record.valueOf(null)).isEmpty();
        }

        @Test
        @DisplayName("valueOfNullable_应该返回正确Record_当输入可能为null时")
        void valueOfNullable_shouldReturnCorrectRecord_whenInputMightBeNull() {
            // Act & Assert
            assertThat(Record.valueOfNullable(null)).isNull();
            
            Record record = Record.valueOfNullable(new FromEntity().setAge(10).setUserName("jack"));
            assertThat(record.getInt("age")).isEqualTo(10);
            assertThat(record.getStr("user_name")).isEqualTo("jack");
        }

        @Test
        @DisplayName("toEntity_应该返回正确实体对象_当从Record转换时")
        void toEntity_shouldReturnCorrectEntity_whenConvertingFromRecord() {
            // Arrange
            Record record = new Record().set("age", "1").set("user_name", "a");
            
            // Act
            FromEntity entity = record.toEntity(FromEntity.class);
            
            // Assert
            assertThat(entity.getAge()).isEqualTo(1);
            assertThat(entity.getUserName()).isEqualTo("a");
        }
    }

    @Nested
    @DisplayName("对象方法测试")
    class ObjectMethodTests {

        @Test
        @DisplayName("testHashCode_应该返回正确哈希值_当设置键值时")
        void testHashCode_shouldReturnCorrectHashValue_whenKeyValueSet() {
            // Arrange
            Record record = new Record();
            record.set("key", 1);
            
            // Act & Assert
            assertThat(record.hashCode()).isEqualTo(106078);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class FromEntity implements BaseEntity {

        private String userName;
        private Integer age;
    }
}
