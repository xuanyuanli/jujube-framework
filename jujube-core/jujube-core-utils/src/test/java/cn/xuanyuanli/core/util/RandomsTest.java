package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;

public class RandomsTest {

    @Test
    public void randomCodes() {
        for (int i = 5; i < 15; i++) {
            String codes = Randoms.randomCodes(i);
            assertThat(Texts.find(codes, "^\\w{" + i + "}$")).isTrue();
        }
    }

    @Test
    public void randomInt() {
        for (int i = 0; i < 10; i++) {
            int iMax = i + 100;
            int num = Randoms.randomInt(i, iMax);
            assertThat(num).isGreaterThanOrEqualTo(i).isLessThanOrEqualTo(iMax);
        }
    }

    @Test
    void randomLong() {
        for (int i = 0; i < 10; i++) {
            int iMax = i + 10000;
            long num = Randoms.randomLong(i, iMax);
            assertThat(num).isGreaterThanOrEqualTo(i).isLessThanOrEqualTo(iMax);
        }
    }

    @Test
    void randomNumber() {
        for (int i = 5; i < 15; i++) {
            String codes = Randoms.randomNumber(i);
            assertThat(Texts.find(codes, "^\\d{" + i + "}$")).isTrue();
        }
    }

    @Test
    void randomNumberNoRepeat() {
        for (int i = 2; i < 10; i++) {
            String codes = Randoms.randomNumberNoRepeat(i);
            assertThat(codes.chars().distinct().count()).isEqualTo(i);
            assertThat(Texts.find(codes, "^\\d{" + i + "}$")).isTrue();
        }
        assertThatThrownBy(() -> Randoms.randomNumberNoRepeat(11));
    }

    @Test
    void randomLetter() {
        String codes = Randoms.randomLetter(10, 1);
        assertThat(Texts.find(codes, "^\\w{" + 10 + "}$")).isTrue();

        codes = Randoms.randomLetter(10, 2);
        assertThat(Texts.find(codes, "^\\w{" + 10 + "}$")).isTrue();

        codes = Randoms.randomLetter(10, 3);
        assertThat(Texts.find(codes, "^\\w{" + 10 + "}$")).isTrue();
    }

    @Test
    void randomCollection() {
        assertThat(Randoms.randomList(Lists.newArrayList(1, 2, 3, 4, 5), 2)).hasSize(2);
    }
}
