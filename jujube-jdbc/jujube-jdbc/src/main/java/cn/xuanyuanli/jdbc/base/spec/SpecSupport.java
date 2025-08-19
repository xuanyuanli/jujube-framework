package cn.xuanyuanli.jdbc.base.spec;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 构建Spec的支持类
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2022/07/16
 */
public class SpecSupport {

    /**
     * 字段名
     */
    public String fieldName;
    /**
     * 值
     */
    public Object value;
    /**
     * 操作人
     */
    public Op operator;

    /**
     * 规范支持
     *
     * @param fieldName 字段名
     * @param operator  操作人
     * @param value     价值
     */
    public SpecSupport(String fieldName, Op operator, Object value) {
        this.fieldName = fieldName;
        this.value = value;
        this.operator = operator;
    }

    private static class FieldNameAndOperator {

        public String fieldName;
        public Op operator;

        public FieldNameAndOperator(String filedName, Op operator) {
            this.fieldName = filedName;
            this.operator = operator;
        }
    }

    /**
     * 把约定格式的key解析为QueryFilter
     *
     * @param searchParams searchParams中key的格式为OPERATOR_FIELDNAME
     * @return {@link Map}<{@link String}, {@link SpecSupport}>
     */
    public static Map<String, SpecSupport> parse(Map<String, Object> searchParams) {
        if (searchParams == null) {
            throw new IllegalArgumentException("param can not be null");
        }
        Map<String, SpecSupport> filters = Maps.newHashMap();
        for (Entry<String, Object> entry : searchParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // 从缓存中取出FieldNameAndOperator，如果存在，则直接使用
            FieldNameAndOperator fieldNameAndOperator = KEY_CACHE.computeIfAbsent(key, k -> {
                // 拆分operator与filedAttribute
                String[] names = StringUtils.splitByWholeSeparator(key, SEPARATOR);
                if (names.length != 2) {
                    throw new RuntimeException(key + "is not a valid filter name");
                }
                // names[0]是操作符；names[1]是字段名；value是字段值
                Op operator = Op.valueOf(names[0].toUpperCase());
                String filedName = names[1];
                return new FieldNameAndOperator(filedName, operator);
            });
            filters.put(key, new SpecSupport(fieldNameAndOperator.fieldName, fieldNameAndOperator.operator, value));
        }
        return filters;
    }

    /**
     * 两个下划线作为分隔符
     */
    static final String SEPARATOR = "__";

    /**
     * key缓存。可以省去一些解析为QueryFilter的时间
     */
    private static final ConcurrentHashMap<String, FieldNameAndOperator> KEY_CACHE = new ConcurrentHashMap<>();

    /**
     * 构建规格的操作符
     */
    public enum Op {
        /**
         * 等于
         */
        EQ,
        /**
         * like
         */
        LIKE,
        /**
         * not like
         */
        NOTLIKE,
        /**
         * 大于
         */
        GT,
        /**
         * 小于
         */
        LT,
        /**
         * 大于等于
         */
        GTE,
        /**
         * 小于等于
         */
        LTE,
        /**
         * not
         */
        NOT,
        /**
         * between,他的值为一个长度为2的数组
         */
        BETWEEN,
        /**
         * 为null
         */
        ISNULL,
        /**
         * 不为null
         */
        ISNOTNULL,
        /**
         * in
         */
        IN,
        /**
         * not in
         */
        NOTIN,
        /**
         * 为空
         */
        ISEMPTY,
        /**
         * 不为空
         */
        ISNOTEMPTY,
        /**
         * JSON_CONTAINS
         */
        JSON_CONTAINS,
        /**
         * or
         */
        OR,
        /**
         * and
         */
        AND,
        ;

        /**
         * 获得正确的查询形式
         *
         * @param op    操作符
         * @param field 字段
         * @return {@link String}
         */
        public static String join(Op op, String field) {
            return op + SpecSupport.SEPARATOR + field;
        }

    }

}
