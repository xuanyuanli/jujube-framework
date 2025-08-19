package cn.xuanyuanli.core.lang;

import java.io.Serial;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Pojos;

/**
 * 一个进阶版的Map，一般作为数据库表的一行数据存在。注意他与Map的最大区别是：如果把Bean转换为Record，则默认驼峰命名变为下划线命名方式
 *
 * @author John Li
 * @date 2021/09/01
 */
public class Record extends HashMap<String, Object> {

    /**
     * 串行版本uid
     */
    @Serial
    private static final long serialVersionUID = -5745367570456272792L;

    /**
     * 记录
     */
    public Record() {
    }

    /**
     * 记录
     *
     * @param map 地图
     */
    public Record(Map<String, Object> map) {
        if (map != null) {
            putAll(map);
        }
    }

    /**
     * 记录
     *
     * @param size 大小
     */
    public Record(int size) {
        super(size);
    }

    /**
     * Get column of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
     *
     * @param column 列
     * @return {@link String}
     */
    public String getStr(String column) {
        Object val = get(column);
        if (val == null) {
            return null;
        }
        return String.valueOf(val);
    }

    /**
     * 获得str
     *
     * @param column     列
     * @param defaultStr 默认str
     * @return {@link String}
     */
    public String getStr(String column, String defaultStr) {
        Object val = get(column);
        if (val == null) {
            return defaultStr;
        }
        return String.valueOf(val);
    }

    /**
     * Get column of mysql type: int, integer, tinyint(n) n &gt; 1, smallint, mediumint
     *
     * @param column 列
     * @return {@link Integer}
     */
    public Integer getInt(String column) {
        return NumberUtils.toInt(String.valueOf(get(column)));
    }

    /**
     * 获得int
     *
     * @param column 列
     * @param def    def
     * @return {@link Integer}
     */
    public Integer getInt(String column, int def) {
        return NumberUtils.toInt(String.valueOf(get(column)), def);
    }

    /**
     * Get column of mysql type: bigint
     *
     * @param column 列
     * @return {@link Long}
     */
    public Long getLong(String column) {
        return NumberUtils.toLong(String.valueOf(get(column)));
    }

    /**
     * 获得长
     *
     * @param column 列
     * @param def    def
     * @return {@link Long}
     */
    public Long getLong(String column, long def) {
        return NumberUtils.toLong(String.valueOf(get(column)), def);
    }

    /**
     * Get column of mysql type: unsigned bigint
     *
     * @param column 列
     * @return {@link java.math.BigInteger}
     */
    public java.math.BigInteger getBigInteger(String column) {
        return (java.math.BigInteger) get(column);
    }

    /**
     * Get column of mysql type: date, year
     *
     * @param column 列
     * @return {@link java.util.Date}
     */
    public java.util.Date getDate(String column) {
        Object val = get(column);
        if (val instanceof Date date) {
            return new java.util.Date(date.getTime());
        } else if (val instanceof java.util.Date) {
            return (java.util.Date) val;
        }
        return null;
    }

    /**
     * Get column of mysql type: real, double
     *
     * @param column 列
     * @return {@link Double}
     */
    public Double getDouble(String column) {
        return NumberUtils.toDouble(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: float
     *
     * @param column 列
     * @return {@link Float}
     */
    public Float getFloat(String column) {
        return NumberUtils.toFloat(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: bit, tinyint(1)
     *
     * @param column 列
     * @return {@link Boolean}
     */
    public Boolean getBoolean(String column) {
        String value = String.valueOf(get(column));
        return !("".equals(value) || "null".equals(value) || "0".equals(value) || "false".equals(value));
    }

    /**
     * Get column of mysql type: decimal, numeric
     *
     * @param column 列
     * @return {@link BigDecimal}
     */
    public BigDecimal getBigDecimal(String column) {
        return new BigDecimal(String.valueOf(get(column)));
    }

    /**
     * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob I have not finished the test.
     *
     * @param column 列
     * @return {@link byte[]}
     */
    public byte[] getBytes(String column) {
        return (byte[]) get(column);
    }

    /**
     * Get column of any type that extends from Number
     *
     * @param column 列
     * @return {@link Number}
     */
    public Number getNumber(String column) {
        return (Number) get(column);
    }

    /**
     * get id
     *
     * @return {@link Long}
     */
    public Long getId() {
        return getLong("id");
    }

    /**
     * get list record
     *
     * @param key 键
     * @return {@link List}<{@link Record}>
     */
    public List<Record> getListRecord(String key) {
        return getList(key, Record::valueOf);
    }

    /**
     * 获得列表
     *
     * @param key      键
     * @param function 函数
     * @return {@link List}<{@link T}>
     */
    private <T> List<T> getList(String key, Function<Object, T> function) {
        Object o = get(key);
        if (o instanceof List<?> list) {
            return list.stream().map(function).collect(Collectors.toList());
        } else if (o instanceof Set<?> list) {
            return list.stream().map(function).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * get list
     *
     * @param key 键
     * @return {@link List}<{@link String}>
     */
    public List<String> getListString(String key) {
        return getList(key, String::valueOf);
    }

    /**
     * 设置
     *
     * @param key   键
     * @param value 价值
     * @return {@link Record}
     */
    public Record set(String key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * Bean对象转换为Record,所有字段名都由驼峰转为下划线格式
     *
     * @param obj obj
     * @return {@link Record}
     */
    public static Record valueOf(Object obj) {
        return new Record(convertBeanToMap(obj));
    }

    /**
     * 获得记录
     *
     * @param key 键
     * @return {@link Record}
     */
    public Record getRecord(String key) {
        return valueOf(get(key));
    }

    /**
     * Bean对象转换为Map,所有字段名都由驼峰转为下划线格式
     *
     * @param javaBean java bean
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    private static Map<String, Object> convertBeanToMap(Object javaBean) {
        if (javaBean == null || javaBean.getClass().isPrimitive()) {
            return null;
        }
        if (javaBean instanceof Map) {
            return (Map<String, Object>) javaBean;
        }
        Field[] fields = javaBean.getClass().getDeclaredFields();
        Map<String, Object> result = new HashMap<>(fields.length);
        for (Field field : fields) {
            Object value;
            try {
                value = Beans.getProperty(javaBean, field.getName());
            } catch (Exception e) {
                continue;
            }
            if (value == null) {
                continue;
            }
            String dbField = CamelCase.toUnderlineName(field.getName());
            result.put(dbField, value);
        }
        return result;
    }

    /**
     * 实体
     *
     * @param tClass t类
     * @param <T>    泛型
     * @return {@link T}
     */
    public <T> T toEntity(Class<T> tClass) {
        return Pojos.mapping(this, tClass);
    }

    /**
     * 值可以为空
     *
     * @param obj obj
     * @return {@link Record}
     */
    public static Record valueOfNullable(Object obj) {
        if (obj == null) {
            return null;
        }
        return new Record(convertBeanToMap(obj));
    }

}
