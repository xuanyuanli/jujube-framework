package cn.xuanyuanli.jdbc.base.jpa.strategy.query;

/**
 * @author John Li
 * @date 2023/4/20
 */
public interface EntityField {


    /**
     * 有列注释
     *
     * @return boolean
     */
    boolean hasColumnAnnotation();

    /**
     * 获得名字
     *
     * @return {@link String}
     */
    String getName();

    /**
     * 获得列注释价值
     *
     * @return {@link String}
     */
    String getColumnAnnotationValue();

    /**
     * 有视觉列注释
     *
     * @return boolean
     */
    boolean hasVisualColumnAnnotation();
}
