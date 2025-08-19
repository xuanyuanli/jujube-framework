package cn.xuanyuanli.core.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.core.lang.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Json工具
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Jsons {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(Jsons.class);
    /**
     * 对象映射器
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        // 反序列化的时候，如果类中不存在属性，设置为忽略；如果不用这个设置，可以使用 @JsonIgnoreProperties注解类(或
        // @JsonIgnore注解字段)到对应字段
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS, true);
        OBJECT_MAPPER.configure(Feature.ALLOW_SINGLE_QUOTES, true);

        // 添加序列化方案
        OBJECT_MAPPER.registerModule(new GuavaModule());
        OBJECT_MAPPER.registerModule(new JodaModule());
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        // Guava的Table没有反序列化方案，只能使用自定义方案了
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Table.class, new TableDeserializer());
        OBJECT_MAPPER.registerModule(simpleModule);
    }

    /**
     * Guavua Table的反序列化方案
     */
    public static class TableDeserializer extends JsonDeserializer<Table<?, ?, ?>> {

        @Override
        public Table<?, ?, ?> deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException {
            final ImmutableTable.Builder<Object, Object, Object> tableBuilder = ImmutableTable.builder();
            @SuppressWarnings("unchecked") final Map<Object, Map<Object, Object>> rowMap = jp.readValueAs(Map.class);
            for (final Map.Entry<Object, Map<Object, Object>> rowEntry : rowMap.entrySet()) {
                final Object rowKey = rowEntry.getKey();
                for (final Map.Entry<Object, Object> cellEntry : rowEntry.getValue().entrySet()) {
                    final Object colKey = cellEntry.getKey();
                    final Object val = cellEntry.getValue();
                    tableBuilder.put(rowKey, colKey, val);
                }
            }
            return tableBuilder.build();
        }

    }

    /**
     * 将对象转换为json字符串
     *
     * @param object 对象
     * @return {@link String}
     */
    public static String toJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            logger.error("write to json string error:{}", object, e);
        }
        return null;
    }

    /**
     * 将对象转换为格式化的json字符串
     *
     * @param object 对象
     * @return {@link String}
     */
    public static String toPrettyJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("write to json string error:{}", object, e);
        }
        return null;
    }

    /**
     * 将json字符串转换为Map
     *
     * @param text 文本
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> parseJsonToMap(String text) {
        return parseJsonToMap(text, String.class, Object.class);
    }

    /**
     * 将json字符串转换为Record
     *
     * @param text 文本
     * @return {@link Record}
     */
    public static Record parseJsonToRecord(String text) {
        return Record.valueOf(parseJsonToMap(text));
    }

    /**
     * 将json字符串转换为Map
     *
     * @param info       信息
     * @param keyClazz   键clazz
     * @param valueClazz 值clazz
     * @return {@link Map}<{@link K}, {@link V}>
     */
    public static <K, V> Map<K, V> parseJsonToMap(String info, Class<K> keyClazz, Class<V> valueClazz) {
        return parseJson(info, TypeFactory.defaultInstance().constructMapType(Map.class, keyClazz, valueClazz));
    }

    /**
     * 将json字符串转换为List
     *
     * @param info  信息
     * @param clazz clazz
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> parseJsonToList(String info, Class<T> clazz) {
        return parseJson(info, TypeFactory.defaultInstance().constructCollectionType(List.class, clazz));
    }

    /**
     * 将json字符串转换为List-Map
     *
     * @param text 文本
     * @return {@link List}<{@link Map}<{@link String},{@link Object}>>
     */
    public static List<Map<String,Object>> parseJsonToListMap(String text) {
        return parseJson(text, new TypeReference<List<Map<String,Object>>>() {
        });
    }

    /**
     * 将json字符串转换为List Long
     *
     * @param text 文本
     * @return {@link List}<{@link Long}>
     */
    public static List<Long> parseJsonToListLong(String text) {
        return parseJsonToList(text, Long.class);
    }

    /**
     * 将json字符串转换为List String
     *
     * @param text 文本
     * @return {@link List}<{@link String}>
     */
    public static List<String> parseJsonToListString(String text) {
        return parseJson(text, new TypeReference<List<String>>() {
        });
    }

    /**
     * 将json字符串转换为复杂类型（如泛型）的Java对象（万能，常用）
     *
     * <pre>
     * 用法：
     *  1、将json字符串转换为User对象：parseJson(text,new TypeReference&lt;User&gt;(){})
     *  2、将json字符串转换为List&lt;String&gt;的泛型：parseJson(text,new TypeReference&lt;List&lt;String&gt;&gt;(){})
     *  3、将json字符串转换为List&lt;Map&lt;String,Object&gt;&gt;的泛型：parseJson(text,new TypeReference&lt;List&lt;Map&lt;String,Object&gt;&gt;&gt;(){})
     * </pre>
     *
     * @param text          json字符串
     * @param typeReference 类型引用
     * @param <T>           泛型
     * @return {@link T}
     */
    public static <T> T parseJson(String text, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (IOException e) {
            logger.error("parseJson", e);
        }
        return null;
    }

    /**
     * 将json字符串转换为对应类型的Java对象（不常用）
     *
     * @param text 文本
     * @param type 类型
     * @param <T>  泛型
     * @return {@link T}
     */
    public static <T> T parseJson(String text, Type type) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, TypeFactory.defaultInstance().constructType(type));
        } catch (IOException e) {
            logger.error("parseJson", e);
        }
        return null;
    }

    /**
     * 将json字符串转换为对应类型的Java对象（常用）
     *
     * @param text  文本
     * @param clazz clazz
     * @param <T>   泛型
     * @return {@link T}
     */
    public static <T> T parseJson(String text, Class<T> clazz) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            logger.error("parseJson", e);
        }
        return null;
    }

    /**
     * 读树
     *
     * @param text 文本
     * @return {@link JsonNode}
     */
    public static JsonNode readTree(String text) {
        try {
            return OBJECT_MAPPER.readTree(text);
        } catch (IOException e) {
            logger.error("readTree", e);
        }
        return null;
    }

    /**
     * 把json转换为pretty输出格式
     *
     * @param json json
     * @return {@link String}
     */
    public static String prettyPrint(String json) {
        return toPrettyJson(parseJsonToMap(json));
    }

}
