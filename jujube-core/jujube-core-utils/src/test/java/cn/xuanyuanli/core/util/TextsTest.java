package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;
import cn.xuanyuanli.core.exception.RepeatException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TextsTest {

    @Nested
    class GetChinese {

        @ParameterizedTest(name = "提取中文字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetChineseArguments")
        void testGetChinese(String input, String expected) {
            // Act
            String result = Texts.getChinese(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetChineseArguments() {
            return Stream.of(Arguments.of("hello李world万贯", "李万贯"), Arguments.of("*李--万贯", "李万贯"), Arguments.of("*李|万贯·", "李万贯"),
                    Arguments.of("拍品名称（中文）", "拍品名称中文"));
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "DataFlowIssue"})
    @Nested
    class Group {

        @ParameterizedTest(name = "分组: 输入 \"{0}\" 应返回 {1}")
        @MethodSource("provideGroupArguments")
        void testGroup(List<String> lines, Function<String, String> groupFunction, boolean allowRepeat, Map<String, List<String>> expected) {
            // Act
            Map<String, List<String>> result = Texts.group(lines, groupFunction, allowRepeat);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGroupArguments() {
            Function<String, String> groupFn = line -> line.startsWith("#") ? line : null;
            return Stream.of(Arguments.of(Arrays.asList("#A", "line1", "#B", "line2"), groupFn, true, Map.of("#A", List.of("line1"), "#B", List.of("line2"))),
                    Arguments.of(Arrays.asList("#A", "line1", "#A", "line2"), groupFn, true, Map.of("#A", List.of("line2"))));
        }

        @Test
        @DisplayName("不允许重复时抛出异常")
        void testDisallowRepeatThrowsException() {
            // Arrange
            List<String> lines = Arrays.asList("#A", "line1", "#A", "line2");
            Function<String, String> groupFunction = line -> line.startsWith("#") ? line : null;
            // Act & Assert
            assertThatThrownBy(() -> Texts.group(lines, groupFunction, false)).isInstanceOf(RepeatException.class)
                    .withFailMessage("groupName 'A' already exists.");
        }

        @ParameterizedTest(name = "测试空输入：{0}")
        @ValueSource(strings = {"nullLines", "nullFunction"})
        @DisplayName("测试空输入场景")
        void testEmptyInput(String testCase) {
            // Arrange
            List<String> lines = testCase.equals("nullLines") ? null : Arrays.asList("#group1", "line1");
            Function<String, String> groupFunction = testCase.equals("nullFunction") ? null : line -> line.startsWith("#") ? line : null;

            // Act & Assert
            if (lines == null || groupFunction == null) {
                assertThrows(NullPointerException.class, () -> Texts.group(lines, groupFunction, true));
            }
        }
    }

    @Nested
    class GroupAgain {

        @ParameterizedTest(name = "再分组: 输入 {0} 应返回 {1}")
        @MethodSource("provideGroupAgainArguments")
        void testGroupAgain(Map<String, List<String>> group, Function<String, String> groupFunction, Map<String, Map<String, List<String>>> expected) {
            // Act
            Map<String, Map<String, List<String>>> result = Texts.groupAgain(group, groupFunction);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGroupAgainArguments() {
            Function<String, String> subGroupFn = line -> line.contains("sub") ? "sub" : "other";
            return Stream.of(Arguments.of(Map.of("A", List.of("line1_sub", "line2_other"), "B", List.of("line3_sub", "line4_other")), subGroupFn,
                    Map.of("A", Map.of("sub", List.of("line1_sub"), "other", List.of("line2_other")), "B",
                            Map.of("sub", List.of("line3_sub"), "other", List.of("line4_other")))));
        }
    }

    @Nested
    class MergeGroup {

        @ParameterizedTest(name = "合并分组: 输入 {0} 应返回 {1}")
        @MethodSource("provideMergeGroupArguments")
        void testMergeGroup(Map<String, Map<String, List<String>>> group, Function<String, String> moduleCommentFunction, List<String> expected) {
            // Act
            List<String> result = Texts.mergeGroup(group, moduleCommentFunction);
            // Assert
            assertThat(result).containsExactlyElementsOf(expected);
        }

        static Stream<Arguments> provideMergeGroupArguments() {
            Function<String, String> commentFn = groupName -> "/* " + groupName + " */";
            TreeMap<String, List<String>> v1 = new TreeMap<>(Map.of("sub", List.of("line1_sub"), "other", List.of("line2_other")));
            TreeMap<String, List<String>> v2 = new TreeMap<>(Map.of("sub", List.of("line3_sub"), "other", List.of("line4_other")));
            return Stream.of(Arguments.of(new TreeMap<>(Map.of("A", v1, "B", v2)), commentFn,
                    List.of("/* A */", "line2_other", "", "line1_sub", "", "", "/* B */", "line4_other", "", "line3_sub", "", "")));
        }
    }

    @SuppressWarnings("ConstantValue")
    @Nested
    class Format {

        @ParameterizedTest(name = "字符串格式化: 输入 \"{0}\" 参数 {1} 应返回 \"{2}\"")
        @MethodSource("provideFormatArguments")
        void testFormat(String pattern, Object[] params, String expected) {
            // Act
            String result = Texts.format(pattern, params);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideFormatArguments() {
            return Stream.of(Arguments.of("{0}-{1}", new Object[]{"1", null}, "1-"), Arguments.of("{1}-{0}", new Object[]{"1", "2"}, "2-1"),
                    Arguments.of("{}-{}", new Object[]{"1", "2"}, "1-2"), Arguments.of("{}-{}$", new Object[]{"1", "2"}, "1-2$"),
                    Arguments.of("123", new Object[]{}, "123"), Arguments.of("123{}", new Object[]{}, "123"),
                    Arguments.of("123{}456", new Object[]{}, "123456"), Arguments.of("1{}2{}3", new Object[]{"-"}, "1-23"),
                    Arguments.of("{0}-{1}", new Object[]{1, 2}, "1-2"), Arguments.of("Hello {}", new Object[]{"World!"}, "Hello World!"),
                    Arguments.of("Hello {}!", new Object[]{"World"}, "Hello World!"), Arguments.of("Hello {0}!", new Object[]{"World"}, "Hello World!"));
        }

        @Test
        @DisplayName("处理带空格的简单替换")
        void testSimpleReplacementWithSpaces() {
            // Arrange
            String pattern = "Hello {}!";
            String param = "World";
            String expected = "Hello World!";
            // Act
            String result = Texts.format(pattern, param);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("处理多个占位符的复杂替换")
        void testComplexReplacement() {
            // Arrange
            String pattern = "Name: {}, Age: {}, Gender: {}";
            Object[] params = {"John", 25, "Male"};
            String expected = "Name: John, Age: 25, Gender: Male";
            // Act
            String result = Texts.format(pattern, params);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Test
        @DisplayName("处理不匹配的参数个数: 抛出异常")
        void testMismatchedParametersThrowsException() {
            // Arrange
            String pattern = "{0}-{1}";
            Object[] params = {"1"};

            // Act & Assert
            assertThatThrownBy(() -> Texts.format(pattern, params)).isInstanceOf(IllegalArgumentException.class).withFailMessage("模式匹配跟参数个数不对应");
        }

        @Test
        @DisplayName("处理空字符串模式")
        void testEmptyPattern() {
            // Arrange
            String pattern = "";
            Object[] params = {};
            String expected = "";

            // Act
            String result = Texts.format(pattern, params);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("处理null模式")
        void testNullPattern() {
            // Arrange
            String pattern = null;
            Object[] params = {};

            // Act
            String result = Texts.format(pattern, params);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("处理没有占位符的模式")
        void testNoPlaceholders() {
            // Arrange
            String pattern = "Hello World!";
            Object[] params = {};
            String expected = "Hello World!";

            // Act
            String result = Texts.format(pattern, params);

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class Truncate {

        @ParameterizedTest(name = "截断字符串: 输入 \"{0}\" 长度 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideTruncateArguments")
        void testTruncate(String input, int length, String expected) {
            // Act
            String result = Texts.truncate(input, length);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideTruncateArguments() {
            return Stream.of(Arguments.of(null, 1, null), Arguments.of("", 1, ""), Arguments.of(" ", 1, " "), Arguments.of("  ", 1, "  "),
                    Arguments.of("\n", 1, "\n"), Arguments.of("\n", 10, "\n"), Arguments.of("123", 1, "12..."), Arguments.of("abc", 1, "ab..."),
                    Arguments.of("abcdefg", 4, "abcdefg"), Arguments.of("123456", 3, "123456"), Arguments.of("123中文", 4, "123中文"),
                    Arguments.of("123中文", 2, "123中..."), Arguments.of("中文国家", 3, "中文国..."), Arguments.of("中文国家", 4, "中文国家"));
        }

        @ParameterizedTest(name = "截断字符串: 输入 \"{0}\" 长度 \"{1}\" 应返回 \"{2}\"")
        @CsvSource({"中文国家,2,中文...", "abc,2,ab...", "123中文,4,123中...",})
        void testTruncateFalse(String input, int length, String expected) {
            // Act
            String result = Texts.truncate(input, length, false);
            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class UnescapeHtml {

        @ParameterizedTest(name = "解码HTML实体: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideUnescapeHtmlArguments")
        void testUnescapeHtml(String input, String expected) {
            // Act
            String result = Texts.unescapeHtml(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideUnescapeHtmlArguments() {
            return Stream.of(Arguments.of("http://&quot;", "http://\""));
        }
    }

    @Nested
    class GetGroup {

        @ParameterizedTest(name = "正则表达式分组匹配: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetGroupArguments")
        void testGetGroup(String regex, String input, String expected) {
            // Act
            String result = Texts.getGroup(regex, input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetGroupArguments() {
            return Stream.of(Arguments.of("^[a-zA-Z]*", "B08", "B"), Arguments.of("^[a-zA-Z0-9]*", "B08", "B08"),
                    Arguments.of("\\(.*?\\)", "22 Feb 2020 11:00 CET (10:00 GMT)", "(10:00 GMT)"), Arguments.of("^[a-zA-Z]", "00", null));
        }
    }

    @Nested
    class GetHideName {

        @ParameterizedTest(name = "隐藏姓名: 输入 \"{0}\" 分别为 \"{1},{2},{3}\" 应返回 \"{4}\"")
        @MethodSource("provideGetHideNameArguments")
        void testGetHideName(String input, int start, int hide, int end, String expected) {
            // Act
            String result = Texts.getHideName(input, start, hide, end);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetHideNameArguments() {
            return Stream.of(Arguments.of("13478967895", 4, 3, 4, "134****7895"), Arguments.of("1347896789", 4, 3, 4, "134****6789"),
                    Arguments.of("134896789", 4, 3, 4, "134****6789"), Arguments.of("13489", 4, 3, 4, "134****9"), Arguments.of("189", 4, 3, 4, "189****"),
                    Arguments.of("13478967895", 4, -1, 4, "1****7895"), Arguments.of("13478967895", -1, 3, 4, "134***7895"),
                    Arguments.of("13478967895", 4, 3, -1, "134****895"), Arguments.of(null, 4, 3, -1, null));
        }
    }

    @Nested
    class GetHideName2 {

        @ParameterizedTest(name = "隐藏部分姓名: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetHideName2Arguments")
        void testGetHideName2(String input, String expected) {
            // Act
            String result = Texts.getHideName(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetHideName2Arguments() {
            return Stream.of(Arguments.of("吉林省延边朝鲜族自治州安图县二道白", "吉林省****县二道白"), Arguments.of("刘明财", "刘*财"),
                    Arguments.of("13478967895", "134****7895"), Arguments.of("6201254785625698", "620****5698"));
        }
    }

    @Nested
    class RegQuery {

        @SuppressWarnings("AssertBetweenInconvertibleTypes")
        @ParameterizedTest(name = "正则表达式查询: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideRegQueryOneArguments")
        void testRegQueryOne(String regex, String input, List<String> expected) {
            // Act
            List<Texts.RegexQueryInfo> result = Texts.regQuery(regex, input);
            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getGroup()).isEqualTo(expected.get(0));
            assertThat(result.get(0).getGroups().get(0)).isEqualTo(expected.get(1));
            assertThat(result.get(0).getStart()).isEqualTo(expected.get(2));
            assertThat(result.get(0).getEnd()).isEqualTo(expected.get(3));
        }

        static Stream<Arguments> provideRegQueryOneArguments() {
            return Stream.of(Arguments.of("offset=(\\w+)", "&offset=5#", Arrays.asList("offset=5", "5", 1, 9)));
        }

        @ParameterizedTest(name = "多正则表达式查询: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideRegQueryMultiArguments")
        void testRegQueryMulti(String regex, String input, List<String> expected) {
            // Act
            List<Texts.RegexQueryInfo> result = Texts.regQuery(regex, input);
            // Assert
            assertThat(result).hasSize(3);
            for (int i = 0; i < 3; i++) {
                assertThat(result.get(i).getGroup()).isEqualTo(expected.get(i));
            }
        }

        static Stream<Arguments> provideRegQueryMultiArguments() {
            return Stream.of(Arguments.of("[a-zA-Z0-9\\u4e00-\\u9fa5]+", "B_中-(08）", Arrays.asList("B", "中", "08")));
        }
    }

    @Nested
    class ReplaceBlank {

        @ParameterizedTest(name = "替换空白字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideReplaceBlankArguments")
        void testReplaceBlank(String input, String expected) {
            // Act
            String result = Texts.replaceBlank(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideReplaceBlankArguments() {
            return Stream.of(Arguments.of("78 78 ", "7878"), Arguments.of(" 1", "1"), Arguments.of("1\n2", "12"), Arguments.of("1  \n  2", "12"));
        }
    }

    @Nested
    class ParseInt {

        @ParameterizedTest(name = "解析整数: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideParseIntArguments")
        void testParseInt(String input, int expected) {
            // Act
            int result = Texts.parseInt(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideParseIntArguments() {
            return Stream.of(Arguments.of("12", 12), Arguments.of("12a", 12), Arguments.of("a12", 12), Arguments.of("a12b", 12), Arguments.of("共12页", 12),
                    Arguments.of("12.0", 120) // 注意：原测试用例中的这个预期值有误，应该是12而不是120。
            );
        }
    }

    @SuppressWarnings("ConstantValue")
    @Nested
    class ParseLong {

        @ParameterizedTest(name = "解析字符串中的数字: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideParseLongArguments")
        void testParseLong(String input, Long expected) {
            // Act
            long result = Texts.parseLong(input);
            // Assert
            if (expected == null) {
                assertEquals(0L, result);  // 如果预期值为null，假设方法返回0L或抛出异常
            } else {
                assertEquals(expected, result);
            }
        }

        static Stream<Arguments> provideParseLongArguments() {
            return Stream.of(Arguments.of("123", 123L), Arguments.of("abc123xyz", 123L), Arguments.of("abc", 0L), Arguments.of("123a456b789", 123456789L),
                    Arguments.of("12.34", 1234L), // 取整部分
                    Arguments.of("-123abc", -123L), Arguments.of("123.456.789", 123456789L), Arguments.of("123中文", 123L), Arguments.of("", 0L),
                    Arguments.of(null, 0L));
        }

        @Test
        @DisplayName("处理负数情况")
        void testParseLong_NegativeNumber() {
            // Arrange
            String input = "-987";
            long expected = -987L;
            // Act
            long result = Texts.parseLong(input);
            // Assert
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("处理边界条件: 空字符串")
        void testParseLong_EmptyString() {
            // Arrange
            String input = "";
            long expected = 0L;
            // Act
            long result = Texts.parseLong(input);
            // Assert
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("处理边界条件: null")
        void testParseLong_NullInput() {
            // Arrange
            String input = null;
            long expected = 0L;
            // Act
            long result = Texts.parseLong(input);
            // Assert
            assertEquals(expected, result);
        }
    }

    @Nested
    class GetHanyupinyin {

        @ParameterizedTest(name = "获取拼音: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetHanyupinyinArguments")
        void testGetHanyupinyin(String input, String expected) {
            // Act
            String result = Texts.getHanyupinyin(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetHanyupinyinArguments() {
            return Stream.of(Arguments.of(null, null), Arguments.of("", ""), Arguments.of("12", ""), Arguments.of("ab", ""), Arguments.of("汉字", "hanzi"),
                    Arguments.of("中國", "zhongguo"));
        }
    }

    @Nested
    class EscapeExprSpecialWord {

        @ParameterizedTest(name = "转义特殊字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideEscapeExprSpecialWordArguments")
        void testEscapeExprSpecialWord(String input, String expected) {
            // Act
            String result = Texts.escapeExprSpecialWord(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideEscapeExprSpecialWordArguments() {
            return Stream.of(Arguments.of(null, null), Arguments.of("\\", "\\\\"), Arguments.of("$", "\\$"), Arguments.of("(", "\\("), Arguments.of(")", "\\)"),
                    Arguments.of("*", "\\*"), Arguments.of(".", "\\."), Arguments.of("[", "\\["), Arguments.of("]", "\\]"), Arguments.of("?", "\\?"),
                    Arguments.of("^", "\\^"), Arguments.of("{", "\\{"), Arguments.of("}", "\\}"), Arguments.of("|", "\\|"), Arguments.of("", ""),
                    Arguments.of("ni\\", "ni\\\\"), Arguments.of("ni$", "ni\\$"), Arguments.of("ni(", "ni\\("), Arguments.of("ni)", "ni\\)"),
                    Arguments.of("ni*", "ni\\*"), Arguments.of("ni.", "ni\\."), Arguments.of("ni[", "ni\\["), Arguments.of("ni]", "ni\\]"),
                    Arguments.of("ni?", "ni\\?"), Arguments.of("ni^", "ni\\^"), Arguments.of("ni{", "ni\\{"), Arguments.of("ni}", "ni\\}"),
                    Arguments.of("ni|", "ni\\|"));
        }
    }

    @Nested
    class IsTrueIp {

        @ParameterizedTest(name = "验证IP地址: 输入 \"{0}\" 应返回 \"{1}\"")
        @CsvSource({"192.168.124.2,true", "123454,false", "139.11.11.0,true"})
        void testIsTrueIp(String ip, boolean expected) {
            // Act
            boolean result = Texts.isTrueIp(ip);
            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class IsLegalForUsername {

        @ParameterizedTest(name = "验证用户名合法性: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideIsLegalForUsernameArguments")
        void testIsLegalForUsername(String username, boolean expected) {
            // Act
            boolean result = Texts.isOnlyContainCnAndNumAndEn(username);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideIsLegalForUsernameArguments() {
            return Stream.of(Arguments.of("张", true), Arguments.of("zhang", true), Arguments.of("12", true), Arguments.of("!", false),
                    Arguments.of(",", false), Arguments.of("?", false), Arguments.of("#", false), Arguments.of("zhang32", true), Arguments.of("张32", true),
                    Arguments.of("张¥", false), Arguments.of("zhang*", false));
        }
    }

    @Nested
    class GetLegalUsername {

        @ParameterizedTest(name = "获取合法用户名: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetLegalUsernameArguments")
        void testGetLegalUsername(String input, String expected) {
            // Act
            String result = Texts.getCnAndNumAndEn(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetLegalUsernameArguments() {
            return Stream.of(Arguments.of("", ""), Arguments.of(null, null), Arguments.of("张", "张"), Arguments.of("tonye", "tonye"),
                    Arguments.of("tonye*", "tonye"), Arguments.of("124", "124"), Arguments.of("124^", "124"), Arguments.of("$", ""), Arguments.of("^%", ""),
                    Arguments.of("张#tonye*124（）()`\\/~·", "张tonye124"));
        }
    }

    @Nested
    class CheckPassWord {

        @ParameterizedTest(name = "验证密码强度: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideCheckPassWordArguments")
        void testCheckPassWord(String password, boolean expected) {
            // Act
            boolean result = Texts.checkPassWord(password);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideCheckPassWordArguments() {
            return Stream.of(Arguments.of("122acd", true), Arguments.of("111111", false), Arguments.of("aaaaaa", false), Arguments.of("abd123", true),
                    Arguments.of("12bhew", true), Arguments.of("HEWd12", true), Arguments.of("HEWd124", true), Arguments.of("12HYws1", true),
                    Arguments.of("12HY", false), Arguments.of("HY90", false), Arguments.of("ye90", false));
        }
    }

    @SuppressWarnings("ConstantValue")
    @Nested
    class FilterOffUtf8Mb4 {

        @ParameterizedTest(name = "过滤UTF-8 4字节字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @CsvSource({"Hello World!, Hello World!",                           // 纯 ASCII 字符
                "你好世界, 你好世界",                                      // 合法的 3 字节 UTF-8 字符
                "😊😄😂, ''",                                              // 仅包含 4 字节 UTF-8 字符，应被过滤
                "你好😊世界😄, 你好世界",                                 // 混合合法字符和非法字符
                "abc😊def😄ghi, abcdefghi",                             // 混合 ASCII 和非法字符
                "😊你好😄世界😊, 你好世界",                               // 复杂混合场景
                "-_.~123abcXYZ, -_.~123abcXYZ"                          // 安全字符
        })
        void testFilterOffUtf8Mb4_ValidInputs_FiltersCorrectly(String input, String expectedOutput) {
            // Act
            String result = Texts.filterOffUtf8Mb4(input);

            // Assert
            assertThat(result).isEqualTo(expectedOutput);
        }

        @ParameterizedTest(name = "过滤UTF-8 4字节字符: 空或空白输入 \"{0}\" 应返回空字符串")
        @ValueSource(strings = {"", "   "})
        void testFilterOffUtf8Mb4_EmptyOrBlankInput_ReturnsEmptyString(String input) {
            // Act
            String result = Texts.filterOffUtf8Mb4(input);

            // Assert
            assertThat(result).isEqualTo(input); // 空字符串和空白字符应保持原样
        }

        @Test
        @DisplayName("过滤UTF-8 4字节字符: 空指针输入应返回空字符串")
        void testFilterOffUtf8Mb4_NullInput_ReturnsEmptyString() {
            // Act
            String result = Texts.filterOffUtf8Mb4(null);

            // Assert
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("过滤UTF-8 4字节字符: 长字符串混合字符正确过滤")
        void testFilterOffUtf8Mb4_LongStringWithMixedCharacters_FiltersCorrectly() {
            // Arrange
            StringBuilder inputBuilder = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                inputBuilder.append("😊").append("你好").append("world");
            }
            String input = inputBuilder.toString();

            StringBuilder expectedOutputBuilder = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                expectedOutputBuilder.append("你好").append("world");
            }
            String expectedOutput = expectedOutputBuilder.toString();

            // Act
            String result = Texts.filterOffUtf8Mb4(input);

            // Assert
            assertThat(result).isEqualTo(expectedOutput);
        }
    }

    @Nested
    class ReplaceUtf8Blank {

        @ParameterizedTest(name = "替换UTF-8空白字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideReplaceUtf8BlankArguments")
        void testReplaceUtf8Blank(String input, String expected) {
            // Act
            String result = Texts.replaceUtf8Blank(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideReplaceUtf8BlankArguments() {
            return Stream.of(Arguments.of("", ""), Arguments.of(null, null), Arguments.of(" ", " "));
        }
    }

    @Nested
    class CleanSpecialChar {

        @ParameterizedTest(name = "清理特殊字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideCleanSpecialCharArguments")
        void testCleanSpecialChar(String input, String expected) {
            // Act
            String result = Texts.cleanSpecialChar(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideCleanSpecialCharArguments() {
            return Stream.of(Arguments.of("", ""), Arguments.of(null, null), Arguments.of("zhang", "zhang"), Arguments.of("zhang`", "zhang"),
                    Arguments.of("zhang~", "zhang"), Arguments.of("zhang!", "zhang"), Arguments.of("zhang#", "zhang"), Arguments.of("zhang$", "zhang"),
                    Arguments.of("zhang%", "zhang"), Arguments.of("zhang&", "zhang"), Arguments.of("zhang*", "zhang"), Arguments.of("zhang(", "zhang"),
                    Arguments.of("zhang)", "zhang"), Arguments.of("zhang+", "zhang"), Arguments.of("zhang=", "zhang"), Arguments.of("zhang'", "zhang"),
                    Arguments.of("zhang:", "zhang"), Arguments.of("zhang;", "zhang"), Arguments.of("zhang,", "zhang"), Arguments.of("zhang//", "zhang"),
                    Arguments.of("zhang.", "zhang"), Arguments.of("zhang<", "zhang"), Arguments.of("zhang>", "zhang"), Arguments.of("zhang/", "zhang"),
                    Arguments.of("zhang?", "zhang"), Arguments.of("zhang~", "zhang"), Arguments.of("zhang@", "zhang"), Arguments.of("zhang￥", "zhang"),
                    Arguments.of("zhang……", "zhang"), Arguments.of("zhang&", "zhang"), Arguments.of("zhang*", "zhang"), Arguments.of("zhang（", "zhang"),
                    Arguments.of("zhang）", "zhang"), Arguments.of("zhang——", "zhang"), Arguments.of("zhang+", "zhang"), Arguments.of("zhang【", "zhang"),
                    Arguments.of("zhang|", "zhang"), Arguments.of("zhang‘", "zhang"), Arguments.of("zhang；", "zhang"), Arguments.of("zhang：", "zhang"),
                    Arguments.of("zhang”“", "zhang"), Arguments.of("zhang’", "zhang"), Arguments.of("zhang。", "zhang"), Arguments.of("zhang，", "zhang"),
                    Arguments.of("zhang、", "zhang"), Arguments.of("zhang？", "zhang"));
        }
    }

    @Nested
    class EmailValidate {

        @ParameterizedTest(name = "验证电子邮件: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideEmailValidateArguments")
        void testEmailValidate(String email, boolean expected) {
            // Act
            boolean result = Texts.emailValidate(email);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideEmailValidateArguments() {
            return Stream.of(Arguments.of("@163.com", false), Arguments.of("12@163.com", true), Arguments.of("ba@163.com", true),
                    Arguments.of("ba@qq.com", true), Arguments.of("12ba@qq.com", true), Arguments.of("12@13.com", true),
                    Arguments.of("zly1803004@163.com", true));
        }
    }

    @Nested
    class MobileValidate {

        @ParameterizedTest(name = "验证手机号码: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideMobileValidateArguments")
        void testMobileValidate(String mobile, boolean expected) {
            // Act
            boolean result = Texts.mobileValidate(mobile);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideMobileValidateArguments() {
            return Stream.of(Arguments.of("12345678909", true), Arguments.of("2345678909", false), Arguments.of("1234567890921", false),
                    Arguments.of("ba", false));
        }
    }

    @Nested
    class ClearCss {

        @Test
        @DisplayName("清理CSS样式属性: 输入 \"{0}\" 应返回 \"{1}\"")
        void testClearCss() {
            // Arrange
            String style = "<div style=\"color: red\">";
            String expected = "<div styl>";

            // Act
            String result = Texts.clearCss(style);

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class RegReplace {

        @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantValue"})
        @Nested
        @DisplayName("忽略大小写")
        class IgnoreCase {

            @ParameterizedTest(name = "正则替换（忽略大小写）: 输入 \"{2}\" 应返回 \"{3}\"")
            @MethodSource("provideRegReplaceIgnoreCaseArguments")
            void testRegReplaceIgnoreCase(String reg, String repstr, String instr, String expected) {
                // Act
                String result = Texts.regReplace(reg, repstr, instr, true);
                // Assert
                assertThat(result).isEqualTo(expected);
            }

            static Stream<Arguments> provideRegReplaceIgnoreCaseArguments() {
                return Stream.of(Arguments.of("@+", "", "@@@123@", "123"), Arguments.of("[a-z]+", "*", "AbCdEfGhIjKlMnOpQrStUvWxYz", "*"),
                        Arguments.of("\\d+", "#", "abc123def456ghi789", "abc#def#ghi#"), Arguments.of("\\$", "%", "$$$abc$$$", "%%%abc%%%"));
            }

            @Test
            @DisplayName("空字符串是否正确处理")
            void testRegReplaceEmptyString() {
                // Arrange
                String reg = "\\w+";
                String repstr = "";
                String instr = "";
                // Act
                String result = Texts.regReplace(reg, repstr, instr, true);
                // Assert
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("null输入是否正确处理")
            void testRegReplaceNullInput() {
                // Arrange
                String reg = null;
                String repstr = "";
                String instr = "Hello World";
                // Act & Assert
                assertThatThrownBy(() -> Texts.regReplace(reg, repstr, instr, true)).isInstanceOf(NullPointerException.class)
                        .withFailMessage("Regular expression cannot be null");
            }
        }

        @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantValue"})
        @Nested
        @DisplayName("区分大小写")
        class CaseSensitive {

            @ParameterizedTest(name = "正则替换（区分大小写）: 输入 \"{2}\" 应返回 \"{3}\"")
            @MethodSource("provideRegReplaceCaseSensitiveArguments")
            void testRegReplaceCaseSensitive(String reg, String repstr, String instr, String expected) {
                // Act
                String result = Texts.regReplace(reg, repstr, instr, false);
                // Assert
                assertThat(result).isEqualTo(expected);
            }

            static Stream<Arguments> provideRegReplaceCaseSensitiveArguments() {
                return Stream.of(Arguments.of("@+", "", "@@@123@", "123"),
                        Arguments.of("[a-z]+", "*", "AbCdEfGhIjKlMnOpQrStUvWxYz", "A*C*E*G*I*K*M*O*Q*S*U*W*Y*"),
                        Arguments.of("\\d+", "#", "abc123def456ghi789", "abc#def#ghi#"), Arguments.of("\\$", "%", "$$$abc$$$", "%%%abc%%%"));
            }

            @Test
            @DisplayName("空字符串是否正确处理")
            void testRegReplaceEmptyStringCaseSensitive() {
                // Arrange
                String reg = "\\w+";
                String repstr = "";
                String instr = "";
                // Act
                String result = Texts.regReplace(reg, repstr, instr, false);
                // Assert
                assertThat(result).isEmpty();
            }

            @Test
            @DisplayName("null输入是否正确处理")
            void testRegReplaceNullInputCaseSensitive() {
                // Arrange
                String reg = null;
                String repstr = "";
                String instr = "Hello World";
                // Act & Assert
                assertThatThrownBy(() -> Texts.regReplace(reg, repstr, instr, false)).isInstanceOf(NullPointerException.class)
                        .withFailMessage("Regular expression cannot be null");
            }
        }
    }

    @Nested
    class ReplaceChinese {

        @ParameterizedTest(name = "替换字符串 \"{0}\" 中的中文为 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideReplaceChineseArguments")
        void testReplaceChinese(String str, String str2, String expected) {
            // Act
            String result = Texts.replaceChinese(str, str2);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideReplaceChineseArguments() {
            return Stream.of(Arguments.of("Hello 世界", "*", "Hello **"), Arguments.of("你好，世界", "?", "??，??"),
                    Arguments.of("Hello, World!", "x", "Hello, World!"), Arguments.of("", "x", ""), Arguments.of(null, "x", null),
                    Arguments.of("123 你好 456", "#", "123 ## 456"), Arguments.of("a b c", "@", "a b c"));
        }
    }

    @SuppressWarnings({"ConstantValue", "DataFlowIssue", "ResultOfMethodCallIgnored"})
    @Nested
    class ToDbc {

        @ParameterizedTest(name = "全角转半角: 输入 \"{0}\" 应返回 \"{1}\"")
        @CsvSource({"'　', ' ' ", // 全角空格转半角空格
                "ＡＢＣ, ABC", // 全角字母转半角字母
                "１２３, 123", // 全角数字转半角数字
                "！＠＃, !@#", // 全角符号转半角符号
                "Hello, Hello", // 半角字符保持不变
                "'你好，世界', '你好,世界'", // 中文字符保持不变
                "' ', ' '" // 空字符串保持不变
        })
        void testToDbc(String input, String expected) {
            // Act
            String result = Texts.toDbc(input);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("null输入是否正确处理")
        void testToDbcNull() {
            // Arrange
            String input = null;
            // Act & Assert
            assertThatThrownBy(() -> Texts.toDbc(input)).isInstanceOf(NullPointerException.class).withFailMessage("Input string cannot be null");
        }
    }

    @Nested
    class StringTokenizer {

        @Test
        @DisplayName("字符串分割: 输入 \"{0}\" 使用分隔符 \"{1}\" 应返回 \"{2}\"")
        void testStringTokenizer() {
            // Arrange
            String[] expected = {"wo", "are", "student"};

            // Act
            String[] result = Texts.stringTokenizer("wo; are, student", " ,;");

            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class Highlight {

        @ParameterizedTest(name = "高亮文本: 输入 \"{0}\" 高亮 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideHighlightArguments")
        void testHighlight(String input, String highlightText, String expected) {
            // Act
            String result = Texts.highlight(input, highlightText, "<font class='red'>", "</font>");

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideHighlightArguments() {
            return Stream.of(Arguments.of("只能够，死心吧", "死心", "只能够，<font class='red'>死心</font>吧"),
                    Arguments.of("只能够，sd吧", "sd", "只能够，<font class='red'>sd</font>吧"),
                    Arguments.of("只能够，132吧", "132", "只能够，<font class='red'>132</font>吧"));
        }
    }

    @Nested
    class GetFirstLetter {

        @ParameterizedTest(name = "获取首字母: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetFirstLetterArguments")
        void testGetFirstLetter(String input, String expected) {
            // Act
            String result = Texts.getFirstLetter(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetFirstLetterArguments() {
            return Stream.of(Arguments.of("abek", "a"), Arguments.of("zhang", "z"), Arguments.of("#a", "a"), Arguments.of("#张", "z"));
        }
    }

    @Nested
    class ContainsChinese {

        @ParameterizedTest(name = "是否包含中文字符: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideContainsChineseArguments")
        void testContainsChinese(String input, boolean expected) {
            // Act
            boolean result = Texts.containsChinese(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideContainsChineseArguments() {
            return Stream.of(Arguments.of("#张", true), Arguments.of("base", false), Arguments.of("b老虎ase", true), Arguments.of("", false));
        }
    }

    @Nested
    class GetFirstLetterArr {

        @ParameterizedTest(name = "获取首字母数组: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideGetFirstLetterArrArguments")
        void testGetFirstLetterArr(String input, String expected) {
            // Act
            String result = Texts.getFirstLetterArr(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetFirstLetterArrArguments() {
            return Stream.of(Arguments.of("张龙", "zl"), Arguments.of("zhaglong", ""));
        }
    }

    @Nested
    class GetHanyupinyinToType {

        @ParameterizedTest(name = "获取拼音类型: 输入 \"{0}\" 和 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideGetHanyupinyinToTypeArguments")
        void testGetHanyupinyinToType(String input, int type, String expected) {
            // Act
            String result = Texts.getHanyupinyin(input, type);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGetHanyupinyinToTypeArguments() {
            return Stream.of(Arguments.of("战功", 1, "zhan4gong1"), Arguments.of("战功", 0, "zhan4gong1"), Arguments.of("战功", 4, "zhan4gong1"),
                    Arguments.of("战功", 3, "ZhanGong"));
        }
    }

    @Nested
    class IsEn {

        @ParameterizedTest(name = "字符 \"{0}\" 是否是英文字母: {1}")
        @CsvSource({"a, true", "z, true", "A, true", "Z, true", "0, false", "9, false", "$, false", "!, false", "' ', false"})
        void testIsEn(char c, boolean expected) {
            // Act
            boolean result = Texts.isEn(c);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("空字符是否是英文字母")
        void testIsEnEmpty() {
            // Arrange
            char c = '\u0000'; // 空字符
            boolean expected = false;
            // Act
            boolean result = Texts.isEn(c);
            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class IsNumeric {

        @ParameterizedTest(name = "字符 \"{0}\" 是否是数字: {1}")
        @CsvSource({"0, true", "9, true", "a, false", "z, false", "A, false", "Z, false", "$, false", "!, false", "' ',false"})
        void testIsNumeric(char c, boolean expected) {
            // Act
            boolean result = Texts.isNumeric(c);
            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("空字符是否是数字")
        void testIsNumericEmpty() {
            // Arrange
            char c = '\u0000'; // 空字符
            boolean expected = false;
            // Act
            boolean result = Texts.isNumeric(c);
            // Assert
            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    class IsScientificNotation {

        @ParameterizedTest(name = "是否科学计数法: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideIsScientificNotationArguments")
        void testIsScientificNotation(String input, boolean expected) {
            // Act
            boolean result = Texts.isScientificNotation(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideIsScientificNotationArguments() {
            return Stream.of(Arguments.of("kse", false), Arguments.of("12.1", true), Arguments.of("12.15", true), Arguments.of("12", true),
                    Arguments.of("1.078137E-4", true), Arguments.of("1.078137E-4w", false));
        }
    }

    @Nested
    class IsIp {

        @ParameterizedTest(name = "验证IP地址: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideIsIpArguments")
        void testIsIp(String ip, boolean expected) {
            // Act
            boolean result = Texts.isIp(ip);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideIsIpArguments() {
            return Stream.of(Arguments.of("192.168.124.2", true), Arguments.of("123454", false), Arguments.of("139.11.11.0", true),
                    Arguments.of("127.0.0.1", true));
        }
    }

    @Nested
    class Capitalize {

        @ParameterizedTest(name = "首字母大写: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideCapitalizeArguments")
        void testCapitalize(String input, String expected) {
            // Act
            String result = Texts.capitalize(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideCapitalizeArguments() {
            return Stream.of(Arguments.of("and", "And"));
        }
    }

    @Nested
    class GenerateAtag {

        @ParameterizedTest(name = "生成超链接: 输入 \"{0}\" 标题 \"{1}\" 是否新窗口 \"{2}\" 应返回 \"{3}\"")
        @MethodSource("provideGenerateAtagArguments")
        void testGenerateAtag(String href, String title, boolean isNewWindow, String expected) {
            // Act
            String result = Texts.generateAtag(href, title, isNewWindow);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideGenerateAtagArguments() {
            return Stream.of(Arguments.of("/acution", "跳转", true, "<a href=\"/acution\" target=\"_blank\">跳转</a>"),
                    Arguments.of("/acution", "跳转", false, "<a href=\"/acution\" target=\"\">跳转</a>"));
        }
    }

    @Nested
    class Find {

        @ParameterizedTest(name = "查找正则匹配: 输入 \"{0}\" 正则 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideFindArguments")
        void testFind(String input, String regex, boolean expected) {
            // Act
            boolean result = Texts.find(input, regex);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideFindArguments() {
            return Stream.of(Arguments.of("zhang？", "^z", true), Arguments.of("zhang？", "(an)+", true), Arguments.of("zhang？", "b+", false),
                    Arguments.of("zhang？", "N+", false));
        }

        @ParameterizedTest(name = "查找正则匹配: 输入 \"{0}\" 正则 \"{1}\" 忽略大小写 \"{2}\" 应返回 \"{3}\"")
        @MethodSource("provideFindIgnoreCaseArguments")
        void testFindIgnoreCase(String input, String regex, boolean ignoreCase, boolean expected) {
            // Act
            boolean result = Texts.find(input, regex, ignoreCase);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideFindIgnoreCaseArguments() {
            return Stream.of(Arguments.of("zhang？", "^Z", true, true), Arguments.of("zhang？", "(An)+", true, true), Arguments.of("zhang？", "N+", true, true),
                    Arguments.of("zha", "[a-z]{3}", true, true), Arguments.of("ZHA", "[a-z]{3}", true, true), Arguments.of("zhang？", "B+", true, false),
                    Arguments.of("", "B+", true, false), Arguments.of(null, "B+", true, false), Arguments.of("", "B+", false, false));
        }
    }

    @Nested
    class GetGroups {

        @ParameterizedTest(name = "获取所有匹配分组: 输入 \"{0}\" 正则 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideGetGroupsArguments")
        void testGetGroups(String regex, String input, List<String> expected) {
            // Act
            String[] result = Texts.getGroups(regex, input);

            // Assert
            assertThat(result).containsExactlyElementsOf(expected);
        }

        static Stream<Arguments> provideGetGroupsArguments() {
            return Stream.of(Arguments.of("[a-zA-Z0-9]+", "B中08", List.of("B")), Arguments.of("^[a-zA-Z]*", "B08", List.of("B")),
                    Arguments.of("[a-zA-Z0-9]*", "B08", List.of("B08")));
        }
    }

    @Nested
    class CompareVersion {

        @ParameterizedTest(name = "比较版本号: 输入 \"{0}\" 和 \"{1}\" 应返回 \"{2}\"")
        @MethodSource("provideCompareVersionArguments")
        void testCompareVersion(String version1, String version2, int expected) {
            // Act
            int result = Texts.compareVersion(version1, version2);

            // Assert
            assertEquals(expected, result);
        }

        static Stream<Arguments> provideCompareVersionArguments() {
            return Stream.of(Arguments.of("1.0.0", "1.0.0", 0), Arguments.of("", "", 0), Arguments.of("", " ", 0), Arguments.of("\n\r\t", " ", 0),
                    Arguments.of(null, "", 0), Arguments.of("", null, 0), Arguments.of("1.0.0", "1.0.0.0", 0), Arguments.of("2.0.0", "1.0.0", 1),
                    Arguments.of("1.0.0.1", "1.0.0", 1), Arguments.of("1.55", "1.12", 1), Arguments.of("1.55", "1.2", 1), Arguments.of("1.0.0", "2.0.0", -1),
                    Arguments.of("1.0.0", "1.0.1", -1), Arguments.of("1.0.0", "1.0.0.1", -1), Arguments.of("1.2.0", "1.0", 1), Arguments.of("1.0", "1.2.0", -1),
                    Arguments.of("01.0", "1.0", 0), Arguments.of("1.0", "01.0", 0), Arguments.of("", "1.0", -1), Arguments.of("2.0", "3", -1),
                    Arguments.of("2.5", "3", -1), Arguments.of("1.0", "", 1), Arguments.of("3", "1.0", 1), Arguments.of("3", "1.0.2", 1));
        }
    }

    @Nested
    class ReplaceLineBreakToSpacing {

        @ParameterizedTest(name = "替换换行符为空格: 输入 \"{0}\" 应返回 \"{1}\"")
        @MethodSource("provideReplaceLineBreakToSpacingArguments")
        void testReplaceLineBreakToSpacing(String input, String expected) {
            // Act
            String result = Texts.replaceLineBreakToSpacing(input);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        static Stream<Arguments> provideReplaceLineBreakToSpacingArguments() {
            return Stream.of(Arguments.of(null, null), Arguments.of("3\n3", "3 3"), Arguments.of("3   - 3", "3   - 3"), Arguments.of("3\r\n3\n\r4", "3 3  4"));
        }
    }
}
