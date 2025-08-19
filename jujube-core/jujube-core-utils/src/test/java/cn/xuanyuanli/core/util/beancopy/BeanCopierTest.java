package cn.xuanyuanli.core.util.beancopy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BeanCopierTest {

    @Test
    void testGetBoolean() {
        Assertions.assertFalse(BeanCopier.getBoolean("false"));
        Assertions.assertFalse(BeanCopier.getBoolean("fa"));
        Assertions.assertFalse(BeanCopier.getBoolean(""));
        Assertions.assertFalse(BeanCopier.getBoolean(null));
        Assertions.assertTrue(BeanCopier.getBoolean("true"));
    }

    @Test
    void testGetChar() {
        Assertions.assertEquals('a', BeanCopier.getChar("a"));
        Assertions.assertEquals(32, BeanCopier.getChar(""));
        Assertions.assertEquals('\u0000', BeanCopier.getChar(null));
    }

    @Test
    void testGetByte() {
        Assertions.assertEquals((byte) 0, BeanCopier.getByte(""));
        Assertions.assertEquals((byte) 0, BeanCopier.getByte(null));
        Assertions.assertEquals((byte) 0, BeanCopier.getByte("0"));
        Assertions.assertEquals((byte) 0, BeanCopier.getByte(Byte.valueOf("0")));
        Assertions.assertEquals((byte) 12, BeanCopier.getByte(Byte.valueOf("12")));
    }

    @Test
    void testGetShort() {
        Assertions.assertEquals((short) 0, BeanCopier.getShort("0"));
        Assertions.assertEquals((short) 0, BeanCopier.getShort(null));
        Assertions.assertEquals((short) 0, BeanCopier.getShort(Short.valueOf("0")));
        Assertions.assertEquals((short) 1, BeanCopier.getShort(Short.valueOf("01")));
    }

    @Test
    void testGetInt() {
        Assertions.assertEquals(0, BeanCopier.getInt(""));
        Assertions.assertEquals(0, BeanCopier.getInt(null));
        Assertions.assertEquals(1, BeanCopier.getInt(1));
        Assertions.assertEquals(0, BeanCopier.getInt(Integer.valueOf("0")));
        Assertions.assertEquals(2, BeanCopier.getInt(Integer.valueOf("2")));
    }

    @Test
    void testGetLong() {
        Assertions.assertEquals(0L, BeanCopier.getLong(""));
        Assertions.assertEquals(0L, BeanCopier.getLong(null));
        Assertions.assertEquals(1L, BeanCopier.getLong(1));
        Assertions.assertEquals(0L, BeanCopier.getLong(Long.valueOf("0")));
        Assertions.assertEquals(2L, BeanCopier.getLong(Long.valueOf("2")));
    }

    @Test
    void testGetFloat() {
        Assertions.assertEquals(0F, BeanCopier.getFloat(""));
        Assertions.assertEquals(0F, BeanCopier.getFloat(null));
        Assertions.assertEquals(1F, BeanCopier.getFloat(1));
        Assertions.assertEquals(0.2F, BeanCopier.getFloat(Float.valueOf("0.2")));
        Assertions.assertEquals(2F, BeanCopier.getFloat(Float.valueOf("2")));
    }

    @Test
    void testGetDouble() {
        Assertions.assertEquals(0D, BeanCopier.getDouble(""));
        Assertions.assertEquals(0D, BeanCopier.getDouble(null));
        Assertions.assertEquals(1D, BeanCopier.getDouble(1));
        Assertions.assertEquals(0.2D, BeanCopier.getDouble(Double.valueOf("0.2")));
        Assertions.assertEquals(2D, BeanCopier.getDouble(Double.valueOf("2")));
    }
}
