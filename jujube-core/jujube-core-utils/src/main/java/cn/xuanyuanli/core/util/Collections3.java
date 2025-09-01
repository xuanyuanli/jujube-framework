package cn.xuanyuanli.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 增强集合操作工具类
 * <p>
 * 提供比 JDK {@link Collections} 和 Guava Collections2 更丰富的集合操作功能，
 * 特别针对业务开发中的常见场景进行优化：
 * <ul>
 * <li><strong>属性提取：</strong>从对象集合中提取特定属性，支持类型转换</li>
 * <li><strong>条件过滤：</strong>基于属性值进行对象筛选和查找</li>
 * <li><strong>集合转换：</strong>集合与 Map 的相互转换，支持自定义映射规则</li>
 * <li><strong>数据去重：</strong>保持顺序的去重操作</li>
 * <li><strong>分组聚合：</strong>按属性值对集合进行分组处理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>核心特性：</strong>
 * <ul>
 * <li>基于 Java 8 Stream API 实现，性能优异</li>
 * <li>与 {@link Beans} 工具类深度集成，支持反射属性访问</li>
 * <li>支持泛型，类型安全</li>
 * <li>空值安全处理</li>
 * <li>保持集合原有顺序（使用 LinkedHashSet 和 LinkedHashMap）</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 假设有用户对象列表
 * List<User> users = Arrays.asList(
 *     new User("张三", 25, "北京"),
 *     new User("李四", 30, "上海"),
 *     new User("王五", 25, "北京")
 * );
 * 
 * // 提取所有用户名称
 * List<String> names = Collections3.extractToListString(users, "name");
 * // 结果: ["张三", "李四", "王五"]
 * 
 * // 根据条件查找用户
 * User user = Collections3.getOne(users, "age", 25);
 * // 结果: 张三（第一个25岁的用户）
 * 
 * // 根据条件筛选用户
 * List<User> beijingUsers = Collections3.getPart(users, "city", "北京");
 * // 结果: [张三, 王五]
 * 
 * // 创建姓名到年龄的映射
 * Map<Object, Object> nameToAge = Collections3.extractToMap(users, "name", "age");
 * // 结果: {"张三": 25, "李四": 30, "王五": 25}
 * 
 * // 数组去重
 * String[] cities = {"北京", "上海", "北京", "广州"};
 * String[] uniqueCities = Collections3.toDiffArray(cities);
 * // 结果: ["北京", "上海", "广州"]
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>设计原则：</strong>
 * <ul>
 * <li><strong>易用性：</strong>简化常见集合操作，减少样板代码</li>
 * <li><strong>性能：</strong>基于 Stream API 和高效数据结构</li>
 * <li><strong>兼容性：</strong>与现有 JDK 和第三方集合工具互补</li>
 * <li><strong>安全性：</strong>空值检查和异常处理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>属性访问基于 {@link Beans} 工具类，需要目标对象有相应的 getter 方法</li>
 * <li>对于大数据量集合，建议使用并行流或考虑性能优化</li>
 * <li>返回的集合类型通常为 ArrayList，需要特定类型请进行转换</li>
 * <li>Map 提取操作会覆盖重复的 key，保留最后出现的值</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 * @see java.util.Collections
 * @see Beans
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Collections3 {

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List&lt;String&gt;. <br> 不同于Collections3，这里返回String集合
     *
     * @param collection   来源集合.
     * @param propertyName 要提取的属性名.
     * @return {@link List}<{@link String}>
     */
    public static List<String> extractToListString(final Collection<?> collection, final String propertyName) {
        return collection.stream().map(obj -> Beans.getPropertyAsString(obj, propertyName)).collect(Collectors.toList());
    }

    /**
     * 根据条件，从集合中取出一个
     *
     * @param coll      集合
     * @param fieldName 字段名
     * @param value     字段值
     * @param <T>       泛型
     * @return {@link T}
     */
    public static <T> T getOne(Collection<T> coll, String fieldName, Object value) {
        Objects.requireNonNull(coll);
        Optional<T> first = coll.stream().filter(t -> value.equals(Beans.getProperty(t, fieldName))).findFirst();
        return first.orElse(null);
    }

    /**
     * 根据条件，从集合中取出符合条件的部分
     *
     * @param coll      集合
     * @param fieldName 字段名
     * @param value     字段值
     * @param <T>       泛型
     * @return {@link List}<{@link T}>
     * @author xuanyuanli Email：xuanyuanli999@gmail.com
     */
    public static <T> List<T> getPart(Collection<T> coll, String fieldName, Object value) {
        Objects.requireNonNull(coll);
        return coll.stream().filter(t -> value.equals(Beans.getProperty(t, fieldName))).collect(Collectors.toList());
    }

    /**
     * 字符串数组去重
     *
     * @param s 年代
     * @return {@link String[]}
     */
    public static String[] toDiffArray(String[] s) {
        Set<String> set = new LinkedHashSet<>();
        Collections.addAll(set, s);
        return set.toArray(new String[]{});
    }

    /**
     * 提取集合中的对象的两个属性(通过Getter函数), 组合成Map.
     *
     * @param collection        来源集合.
     * @param keyPropertyName   要提取为Map中的Key值的属性名.
     * @param valuePropertyName 要提取为Map中的Value值的属性名.
     * @param <T>               泛型
     * @return {@link Map}<{@link Object}, {@link Object}>
     */
    public static <T> Map<Object, Object> extractToMap(final Collection<T> collection, final String keyPropertyName, final String valuePropertyName) {
        Map<Object, Object> map = new HashMap<>(collection.size());
        for (Object obj : collection) {
            map.put(Beans.getProperty(obj, keyPropertyName), Beans.getProperty(obj, valuePropertyName));
        }
        return map;
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
     *
     * @param collection   来源集合.
     * @param propertyName 要提取的属性名.
     * @param <T>          泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> extractToList(final Collection<?> collection, final String propertyName) {
        return extractToList(collection, propertyName, null);
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成List.
     *
     * @param collection   来源集合.
     * @param propertyName 要提取的属性名.
     * @param expectType   期望类型
     * @param <T>          泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> extractToList(final Collection<?> collection, final String propertyName, Class<T> expectType) {
        if (collection == null) {
            return null;
        }
        List<Object> list = new ArrayList<>(collection.size());
        for (Object obj : collection) {
            Object property = Beans.getProperty(obj, propertyName);
            if (expectType != null) {
                property = Beans.getExpectTypeValue(property, expectType);
            }
            list.add(property);
        }
        return (List<T>) list;
    }

    /**
     * 提取集合中的对象的一个属性(通过Getter函数), 组合成由分割符分隔的字符串.
     *
     * @param collection   来源集合.
     * @param propertyName 要提取的属性名.
     * @param separator    分隔符.
     * @return {@link String}
     */
    public static String extractToString(final Collection<?> collection, final String propertyName, final String separator) {
        List<String> list = extractToListString(collection, propertyName);
        return StringUtils.join(list, separator);
    }

    /**
     * 判断集合是否为空.
     *
     * @param collection 集合
     * @return boolean
     */
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null) || collection.isEmpty();
    }

    /**
     * 判断Map是否为空.
     *
     * @param map map
     * @return boolean
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null) || map.isEmpty();
    }

    /**
     * 判断集合是否为非空.
     *
     * @param collection 集合
     * @return boolean
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return (collection != null) && !(collection.isEmpty());
    }

    /**
     * 返回a+b的新List.
     *
     * @param a   一个
     * @param b   b
     * @param <T> 泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> union(final Collection<T> a, final Collection<T> b) {
        List<T> result = new ArrayList<>(a);
        result.addAll(b);
        return result;
    }

    /**
     * 返回a-b(集合a中有，而b中没有)的新List.
     *
     * @param a   一个
     * @param b   b
     * @param <T> 泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> subtract(final Collection<T> a, final Collection<T> b) {
        return a.stream().filter(t -> !b.contains(t)).collect(Collectors.toList());
    }

    /**
     * 返回a与b的交集的新List.
     *
     * @param a   一个
     * @param b   b
     * @param <T> 泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        List<T> list = new ArrayList<>();

        for (T element : a) {
            if (b.contains(element)) {
                list.add(element);
            }
        }
        return list;
    }

    /**
     * 枚举列表
     *
     * @param enumeration 枚举
     * @param <T>         泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> enumerationToList(Enumeration<T> enumeration) {
        if (enumeration == null) {
            return null;
        }
        List<T> list = new ArrayList<>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }

    /**
     * 根据key排序map
     *
     * @param map        map
     * @param comparator 比较器
     * @param <K>        泛型
     * @param <V>        泛型
     * @return {@link Map}<{@link K}, {@link V}>
     */
    public static <K, V> Map<K, V> sortMapByKey(Map<K, V> map, Comparator<K> comparator) {
        Map<K, V> result = new LinkedHashMap<>();
        List<Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((o1, o2) -> comparator.compare(o1.getKey(), o2.getKey()));
        for (Entry<K, V> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 根据value排序map
     *
     * @param map        map
     * @param comparator 比较器
     * @param <K>        泛型
     * @param <V>        泛型
     * @return {@link Map}<{@link K}, {@link V}>
     */
    public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map, Comparator<V> comparator) {
        Map<K, V> result = new LinkedHashMap<>();
        List<Entry<K, V>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort((o1, o2) -> comparator.compare(o1.getValue(), o2.getValue()));
        for (Entry<K, V> entry : entryList) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 是否存在集合中的字段值为 detectVal(只用于判断基本类型)
     *
     * @param source    源
     * @param fieldName 字段名
     * @param detectVal 检测值
     * @return boolean
     */
    public static boolean containsFieldValue(Collection<?> source, String fieldName, Object detectVal) {
        if (source == null) {
            return false;
        }
        return source.stream().anyMatch(t -> detectVal.equals(Beans.getProperty(t, fieldName)));
    }

    /**
     * 根据某个字段去重
     *
     * @param list     列表
     * @param function 函数
     * @param <T>      泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> deWeight(List<T> list, Function<T, ?> function) {
        Set<Object> uids = new HashSet<>();
        return list.stream().filter(u -> {
            if (uids.contains(function.apply(u))) {
                return false;
            }
            uids.add(function.apply(u));
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 新哈希map
     *
     * @param keyValue 键价值
     * @param <V>      泛型
     * @return {@link Map}<{@link String}, {@link V}>
     */
    public static <V> Map<String, V> newHashMap(Object... keyValue) {
        Map<String, V> map = new HashMap<>(16);
        if (keyValue == null || keyValue.length == 0) {
            return map;
        }
        if (keyValue.length % 2 != 0) {
            throw new IllegalArgumentException("参数必须成对出现");
        }
        for (int i = 0; i < keyValue.length; i += 2) {
            map.put(String.valueOf(keyValue[i]), (V) keyValue[i + 1]);
        }
        return map;
    }


    /**
     * 迭代器转换为List
     *
     * @param iterator 迭代器
     * @param <T>      泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> getListFromIterator(Iterator<T> iterator) {
        // Convert iterator to iterable
        Iterable<T> iterable = () -> iterator;
        // Create a List from the Iterable
        // Return the List
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }
}
