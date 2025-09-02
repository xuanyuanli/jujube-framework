package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CamelCase 驼峰命名转换测试")
class CamelCaseTest {

    @Nested
    @DisplayName("转下划线命名测试")
    class ToUnderlineNameTests {

        @Test
        @DisplayName("toUnderlineName_应该转换为下划线命名_当输入驼峰命名时")
        void toUnderlineName_shouldConvertToUnderlineName_whenInputIsCamelCase() {
            // Arrange
            String camelCaseName = "cardId";

            // Act
            String result = CamelCase.toUnderlineName(camelCaseName);

            // Assert
            assertThat(result).isEqualTo("card_id");
        }
    }

    @Nested
    @DisplayName("转驼峰命名测试")
    class ToCamelCaseTests {

        @Test
        @DisplayName("toCamelCase_应该转换为驼峰命名_当输入下划线命名时")
        void toCamelCase_shouldConvertToCamelCase_whenInputIsUnderlineName() {
            // Arrange
            String underlineName = "card_id";

            // Act
            String result = CamelCase.toCamelCase(underlineName);

            // Assert
            assertThat(result).isEqualTo("cardId");
        }

        @Test
        @DisplayName("toSpecilCamelCase_应该转换为特殊驼峰命名_当输入混合格式时")
        void toSpecilCamelCase_shouldConvertToSpecialCamelCase_whenInputIsMixedFormat() {
            // Arrange
            String mixedName = "Card_id";

            // Act
            String result = CamelCase.toSpecilCamelCase(mixedName);

            // Assert
            assertThat(result).isEqualTo("cardId");
        }

        @Test
        @DisplayName("toCapitalizeCamelCase_应该转换为首字母大写驼峰_当输入下划线命名时")
        void toCapitalizeCamelCase_shouldConvertToCapitalizedCamelCase_whenInputIsUnderlineName() {
            // Arrange
            String underlineName = "card_id";

            // Act
            String result = CamelCase.toCapitalizeCamelCase(underlineName);

            // Assert
            assertThat(result).isEqualTo("CardId");
        }
    }
}
