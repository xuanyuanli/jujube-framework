package cn.xuanyuanli.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CalcsTest {

    @Test
    void isLowNumber() {
        Assertions.assertThat(Calcs.isLow(0.0, 1)).isTrue();
        Assertions.assertThat(Calcs.isLow(0.0, 0)).isFalse();
        Assertions.assertThat(Calcs.isLow(1.01, 1)).isFalse();
        Assertions.assertThat(Calcs.isLow(1.0000000001, 1)).isFalse();
    }

    @Test
    void isLowString() {
        Assertions.assertThat(Calcs.isLow("0.0", "1")).isTrue();
        Assertions.assertThat(Calcs.isLow("0.0", "0")).isFalse();
        Assertions.assertThat(Calcs.isLow("1.01", "1")).isFalse();
        Assertions.assertThat(Calcs.isLow("1.0000000001", "1")).isFalse();
    }

    @Test
    void isLteNumber() {
        Assertions.assertThat(Calcs.isLte(0.0, 1)).isTrue();
        Assertions.assertThat(Calcs.isLte(0.0, 0)).isTrue();
        Assertions.assertThat(Calcs.isLte(1.01, 1)).isFalse();
        Assertions.assertThat(Calcs.isLte(1.0000000001, 1)).isFalse();
    }

    @Test
    public void equ() {
        Assertions.assertThat(Calcs.equ(0.0, 0)).isTrue();
        Assertions.assertThat(Calcs.equ(0.000001, 0)).isFalse();
        Assertions.assertThat(Calcs.equ(0.0f, 0)).isTrue();
    }

    @Test
    public void addNumberScale() {
        Assertions.assertThat(0.1 + 0.2).isEqualTo(0.30000000000000004);
        Assertions.assertThat(Calcs.add(0.0, 0, 2)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.add(0.1 + 0.2, 0.2, 18)).isEqualTo(0.50000000000000004);
        Assertions.assertThat(Calcs.add(0.1 + 0.2, 0.2, 18)).isEqualTo(0.5);
        Assertions.assertThat(Calcs.add(0.01, 0, 2)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.add(0.005, 0, 2)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.add(0.004, 0, 2)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.add(0.004, 0, 3)).isEqualTo(0.004);
        Assertions.assertThat(Calcs.add(12000000000.2536D, 100000000, 5)).isEqualTo(12100000000.2536);
        Assertions.assertThat(Calcs.add(12000000000.2536D, 100000000, 3)).isEqualTo(12100000000.254);
    }

    @Test
    public void addNumber() {
        Assertions.assertThat(Calcs.add(0.0, 0.0)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.add(0.01, 0)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.add(0.005, 0)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.add(0.004, 0)).isEqualTo(0.0);
    }

    @Test
    void addStringScale() {
        Assertions.assertThat(Calcs.add("0.0", "0", 2)).isEqualTo("0.00");
        Assertions.assertThat(Calcs.add("0.0", "0", 3)).isEqualTo("0.000");
        Assertions.assertThat(Calcs.add("0.01", "0", 2)).isEqualTo("0.01");
        Assertions.assertThat(Calcs.add("0.005", "0", 2)).isEqualTo("0.01");
        Assertions.assertThat(Calcs.add("0.004", "0", 3)).isEqualTo("0.004");
    }

    @Test
    void addString() {
        Assertions.assertThat(Calcs.add("0.0", "0")).isEqualTo("0.00");
        Assertions.assertThat(Calcs.add("0.01", "0")).isEqualTo("0.01");
        Assertions.assertThat(Calcs.add("0.005", "0")).isEqualTo("0.01");
        Assertions.assertThat(Calcs.add("0.004", "0")).isEqualTo("0.00");
    }

    @Test
    public void sum() {
        Assertions.assertThat(Calcs.getSum(0.0, 0)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.getSum(0.0, null)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.getSum(0.01, 0)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.getSum(0.01, null)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.getSum(0.005, 0)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.getSum(0.004, 0)).isEqualTo(0.0);
        Assertions.assertThat(Calcs.getSum(0.004, 0, 0.006)).isEqualTo(0.01);
        Assertions.assertThat(Calcs.getSum(0.04, 0, 2)).isEqualTo(2.04);
        Assertions.assertThat(Calcs.getSum(0.004, 0, 2, 0.01)).isEqualTo(2.01);
        Assertions.assertThat(Calcs.getSum(0.004, 0, 2, 0.001, 0.005)).isEqualTo(2.01);
        Assertions.assertThat(Calcs.getSum(0.004, 0, 2, 0.001, 0.005, null)).isEqualTo(2.01);
    }

    @Test
    public void mulNumber() {
        Assertions.assertThat(Calcs.mul(1.0, 0.996)).isEqualTo(1.0);
        Assertions.assertThat(Calcs.mul(123.55, 0.996)).isEqualTo(123.06);
    }

    @Test
    public void mulNumberScale() {
        Assertions.assertThat(Calcs.mul(1.0, 0.996, 3)).isEqualTo(0.996);
        Assertions.assertThat(Calcs.mul(123.55, 0.996, 4)).isEqualTo(123.0558);
    }

    @Test
    public void mulString() {
        Assertions.assertThat(Calcs.mul("1.0", "0.996")).isEqualTo("1.00");
        Assertions.assertThat(Calcs.mul("123.55", "0.996")).isEqualTo("123.06");
    }

    @Test
    public void mulStringScale() {
        Assertions.assertThat(Calcs.mul("1.0", "0.996", 3)).isEqualTo("0.996");
        Assertions.assertThat(Calcs.mul("123.55", "0.996", 4)).isEqualTo("123.0558");
    }

    @Test
    void subString() {
        Assertions.assertThat(Calcs.sub("1.0", "0.996")).isEqualTo("0.00");
        Assertions.assertThat(Calcs.sub("123.55", "0.996")).isEqualTo("122.55");
    }

    @Test
    void subStringScale() {
        Assertions.assertThat(Calcs.sub("1.0", "0.996", 3)).isEqualTo("0.004");
        Assertions.assertThat(Calcs.sub("123.55", "0.996", 4)).isEqualTo("122.5540");
    }

    @Test
    void subNumber() {
        Assertions.assertThat(Calcs.sub(1.0, 0.996)).isEqualTo(0);
        Assertions.assertThat(Calcs.sub(123.55, 0.996)).isEqualTo(122.55);
    }

    @Test
    void subNumberScale() {
        Assertions.assertThat(Calcs.sub(1.0, 0.996, 3)).isEqualTo(0.004);
        Assertions.assertThat(Calcs.sub(123.55, 0.996, 4)).isEqualTo(122.554);
    }

    @Test
    void divString() {
        Assertions.assertThat(Calcs.div("12", "1")).isEqualTo("12.00");
        Assertions.assertThat(Calcs.div("12", "0")).isEqualTo("12.00");
        Assertions.assertThat(Calcs.div("1", "0.996")).isEqualTo("1.00");
        Assertions.assertThat(Calcs.div("123.55", "0.996")).isEqualTo("124.05");
    }

    @Test
    void divStringScale() {
        Assertions.assertThat(Calcs.div("12", "1", 3)).isEqualTo("12.000");
        Assertions.assertThat(Calcs.div("1", "0.996", 3)).isEqualTo("1.004");
        Assertions.assertThat(Calcs.div("123.55", "0.996", 5)).isEqualTo("124.04618");
    }

    @Test
    void divNumber() {
        Assertions.assertThat(Calcs.div(12, 1)).isEqualTo(12.0);
        Assertions.assertThat(Calcs.div(12, 0)).isEqualTo(12.0);
        Assertions.assertThat(Calcs.div(1, 0.996)).isEqualTo(1.0);
        Assertions.assertThat(Calcs.div(123.55, 0.996)).isEqualTo(124.05);
    }

    @Test
    void divNumberScale() {
        Assertions.assertThat(Calcs.div(12, 1, 3)).isEqualTo(12.0);
        Assertions.assertThat(Calcs.div(1, 0.996, 3)).isEqualTo(1.004);
        Assertions.assertThat(Calcs.div(123.55, 0.996, 5)).isEqualTo(124.04618);
    }

    @Test
    void getAverage() {
        Assertions.assertThat(Calcs.getAverage(new ArrayList<>(Arrays.asList(12, 13, 14, 11, 10)))).isEqualTo(12);
    }

    @Test
    void getMedian() {
        Assertions.assertThat(Calcs.getMedian(new ArrayList<>(Arrays.asList(12, 13, 14, 11, 10)))).isEqualTo(13);
    }
}
