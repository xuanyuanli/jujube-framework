package cn.xuanyuanli.jdbc.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;
import cn.xuanyuanli.jdbc.base.jpa.entity.RecordEntity;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityClass;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.EntityField;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaEntityClass;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.lang.Record;
import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Pojos;

/**
 * Jdbc专用的Pojos，添加了Entity与数据库表的字段对照关系处理逻辑
 *
 * @author xuanyuanli
 */
public class JdbcPojos {

    private static final ConcurrentMap<String, Pojos.FieldMapping> FIELD_MAPPING_CACHE = new ConcurrentHashMap<>();

    /**
     * Entity与数据库字段的对应数据
     */
    private static final ConcurrentMap<String, List<FieldColumn>> CLASS_FIELDS_DATA = new ConcurrentHashMap<>();

    /**
     * 获得Entity与数据库表的字段对照关系
     *
     * @param entityClass 实体类
     * @return {@link List}<{@link FieldColumn}>
     */
    public static List<FieldColumn> getFieldColumns(EntityClass entityClass) {
        return CLASS_FIELDS_DATA.computeIfAbsent(entityClass.getName(), key -> {
            EntityField[] declaredFields = entityClass.getDeclaredFields();
            List<FieldColumn> list = new ArrayList<>(declaredFields.length);
            for (EntityField declaredField : declaredFields) {
                FieldColumn fieldColumn = new FieldColumn();
                String fieldName = declaredField.getName();
                fieldColumn.setField(fieldName);
                if (declaredField.hasColumnAnnotation()) {
                    fieldColumn.setColumn(declaredField.getColumnAnnotationValue());
                } else {
                    fieldColumn.setColumn(CamelCase.toUnderlineName(fieldName));
                }
                fieldColumn.setVisual(declaredField.hasVisualColumnAnnotation());
                list.add(fieldColumn);
            }
            return list;
        });
    }

    /**
     * 把原始对象映射为对应类型的Pojo
     *
     * @param sourceObj   源obj
     * @param entityClass 实体类
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T extends BaseEntity> T mapping(RecordEntity sourceObj, Class<T> entityClass) {
        return Pojos.mapping(sourceObj, entityClass, getFieldMapping(entityClass));
    }

    /**
     * 获得FieldMapping
     */
    private static <T extends BaseEntity> Pojos.FieldMapping getFieldMapping(Class<T> entityClass) {
        return FIELD_MAPPING_CACHE.computeIfAbsent(entityClass.getName(), clazz -> {
            List<FieldColumn> fieldColumns = getFieldColumns(new JavaEntityClass(entityClass));
            Pojos.FieldMapping fieldMapping = new Pojos.FieldMapping();
            for (FieldColumn fieldColumn : fieldColumns) {
                fieldMapping.field(fieldColumn.getColumn(), fieldColumn.getField());
            }
            return fieldMapping;
        });
    }

    /**
     * 把原始对象集合映射为对应类型的Pojo集合
     *
     * @param source 源
     * @param clazz  clazz
     * @return {@link List}<{@link T}>
     * @param <T> 泛型
     */
    public static <T extends BaseEntity> List<T> mappingArray(List<? extends Record> source, Class<T> clazz) {
        return Pojos.mappingArray(source, clazz, getFieldMapping(clazz));
    }

    /**
     * 字段与数据库字段的存储类
     */
    @Data
    public static class FieldColumn {

        /**
         * Entity字段名
         */
        private String field;
        /**
         * 表列名
         */
        private String column;
        /**
         * 是否是虚拟列
         */
        private Boolean visual;
    }
}
