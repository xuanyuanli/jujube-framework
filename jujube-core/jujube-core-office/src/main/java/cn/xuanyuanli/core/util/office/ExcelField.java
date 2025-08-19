package cn.xuanyuanli.core.util.office;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记字段为Excel列
 *
 * @author John Li
 * @date 2022/04/29
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelField {

    /**
     * 列表
     *
     * @return {@link String}
     */
    String value();

    /**
     * 日期格式化模式
     *
     * @return {@link String}
     */
    String dateFormat() default "";

    /**
     * 数字格式化模式
     *
     * @return {@link String}
     */
    String numberFormat() default "";

    /**
     * 自定义格式化模式，使用{}表示字段值
     *
     * @return {@link String}
     */
    String customizeFormat() default "";

    /**
     * 列顺序，不填则按照字段自然顺序
     *
     * @return int
     */
    int colIndex() default -1;
}
