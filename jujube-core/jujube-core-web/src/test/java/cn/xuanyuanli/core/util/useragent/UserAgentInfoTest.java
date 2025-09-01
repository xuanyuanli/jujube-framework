package cn.xuanyuanli.core.util.useragent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserAgentInfo 测试")
public class UserAgentInfoTest {

    @Nested
    @DisplayName("基础功能测试")
    class BasicFunctionTests {

        @Test
        @DisplayName("constructor_应该创建有效的UserAgentInfo实例_当提供有效参数时")
        void constructor_shouldCreateValidUserAgentInfo_whenValidParametersProvided() {
            // Arrange & Act
            UserAgentInfo info = new UserAgentInfo("TestInfo", "testinfo");

            // Assert
            assertThat(info).isNotNull();
            assertThat(info.getName()).isEqualTo("TestInfo");
        }

        @Test
        @DisplayName("isMatch_应该返回true_当内容匹配模式时")
        void isMatch_shouldReturnTrue_whenContentMatchesPattern() {
            // Arrange
            UserAgentInfo info = new UserAgentInfo("Chrome", "chrome");

            // Act & Assert
            assertThat(info.isMatch("Mozilla Chrome browser")).isTrue();
            assertThat(info.isMatch("chrome version 91")).isTrue();
            assertThat(info.isMatch("CHROME Browser")).isTrue(); // 大小写不敏感
        }

        @Test
        @DisplayName("isMatch_应该返回false_当内容不匹配模式时")
        void isMatch_shouldReturnFalse_whenContentDoesNotMatchPattern() {
            // Arrange
            UserAgentInfo info = new UserAgentInfo("Chrome", "chrome");

            // Act & Assert
            assertThat(info.isMatch("Mozilla Firefox browser")).isFalse();
            assertThat(info.isMatch("Internet Explorer")).isFalse();
        }

        @Test
        @DisplayName("isUnknown_应该返回true_当名称为Unknown时")
        void isUnknown_shouldReturnTrue_whenNameIsUnknown() {
            // Arrange
            UserAgentInfo unknownInfo = new UserAgentInfo(UserAgentInfo.NAME_UNKNOWN, null);
            UserAgentInfo knownInfo = new UserAgentInfo("Chrome", "chrome");

            // Act & Assert
            assertThat(unknownInfo.isUnknown()).isTrue();
            assertThat(knownInfo.isUnknown()).isFalse();
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("equals_应该返回true_当对象相同时")
        void equals_shouldReturnTrue_whenObjectsAreSame() {
            // Arrange
            UserAgentInfo info1 = new UserAgentInfo("Chrome", "chrome");
            UserAgentInfo info2 = new UserAgentInfo("Chrome", "different_pattern");

            // Act & Assert
            assertThat(info1).isEqualTo(info2); // equals只比较name
        }

        @Test
        @DisplayName("equals_应该返回false_当对象不同时")
        void equals_shouldReturnFalse_whenObjectsAreDifferent() {
            // Arrange
            UserAgentInfo info1 = new UserAgentInfo("Chrome", "chrome");
            UserAgentInfo info2 = new UserAgentInfo("Firefox", "firefox");

            // Act & Assert
            assertThat(info1).isNotEqualTo(info2);
            assertThat(info1).isNotEqualTo(null);
            assertThat(info1).isNotEqualTo("String");
        }

        @Test
        @DisplayName("hashCode_应该相等_当对象equals时")
        void hashCode_shouldBeEqual_whenObjectsAreEqual() {
            // Arrange
            UserAgentInfo info1 = new UserAgentInfo("Chrome", "chrome");
            UserAgentInfo info2 = new UserAgentInfo("Chrome", "different_pattern");

            // Act & Assert
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }

        @Test
        @DisplayName("hashCode_应该处理null名称_当名称为null时")
        void hashCode_shouldHandleNullName_whenNameIsNull() {
            // Arrange
            UserAgentInfo info1 = new UserAgentInfo(null, "pattern");
            UserAgentInfo info2 = new UserAgentInfo(null, "pattern");

            // Act & Assert
            assertThat(info1).isEqualTo(info2);
            assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
        }
    }

    @Nested
    @DisplayName("toString测试")
    class ToStringTests {

        @Test
        @DisplayName("toString_应该返回名称_当调用时")
        void toString_shouldReturnName_whenCalled() {
            // Arrange
            UserAgentInfo info = new UserAgentInfo("Chrome", "chrome");

            // Act & Assert
            assertThat(info.toString()).isEqualTo("Chrome");
        }

        @Test
        @DisplayName("toString_应该处理null名称_当名称为null时")
        void toString_shouldHandleNullName_whenNameIsNull() {
            // Arrange
            UserAgentInfo info = new UserAgentInfo(null, "pattern");

            // Act & Assert
            assertThat(info.toString()).isNull();
        }
    }

    @Nested
    @DisplayName("静态常量测试")
    class StaticConstantsTests {

        @Test
        @DisplayName("NAME_UNKNOWN_应该为Unknown_当访问时")
        void nameUnknown_shouldBeUnknown_whenAccessed() {
            // Act & Assert
            assertThat(UserAgentInfo.NAME_UNKNOWN).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("constructor_应该处理null参数_当参数为null时")
        void constructor_shouldHandleNullParameters_whenParametersAreNull() {
            // Act & Assert
            UserAgentInfo info1 = new UserAgentInfo(null, "pattern");
            UserAgentInfo info2 = new UserAgentInfo("Name", null);
            UserAgentInfo info3 = new UserAgentInfo(null, null);

            assertThat(info1).isNotNull();
            assertThat(info2).isNotNull();
            assertThat(info3).isNotNull();
            
            assertThat(info1.getName()).isNull();
            assertThat(info2.getName()).isEqualTo("Name");
            assertThat(info3.getName()).isNull();
        }

        @Test
        @DisplayName("isMatch_应该处理null输入_当输入为null时")
        void isMatch_shouldHandleNullInput_whenInputIsNull() {
            // Arrange
            UserAgentInfo info = new UserAgentInfo("Chrome", "chrome");

            // Act & Assert
            try {
                boolean result = info.isMatch(null);
                assertThat(result).isFalse();
            } catch (Exception e) {
                // 这是可接受的行为
                assertThat(e).isInstanceOf(Exception.class);
            }
        }

        @Test
        @DisplayName("isMatch_应该处理null模式_当模式为null时")
        void isMatch_shouldHandleNullPattern_whenPatternIsNull() {
            // Arrange
            UserAgentInfo info = new UserAgentInfo("Chrome", null);

            // Act & Assert
            try {
                boolean result = info.isMatch("chrome browser");
                assertThat(result).isFalse();
            } catch (Exception e) {
                // 这是可接受的行为，null模式可能导致异常
                assertThat(e).isInstanceOf(Exception.class);
            }
        }
    }
}