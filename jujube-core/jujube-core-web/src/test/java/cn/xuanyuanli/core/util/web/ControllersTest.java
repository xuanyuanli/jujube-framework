package cn.xuanyuanli.core.util.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Controllers 测试")
class ControllersTest {

    @Nested
    @DisplayName("重定向测试")
    class RedirectTests {

        @Test
        @DisplayName("redirect_应该返回带redirect前缀的路径_当提供相对路径时")
        void redirect_shouldReturnPathWithRedirectPrefix_whenRelativePathProvided() {
            // Act
            String result = Controllers.redirect("a");

            // Assert
            assertThat(result).isEqualTo("redirect:a");
        }

        @Test
        @DisplayName("redirect_应该返回带redirect前缀的路径_当提供绝对路径时")
        void redirect_shouldReturnPathWithRedirectPrefix_whenAbsolutePathProvided() {
            // Act
            String result = Controllers.redirect("/a");

            // Assert
            assertThat(result).isEqualTo("redirect:/a");
        }

        @Test
        @DisplayName("redirect_应该正确处理空字符串_当提供空路径时")
        void redirect_shouldHandleEmptyString_whenEmptyPathProvided() {
            // Act
            String result = Controllers.redirect("");

            // Assert
            assertThat(result).isEqualTo("redirect:");
        }

        @Test
        @DisplayName("redirect_应该正确处理null值_当提供null路径时")
        void redirect_shouldHandleNull_whenNullPathProvided() {
            // Act
            String result = Controllers.redirect(null);

            // Assert
            assertThat(result).isEqualTo("redirect:null");
        }
    }
}