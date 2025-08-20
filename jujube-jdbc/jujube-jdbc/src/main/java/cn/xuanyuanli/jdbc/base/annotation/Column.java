package cn.xuanyuanli.jdbc.base.annotation;

import java.lang.annotation.*;

/**
 * @author xuanyuanli
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Column {

    /**
     * 值
     *
     * @return {@link String}
     */
    String value() default "";
}
