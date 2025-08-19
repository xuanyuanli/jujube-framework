package cn.xuanyuanli.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DaoInitializeExceptionTest {

    @Test
    void DaoInitializeException() {
        DaoInitializeException exception = new DaoInitializeException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void DaoInitializeException1() {
        DaoInitializeException exception = new DaoInitializeException("123");
        assertEquals(exception.getMessage(),"123");
        assertNull(exception.getCause());
    }

    @Test
    void DaoInitializeException2() {
        RuntimeException e = new RuntimeException("e");
        DaoInitializeException exception = new DaoInitializeException("123", e);
        assertEquals(exception.getMessage(),"123");
        assertEquals(exception.getCause(),e);
    }
}
