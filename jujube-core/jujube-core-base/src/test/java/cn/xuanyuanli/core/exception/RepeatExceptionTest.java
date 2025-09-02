package cn.xuanyuanli.core.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * RepeatException 异常类测试
 * 验证异常的各种构造函数和边界情况
 */
@DisplayName("RepeatException 异常类测试")
class RepeatExceptionTest {

    @Nested
    @DisplayName("构造函数测试")
    class ConstructorTests {

        @Test
        @DisplayName("RepeatException_应该创建空异常_当使用无参构造函数时")
        void RepeatException_shouldCreateEmptyException_whenUsingNoArgsConstructor() {
            // Act
            RepeatException exception = new RepeatException();

            // Assert
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("RepeatException_应该设置消息_当使用带消息的构造函数时")
        void RepeatException_shouldSetMessage_whenUsingMessageConstructor() {
            // Arrange
            String message = "Test exception message";

            // Act
            RepeatException exception = new RepeatException(message);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("RepeatException_应该设置消息和原因_当使用带消息和原因的构造函数时")
        void RepeatException_shouldSetMessageAndCause_whenUsingMessageAndCauseConstructor() {
            // Arrange
            String message = "Test exception message";
            RuntimeException cause = new RuntimeException("Root cause");

            // Act
            RepeatException exception = new RepeatException(message, cause);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("RepeatException_应该处理null消息_当传入null消息时")
        void RepeatException_shouldHandleNullMessage_whenPassingNullMessage() {
            // Act
            RepeatException exception = new RepeatException(null);

            // Assert
            assertThat(exception.getMessage()).isNull();
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("RepeatException_应该处理null原因_当传入null原因时")
        void RepeatException_shouldHandleNullCause_whenPassingNullCause() {
            // Arrange
            String message = "Test message";

            // Act
            RepeatException exception = new RepeatException(message, null);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(message);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("RepeatException_应该处理空字符串消息_当传入空字符串消息时")
        void RepeatException_shouldHandleEmptyStringMessage_whenPassingEmptyStringMessage() {
            // Arrange
            String emptyMessage = "";

            // Act
            RepeatException exception = new RepeatException(emptyMessage);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(emptyMessage);
            assertThat(exception.getCause()).isNull();
        }

        @Test
        @DisplayName("RepeatException_应该处理空白字符串消息_当传入空白字符串消息时")
        void RepeatException_shouldHandleWhitespaceMessage_whenPassingWhitespaceMessage() {
            // Arrange
            String whitespaceMessage = "   ";

            // Act
            RepeatException exception = new RepeatException(whitespaceMessage);

            // Assert
            assertThat(exception.getMessage()).isEqualTo(whitespaceMessage);
            assertThat(exception.getCause()).isNull();
        }
    }

    @Nested
    @DisplayName("异常类型验证测试")
    class ExceptionTypeTests {

        @Test
        @DisplayName("RepeatException_应该继承RuntimeException_当检查类型层次时")
        void RepeatException_shouldExtendRuntimeException_whenCheckingTypeHierarchy() {
            // Act
            RepeatException exception = new RepeatException("Test message");

            // Assert
            assertThat(exception).isInstanceOf(RuntimeException.class);
            assertThat(exception).isInstanceOf(Exception.class);
            assertThat(exception).isInstanceOf(Throwable.class);
        }
    }
}
