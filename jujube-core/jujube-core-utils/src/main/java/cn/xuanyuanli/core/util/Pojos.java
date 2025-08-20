package cn.xuanyuanli.core.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import cn.xuanyuanli.core.util.beancopy.BeanCopier;
import cn.xuanyuanli.core.util.beancopy.BeanCopierFactory;
import cn.xuanyuanli.core.util.beancopy.BeanCopyPropertyItem;
import cn.xuanyuanli.core.util.beancopy.JavassistBeanCopierFactory;

import javax.annotation.Nonnull;

/**
 * Java对象映射（属性复制）工具。
 *
 * <h3>字段名映射规则：</h3>
 * <ol>
 *   <li>驼峰命名和下划线命名可以完成自动转换并赋值。<br>
 *       例如：A对象到B对象，A中有user_type字段，B中有userType，可以完成user_type -> userType的字段赋值；<br>
 *       同理，也可以完成userType -> user_type的赋值。</li>
 *   <li>支持父类属性的获取和赋值。</li>
 *   <li>所谓的字段名，其实是属性：对于源对象，指的是get方法；对于目标对象来说，指的是set方法。</li>
 * </ol>
 *
 * <h3>属性类型赋值规则：</h3>
 * <ol>
 *   <li>如果类型相同，则复制。</li>
 *   <li>如果类型不同，且都为基本类型，可以完成自动转换赋值，例如int到string，string到double等。</li>
 *   <li>如果类型不同，且不为基本类型，不能进行自动转换赋值。</li>
 *   <li>如果类型为集合，且集合泛型相同，可以完成自动赋值。</li>
 *   <li>如果类型为集合，且集合泛型不同，不能完成自动赋值（存在泛型消除问题，可以理解为不能自动赋值）。</li>
 * </ol>
 *
 * <h3>使用说明：</h3>
 * <ol>
 *   <li>优先使用vo2dto插件。</li>
 *   <li>其次，必须在Jujube-Ext插件的配合下使用，此插件带有类复制检测。</li>
 *   <li>不允许非上述场景下使用，容易造成潜在问题。</li>
 * </ol>
 *
 * <h3>性能报告：</h3>
 * <ol>
 *   <li>参考PojosBenchmark的测试结果：BeanCopier的性能接近原始get set操作，getBeanCopierFromCache的性能是其1/4</li>
 *   <li>IntelliJ Profiler对于PojosTests#mappingRecordPrimitiveToWrap_multiple()方法的测试结果</li>
 *   <ul>
 *       <li>getBeanCopierFromCache()方法占用了94%，其中字符串拼接占用了35%，ConcurrentHashMap#computeIfAbsent()占用了65%</li>
 *       <li>BeanCopier的性能是没有问题，主要耗时在getBeanCopierFromCache()方法</li>
 *   </ul>
 * </ol>
 *
 * @author xuanyuanli
 * @since 2021/09/01
 */

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Pojos {

    /**
     * Bean复制工厂
     */
    private static final BeanCopierFactory BEAN_COPIER_FACTORY = new JavassistBeanCopierFactory();
    /**
     * Bean复制机缓存
     */
    private static final ConcurrentMap<QuadKey, BeanCopier> BEAN_COPIER_CACHE = new ConcurrentHashMap<>();

    /**
     * 把原始对象映射为对应类型的Pojo
     *
     * @param sourceObj 源obj
     * @param destClass 目标类
     * @param <T>       泛型
     * @return {@link T}
     */
    public static <T> T mapping(Object sourceObj, Class<T> destClass) {
        return mapping(sourceObj, destClass, null);
    }

    /**
     * 把原始对象映射为对应类型的Pojo
     *
     * @param fieldMapping 字段映射
     * @param sourceObj    源obj
     * @param destClass    目标类
     * @param <T>          泛型
     * @return {@link T}
     */
    public static <T> T mapping(Object sourceObj, Class<T> destClass, FieldMapping fieldMapping) {
        if (destClass == null || sourceObj == null) {
            return null;
        }
        if (Map.class.isAssignableFrom(destClass)) {
            throw new IllegalArgumentException("destClass不能为Map");
        }
        return (T) getBeanCopierFromCache(sourceObj, destClass, fieldMapping, true).copyBean(sourceObj, destClass, true);
    }

    /**
     * 获得bean复制者
     *
     * @param sourceObj    源obj
     * @param destClass    桌子类
     * @param fieldMapping 字段映射
     * @param cover        封面
     * @return {@link BeanCopier}
     */
    public static BeanCopier getBeanCopierFromCache(Object sourceObj, Class<?> destClass, FieldMapping fieldMapping, boolean cover) {
        QuadKey quadKey = new QuadKey(sourceObj.getClass(), destClass, sourceObj instanceof Map ? getRowKey(sourceObj) : null, cover);
        return BEAN_COPIER_CACHE.computeIfAbsent(quadKey, key -> getRealBeanCopier(sourceObj, destClass, cover, fieldMapping));
    }

    private static TreeSet<?> getRowKey(Object sourceObj) {
        return new TreeSet<>(((Map<?, ?>) sourceObj).keySet());
    }

    /**
     * 获取真正bean复印机
     *
     * @param sourceObj    来源obj
     * @param destClass    dest类
     * @param cover        覆盖
     * @param fieldMapping 字段映射
     * @return {@link BeanCopier }
     */
    private static BeanCopier getRealBeanCopier(Object sourceObj, Class<?> destClass, boolean cover, FieldMapping fieldMapping) {
        if (fieldMapping == null) {
            fieldMapping = new FieldMapping();
        }
        List<String> fieldNames = getFieldNameList(sourceObj);
        Map<String, String> mapping = fieldMapping.getFieldMapping();
        for (String fieldName : fieldNames) {
            if (!mapping.containsKey(fieldName)) {
                PropertyDescriptor field = Beans.getPropertyDescriptor(destClass, fieldName);
                if (field == null) {
                    String camelCase = CamelCase.toCamelCase(fieldName);
                    field = Beans.getPropertyDescriptor(destClass, camelCase);
                    if (field == null) {
                        field = Beans.getPropertyDescriptor(destClass, CamelCase.toUnderlineName(fieldName));
                    }
                }
                // 如果fieldMapping包含了映射关系，那么以他为准。所以这里加个!mapping.containsValue(field.getName())的判断
                if (field != null && !mapping.containsValue(field.getName())) {
                    fieldMapping.field(fieldName, field.getName());
                }
            }
        }
        ArrayList<BeanCopyPropertyItem> items = new ArrayList<>();
        for (Entry<String, String> entry : fieldMapping.getFieldMapping().entrySet()) {
            BeanCopyPropertyItem item = new BeanCopyPropertyItem();
            if (sourceObj instanceof Map) {
                item.setSourcePropertyName(entry.getKey());
                item.setSourceIsMap(true);
            } else {
                item.setSourceProperty(Beans.getPropertyDescriptor(sourceObj.getClass(), entry.getKey()));
            }
            item.setTargetProperty(Beans.getPropertyDescriptor(destClass, entry.getValue()));
            PropertyDescriptor targetProperty = item.getTargetProperty();
            PropertyDescriptor sourceProperty = item.getSourceProperty();
            if (targetProperty == null || targetProperty.getWriteMethod() == null) {
                continue;
            }
            Type[] parameterTypes = targetProperty.getWriteMethod().getGenericParameterTypes();
            if (parameterTypes.length > 0) {
                Type writeType = parameterTypes[0];
                Class<?> clType = writeType instanceof ParameterizedType ? (Class<?>) ((ParameterizedType) writeType).getRawType()
                        : (writeType instanceof Class ? (Class<?>) writeType : null);
                if (item.isSourceIsMap()) {
                    items.add(item);
                } else {
                    if (sourceProperty != null && sourceProperty.getReadMethod() != null) {
                        // 复制类型完整一致的字段和基础类型的字段
                        if (sourceProperty.getReadMethod().getGenericReturnType().equals(writeType) || Beans.isBasicType(clType)) {
                            items.add(item);
                        }
                    }
                }
            }
        }
        BeanCopier beanCopier = BEAN_COPIER_FACTORY.createBeanCopier(sourceObj.getClass(), destClass, items, cover);
        log.debug("初次获取BeanCopier[{} to {}]", sourceObj.getClass().getName(), destClass.getName());
        return beanCopier;
    }

    /**
     * 获得字段名称集合
     *
     * @param sourceObj 源obj
     * @return {@link List}<{@link String}>
     */
    private static List<String> getFieldNameList(Object sourceObj) {
        List<String> fieldNames;
        if (sourceObj instanceof Map) {
            Map<String, ?> map = (Map<String, ?>) sourceObj;
            fieldNames = new ArrayList<>(new TreeSet<>(map.keySet()));
        } else {
            fieldNames = Beans.getAllDeclaredFieldNames(sourceObj.getClass());
        }
        return fieldNames;
    }

    /**
     * 复制一个对象的值到另一个对象
     *
     * @param cover     是否覆盖destObj字段的值。<br> 如果为true，则sourceObj对应字段值会覆盖destObj中对应字段值（不论sourceObj值为空或不为空）<br> 如果为false，则destObj值不为空则不覆盖，为空则覆盖
     * @param sourceObj 源obj
     * @param destObj   obj不在座位上
     */
    public static void copy(Object sourceObj, Object destObj, boolean cover) {
        if (sourceObj == null || destObj == null) {
            throw new NullPointerException();
        }
        if (destObj instanceof Map) {
            throw new IllegalArgumentException("destClass不能为Map");
        }
        getBeanCopierFromCache(sourceObj, destObj.getClass(), null, cover).copyBean(sourceObj, destObj, false);
    }

    /**
     * 复制
     *
     * @param sourceObj 源obj
     * @param destObj   obj不在座位上
     * @see #copy(Object, Object, boolean)
     */
    public static void copy(Object sourceObj, Object destObj) {
        copy(sourceObj, destObj, true);
    }

    /**
     * 把原始对象集合映射为对应类型的Pojo集合
     *
     * @param source 源
     * @param class1 class1
     * @param <T>    泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> mappingArray(List<?> source, Class<T> class1) {
        return mappingArray(source, class1, null);
    }

    /**
     * 把原始对象集合映射为对应类型的Pojo集合
     *
     * @param fieldMapping 字段映射
     * @param source       源
     * @param destClass    桌子类
     * @param <T>          泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> mappingArray(List<?> source, Class<T> destClass, FieldMapping fieldMapping) {
        if (destClass == null || source == null) {
            return null;
        }
        if (Map.class.isAssignableFrom(destClass)) {
            throw new IllegalArgumentException("destClass不能为Map");
        }
        List<T> list = new ArrayList<>(source.size());
        if (!source.isEmpty()) {
            // 如果source是map，则不能复用BeanCopier
            if (source.stream().anyMatch(e -> Map.class.isAssignableFrom(e.getClass()))) {
                list = source.stream().map(e -> mapping(e, destClass, fieldMapping)).collect(Collectors.toList());
            } else {
                BeanCopier beanCopier = getBeanCopierFromCache(source.get(0), destClass, fieldMapping, true);
                for (Object obj : source) {
                    list.add((T) beanCopier.copyBean(obj, destClass, true));
                }
            }
        }
        return list;
    }

    /**
     * 字段对应类(key-value: sourceFieldName-destFieldName)
     */
    public static class FieldMapping {

        private final Map<String, String> mapping = new LinkedHashMap<>();

        /**
         * 字段映射
         *
         * @param sourceField 源领域
         * @param destField   目标场
         * @return FieldMapping
         */
        public FieldMapping field(String sourceField, String destField) {
            mapping.put(sourceField, destField);
            return this;
        }

        /**
         * 获得字段对应表(key-value: sourceFieldName-destFieldName)
         */
        public Map<String, String> getFieldMapping() {
            return mapping;
        }

    }

    public record QuadKey(Class<?> sourceClass, Class<?> destClass, TreeSet<?> mapKeys, boolean cover) {

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            QuadKey quadKey = (QuadKey) o;
            return cover == quadKey.cover && Objects.equals(destClass, quadKey.destClass) && Objects.equals(mapKeys, quadKey.mapKeys) && Objects.equals(
                    sourceClass, quadKey.sourceClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceClass, destClass, mapKeys, cover);
        }
    }
}
