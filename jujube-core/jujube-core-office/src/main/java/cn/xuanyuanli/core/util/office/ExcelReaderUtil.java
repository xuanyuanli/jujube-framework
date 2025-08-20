package cn.xuanyuanli.core.util.office;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import cn.xuanyuanli.core.util.Calcs;
import cn.xuanyuanli.core.util.Files;

/**
 * Excel读取工具类
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExcelReaderUtil {

    /**
     * 默认值
     */
    public static final double DEFAULT_VALUE = -1d;

    /**
     * Excel 自动把数字，转换为double形式。如果使用toint()，则会得到0；所以Excel中获得数字，都用这个方法
     *
     * @param excelNumber excel数量
     * @return {@link Number}
     */
    public static Number toNumber(String excelNumber) {
        return NumberUtils.toDouble(excelNumber);
    }

    /**
     * 获得预期的字符串值
     * <p>
     * 如果我们需要的类型为String，而excle中用户填写了类似2001的数字，那么获得实际值的时候会变成：2001.0(excel自动转换)， 这是不符合要求的； 使用这个方法 ，可以获得预期的类型真实值
     *
     * @param value 实际值
     * @return 预期的真实值
     */
    public static String toExpectStringValue(String value) {
        String result = value;
        double convertDouble = NumberUtils.toDouble(value, -1);
        if (!Calcs.equ(convertDouble, DEFAULT_VALUE)) {
            result = String.valueOf((int) convertDouble);
        }
        return result;
    }

    /**
     * 判断是否是空白行。会对每个cell进行blank验证，如果行中每个单元格都是blank，则返回ture
     *
     * @param row 行
     * @return boolean
     */
    public static boolean isEmtpyRow(List<String> row) {
        boolean result = true;
        for (String cell : row) {
            if (StringUtils.isNotBlank(cell)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 剔除模板表头各个单元格中的特殊字符，然后返回
     *
     * @param row 行
     * @return {@link List}<{@link String}>
     */
    public static List<String> templateTitlesToStandardAttrs(List<String> row) {
        List<String> list = new ArrayList<>();
        for (String cell : row) {
            list.add(rejectSpecialChar(cell));
        }
        return list;
    }

    /**
     * 剔除特殊字符
     *
     * @param title 标题
     * @return {@link String}
     */
    public static String rejectSpecialChar(String title) {
        return title.replaceAll("[*/()（）￥$#%&_\\-=+{}\\[\\]'\",.，。?？]", "");
    }

    /**
     * 获得转换后的数字，例如带逗号的数字等，返回转换后的正确值
     *
     * @param number 数量
     * @return {@link Number}
     */
    public static Number parseToNumber(String number) {
        if (StringUtils.isBlank(number)) {
            return 0;
        }
        try {
            return NumberFormat.getInstance().parse(number);
        } catch (ParseException e) {
            return 0;
        }
    }

    /**
     * 根据后缀判断是否是excle
     *
     * @param fileName 文件名称
     * @return boolean
     */
    public static boolean isExcelFile(String fileName) {
        String extention = Files.getExtention(fileName);
        return Objects.equals(extention, ".xls") || Objects.equals(extention, ".xlsx");
    }
}
