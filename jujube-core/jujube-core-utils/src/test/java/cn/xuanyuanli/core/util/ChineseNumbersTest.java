package cn.xuanyuanli.core.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ChineseNumbersTest {

    @Test
    void chineseNumberToEnglish() {
        Assertions.assertThat(ChineseNumbers.chineseNumberToEnglish("捌玖")).isEqualTo(89);
        Assertions.assertThat(ChineseNumbers.chineseNumberToEnglish("十五")).isEqualTo(15);
        Assertions.assertThat(ChineseNumbers.chineseNumberToEnglish("一点五")).isEqualTo(1.5);
        Assertions.assertThat(ChineseNumbers.chineseNumberToEnglish("一百五")).isEqualTo(150);
        Assertions.assertThat(ChineseNumbers.chineseNumberToEnglish("一百五十")).isEqualTo(150);
        Assertions.assertThat(ChineseNumbers.chineseNumberToEnglish("一千两百万")).isEqualTo(12000000);
    }

    @Test
    void englishNumberToChinese() {
        Assertions.assertThat(ChineseNumbers.englishNumberToChinese("15")).isEqualTo("十五");
        Assertions.assertThat(ChineseNumbers.englishNumberToChinese("1.5")).isEqualTo("一点五");
        Assertions.assertThat(ChineseNumbers.englishNumberToChinese("150")).isEqualTo("一百五十");
        Assertions.assertThat(ChineseNumbers.englishNumberToChinese("1500")).isEqualTo("一千五百");
        Assertions.assertThat(ChineseNumbers.englishNumberToChinese("15000")).isEqualTo("一万五千");
    }
}
