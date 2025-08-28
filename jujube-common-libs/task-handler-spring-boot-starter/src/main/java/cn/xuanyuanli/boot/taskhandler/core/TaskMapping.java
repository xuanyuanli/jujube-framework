package cn.xuanyuanli.boot.taskhandler.core;

import java.lang.annotation.*;

/**
 * @author John Li
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface TaskMapping {
    String value() default "";
}
