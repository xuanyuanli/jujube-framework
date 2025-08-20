package cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl;

import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import cn.xuanyuanli.jdbc.base.annotation.SelectField;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityClass;
import cn.xuanyuanli.core.util.Beans;

/**
 * @author xuanyuanli
 * @date 2023/4/20
 */
@AllArgsConstructor
public class JavaDaoMethod implements DaoMethod {

    private Method method;

    @Override
    public EntityClass getEntityClass() {
        Class<?> declaringClass = method.getDeclaringClass();
        return new JavaEntityClass(Beans.getClassGenericType(declaringClass));
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public boolean hasSelectFieldAnnotation() {
        return method.isAnnotationPresent(SelectField.class);
    }

    @Override
    public String[] getSelectFieldAnnotationValue() {
        SelectField annotation = method.getAnnotation(SelectField.class);
        return annotation.value();
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
