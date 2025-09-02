package cn.xuanyuanli.core.util.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PatternHolder 正则表达式缓存测试")
class PatternHolderTest {

    @Nested
    @DisplayName("模式获取测试")
    class PatternRetrievalTests {

        @Test
        @DisplayName("getPattern_应该返回正确的正则模式_当多次获取同一模式时")
        void getPattern_shouldReturnCorrectPattern_whenGettingSamePatternMultipleTimes() {
            // Arrange
            String regex = "^(\\w)+$";

            // Act & Assert
            assertThat(PatternHolder.getPattern(regex).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.getPattern(regex).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.getPattern(regex).pattern()).isEqualTo(regex);
        }

        @Test
        @DisplayName("getPattern_应该根据标志返回不同模式_当使用不同的大小写敏感标志时")
        void getPattern_shouldReturnDifferentPatterns_whenUsingDifferentCaseFlags() {
            // Arrange
            String regex = "^(\\w)+$";

            // Act & Assert
            assertThat(PatternHolder.getPattern(regex, true).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.getPattern(regex, true).flags()).isEqualTo(2);
            assertThat(PatternHolder.getPattern(regex, false).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.getPattern(regex, false).flags()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("模式编译测试")
    class PatternCompilationTests {

        @Test
        @DisplayName("compile_应该返回带有正确标志的模式_当使用不同标志时")
        void compile_shouldReturnPatternWithCorrectFlags_whenUsingDifferentFlags() {
            // Arrange
            String regex = "^(\\w)+$";

            // Act & Assert
            assertThat(PatternHolder.compile(regex, Pattern.CASE_INSENSITIVE).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.compile(regex, Pattern.CASE_INSENSITIVE).flags()).isEqualTo(Pattern.CASE_INSENSITIVE);
            
            assertThat(PatternHolder.compile(regex, Pattern.MULTILINE).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.compile(regex, Pattern.MULTILINE).flags()).isEqualTo(Pattern.MULTILINE);
            
            assertThat(PatternHolder.compile(regex, Pattern.LITERAL).pattern()).isEqualTo(regex);
            assertThat(PatternHolder.compile(regex, Pattern.LITERAL).flags()).isEqualTo(Pattern.LITERAL);
        }
    }

    @Nested
    @DisplayName("特殊字符转义测试")
    class SpecialCharacterEscapeTests {

        @Test
        @DisplayName("escapeExprSpecialWord_应该正确转义所有正则表达式特殊字符_当输入特殊字符时")
        void escapeExprSpecialWord_shouldCorrectlyEscapeAllSpecialChars_whenInputSpecialCharacters() {
            // Act & Assert
            assertThat(PatternHolder.escapeExprSpecialWord("$")).isEqualTo("\\$");
            assertThat(PatternHolder.escapeExprSpecialWord("?")).isEqualTo("\\?");
            assertThat(PatternHolder.escapeExprSpecialWord("(")).isEqualTo("\\(");
            assertThat(PatternHolder.escapeExprSpecialWord(")")).isEqualTo("\\)");
            assertThat(PatternHolder.escapeExprSpecialWord("*")).isEqualTo("\\*");
            assertThat(PatternHolder.escapeExprSpecialWord("+")).isEqualTo("\\+");
            assertThat(PatternHolder.escapeExprSpecialWord(".")).isEqualTo("\\.");
            assertThat(PatternHolder.escapeExprSpecialWord("[")).isEqualTo("\\[");
            assertThat(PatternHolder.escapeExprSpecialWord("]")).isEqualTo("\\]");
            assertThat(PatternHolder.escapeExprSpecialWord("^")).isEqualTo("\\^");
            assertThat(PatternHolder.escapeExprSpecialWord("{")).isEqualTo("\\{");
            assertThat(PatternHolder.escapeExprSpecialWord("}")).isEqualTo("\\}");
            assertThat(PatternHolder.escapeExprSpecialWord("|")).isEqualTo("\\|");
            assertThat(PatternHolder.escapeExprSpecialWord("\\")).isEqualTo("\\\\");
        }
    }
}
