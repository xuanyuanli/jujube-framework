package cn.xuanyuanli.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Exceptions 工具类测试")
class ExceptionsTest {

    @Test
    @DisplayName("应该抛出运行时异常")
    void shouldThrowRuntimeException() {
        IllegalArgumentException originalException = new IllegalArgumentException("Original exception");
        Assertions.assertThrows(RuntimeException.class, () -> Exceptions.throwException(originalException));
    }

    @Test
    @DisplayName("抛出null异常应该抛出空指针异常")
    void shouldThrowNullPointerExceptionWhenExceptionIsNull() {
        Assertions.assertThrows(NullPointerException.class, () -> Exceptions.throwException(null));
    }

    @Test
    @DisplayName("应该将异常转换为字符串")
    void shouldConvertExceptionToString() {
        IllegalArgumentException exception = new IllegalArgumentException("Test message");
        String result = Exceptions.exceptionToString(exception);
        Assertions.assertTrue(result.startsWith("java.lang.IllegalArgumentException"));
        Assertions.assertTrue(result.contains("Test message"));
    }

    @Test
    @DisplayName("处理null异常时应该返回适当的字符串")
    void shouldHandleNullExceptionToString() {
        String result = Exceptions.exceptionToString(null);
        Assertions.assertEquals("null", result);
    }

    @Test
    @DisplayName("应该按指定长度裁剪异常字符串")
    void shouldTruncateExceptionStringToSpecifiedLength() {
        IllegalArgumentException exception = new IllegalArgumentException("Test message");
        String result = Exceptions.exceptionToString(exception, 9);
        Assertions.assertEquals("java.lang", result);
    }

    @Test
    @DisplayName("负数长度应该返回空字符串")
    void shouldReturnEmptyStringForNegativeLength() {
        IllegalArgumentException exception = new IllegalArgumentException("Test message");
        String result = Exceptions.exceptionToString(exception, -1);
        Assertions.assertEquals("", result);
    }

    @Test
    @DisplayName("零长度应该返回空字符串")
    void shouldReturnEmptyStringForZeroLength() {
        IllegalArgumentException exception = new IllegalArgumentException("Test message");
        String result = Exceptions.exceptionToString(exception, 0);
        Assertions.assertEquals("", result);
    }

    @Test
    @DisplayName("长度超过异常字符串长度时应该返回完整字符串")
    void shouldReturnCompleteStringWhenLengthExceedsExceptionStringLength() {
        IllegalArgumentException exception = new IllegalArgumentException();
        String fullString = Exceptions.exceptionToString(exception);
        String result = Exceptions.exceptionToString(exception, fullString.length() + 10);
        Assertions.assertEquals(fullString, result);
    }
}

