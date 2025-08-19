package cn.xuanyuanli.jdbc.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DaoQueryExceptionTest {
    @Test
    void DaoQueryException() {
        DaoQueryException exception = new DaoQueryException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void DaoQueryException1() {
        DaoQueryException exception = new DaoQueryException("123");
        assertEquals(exception.getMessage(),"123");
        assertNull(exception.getCause());
    }

    @Test
    void DaoQueryException2() {
        RuntimeException e = new RuntimeException("e");
        DaoQueryException exception = new DaoQueryException("123", e);
        assertEquals(exception.getMessage(),"123");
        assertEquals(exception.getCause(),e);
    }
}
