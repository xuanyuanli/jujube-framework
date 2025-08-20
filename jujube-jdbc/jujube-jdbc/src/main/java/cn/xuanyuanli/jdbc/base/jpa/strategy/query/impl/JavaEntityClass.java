package cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityClass;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityField;

/**
 * @author xuanyuanli
 * @date 2023/4/20
 */
@AllArgsConstructor
public class JavaEntityClass implements EntityClass {

    private Class<?> clazz;

    @Override
    public EntityField[] getDeclaredFields() {
        return Arrays.stream(clazz.getDeclaredFields()).map(JavaEntityField::new).toArray(EntityField[]::new);
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

}
