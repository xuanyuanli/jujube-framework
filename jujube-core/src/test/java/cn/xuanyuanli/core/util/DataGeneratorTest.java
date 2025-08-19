package cn.xuanyuanli.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.Data;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DataGeneratorTest {

    @Test
    public void generateRandomValueByParamType() {
        String str = DataGenerator.generateRandomValueByParamType(String.class);
        Assertions.assertThat(str.length()).isGreaterThanOrEqualTo(1).isLessThanOrEqualTo(16);

        Short aShort = DataGenerator.generateRandomValueByParamType(Short.class);
        Assertions.assertThat(aShort).isGreaterThan(Short.MIN_VALUE).isLessThanOrEqualTo(Short.MAX_VALUE);

        Boolean aBoolean = DataGenerator.generateRandomValueByParamType(Boolean.class);
        Assertions.assertThat(aBoolean).isIn(true, false);

        Double aDouble = DataGenerator.generateRandomValueByParamType(Double.class);
        Assertions.assertThat(aDouble).isGreaterThan(Double.MIN_VALUE).isLessThanOrEqualTo(Double.MAX_VALUE);

        Float aFloat = DataGenerator.generateRandomValueByParamType(Float.class);
        Assertions.assertThat(aFloat).isGreaterThan(Float.MIN_VALUE).isLessThanOrEqualTo(Float.MAX_VALUE);

        Long aLong = DataGenerator.generateRandomValueByParamType(Long.class);
        Assertions.assertThat(aLong).isGreaterThan(Long.MIN_VALUE).isLessThanOrEqualTo(Long.MAX_VALUE);

        Integer integer = DataGenerator.generateRandomValueByParamType(Integer.class);
        Assertions.assertThat(integer).isGreaterThan(Integer.MIN_VALUE).isLessThanOrEqualTo(Integer.MAX_VALUE);

        Type type = DataGenerator.generateRandomValueByParamType(Type.class);
        Assertions.assertThat(type).isNull();
    }

    @Test
    public void generateDefaultValueByParamType() {
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(String.class)).isEqualTo("");
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Short.class)).isEqualTo((short) 0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(short.class)).isEqualTo((short) 0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Boolean.class)).isEqualTo(false);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(boolean.class)).isEqualTo(false);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Double.class)).isEqualTo(0.0d);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(double.class)).isEqualTo(0.0d);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Float.class)).isEqualTo(0.0f);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(float.class)).isEqualTo(0.0f);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Long.class)).isEqualTo(0L);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(long.class)).isEqualTo(0L);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Integer.class)).isEqualTo(0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(int.class)).isEqualTo(0);
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Byte.class)).isEqualTo(Byte.valueOf("0"));
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(byte.class)).isEqualTo(Byte.valueOf("0"));
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Character.class)).isEqualTo('\u0000');
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(char.class)).isEqualTo('\u0000');
        Assertions.assertThat(DataGenerator.generateDefaultValueByParamType(Type.class)).isNull();
    }

    @Test
    void testFullMap() {
        // 调用方法
        Map<String, Object> result = DataGenerator.fullMap();

        // 验证 Map 的大小
        assertEquals(8, result.size());

        // 验证每个键值对的类型
        assertInstanceOf(String.class, result.get("string"));
        assertInstanceOf(Short.class, result.get("short"));
        assertInstanceOf(Float.class, result.get("float"));
        assertInstanceOf(Double.class, result.get("double"));
        assertInstanceOf(Integer.class, result.get("int"));
        assertInstanceOf(Long.class, result.get("long"));
        assertInstanceOf(Date.class, result.get("date"));
        assertInstanceOf(List.class, result.get("array"));

        // 验证数组内容
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), result.get("array"));
    }

    @Test
    public void fullObject() {
        String str = DataGenerator.fullObject(String.class);
        Assertions.assertThat(Objects.requireNonNull(str).length()).isGreaterThanOrEqualTo(1).isLessThanOrEqualTo(16);

        Short aShort = DataGenerator.fullObject(Short.class);
        Assertions.assertThat(aShort).isGreaterThan(Short.MIN_VALUE).isLessThanOrEqualTo(Short.MAX_VALUE);

        Boolean aBoolean = DataGenerator.fullObject(Boolean.class);
        Assertions.assertThat(aBoolean).isIn(true, false);

        Double aDouble = DataGenerator.fullObject(Double.class);
        Assertions.assertThat(aDouble).isGreaterThan(Double.MIN_VALUE).isLessThanOrEqualTo(Double.MAX_VALUE);

        Float aFloat = DataGenerator.fullObject(Float.class);
        Assertions.assertThat(aFloat).isGreaterThan(Float.MIN_VALUE).isLessThanOrEqualTo(Float.MAX_VALUE);

        Long aLong = DataGenerator.fullObject(Long.class);
        Assertions.assertThat(aLong).isGreaterThan(Long.MIN_VALUE).isLessThanOrEqualTo(Long.MAX_VALUE);

        Integer integer = DataGenerator.fullObject(Integer.class);
        Assertions.assertThat(integer).isGreaterThan(Integer.MIN_VALUE).isLessThanOrEqualTo(Integer.MAX_VALUE);

        FullObjEnum fullObjectEnum = DataGenerator.fullObject(FullObjEnum.class);
        Assertions.assertThat(fullObjectEnum).isIn((Object[]) FullObjEnum.values());

        FullObjTest test = DataGenerator.fullObject(FullObjTest.class);
        Assertions.assertThat(Objects.requireNonNull(test).getList().size()).isBetween(1, 5);

        FullObjInterface testInterface = DataGenerator.fullObject(FullObjInterface.class);
        Assertions.assertThat(testInterface).isNull();
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
