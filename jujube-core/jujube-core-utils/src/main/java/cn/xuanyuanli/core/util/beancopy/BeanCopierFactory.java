package cn.xuanyuanli.core.util.beancopy;

import java.util.List;

/**
 * @author John Li
 */
public interface BeanCopierFactory {

    /**
     * 创建bean复印机
     *
     * @param sourceClass 源类
     * @param targetClass 目标类
     * @param items       项目
     * @param cover       覆盖
     * @return {@link BeanCopier}
     */
    BeanCopier createBeanCopier(Class<?> sourceClass, Class<?> targetClass, List<BeanCopyPropertyItem> items,boolean cover);
}
