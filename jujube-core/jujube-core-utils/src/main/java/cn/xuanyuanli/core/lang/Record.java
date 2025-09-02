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
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class Record extends HashMap<String, Object> {

    /**
     * 序列化版本UID，用于确保序列化兼容性
     */
    @Serial
    private static final long serialVersionUID = -5745367570456272792L;

    /**
     * 构造一个空的Record对象
     */
    public Record() {
    }

    /**
     * 使用指定的Map构造Record对象
     *
     * @param map 初始化数据的Map对象
     */
    public Record(Map<String, Object> map) {
        if (map != null) {
            putAll(map);
        }
    }

    /**
     * 构造指定初始容量的Record对象
     *
     * @param size 初始容量大小
     */
    public Record(int size) {
        super(size);
    }

    /**
     * 获取字符串类型的字段值，适用于 varchar、char、enum、set、text、tinytext、mediumtext、longtext 等MySQL类型
     *
     * @param column 字段名
     * @return 字符串值，如果字段不存在则返回null
     */
    public String getStr(String column) {
        Object val = get(column);
        if (val == null) {
            return null;
        }
        return String.valueOf(val);
    }

    /**
     * 获取字符串类型的字段值，如果字段不存在则返回默认值
     *
     * @param column     字段名
     * @param defaultStr 默认字符串值
     * @return 字符串值，如果字段不存在则返回默认值
     */
    public String getStr(String column, String defaultStr) {
        Object val = get(column);
        if (val == null) {
            return defaultStr;
        }
        return String.valueOf(val);
    }

    /**
     * 获取整数类型的字段值，适用于 int、integer、tinyint(n) n > 1、smallint、mediumint 等MySQL类型
     *
     * @param column 字段名
     * @return 整数值，如果转换失败则返回0
     */
    public Integer getInt(String column) {
        return NumberUtils.toInt(String.valueOf(get(column)));
    }

    /**
     * 获取整数类型的字段值，如果转换失败则返回默认值
     *
     * @param column 字段名
     * @param def    默认整数值
     * @return 整数值，如果转换失败则返回默认值
     */
    public Integer getInt(String column, int def) {
        return NumberUtils.toInt(String.valueOf(get(column)), def);
    }

    /**
     * 获取长整数类型的字段值，适用于 bigint 等MySQL类型
     *
     * @param column 字段名
     * @return 长整数值，如果转换失败则返回0L
     */
    public Long getLong(String column) {
        return NumberUtils.toLong(String.valueOf(get(column)));
    }

    /**
     * 获取长整数类型的字段值，如果转换失败则返回默认值
     *
     * @param column 字段名
     * @param def    默认长整数值
     * @return 长整数值，如果转换失败则返回默认值
     */
    public Long getLong(String column, long def) {
        return NumberUtils.toLong(String.valueOf(get(column)), def);
    }

    /**
     * 获取大整数类型的字段值，适用于 unsigned bigint 等MySQL类型
     *
     * @param column 字段名
     * @return 大整数值，可能为null
     */
    public java.math.BigInteger getBigInteger(String column) {
        return (java.math.BigInteger) get(column);
    }

    /**
     * 获取日期类型的字段值，适用于 date、year 等MySQL类型
     *
     * @param column 字段名
     * @return 日期对象，如果字段不存在或类型不匹配则返回null
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
     * 获取双精度浮点数类型的字段值，适用于 real、double 等MySQL类型
     *
     * @param column 字段名
     * @return 双精度浮点数值，如果转换失败则返回0.0
     */
    public Double getDouble(String column) {
        return NumberUtils.toDouble(String.valueOf(get(column)));
    }

    /**
     * 获取单精度浮点数类型的字段值，适用于 float 等MySQL类型
     *
     * @param column 字段名
     * @return 单精度浮点数值，如果转换失败则返回0.0f
     */
    public Float getFloat(String column) {
        return NumberUtils.toFloat(String.valueOf(get(column)));
    }

    /**
     * 获取布尔类型的字段值，适用于 bit、tinyint(1) 等MySQL类型
     *
     * @param column 字段名
     * @return 布尔值，空字符串、null、"0"、"false"视为false，其他为true
     */
    public Boolean getBoolean(String column) {
        String value = String.valueOf(get(column));
        return !("".equals(value) || "null".equals(value) || "0".equals(value) || "false".equals(value));
    }

    /**
     * 获取高精度小数类型的字段值，适用于 decimal、numeric 等MySQL类型
     *
     * @param column 字段名
     * @return 高精度小数对象
     * @throws NumberFormatException 如果字段值无法转换为BigDecimal
     */
    public BigDecimal getBigDecimal(String column) {
        return new BigDecimal(String.valueOf(get(column)));
    }

    /**
     * 获取字节数组类型的字段值，适用于 binary、varbinary、tinyblob、blob、mediumblob、longblob 等MySQL类型
     *
     * @param column 字段名
     * @return 字节数组，可能为null
     */
    public byte[] getBytes(String column) {
        return (byte[]) get(column);
    }

    /**
     * 获取任意数值类型的字段值，适用于所有继承自Number的类型
     *
     * @param column 字段名
     * @return Number对象，可能为null
     */
    public Number getNumber(String column) {
        return (Number) get(column);
    }

    /**
     * 获取ID字段的值，等同于调用getLong("id")
     *
     * @return ID值，如果转换失败则返回0L
     */
    public Long getId() {
        return getLong("id");
    }

    /**
     * 获取Record列表类型的字段值
     *
     * @param key 字段名
     * @return Record对象列表，如果字段不存在或类型不匹配则返回null
     */
    public List<Record> getListRecord(String key) {
        return getList(key, Record::valueOf);
    }

    /**
     * 通用列表获取方法，将字段值转换为指定类型的列表
     *
     * @param key      字段名
     * @param function 类型转换函数
     * @param <T>      列表元素类型
     * @return 转换后的列表，如果字段不存在或类型不匹配则返回null
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
     * 获取字符串列表类型的字段值
     *
     * @param key 字段名
     * @return 字符串列表，如果字段不存在或类型不匹配则返回null
     */
    public List<String> getListString(String key) {
        return getList(key, String::valueOf);
    }

    /**
     * 设置字段值，支持链式调用
     *
     * @param key   字段名
     * @param value 字段值
     * @return 当前Record对象，支持链式调用
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
     * 获取嵌套的Record对象
     *
     * @param key 字段名
     * @return Record对象，通过valueOf方法转换字段值得到
     */
    public Record getRecord(String key) {
        return valueOf(get(key));
    }

    /**
     * 将Bean对象转换为Map，所有字段名都由驼峰命名转为下划线格式
     *
     * @param javaBean Java Bean对象
     * @return 转换后的Map，字段名采用下划线格式，如果对象为null或基本类型则返回null
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
     * 将当前Record转换为指定类型的实体对象
     *
     * @param tClass 目标实体类的Class对象
     * @param <T>    目标实体类型
     * @return 转换后的实体对象
     */
    public <T> T toEntity(Class<T> tClass) {
        return Pojos.mapping(this, tClass);
    }

    /**
     * 将对象转换为Record，允许对象为null
     *
     * @param obj 待转换的对象，可以为null
     * @return 转换后的Record对象，如果输入对象为null则返回null
     */
    public static Record valueOfNullable(Object obj) {
        if (obj == null) {
            return null;
        }
        return new Record(convertBeanToMap(obj));
    }

}
