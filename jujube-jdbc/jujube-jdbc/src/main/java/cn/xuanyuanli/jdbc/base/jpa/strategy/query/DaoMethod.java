package cn.xuanyuanli.jdbc.base.jpa.strategy.query;

/**
 * Dao中的Method抽象
 *
 * @author John Li
 * @date 2023/4/20
 */
public interface DaoMethod {

    /**
     * 获得声明类
     *
     * @return {@link EntityClass}
     */
    EntityClass getEntityClass();

    /**
     * 获得名字
     *
     * @return {@link String}
     */
    String getName();

    /**
     * 有选择字段注释
     *
     * @return boolean
     */
    boolean hasSelectFieldAnnotation();

    /**
     * 获得选择场注释价值
     *
     * @return {@link String[]}
     */
    String[] getSelectFieldAnnotationValue();
}
