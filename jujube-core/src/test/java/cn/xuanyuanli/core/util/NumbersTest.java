package cn.xuanyuanli.core.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumbersTest {

    @Test
    public void numberFormat() {
        Assertions.assertThat(Numbers.numberFormat(123.344, "##.####")).isEqualTo("123.344");
        Assertions.assertThat(Numbers.numberFormat(123.3444, "##.####")).isEqualTo("123.3444");
        Assertions.assertThat(Numbers.numberFormat(123.34444, "##.####")).isEqualTo("123.3444");
        Assertions.assertThat(Numbers.numberFormat(13.34444, "###")).isEqualTo("13");
        Assertions.assertThat(Numbers.numberFormat(123000000.34444, "##")).isEqualTo("123000000");
        Assertions.assertThat(Numbers.numberFormat(0, "##.##")).isEqualTo("0");
        Assertions.assertThat(Numbers.numberFormat(null, "##.##")).isNull();
    }


    @Test
    public void moneyFormat() {
        Assertions.assertThat(Numbers.moneyFormat(700000000.224)).isEqualTo("700,000,000.22");
        Assertions.assertThat(Numbers.moneyFormat(700000000.225)).isEqualTo("700,000,000.23");
        Assertions.assertThat(Numbers.moneyFormat(7000000000.0011)).isEqualTo("7,000,000,000");
        Assertions.assertThat(Numbers.moneyFormat(0.0)).isEqualTo("0");
        Assertions.assertThat(Numbers.moneyFormat(null)).isNull();
        Assertions.assertThat(Numbers.moneyFormat(null)).isNull();
    }

    @Test
    public void moneyFormatOfZhPrefix() {
        Assertions.assertThat(Numbers.moneyFormatOfZhPrefix(7000000000.0011, true)).isEqualTo("700,000万");
        Assertions.assertThat(Numbers.moneyFormatOfZhPrefix(7000000000.0011, false)).isEqualTo("7,000,000,000");
        Assertions.assertThat(Numbers.moneyFormatOfZhPrefix(0.0, false)).isEqualTo("0");
        Assertions.assertThat(Numbers.moneyFormatOfZhPrefix(null, false)).isNull();
        Assertions.assertThat(Numbers.moneyFormatOfZhPrefix(null, true)).isNull();
    }

    @Test
    public void numberToString() {
        Assertions.assertThat(Numbers.numberToString(890000000000.8)).isEqualTo("890000000000.8");
        Assertions.assertThat(Numbers.numberToString(89.898012555D)).isEqualTo("89.898012555");
        Assertions.assertThat(Numbers.numberToString(89.898F)).isEqualTo("89.898");
        Assertions.assertThat(Numbers.numberToString(898)).isEqualTo("898");
        Assertions.assertThat(Numbers.numberToString(80000000000000L)).isEqualTo("80000000000000");
        Assertions.assertThat(Numbers.numberToString(null)).isNull();
    }
}
