package cn.xuanyuanli.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 数字处理工具类
 * <p>
 * 提供数字格式化、解析和转换的实用方法，特别适用于：
 * <ul>
 * <li><strong>金钱格式化：</strong>支持中文万元单位和千分位格式</li>
 * <li><strong>数字解析：</strong>从混合字符串中提取纯数字</li>
 * <li><strong>格式转换：</strong>数字格式化和科学计数法处理</li>
 * <li><strong>负数处理：</strong>智能识别负号位置</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>金钱格式化功能：</strong>
 * <ul>
 * <li>支持千分位分隔符格式（如：1,234.56）</li>
 * <li>支持中文万元单位格式（如：12.34万）</li>
 * <li>自动处理小数位数和舍入</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>数字解析功能：</strong>
 * <ul>
 * <li>从包含字母、符号的字符串中提取纯数字</li>
 * <li>自动识别负数（以 "-" 开头的数字）</li>
 * <li>支持返回 Integer 或 Long 类型</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 金钱格式化
 * String formatted = Numbers.moneyFormat(1234567); // "1,234,567"
 * String withUnit = Numbers.moneyFormatOfZhPrefix(12345, true); // "1.2345万"
 * 
 * // 自定义数字格式化
 * String custom = Numbers.numberFormat(123.456, "##.##"); // "123.46"
 * 
 * // 从字符串解析数字
 * Integer num1 = Numbers.parseInt("abc123def"); // 123
 * Long num2 = Numbers.parseLong("-price:9999元"); // -9999
 * 
 * // 数字转字符串（非科学计数法）
 * String plain = Numbers.numberToString(1.23E+4); // "12300"
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>金钱格式化使用 {@link DecimalFormat}，遵循标准舍入规则</li>
 * <li>负数识别基于正则表达式 "^-\\d"，仅识别开头为负号的情况</li>
 * <li>数字解析会忽略所有非数字字符，包括空格和标点</li>
 * <li>科学计数法转换使用 {@link BigDecimal} 确保精度</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class Numbers {

    /**
     * 金钱格式化（标准千分位格式）
     * <p>
     * 使用千分位分隔符格式化数字，适用于金额显示。
     * 等同于调用 {@code moneyFormatOfZhPrefix(number, false)}。
     * </p>
     *
     * @param number 待格式化的数字，支持所有 {@link Number} 类型
     * @return 格式化后的字符串，如 "1,234,567.89"；如果输入为 null 则返回 null
     * 
     * @see #moneyFormatOfZhPrefix(Number, boolean)
     */
    public static String moneyFormat(Number number) {
        return moneyFormatOfZhPrefix(number, false);
    }

    /**
     * 金钱格式化（支持中文万元单位）
     * <p>
     * 根据参数选择返回千分位格式或中文万元格式：
     * <ul>
     * <li>当 {@code isAddSuffix = false} 时：返回 "##,###.##" 格式</li>
     * <li>当 {@code isAddSuffix = true} 且数值 >= 10,000 时：转换为万元单位，格式 "##,###.####万"</li>
     * <li>当 {@code isAddSuffix = true} 且数值 < 10,000 时：仍返回 "##,###.##" 格式</li>
     * </ul>
     * </p>
     *
     * @param number      待格式化的数字，支持所有 {@link Number} 类型
     * @param isAddSuffix 是否添加"万"后缀
     * @return 格式化后的字符串，如 "12.34万" 或 "1,234.56"；如果输入为 null 则返回 null
     */
    public static String moneyFormatOfZhPrefix(Number number, boolean isAddSuffix) {
        if (number == null) {
            return null;
        }
        String suffix = "万";
        String result;
        DecimalFormat myformat = new DecimalFormat();
        double num = number.doubleValue();
        if (isAddSuffix && num >= 10000) {
            num = num / 10000;
            myformat.applyPattern("##,###.####");
            result = myformat.format(num);
            result += suffix;
        } else {
            myformat.applyPattern("##,###.##");
            result = myformat.format(num);
        }
        return result;
    }

    /**
     * 自定义数字格式化
     * <p>
     * 使用指定的格式模式格式化数字。支持 {@link DecimalFormat} 的所有格式模式。
     * </p>
     * 
     * <p>
     * 常用格式模式示例：
     * <ul>
     * <li>"##.##" - 最多两位小数：123.45</li>
     * <li>"00.00" - 固定两位小数：01.20</li>
     * <li>"#,###.##" - 千分位分隔：1,234.56</li>
     * <li>"0.00%" - 百分比格式：12.34%</li>
     * </ul>
     * </p>
     *
     * @param number  待格式化的数字，支持所有 {@link Number} 类型
     * @param pattern 格式模式字符串，遵循 {@link DecimalFormat} 规范
     * @return 格式化后的字符串；如果 number 为 null 则返回 null
     * 
     * @throws IllegalArgumentException 如果 pattern 格式不正确
     * @see DecimalFormat
     */
    public static String numberFormat(Number number, String pattern) {
        if (number == null) {
            return null;
        }
        DecimalFormat myformat = new DecimalFormat(pattern);
        return myformat.format(number);
    }

    /**
     * 从字符串中解析整数
     * <p>
     * 提取字符串中的所有数字字符，组合成整数。忽略所有非数字字符，
     * 自动识别负号（仅当字符串以 "-" 开头时）。
     * </p>
     * 
     * <p>
     * 解析规则：
     * <ul>
     * <li>提取所有数字字符 0-9</li>
     * <li>如果字符串匹配 "^-\d" 模式，结果为负数</li>
     * <li>忽略空格、标点符号、字母等非数字字符</li>
     * </ul>
     * </p>
     *
     * @param source 包含数字的源字符串，如 "abc123def"、"-price:456元"
     * @return 解析出的整数值；空字符串或无数字时返回 0
     * 
     * @throws ArithmeticException 如果解析出的数字超出 Integer 范围
     * @see #parseLong(String)
     */
    public static Integer parseInt(String source) {
        return Math.toIntExact(parseLong(source));
    }

    /**
     * 从字符串中解析长整数
     * <p>
     * 提取字符串中的所有数字字符，组合成长整数。忽略所有非数字字符，
     * 自动识别负号（仅当字符串以 "-" 开头时）。
     * </p>
     * 
     * <p>
     * 解析规则：
     * <ul>
     * <li>提取所有数字字符 0-9</li>
     * <li>如果字符串匹配 "^-\d" 模式，结果为负数</li>
     * <li>忽略空格、标点符号、字母等非数字字符</li>
     * </ul>
     * </p>
     * 
     * <p>
     * 使用示例：
     * <pre>
     * parseLong("abc123def")     = 123L
     * parseLong("-price:999元")  = -999L
     * parseLong("1,234.56")     = 123456L
     * parseLong("no numbers")   = 0L
     * </pre>
     * </p>
     *
     * @param source 包含数字的源字符串
     * @return 解析出的长整数值；空字符串或无数字时返回 0L
     * 
     * @see #parseInt(String)
     */
    public static Long parseLong(String source) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(source)) {
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (Character.isDigit(ch)) {
                    builder.append(ch);
                }
            }
        }
        long aLong = NumberUtils.toLong(builder.toString());
        if (Texts.find(source, "^-\\d")) {
            return -aLong;
        }
        return aLong;
    }

    /**
     * 数字转字符串（非科学计数法）
     * <p>
     * 将数字转换为普通数字格式的字符串，避免科学计数法表示。
     * 使用 {@link BigDecimal#toPlainString()} 确保精度和格式正确。
     * </p>
     * 
     * <p>
     * 转换示例：
     * <pre>
     * numberToString(1.23E+4)     = "12300"
     * numberToString(1.23E-4)     = "0.000123"
     * numberToString(123456789)   = "123456789"
     * numberToString(0.123)       = "0.123"
     * numberToString(null)        = null
     * </pre>
     * </p>
     *
     * @param value 待转换的数字，支持所有 {@link Number} 类型
     * @return 非科学计数法格式的字符串；如果输入为 null 则返回 null
     * 
     * @see BigDecimal#toPlainString()
     */
    public static String numberToString(Number value) {
        return value == null ? null : new BigDecimal(value + "").toPlainString();
    }
}
