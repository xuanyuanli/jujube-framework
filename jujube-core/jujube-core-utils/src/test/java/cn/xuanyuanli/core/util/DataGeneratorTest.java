package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Data;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("DataGenerator 数据生成器测试")
class DataGeneratorTest {

    @Nested
    @DisplayName("随机值生成测试")
    class RandomValueGenerationTests {

        @Test
        @DisplayName("generateRandomValueByParamType_应该生成合适的随机值_当输入不同类型时")
        void generateRandomValueByParamType_shouldGenerateAppropriateRandomValues_whenInputDifferentTypes() {
            // Act & Assert
            String str = DataGenerator.generateRandomValueByParamType(String.class);
            assertThat(str.length()).isBetween(1, 16);

            Short aShort = DataGenerator.generateRandomValueByParamType(Short.class);
            assertThat(aShort).isBetween((short) (Short.MIN_VALUE + 1), Short.MAX_VALUE);

            Boolean aBoolean = DataGenerator.generateRandomValueByParamType(Boolean.class);
            assertThat(aBoolean).isIn(true, false);

            Double aDouble = DataGenerator.generateRandomValueByParamType(Double.class);
            assertThat(aDouble).isBetween(-Double.MAX_VALUE, Double.MAX_VALUE);

            Float aFloat = DataGenerator.generateRandomValueByParamType(Float.class);
            assertThat(aFloat).isBetween(-Float.MAX_VALUE, Float.MAX_VALUE);

            Long aLong = DataGenerator.generateRandomValueByParamType(Long.class);
            assertThat(aLong).isBetween(Long.MIN_VALUE + 1, Long.MAX_VALUE);

            Integer integer = DataGenerator.generateRandomValueByParamType(Integer.class);
            assertThat(integer).isBetween(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);

            Type type = DataGenerator.generateRandomValueByParamType(Type.class);
            assertThat(type).isNull();
        }
    }

    @Nested
    @DisplayName("默认值生成测试")
    class DefaultValueGenerationTests {

        @Test
        @DisplayName("generateDefaultValueByParamType_应该生成正确默认值_当输入不同类型时")
        void generateDefaultValueByParamType_shouldGenerateCorrectDefaultValues_whenInputDifferentTypes() {
            // Act & Assert
            assertThat(DataGenerator.generateDefaultValueByParamType(String.class)).isEqualTo("");
            assertThat(DataGenerator.generateDefaultValueByParamType(Short.class)).isEqualTo((short) 0);
            assertThat(DataGenerator.generateDefaultValueByParamType(short.class)).isEqualTo((short) 0);
            assertThat(DataGenerator.generateDefaultValueByParamType(Boolean.class)).isEqualTo(false);
            assertThat(DataGenerator.generateDefaultValueByParamType(boolean.class)).isEqualTo(false);
            assertThat(DataGenerator.generateDefaultValueByParamType(Double.class)).isEqualTo(0.0d);
            assertThat(DataGenerator.generateDefaultValueByParamType(double.class)).isEqualTo(0.0d);
            assertThat(DataGenerator.generateDefaultValueByParamType(Float.class)).isEqualTo(0.0f);
            assertThat(DataGenerator.generateDefaultValueByParamType(float.class)).isEqualTo(0.0f);
            assertThat(DataGenerator.generateDefaultValueByParamType(Long.class)).isEqualTo(0L);
            assertThat(DataGenerator.generateDefaultValueByParamType(long.class)).isEqualTo(0L);
            assertThat(DataGenerator.generateDefaultValueByParamType(Integer.class)).isEqualTo(0);
            assertThat(DataGenerator.generateDefaultValueByParamType(int.class)).isEqualTo(0);
            assertThat(DataGenerator.generateDefaultValueByParamType(Byte.class)).isEqualTo(Byte.valueOf("0"));
            assertThat(DataGenerator.generateDefaultValueByParamType(byte.class)).isEqualTo(Byte.valueOf("0"));
            assertThat(DataGenerator.generateDefaultValueByParamType(Character.class)).isEqualTo('\u0000');
            assertThat(DataGenerator.generateDefaultValueByParamType(char.class)).isEqualTo('\u0000');
            assertThat(DataGenerator.generateDefaultValueByParamType(Type.class)).isNull();
        }
    }

    @Nested
    @DisplayName("Map生成测试")
    class MapGenerationTests {

        @Test
        @DisplayName("fullMap_应该生成完整Map_当调用时")
        void fullMap_shouldGenerateFullMap_whenCalled() {
            // Act
            Map<String, Object> result = DataGenerator.fullMap();

            // Assert
            assertThat(result).hasSize(8);
            assertThat(result.get("string")).isInstanceOf(String.class);
            assertThat(result.get("short")).isInstanceOf(Short.class);
            assertThat(result.get("float")).isInstanceOf(Float.class);
            assertThat(result.get("double")).isInstanceOf(Double.class);
            assertThat(result.get("int")).isInstanceOf(Integer.class);
            assertThat(result.get("long")).isInstanceOf(Long.class);
            assertThat(result.get("date")).isInstanceOf(Date.class);
            assertThat(result.get("array")).isInstanceOf(List.class)
                .isEqualTo(Arrays.asList(1, 2, 3, 4, 5));
        }
    }

    @Nested
    @DisplayName("完整对象生成测试")
    class FullObjectGenerationTests {

        @Test
        @DisplayName("fullObject_应该生成完整对象_当输入基本类型时")
        void fullObject_shouldGenerateFullObject_whenInputBasicTypes() {
            // Act & Assert
            String str = DataGenerator.fullObject(String.class);
            assertThat(Objects.requireNonNull(str).length()).isBetween(1, 16);

            Short aShort = DataGenerator.fullObject(Short.class);
            assertThat(aShort).isBetween((short) (Short.MIN_VALUE + 1), Short.MAX_VALUE);

            Boolean aBoolean = DataGenerator.fullObject(Boolean.class);
            assertThat(aBoolean).isIn(true, false);

            Double aDouble = DataGenerator.fullObject(Double.class);
            assertThat(aDouble).isBetween(-Double.MAX_VALUE, Double.MAX_VALUE);

            Float aFloat = DataGenerator.fullObject(Float.class);
            assertThat(aFloat).isBetween(-Float.MAX_VALUE, Float.MAX_VALUE);

            Long aLong = DataGenerator.fullObject(Long.class);
            assertThat(aLong).isBetween(Long.MIN_VALUE + 1, Long.MAX_VALUE);

            Integer integer = DataGenerator.fullObject(Integer.class);
            assertThat(integer).isBetween(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("fullObject_应该生成完整对象_当输入特殊类型时")
        void fullObject_shouldGenerateFullObject_whenInputSpecialTypes() {
            // Act & Assert
            FullObjEnum fullObjectEnum = DataGenerator.fullObject(FullObjEnum.class);
            assertThat(fullObjectEnum).isIn((Object[]) FullObjEnum.values());

            FullObjTest test = DataGenerator.fullObject(FullObjTest.class);
            assertThat(Objects.requireNonNull(test).getList().size()).isBetween(1, 5);

            FullObjInterface testInterface = DataGenerator.fullObject(FullObjInterface.class);
            assertThat(testInterface).isNull();
        }
    }

    @Data
    public static class FullObjTest {

        private List<String> list;
    }

    public interface FullObjInterface {

    }

    public enum FullObjEnum {
        test1, test2
    }
}
