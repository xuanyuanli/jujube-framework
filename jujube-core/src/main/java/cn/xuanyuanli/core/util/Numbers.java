package cn.xuanyuanli.core.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 数字工具
 *
 * @author John Li
 * @date 2021/09/01
 */
public class Numbers {

    /**
     * 金钱格式化
     *
     * @param number 数量
     * @return 返回格式如：700,000.0或70万
     */
    public static String moneyFormat(Number number) {
        return moneyFormatOfZhPrefix(number, false);
    }

    /**
     * 金钱格式化
     *
     * @param isAddSuffix 是否添加"万"后缀。如果不添加，则返回"##,###.##"格式的数字
     * @param number      数量
     * @return 返回格式如：700,000.0或70万
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
     * 数字格式化
     *
     * @param pattern 如“##.##”的字符串
     * @param number  数量
     * @return {@link String}
     */
    public static String numberFormat(Number number, String pattern) {
        if (number == null) {
            return null;
        }
        DecimalFormat myformat = new DecimalFormat(pattern);
        return myformat.format(number);
    }

    /**
     * 只处理source中的数字部分
     *
     * @param source 源
     * @return {@link Integer}
     */
    public static Integer parseInt(String source) {
        return Math.toIntExact(parseLong(source));
    }

    /**
     * 只处理source中的数字部分
     *
     * @param source 源
     * @return {@link Long}
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
     * 把number转换为string，非科学计数法
     *
     * @param value 价值
     * @return {@link String}
     */
    public static String numberToString(Number value) {
        return value == null ? null : new BigDecimal(value + "").toPlainString();
    }
}
