package cn.xuanyuanli.jdbc.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 获得分页总条数的自定义策略
 *
 * @author xuanyuanli
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GetCountStrategy {

    /**
     * 方法名。此方法，必须有String参数，且返回String
     *
     * @return {@link String}
     */
    String value();
}
