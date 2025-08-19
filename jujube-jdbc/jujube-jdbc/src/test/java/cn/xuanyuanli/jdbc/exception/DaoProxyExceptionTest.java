package cn.xuanyuanli.jdbc.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DaoProxyExceptionTest {
    @Test
    void DaoProxyException() {
        DaoProxyException exception = new DaoProxyException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void DaoProxyException1() {
        DaoProxyException exception = new DaoProxyException("123");
        assertEquals(exception.getMessage(),"123");
        assertNull(exception.getCause());
    }

    @Test
    void DaoProxyException2() {
        RuntimeException e = new RuntimeException("e");
        DaoProxyException exception = new DaoProxyException("123", e);
        assertEquals(exception.getMessage(),"123");
        assertEquals(exception.getCause(),e);
    }
}
