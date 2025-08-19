package cn.xuanyuanli.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExceptionsTest {

    @Test
    void testThrowException() {
        Assertions.assertThrows(RuntimeException.class, () -> Exceptions.throwException(new IllegalArgumentException()));
    }

    @Test
    void testExceptionToString() {
        String result = Exceptions.exceptionToString(new IllegalArgumentException());
        Assertions.assertTrue(result.startsWith("java.lang.IllegalArgumentException"));
    }

    @Test
    void testExceptionToString2() {
        String result = Exceptions.exceptionToString(new IllegalArgumentException(), 9);
        Assertions.assertEquals(result,"java.lang");
    }
}

