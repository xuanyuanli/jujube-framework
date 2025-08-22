package cn.xuanyuanli.core.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RepeatException 测试")
class RepeatExceptionTest {
    @Test
    @DisplayName("无参构造函数应该创建空异常")
    void shouldCreateEmptyExceptionWithNoArgsConstructor() {
        RepeatException exception = new RepeatException();
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("带消息的构造函数应该设置消息")
    void shouldSetMessageWithMessageConstructor() {
        String message = "Test exception message";
        RepeatException exception = new RepeatException(message);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("带消息和原因的构造函数应该设置消息和原因")
    void shouldSetMessageAndCauseWithMessageAndCauseConstructor() {
        String message = "Test exception message";
        RuntimeException cause = new RuntimeException("Root cause");
        RepeatException exception = new RepeatException(message, cause);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("空消息应该被正确处理")
    void shouldHandleNullMessage() {
        RepeatException exception = new RepeatException(null);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("空原因应该被正确处理")
    void shouldHandleNullCause() {
        String message = "Test message";
        RepeatException exception = new RepeatException(message, null);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("空字符串消息应该被正确处理")
    void shouldHandleEmptyStringMessage() {
        String emptyMessage = "";
        RepeatException exception = new RepeatException(emptyMessage);
        assertEquals(emptyMessage, exception.getMessage());
        assertNull(exception.getCause());
    }
}
