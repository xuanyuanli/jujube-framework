package cn.xuanyuanli.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 中国数字
 *
 * @author John Li
 * @date 2021/12/18
 */
@Slf4j
public class ChineseNumbers {

    /**
     * 数字
     */
    private static final String[] DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    /**
     * 数字map
     */
    private static final Map<Character, Integer> DIGITS_MAP = new HashMap<>();
    /**
     * 数字模式
     */
    private static final Pattern DIGITS_PATTERN;
    /**
     * 英语十进制模式
     */
    private static final Pattern ENGLISH_DECIMAL_PATTERN = Pattern.compile("([0-9]*)\\.([0-9]+)");
    /**
     * 英语分数模式
     */
    private static final Pattern ENGLISH_FRACTION_PATTERN = Pattern.compile("([0-9]*)/([0-9]+)");

    /**
     * 前湾数字
     */
    private static final String[] BEFORE_WAN_DIGITS = {"十", "百", "千"};

    /**
     * 后湾数字
     */
    private static final String[] AFTER_WAN_DIGITS = {"", "万", "亿", "兆", "京"};

    /**
     * -
     */
    private static final String MINUS = "负";
    /**
     * 小数
     */
    private static final String DECIMAL = "点";
    /**
     * 分数
     */
    private static final String FRACTION = "分之";

    static {
        DIGITS_MAP.put('0', 0);
        DIGITS_MAP.put('1', 1);
        DIGITS_MAP.put('2', 2);
        DIGITS_MAP.put('3', 3);
        DIGITS_MAP.put('4', 4);
        DIGITS_MAP.put('5', 5);
        DIGITS_MAP.put('6', 6);
        DIGITS_MAP.put('7', 7);
        DIGITS_MAP.put('8', 8);
        DIGITS_MAP.put('9', 9);
        DIGITS_MAP.put('〇', 0);
        DIGITS_MAP.put('一', 1);
        DIGITS_MAP.put('七', 7);
        DIGITS_MAP.put('三', 3);
        DIGITS_MAP.put('两', 2);
        DIGITS_MAP.put('九', 9);
        DIGITS_MAP.put('二', 2);
        DIGITS_MAP.put('五', 5);
        DIGITS_MAP.put('伍', 5);
        DIGITS_MAP.put('兩', 2);
        DIGITS_MAP.put('八', 8);
        DIGITS_MAP.put('六', 6);
        DIGITS_MAP.put('叁', 3);
        DIGITS_MAP.put('參', 3);
        DIGITS_MAP.put('叄', 3);
        DIGITS_MAP.put('四', 4);
        DIGITS_MAP.put('壹', 1);
        DIGITS_MAP.put('捌', 8);
        DIGITS_MAP.put('柒', 7);
        DIGITS_MAP.put('玖', 9);
        DIGITS_MAP.put('肆', 4);
        DIGITS_MAP.put('貳', 2);
        DIGITS_MAP.put('贰', 2);
        DIGITS_MAP.put('陆', 6);
        DIGITS_MAP.put('陸', 6);
        DIGITS_MAP.put('零', 0);
        DIGITS_MAP.put('０', 0);
        DIGITS_MAP.put('１', 1);
        DIGITS_MAP.put('２', 2);
        DIGITS_MAP.put('３', 3);
        DIGITS_MAP.put('４', 4);
        DIGITS_MAP.put('５', 5);
        DIGITS_MAP.put('６', 6);
        DIGITS_MAP.put('７', 7);
        DIGITS_MAP.put('８', 8);
        DIGITS_MAP.put('９', 9);
        String join = DIGITS_MAP.keySet().stream().map(String::valueOf).collect(Collectors.joining(""));
        String pattern = String.format("^[%s]+$", join);
        DIGITS_PATTERN = Pattern.compile(pattern);
    }

    /**
     * 将英文表示的数字转化为中文表示的数字，支持负数、小数、不支持分数
     *
     * @param text 英文表示的数字，比如：2009000，5.3，-5.3
     * @return {@link String}
     */
    public static String englishNumberToChinese(String text) {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("empty input");
        }
        boolean negative = false;
        if (text.length() == 1 && text.charAt(0) == '0') {
            return DIGITS[0];
        }
        if (text.charAt(0) == '-') {
            negative = true;
            text = text.substring(1);
        }
        Matcher m = ENGLISH_DECIMAL_PATTERN.matcher(text);
        String result;
        if (m.find()) {
            result = englishNumberToChineseFull(m.group(1)) + DECIMAL + englishNumberToChineseBrief(m.group(2));
        } else {
            m = ENGLISH_FRACTION_PATTERN.matcher(text);
            if (m.find()) {
                result = englishNumberToChineseFull(m.group(2)) + FRACTION + englishNumberToChineseFull(m.group(1));
            } else {
                result = englishNumberToChineseFull(text);
            }
        }

        if (negative) {
            result = MINUS + result;
        }
        return result;
    }

    /**
     * 直接映射英文数字为中文数字，对应输入的小数部分如此处理
     *
     * @param text 文本
     * @return {@link String}
     */
    private static String englishNumberToChineseBrief(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(DIGITS[text.charAt(i) - '0']);
        }
        return result.toString();
    }

    /**
     * 非输入的小数部分需要做更复杂的转换
     *
     * @param text 文本
     * @return {@link String}
     */
    private static String englishNumberToChineseFull(String text) {
        int power = 0;
        boolean canAddZero = false;
        boolean inZero = false;
        Map<Integer, Integer> powers = new HashMap<>(16);
        long number = Long.parseLong(text);
        while (Math.pow(10, power) <= number) {
            int value = (int) ((number % (Math.pow(10, power + 1))) / (Math.pow(10, power)));
            powers.put(power, value);
            number -= (long) (number % (Math.pow(10, power + 1)));
            power++;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < power; i++) {
            if (i % 4 == 0) {
                if (powers.get(i) != 0) {
                    inZero = false;
                    canAddZero = true;
                    result.insert(0, DIGITS[powers.get(i)] + AFTER_WAN_DIGITS[i / 4]);
                } else {
                    if (((i + 3 < power) && powers.get(i + 3) != 0) ||
                            ((i + 2 < power) && powers.get(i + 2) != 0) ||
                            ((i + 1 < power) && powers.get(i + 1) != 0)) {
                        result.insert(0, AFTER_WAN_DIGITS[i / 4]);
                        canAddZero = false;
                    }
                }
            } else {
                if (powers.get(i) != 0) {
                    inZero = false;
                    canAddZero = true;
                    if (power == 2 && powers.get(i) == 1) {
                        result.insert(0, BEFORE_WAN_DIGITS[(i % 4) - 1]);
                    } else {
                        result.insert(0, DIGITS[powers.get(i)] + BEFORE_WAN_DIGITS[(i % 4) - 1]);
                    }
                } else {
                    if (canAddZero && !inZero) {
                        inZero = true;
                        result.insert(0, DIGITS[powers.get(i)]);
                    }
                }
            }
        }
        return result.toString();
    }

    /**
     * 中国许多英语
     *
     * @param text 输入中文数字，支持正负数、小数、分数，比如：五千四百九十一万四千七百一十
     * @return 转化为double的结果
     */
    public static double chineseNumberToEnglish(String text) {
        if (StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("empty input");
        }
        double result;
        if (text.contains(FRACTION)) {
            int idx = text.indexOf(FRACTION);
            result = chineseToEnglishFull(text.substring(idx + 2)) / chineseToEnglishFull(text.substring(0, idx));
        } else if (text.length() > 1) {
            if (DIGITS_PATTERN.matcher(text).find()) {
                result = chineseToEnglishBrief(text);
            } else {
                result = chineseToEnglishFull(text);
            }
        } else {
            result = chineseToEnglishFull(text);
        }
        return result;
    }

    /**
     * 输入如果完全匹配中文对应的数字，直接调用此函数映射输出
     *
     * @param text 文本
     * @return long
     */
    private static long chineseToEnglishBrief(String text) {
        char[] chars = text.toCharArray();
        long total = 0;
        for (char aChar : chars) {
            total *= 10;
            total += DIGITS_MAP.get(aChar);
        }
        return total;
    }

    /**
     * 对应复杂的中文数字串
     *
     * @param text 文本
     * @return double
     */
    private static double chineseToEnglishFull(String text) {
        text = text.replace("万亿", "兆");
        text = text.replace("萬億", "兆");
        text = text.replace("亿万", "兆");
        text = text.replace("億萬", "兆");
        text = text.replace("個", "");
        text = text.replace("个", "");
        text = text.replace("廿", "二十");
        text = text.replace("卄", "二十");
        text = text.replace("卅", "三十");
        text = text.replace("卌", "四十");
        double total = 0;
        double levelTotal = 0;
        long digitVal;
        boolean negative = false;
        int power = 0;
        boolean afterDecimal = false;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            // 负数，跳过首字母，记录标记位，剩下部分转化为英文数字
            if (i == 0 && (c == '负' || c == '負' || c == '-')) {
                negative = true;
                // 序数，跳过首字母，后面部分转化成英文数字
            } else //noinspection StatementWithEmptyBody
                if (i == 0 && c == '第') {
                // 忽略
            } else if (c == '點' || c == '点' || c == '.' || c == '．') {
                afterDecimal = true;
                // 小数点之后的第一位是10^-1，第二位是10^-2，以此类推
                power = -1;
                // 遇到一个level层级兆，levelTotal清0
            } else if (c == '兆') {
                power = 12;
                if (levelTotal == 0) {
                    levelTotal = 1;
                }
                total += levelTotal * Math.pow(10, power);
                levelTotal = 0;
                power -= 4;
            } else if (c == '亿' || c == '億') {
                power = 8;
                if (levelTotal == 0) {
                    levelTotal = 1;
                }
                total += levelTotal * Math.pow(10, power);
                levelTotal = 0;
                power -= 4;
                // 遇到一个level层级万，levelTotal清0
            } else if (c == '万' || c == '萬') {
                power = 4;
                if (levelTotal == 0) {
                    levelTotal = 1;
                }
                total += levelTotal * Math.pow(10, power);
                levelTotal = 0;
                power -= 4;
                // 千五,百五这种
            } else if (c == '千' || c == '仟') {
                levelTotal += 1000;
            } else if (c == '百' || c == '佰') {
                levelTotal += 100;
                // 十四万零九百
            } else if (c == '十' || c == '拾') {
                levelTotal += 10;
            } else if (c == '零' || c == '〇' || c == '0' || c == '０') {
                power = 0;
            } else if (DIGITS_MAP.containsKey(c)) {
                digitVal = DIGITS_MAP.get(c);
                // 小数点后面应该都是可以直接转化为数字的中文数字
                if (afterDecimal) {
                    levelTotal += digitVal * Math.pow(10, power);
                    power--;
                    while (i + 1 < chars.length && DIGITS_MAP.containsKey(chars[i + 1])) {
                        levelTotal += DIGITS_MAP.get(chars[i + 1]) * Math.pow(10, power);
                        power--;
                        i++;
                    }
                    // 对于 x十， x百， x千这些，x只可能是一位数字，所以可以直接转换
                } else if (i + 1 < chars.length) {
                    char nextChar = chars[i + 1];
                    if (nextChar == '十' || nextChar == '拾') {
                        levelTotal += digitVal * 10;
                        i++;
                    } else if (nextChar == '百' || nextChar == '佰') {
                        levelTotal += digitVal * 100;
                        i++;
                    } else if (nextChar == '千' || nextChar == '仟') {
                        levelTotal += digitVal * 1000;
                        i++;
                    } else if (DIGITS_MAP.containsKey(nextChar)) {
                        levelTotal *= 10;
                        levelTotal += digitVal;
                        while (i + 1 < chars.length && DIGITS_MAP.containsKey(chars[i + 1])) {
                            levelTotal *= 10;
                            levelTotal += DIGITS_MAP.get(chars[i + 1]);
                            i++;
                        }
                    } else {
                        levelTotal += digitVal;
                    }
                } else {
                    //处理这样的数字串的最后一位：三百五，五千三
                    if (i > 0) {
                        char prevChar = chars[i - 1];
                        if (prevChar == '兆') {
                            levelTotal += digitVal * Math.pow(10, 11);
                        } else if (prevChar == '亿' || prevChar == '億') {
                            levelTotal += digitVal * Math.pow(10, 7);
                        } else if (prevChar == '萬' || prevChar == '万') {
                            levelTotal += digitVal * 1000;
                        } else if (prevChar == '千' || prevChar == '仟') {
                            levelTotal += digitVal * 100;
                        } else if (prevChar == '百' || prevChar == '佰') {
                            levelTotal += digitVal * 10;
                        } else {
                            levelTotal += digitVal;
                        }
                    } else {
                        levelTotal += digitVal;
                    }
                }
            } else {
                throw new IllegalArgumentException("bad input:" + text);
            }
        }
        total += levelTotal;
        if (negative) {
            total = -total;
        }
        return total;
    }

}
