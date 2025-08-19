package cn.xuanyuanli.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class RepeatExceptionTest {
    @Test
    void RepeatException() {
        RepeatException exception = new RepeatException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void RepeatException1() {
        RepeatException exception = new RepeatException("123");
        assertEquals(exception.getMessage(),"123");
        assertNull(exception.getCause());
    }

    @Test
    void RepeatException2() {
        RuntimeException e = new RuntimeException("e");
        RepeatException exception = new RepeatException("123", e);
        assertEquals(exception.getMessage(),"123");
        assertEquals(exception.getCause(),e);
    }
}
