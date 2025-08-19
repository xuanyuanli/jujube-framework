package cn.xuanyuanli.jdbc.base.jpa.strategy.query;

/**
 * 数据库实体对应的类。如果是Dao类中，则是Dao类的泛型类型
 *
 * @author John Li
 * @date 2023/4/20
 */
public interface EntityClass {

    /**
     * 获得宣布字段
     *
     * @return {@link EntityField[]}
     */
    EntityField[] getDeclaredFields();

    /**
     * 获得名字
     *
     * @return {@link String}
     */
    String getName();

    /**
     * 字符串
     *
     * @return {@link String}
     */
    @Override
    String toString();
}
