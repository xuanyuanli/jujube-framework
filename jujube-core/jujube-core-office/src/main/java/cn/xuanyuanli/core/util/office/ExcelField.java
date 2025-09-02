package cn.xuanyuanli.core.util.office;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel 字段映射注解
 * <p>
 * 用于标记 Java 对象字段与 Excel 列的映射关系，支持灵活的数据格式化和列位置控制。
 * 适用于 Excel 文件的导入导出功能，提供以下特性：
 * <ul>
 * <li><strong>列名映射：</strong>指定字段对应的 Excel 列名或列标题</li>
 * <li><strong>日期格式化：</strong>支持自定义日期格式模式</li>
 * <li><strong>数字格式化：</strong>支持自定义数字格式模式</li>
 * <li><strong>自定义格式化：</strong>支持任意格式的字符串模板</li>
 * <li><strong>列位置控制：</strong>可指定字段在 Excel 中的列位置</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * public class UserExportDto {
 *     @ExcelField("用户姓名")
 *     private String name;
 *     
 *     @ExcelField(value = "出生日期", dateFormat = "yyyy年MM月dd日")
 *     private Date birthDate;
 *     
 *     @ExcelField(value = "账户余额", numberFormat = "#,##0.00")
 *     private BigDecimal balance;
 *     
 *     @ExcelField(value = "用户状态", customizeFormat = "状态：{}")
 *     private String status;
 *     
 *     @ExcelField(value = "用户ID", colIndex = 0)
 *     private Long id;
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>格式化说明：</strong>
 * <ul>
 * <li><strong>dateFormat：</strong>遵循 {@link java.text.SimpleDateFormat} 规范</li>
 * <li><strong>numberFormat：</strong>遵循 {@link java.text.DecimalFormat} 规范</li>
 * <li><strong>customizeFormat：</strong>使用 {} 作为占位符，会被字段值替换</li>
 * <li><strong>colIndex：</strong>从0开始的列索引，-1表示使用字段声明顺序</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注解处理优先级：</strong>
 * 如果同时指定多种格式，按以下优先级应用：
 * customizeFormat > dateFormat > numberFormat > 默认格式
 * </p>
 *
 * @author xuanyuanli
 * @date 2022/04/29
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelField {

    /**
     * Excel 列名或列标题
     * <p>
     * 指定字段对应的 Excel 列名称。在导入时用于匹配列标题，
     * 在导出时作为列标题显示。支持中英文混合命名。
     * </p>
     *
     * @return Excel 列名或标题，不能为空
     */
    String value();

    /**
     * 日期格式化模式
     * <p>
     * 当字段类型为 Date、LocalDate、LocalDateTime 等日期类型时生效。
     * 遵循 {@link java.text.SimpleDateFormat} 标准格式化规则。
     * </p>
     * 
     * <p>
     * <strong>常用格式示例：</strong>
     * <ul>
     * <li>"yyyy-MM-dd" - 日期格式：2023-12-25</li>
     * <li>"yyyy年MM月dd日" - 中文日期：2023年12月25日</li>
     * <li>"yyyy-MM-dd HH:mm:ss" - 日期时间：2023-12-25 14:30:00</li>
     * </ul>
     * </p>
     *
     * @return 日期格式化模式，默认为空字符串表示使用默认格式
     * @see java.text.SimpleDateFormat
     */
    String dateFormat() default "";

    /**
     * 数字格式化模式
     * <p>
     * 当字段类型为 Number 及其子类（Integer、Long、BigDecimal 等）时生效。
     * 遵循 {@link java.text.DecimalFormat} 标准格式化规则。
     * </p>
     * 
     * <p>
     * <strong>常用格式示例：</strong>
     * <ul>
     * <li>"#,##0" - 整数千分位：1,234</li>
     * <li>"#,##0.00" - 两位小数千分位：1,234.56</li>
     * <li>"0.00%" - 百分比格式：12.34%</li>
     * <li>"¤#,##0.00" - 货币格式：¥1,234.56</li>
     * </ul>
     * </p>
     *
     * @return 数字格式化模式，默认为空字符串表示使用默认格式
     * @see java.text.DecimalFormat
     */
    String numberFormat() default "";

    /**
     * 自定义格式化模式
     * <p>
     * 提供最灵活的字符串格式化方式，使用 {} 作为占位符表示字段值。
     * 优先级高于 dateFormat 和 numberFormat，可用于任意类型的字段。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <ul>
     * <li>"状态：{}" - 结果：状态：激活</li>
     * <li>"用户（ID：{}）" - 结果：用户（ID：12345）</li>
     * <li>"等级：LV{}" - 结果：等级：LV5</li>
     * <li>"{}%" - 结果：85.5%</li>
     * </ul>
     * </p>
     *
     * @return 自定义格式化模板，默认为空字符串表示不使用此功能
     */
    String customizeFormat() default "";

    /**
     * Excel 列位置索引
     * <p>
     * 指定字段在 Excel 中的列位置，从 0 开始计数。可用于控制导出时
     * 的列顺序，也可用于导入时的列位置匹配。
     * </p>
     * 
     * <p>
     * <strong>使用说明：</strong>
     * <ul>
     * <li>设置为 -1（默认值）：使用字段在类中的声明顺序</li>
     * <li>设置为 0 及以上：使用指定的列位置</li>
     * <li>多个字段指定相同 colIndex 时，行为未定义</li>
     * </ul>
     * </p>
     *
     * @return 列位置索引，-1 表示使用字段声明顺序，0 及以上表示指定列位置
     */
    int colIndex() default -1;
}
