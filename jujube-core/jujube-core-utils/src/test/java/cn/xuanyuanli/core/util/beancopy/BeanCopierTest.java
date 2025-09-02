package cn.xuanyuanli.core.util.beancopy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BeanCopier 工具类测试")
class BeanCopierTest {

    @Nested
    @DisplayName("基本类型转换测试")
    class PrimitiveTypeConversionTests {

        @Test
        @DisplayName("getBoolean_应该返回正确布尔值_当使用不同输入时")
        void getBoolean_shouldReturnCorrectBooleanValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getBoolean("false")).isFalse();
            assertThat(BeanCopier.getBoolean("fa")).isFalse();
            assertThat(BeanCopier.getBoolean("")).isFalse();
            assertThat(BeanCopier.getBoolean(null)).isFalse();
            assertThat(BeanCopier.getBoolean("true")).isTrue();
        }

        @Test
        @DisplayName("getChar_应该返回正确字符值_当使用不同输入时")
        void getChar_shouldReturnCorrectCharValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getChar("a")).isEqualTo('a');
            assertThat(BeanCopier.getChar("")).isEqualTo(' ');
            assertThat(BeanCopier.getChar(null)).isEqualTo('\u0000');
        }

        @Test
        @DisplayName("getByte_应该返回正确字节值_当使用不同输入时")
        void getByte_shouldReturnCorrectByteValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getByte("")).isEqualTo((byte) 0);
            assertThat(BeanCopier.getByte(null)).isEqualTo((byte) 0);
            assertThat(BeanCopier.getByte("0")).isEqualTo((byte) 0);
            assertThat(BeanCopier.getByte(Byte.valueOf("0"))).isEqualTo((byte) 0);
            assertThat(BeanCopier.getByte(Byte.valueOf("12"))).isEqualTo((byte) 12);
        }

        @Test
        @DisplayName("getShort_应该返回正确短整型值_当使用不同输入时")
        void getShort_shouldReturnCorrectShortValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getShort("0")).isEqualTo((short) 0);
            assertThat(BeanCopier.getShort(null)).isEqualTo((short) 0);
            assertThat(BeanCopier.getShort(Short.valueOf("0"))).isEqualTo((short) 0);
            assertThat(BeanCopier.getShort(Short.valueOf("01"))).isEqualTo((short) 1);
        }

        @Test
        @DisplayName("getInt_应该返回正确整型值_当使用不同输入时")
        void getInt_shouldReturnCorrectIntValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getInt("")).isEqualTo(0);
            assertThat(BeanCopier.getInt(null)).isEqualTo(0);
            assertThat(BeanCopier.getInt(1)).isEqualTo(1);
            assertThat(BeanCopier.getInt(Integer.valueOf("0"))).isEqualTo(0);
            assertThat(BeanCopier.getInt(Integer.valueOf("2"))).isEqualTo(2);
        }

        @Test
        @DisplayName("getLong_应该返回正确长整型值_当使用不同输入时")
        void getLong_shouldReturnCorrectLongValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getLong("")).isEqualTo(0L);
            assertThat(BeanCopier.getLong(null)).isEqualTo(0L);
            assertThat(BeanCopier.getLong(1)).isEqualTo(1L);
            assertThat(BeanCopier.getLong(Long.valueOf("0"))).isEqualTo(0L);
            assertThat(BeanCopier.getLong(Long.valueOf("2"))).isEqualTo(2L);
        }

        @Test
        @DisplayName("getFloat_应该返回正确浮点值_当使用不同输入时")
        void getFloat_shouldReturnCorrectFloatValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getFloat("")).isEqualTo(0F);
            assertThat(BeanCopier.getFloat(null)).isEqualTo(0F);
            assertThat(BeanCopier.getFloat(1)).isEqualTo(1F);
            assertThat(BeanCopier.getFloat(Float.valueOf("0.2"))).isEqualTo(0.2F);
            assertThat(BeanCopier.getFloat(Float.valueOf("2"))).isEqualTo(2F);
        }

        @Test
        @DisplayName("getDouble_应该返回正确双精度值_当使用不同输入时")
        void getDouble_shouldReturnCorrectDoubleValue_whenUsingDifferentInputs() {
            // Act & Assert
            assertThat(BeanCopier.getDouble("")).isEqualTo(0D);
            assertThat(BeanCopier.getDouble(null)).isEqualTo(0D);
            assertThat(BeanCopier.getDouble(1)).isEqualTo(1D);
            assertThat(BeanCopier.getDouble(Double.valueOf("0.2"))).isEqualTo(0.2D);
            assertThat(BeanCopier.getDouble(Double.valueOf("2"))).isEqualTo(2D);
        }
    }
}
