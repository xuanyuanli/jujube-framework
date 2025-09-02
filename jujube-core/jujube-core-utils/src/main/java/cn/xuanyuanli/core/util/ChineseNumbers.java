package cn.xuanyuanli.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 中文数字转换工具类
 * <p>
 * 提供阿拉伯数字与中文数字之间的相互转换功能，支持：
 * <ul>
 *     <li>正负数转换</li>
 *     <li>小数转换</li>
 *     <li>分数转换</li>
 *     <li>繁体中文数字</li>
 *     <li>全角数字</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>
 * // 阿拉伯数字转中文
 * String chinese = ChineseNumbers.englishNumberToChinese("12345");  // 一万二千三百四十五
 * 
 * // 中文数字转阿拉伯数字
 * double number = ChineseNumbers.chineseNumberToEnglish("一万二千三百四十五");  // 12345.0
 * </pre>
 *
 * @author xuanyuanli
 * @date 2021/12/18
 */
@Slf4j
public class ChineseNumbers {

    /**
     * 中文数字字符数组，索引对应阿拉伯数字0-9
     */
    private static final String[] DIGITS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    
    /**
     * 数字字符映射表，支持阿拉伯数字、中文数字（简体、繁体）、全角数字等多种格式
     */
    private static final Map<Character, Integer> DIGITS_MAP = new HashMap<>();
    
    /**
     * 纯数字字符模式，用于判断输入是否为纯数字字符组合
     */
    private static final Pattern DIGITS_PATTERN;
    
    /**
     * 英文小数模式，匹配如"123.45"格式的小数
     */
    private static final Pattern ENGLISH_DECIMAL_PATTERN = Pattern.compile("([0-9]*)\\.([0-9]+)");
    
    /**
     * 英文分数模式，匹配如"3/4"格式的分数
     */
    private static final Pattern ENGLISH_FRACTION_PATTERN = Pattern.compile("([0-9]*)/([0-9]+)");

    /**
     * 万位以下的中文数字单位：十、百、千
     */
    private static final String[] BEFORE_WAN_DIGITS = {"十", "百", "千"};

    /**
     * 万位及以上的中文数字单位：个、万、亿、兆、京
     */
    private static final String[] AFTER_WAN_DIGITS = {"", "万", "亿", "兆", "京"};

    /**
     * 负数标识符
     */
    private static final String MINUS = "负";
    
    /**
     * 小数点标识符
     */
    private static final String DECIMAL = "点";
    
    /**
     * 分数标识符
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
     * 将阿拉伯数字字符串转换为中文数字字符串
     * <p>
     * 支持转换格式：
     * <ul>
     *     <li>正整数：如"12345" → "一万二千三百四十五"</li>
     *     <li>负整数：如"-123" → "负一百二十三"</li>
     *     <li>小数：如"3.14" → "三点一四"</li>
     *     <li>分数：如"3/4" → "四分之三"</li>
     * </ul>
     *
     * @param text 阿拉伯数字字符串，如"2009000"、"5.3"、"-5.3"、"3/4"
     * @return 转换后的中文数字字符串
     * @throws IllegalArgumentException 当输入为空或格式不正确时抛出
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
     * 简单映射阿拉伯数字为中文数字
     * <p>
     * 将每个阿拉伯数字字符直接映射为对应的中文数字字符，主要用于处理小数部分。
     * 例如：将"123"转换为"一二三"。
     *
     * @param text 纯阿拉伯数字字符串
     * @return 直接映射后的中文数字字符串
     */
    private static String englishNumberToChineseBrief(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append(DIGITS[text.charAt(i) - '0']);
        }
        return result.toString();
    }

    /**
     * 完整转换阿拉伯数字为中文数字（含单位）
     * <p>
     * 对整数部分进行复杂的转换，包括添加适当的中文数字单位（如十、百、千、万、亿等）。
     * 使用权重计算法将数字按位分解，然后组合成标准的中文数字表达方式。
     * 例如：将"12345"转换为"一万二千三百四十五"。
     *
     * @param text 纯阿拉伯数字字符串
     * @return 包含中文数字单位的完整中文数字字符串
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
     * 将中文数字字符串转换为阿拉伯数字
     * <p>
     * 支持转换格式：
     * <ul>
     *     <li>正负整数：如"一万二千三百四十五" → 12345.0</li>
     *     <li>小数：如"三点一四" → 3.14</li>
     *     <li>分数：如"四分之三" → 0.75</li>
     *     <li>繁体数字：如"壹萬貳仟參佰肆拾伍" → 12345.0</li>
     *     <li>全角数字：如"１２３４５" → 12345.0</li>
     * </ul>
     *
     * @param text 中文数字字符串，如"五千四百九十一万四千七百一十"、"三点一四"、"四分之三"
     * @return 转换后的数字值
     * @throws IllegalArgumentException 当输入为空或格式不正确时抛出
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
     * 简单转换中文数字为阿拉伯数字（纯数字映射）
     * <p>
     * 当输入完全由数字字符组成时，直接进行字符到数字的映射转换。
     * 例如：将"一二三四五"转换为12345。
     *
     * @param text 纯数字字符的中文字符串
     * @return 转换后的长整型数字
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
     * 完整转换中文数字为阿拉伯数字（含单位解析）
     * <p>
     * 处理包含中文数字单位（如十、百、千、万、亿等）的复杂中文数字字符串。
     * 支持多种格式：
     * <ul>
     *     <li>标准中文数字：如"一万二千三百四十五"</li>
     *     <li>简化表达：如"三百五"（表示350）</li>
     *     <li>负数：如"负一百二十三"</li>
     *     <li>小数：如"三点一四"</li>
     *     <li>繁体数字：如"壹萬貳仟"</li>
     *     <li>特殊写法：如"廿"（表示二十）、"卅"（表示三十）等</li>
     * </ul>
     * 
     * <p>转换过程包括文本预处理、逐字符解析、单位计算和数值累加等步骤。
     *
     * @param text 包含中文数字单位的字符串
     * @return 转换后的双精度浮点数
     * @throws IllegalArgumentException 当遇到无法识别的字符时抛出
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
