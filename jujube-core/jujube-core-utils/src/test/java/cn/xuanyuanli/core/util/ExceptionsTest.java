package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Exceptions 异常工具类测试")
class ExceptionsTest {

    @Nested
    @DisplayName("异常抛出测试")
    class ExceptionThrowingTests {

        @Test
        @DisplayName("throwException_应该抛出运行时异常_当输入非空异常时")
        void throwException_shouldThrowRuntimeException_whenInputNonNullException() {
            // Arrange
            IllegalArgumentException originalException = new IllegalArgumentException("Original exception");

            // Act & Assert
            assertThatThrownBy(() -> Exceptions.throwException(originalException))
                .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("throwException_应该抛出空指针异常_当输入null异常时")
        void throwException_shouldThrowNullPointerException_whenInputNullException() {
            // Act & Assert
            assertThatThrownBy(() -> Exceptions.throwException(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("异常转字符串测试")
    class ExceptionToStringTests {

        @Test
        @DisplayName("exceptionToString_应该转换异常为字符串_当输入正常异常时")
        void exceptionToString_shouldConvertExceptionToString_whenInputNormalException() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test message");

            // Act
            String result = Exceptions.exceptionToString(exception);

            // Assert
            assertThat(result).startsWith("java.lang.IllegalArgumentException")
                .contains("Test message");
        }

        @Test
        @DisplayName("exceptionToString_应该返回null字符串_当输入null异常时")
        void exceptionToString_shouldReturnNullString_whenInputNullException() {
            // Act
            String result = Exceptions.exceptionToString(null);

            // Assert
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("exceptionToString_应该按指定长度裁剪_当指定长度时")
        void exceptionToString_shouldTruncateToSpecifiedLength_whenLengthSpecified() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test message");

            // Act
            String result = Exceptions.exceptionToString(exception, 9);

            // Assert
            assertThat(result).isEqualTo("java.lang");
        }

        @Test
        @DisplayName("exceptionToString_应该返回空字符串_当长度为负数时")
        void exceptionToString_shouldReturnEmptyString_whenLengthIsNegative() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test message");

            // Act
            String result = Exceptions.exceptionToString(exception, -1);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("exceptionToString_应该返回空字符串_当长度为零时")
        void exceptionToString_shouldReturnEmptyString_whenLengthIsZero() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException("Test message");

            // Act
            String result = Exceptions.exceptionToString(exception, 0);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("exceptionToString_应该返回完整字符串_当长度超过原字符串长度时")
        void exceptionToString_shouldReturnCompleteString_whenLengthExceedsOriginalLength() {
            // Arrange
            IllegalArgumentException exception = new IllegalArgumentException();
            String fullString = Exceptions.exceptionToString(exception);

            // Act
            String result = Exceptions.exceptionToString(exception, fullString.length() + 10);

            // Assert
            assertThat(result).isEqualTo(fullString);
        }
    }
}

