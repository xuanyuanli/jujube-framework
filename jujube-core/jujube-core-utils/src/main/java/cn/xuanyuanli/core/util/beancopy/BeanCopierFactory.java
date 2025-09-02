package cn.xuanyuanli.core.util.beancopy;

import java.util.List;

/**
 * Bean复制器工厂接口
 * 
 * <p>提供创建Bean复制器的工厂方法，用于在不同类型的Java对象之间进行属性复制。
 * 该工厂可以根据源类和目标类创建相应的Bean复制器实例，支持自定义属性映射和覆盖策略。</p>
 * 
 * @author xuanyuanli
 */
public interface BeanCopierFactory {

    /**
     * 创建Bean复制器实例
     * 
     * <p>根据指定的源类型和目标类型创建一个Bean复制器，用于在两种类型的对象之间进行属性复制。
     * 可以通过属性映射项配置自定义的属性映射关系，并设置是否覆盖目标对象的现有属性值。</p>
     *
     * @param sourceClass 源对象的类型，不能为null
     * @param targetClass 目标对象的类型，不能为null
     * @param items       属性复制配置项列表，用于定义源对象和目标对象之间的属性映射关系，可以为null或空列表
     * @param cover       是否覆盖目标对象中已存在的属性值，true表示覆盖，false表示跳过已有值
     * @return 创建的Bean复制器实例，不会返回null
     */
    BeanCopier createBeanCopier(Class<?> sourceClass, Class<?> targetClass, List<BeanCopyPropertyItem> items,boolean cover);
}
