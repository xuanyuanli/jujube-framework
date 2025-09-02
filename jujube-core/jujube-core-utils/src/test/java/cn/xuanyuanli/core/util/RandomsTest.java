package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Randoms 随机数工具类测试")
class RandomsTest {

    @Nested
    @DisplayName("随机字符串生成测试")
    class RandomStringGenerationTests {

        @Test
        @DisplayName("randomCodes_应该生成指定长度的随机代码_当提供有效长度时")
        void randomCodes_shouldGenerateRandomCodesWithSpecifiedLength_whenValidLengthProvided() {
            // Act & Assert
            for (int i = 5; i < 15; i++) {
                String codes = Randoms.randomCodes(i);
                assertThat(Texts.find(codes, "^\\w{" + i + "}$")).isTrue();
            }
        }

        @Test
        @DisplayName("randomNumber_应该生成指定长度的随机数字字符串_当提供有效长度时")
        void randomNumber_shouldGenerateRandomNumbersWithSpecifiedLength_whenValidLengthProvided() {
            // Act & Assert
            for (int i = 5; i < 15; i++) {
                String codes = Randoms.randomNumber(i);
                assertThat(Texts.find(codes, "^\\d{" + i + "}$")).isTrue();
            }
        }

        @Test
        @DisplayName("randomNumberNoRepeat_应该生成不重复数字字符串_当长度小于等于10时")
        void randomNumberNoRepeat_shouldGenerateNonRepeatingNumbers_whenLengthLessThanOrEqualTo10() {
            // Act & Assert
            for (int i = 2; i < 10; i++) {
                String codes = Randoms.randomNumberNoRepeat(i);
                assertThat(codes.chars().distinct().count()).isEqualTo(i);
                assertThat(Texts.find(codes, "^\\d{" + i + "}$")).isTrue();
            }
        }

        @Test
        @DisplayName("randomNumberNoRepeat_应该抛出异常_当长度大于10时")
        void randomNumberNoRepeat_shouldThrowException_whenLengthGreaterThan10() {
            // Act & Assert
            assertThatThrownBy(() -> Randoms.randomNumberNoRepeat(11));
        }

        @Test
        @DisplayName("randomLetter_应该根据类型生成随机字母_当使用不同类型时")
        void randomLetter_shouldGenerateRandomLettersByType_whenUsingDifferentTypes() {
            // Act & Assert
            String codes = Randoms.randomLetter(10, 1);
            assertThat(Texts.find(codes, "^\\w{" + 10 + "}$")).isTrue();

            codes = Randoms.randomLetter(10, 2);
            assertThat(Texts.find(codes, "^\\w{" + 10 + "}$")).isTrue();

            codes = Randoms.randomLetter(10, 3);
            assertThat(Texts.find(codes, "^\\w{" + 10 + "}$")).isTrue();
        }
    }

    @Nested
    @DisplayName("随机数值生成测试")
    class RandomNumberGenerationTests {

        @Test
        @DisplayName("randomInt_应该生成指定范围内的随机整数_当提供有效范围时")
        void randomInt_shouldGenerateRandomIntWithinRange_whenValidRangeProvided() {
            // Act & Assert
            for (int i = 0; i < 10; i++) {
                int iMax = i + 100;
                int num = Randoms.randomInt(i, iMax);
                assertThat(num).isGreaterThanOrEqualTo(i).isLessThanOrEqualTo(iMax);
            }
        }

        @Test
        @DisplayName("randomLong_应该生成指定范围内的随机长整数_当提供有效范围时")
        void randomLong_shouldGenerateRandomLongWithinRange_whenValidRangeProvided() {
            // Act & Assert
            for (int i = 0; i < 10; i++) {
                int iMax = i + 10000;
                long num = Randoms.randomLong(i, iMax);
                assertThat(num).isGreaterThanOrEqualTo(i).isLessThanOrEqualTo(iMax);
            }
        }
    }

    @Nested
    @DisplayName("随机集合操作测试")
    class RandomCollectionTests {

        @Test
        @DisplayName("randomList_应该返回指定数量的随机元素_当从集合中选择时")
        void randomList_shouldReturnSpecifiedNumberOfRandomElements_whenSelectingFromCollection() {
            // Arrange
            ArrayList<Integer> sourceList = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));
            
            // Act & Assert
            assertThat(Randoms.randomList(sourceList, 2)).hasSize(2);
        }
    }
}
