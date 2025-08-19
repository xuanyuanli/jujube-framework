package cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl;

import java.lang.reflect.Field;
import lombok.AllArgsConstructor;
import cn.xuanyuanli.jdbc.base.annotation.Column;
import cn.xuanyuanli.jdbc.base.annotation.VisualColumn;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityField;

/**
 * @author John Li
 * @date 2023/4/20
 */
@AllArgsConstructor
public class JavaEntityField implements EntityField {

    private Field field;

    @Override
    public boolean hasColumnAnnotation() {
        return field.isAnnotationPresent(Column.class);
    }

    @Override
    public String getName() {
        return field.getName();
    }

    @Override
    public String getColumnAnnotationValue() {
        Column annotation = field.getAnnotation(Column.class);
        return annotation.value();
    }

    @Override
    public boolean hasVisualColumnAnnotation() {
        return field.isAnnotationPresent(VisualColumn.class);
    }

    @Override
    public String toString() {
        return field.toString();
    }
}
