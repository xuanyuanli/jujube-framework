package cn.xuanyuanli.core.util.support;

import java.util.regex.Pattern;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PatternHolderTest {

    @Test
    void getPattern() {
        String regex = "^(\\w)+$";
        Assertions.assertThat(PatternHolder.getPattern(regex).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.getPattern(regex).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.getPattern(regex).pattern()).isEqualTo(regex);
    }

    @Test
    void getPattern2() {
        String regex = "^(\\w)+$";
        Assertions.assertThat(PatternHolder.getPattern(regex,true).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.getPattern(regex,true).flags()).isEqualTo(2);
        Assertions.assertThat(PatternHolder.getPattern(regex,false).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.getPattern(regex,false).flags()).isEqualTo(0);
    }

    @Test
    void compile() {
        String regex = "^(\\w)+$";
        Assertions.assertThat(PatternHolder.compile(regex, Pattern.CASE_INSENSITIVE).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.compile(regex,Pattern.CASE_INSENSITIVE).flags()).isEqualTo(Pattern.CASE_INSENSITIVE);
        Assertions.assertThat(PatternHolder.compile(regex,Pattern.MULTILINE).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.compile(regex,Pattern.MULTILINE).flags()).isEqualTo(Pattern.MULTILINE);
        Assertions.assertThat(PatternHolder.compile(regex,Pattern.LITERAL).pattern()).isEqualTo(regex);
        Assertions.assertThat(PatternHolder.compile(regex,Pattern.LITERAL).flags()).isEqualTo(Pattern.LITERAL);
    }

    @Test
    void escapeExprSpecialWord() {
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("$")).isEqualTo("\\$");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("?")).isEqualTo("\\?");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("(")).isEqualTo("\\(");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord(")")).isEqualTo("\\)");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("*")).isEqualTo("\\*");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("+")).isEqualTo("\\+");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord(".")).isEqualTo("\\.");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("[")).isEqualTo("\\[");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("]")).isEqualTo("\\]");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("^")).isEqualTo("\\^");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("{")).isEqualTo("\\{");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("}")).isEqualTo("\\}");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("|")).isEqualTo("\\|");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("\\")).isEqualTo("\\\\");
    }
}
