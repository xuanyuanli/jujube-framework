package cn.xuanyuanli.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;

/**
 * Java Bean 操作工具类
 * <p>
 * 提供全面的 Bean 操作功能，包括反射操作、属性访问、类型转换、泛型处理等。
 * 主要功能涵盖：
 * <ul>
 * <li><strong>Bean 属性操作：</strong>获取、设置、复制 Bean 属性值</li>
 * <li><strong>反射工具：</strong>方法调用、字段访问、类型检查</li>
 * <li><strong>泛型处理：</strong>泛型类型解析、参数化类型获取</li>
 * <li><strong>类型转换：</strong>自动类型转换和兼容性处理</li>
 * <li><strong>缓存优化：</strong>反射结果缓存，提升性能</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>技术架构：</strong>
 * <ul>
 * <li>基于 Apache Commons BeanUtils 进行 Bean 操作</li>
 * <li>使用 Spring CGLIB 的 BeanMap 进行对象映射</li>
 * <li>集成 Spring Core 的参数名发现机制</li>
 * <li>自定义泛型类型解析算法</li>
 * <li>多级缓存机制优化反射性能</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>缓存机制：</strong>
 * <ul>
 * <li>{@code PROPERTY_DESCRIPTOR_CACHE} - 属性描述符缓存</li>
 * <li>{@code METHOD_CACHE} - 方法缓存</li>
 * <li>{@code DECLARED_METHOD_CACHE} - 声明方法缓存</li>
 * <li>{@code SELF_DECLARED_METHOD_CACHE} - 自身声明方法缓存</li>
 * <li>{@code BEANINFO_CACHE} - BeanInfo 缓存</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // Bean 属性操作
 * Object value = Beans.getProperty(bean, "propertyName");
 * Beans.setProperty(bean, "propertyName", newValue);
 * String strValue = Beans.getPropertyAsString(bean, "propertyName");
 * 
 * // Bean 复制
 * Beans.copyProperties(sourceBean, targetBean);
 * 
 * // 反射操作
 * Method method = Beans.getMethod(clazz, "methodName", paramTypes);
 * Object result = Beans.invokeMethod(bean, method, args);
 * 
 * // 泛型类型处理
 * Class<?> genericType = Beans.getGenericClass(field, 0);
 * Type[] actualTypes = Beans.getActualTypeArguments(parameterizedType);
 * 
 * // 类型转换
 * Integer intValue = Beans.convertValue(stringValue, Integer.class);
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>性能优化：</strong>
 * <ul>
 * <li>使用 {@link ConcurrentHashMap} 实现线程安全的缓存</li>
 * <li>通过 {@link AtomicReference} 支持空值缓存</li>
 * <li>避免重复的反射操作，提升运行时性能</li>
 * <li>支持递归调用的缓存设计</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>注意事项：</strong>
 * <ul>
 * <li>所有异常都被捕获并转换为 {@link RuntimeException}</li>
 * <li>属性访问优先使用 Getter/Setter 方法，其次直接字段访问</li>
 * <li>泛型类型解析支持复杂的嵌套泛型结构</li>
 * <li>类型转换基于 Apache Commons BeanUtils 的转换机制</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 * @see org.apache.commons.beanutils.BeanUtils
 * @see org.springframework.cglib.beans.BeanMap
 * @see java.beans.Introspector
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Beans {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(Beans.class);

    /**
     * PropertyDescriptor的缓存。key为classname+fieldName,value使用AtomicReference包装，因为ConcurrentMap不支持空值
     */
    private static final ConcurrentMap<String, AtomicReference<PropertyDescriptor>> PROPERTY_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();
    /**
     * ConcurrentMap.computeIfAbsent不能递归调用，所以建立三个方法的缓存
     */
    private static final ConcurrentMap<String, AtomicReference<Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    /**
     * 声明方法缓存
     */
    private static final ConcurrentMap<String, AtomicReference<Method>> DECLARED_METHOD_CACHE = new ConcurrentHashMap<>();
    /**
     * 自我声明方法缓存
     */
    private static final ConcurrentMap<String, AtomicReference<Method>> SELF_DECLARED_METHOD_CACHE = new ConcurrentHashMap<>();

    /**
     * beaninfo缓存
     */
    private static final ConcurrentMap<String, BeanInfo> BEANINFO_CACHE = new ConcurrentHashMap<>();

    /**
     * 缓存字段名
     */
    private static final ConcurrentMap<String, List<String>> FIELDNAMES_CACHE = new ConcurrentHashMap<>();

    /**
     * classgenerictype缓存
     */
    private static final ConcurrentMap<String, Class<?>> CLASSGENERICTYPE_CACHE = new ConcurrentHashMap<>();

    /**
     * 字段缓存
     */
    private static final ConcurrentMap<String, AtomicReference<Field>> FIELD_CACHE = new ConcurrentHashMap<>();
    /**
     * 自我字段缓存
     */
    private static final ConcurrentMap<String, AtomicReference<Field>> SELF_FIELD_CACHE = new ConcurrentHashMap<>();
    /**
     * ParameterNameDiscoverer的对象
     */
    private final static ParameterNameDiscoverer DISCOVERER = new DefaultParameterNameDiscoverer();
    /**
     * 基本类型封装类列表
     */
    private final static List<Class<?>> BASIC_TYPE = new ArrayList<>(Arrays.asList(Double.class, String.class, Float.class, Byte.class, Integer.class, Character.class,
            Long.class, Short.class, Boolean.class));
    /**
     * 转换工具类
     */
    private final static ConvertUtilsBean CONVERT_UTILS_BEAN = BeanUtilsBean.getInstance().getConvertUtils();
    /**
     * errlog
     */
    private static int errlog = 0;

    /**
     * 把对象转换为map
     *
     * @param obj obj
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> beanToMap(Object obj) {
        return beanToMap(obj, false);
    }

    /**
     * 把对象转换为map（Cglib的BeanMap性能最高）
     *
     * @param obj        obj
     * @param filterNull 滤波器零
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> beanToMap(Object obj, boolean filterNull) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        BeanMap beanMap = BeanMap.create(obj);
        Map<String, Object> hashMap = new HashMap<>(beanMap.size());
        for (Object key : beanMap.keySet()) {
            if ("class".equals(key)) {
                continue;
            }
            Object value = beanMap.get(key);
            if (filterNull) {
                if (value != null) {
                    hashMap.put((String) key, value);
                }
            } else {
                hashMap.put((String) key, value);
            }
        }
        return hashMap;
    }

    /**
     * 根据类获得实例
     *
     * @param cl cl
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T> T getInstance(Class<T> cl) {
        try {
            return cl.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(cl.getName(), e);
        }
    }

    /**
     * 根据Class的完整限定名装配Class
     *
     * @param className 类名
     * @return {@link Class}<{@link ?}>
     */
    public static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 自己实现的set方法(解决链式调用后setProperty不管用的情况)
     *
     * @param bean  豆
     * @param name  名字
     * @param value 价值
     */
    public static void setProperty(Object bean, String name, Object value) {
        PropertyDescriptor descriptor = getPropertyDescriptor(bean.getClass(), name);
        if (descriptor == null) {
            throw new RuntimeException(Texts.format("类中[{}]没有找到此属性[{}]", bean.getClass(), name));
        }
        Class<?> type = descriptor.getPropertyType();
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod != null) {
            invoke(writeMethod, bean, getExpectTypeValue(value, type));
        }
    }

    /**
     * 值类型转换，copy自BeanUtils.convert
     *
     * @param value 价值
     * @param type  类型
     * @return {@link T}
     */
    private static <T> T convert(final Object value, final Class<T> type) {
        T newValue = null;
        if (value instanceof String) {
            newValue = (T) CONVERT_UTILS_BEAN.convert((String) value, type);
        } else {
            final Converter converter = CONVERT_UTILS_BEAN.lookup(type);
            if (converter != null) {
                newValue = converter.convert(type, value);
            } else if (type.isAssignableFrom(value.getClass())) {
                newValue = (T) value;
            }
        }
        return newValue;
    }

    /**
     * 自己实现的getter方法(解决字段第二个字母为大写的情况)
     *
     * @param bean 豆
     * @param name 名字
     * @return {@link Object}
     */
    public static Object getProperty(Object bean, String name) {
        if (bean instanceof Map) {
            return ((Map<?, ?>) bean).get(name);
        }
        PropertyDescriptor propertyDescriptor = Beans.getPropertyDescriptor(bean.getClass(), name);
        if (propertyDescriptor != null && propertyDescriptor.getReadMethod() != null) {
            return invoke(propertyDescriptor.getReadMethod(), bean);
        }
        return null;
    }

    /**
     * 通过getter方法来获取转换为String后的指
     *
     * @param bean 豆
     * @param name 名字
     * @return {@link String}
     */
    public static String getPropertyAsString(Object bean, String name) {
        return convert(getProperty(bean, name), String.class);
    }

    /**
     * 获得所有的public方法
     *
     * @param cl             cl
     * @param methodName     方法名称
     * @param parameterTypes 参数类型
     * @return {@link Method}
     */
    public static Method getMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        String key = cl.getName() + "." + methodName + "(" + StringUtils.join(parameterTypes, ",") + ")";
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            Method method = null;
            try {
                method = cl.getMethod(methodName, parameterTypes);
            } catch (Exception ignored) {
            }
            return new AtomicReference<>(method);
        }).get();
    }

    /**
     * 获得类的所有声明方法，包括父类和接口中的
     *
     * @param clazz          clazz
     * @param methodName     方法名称
     * @param parameterTypes 参数类型
     * @return {@link Method}
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        String key = clazz.getName() + "." + methodName + "(" + StringUtils.join(parameterTypes, ",") + ")";
        return DECLARED_METHOD_CACHE.computeIfAbsent(key, k -> {
            Method method = null;
            try {
                method = getSelfDeclaredMethod(clazz, methodName, parameterTypes);
                if (method == null) {
                    List<Class<?>> classList = getAllInterfacesAndParentClass(clazz);
                    for (Class<?> aClass : classList) {
                        method = getSelfDeclaredMethod(aClass, methodName, parameterTypes);
                        if (method != null) {
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            return new AtomicReference<>(method);
        }).get();
    }

    /**
     * 获得所有接口和父类
     *
     * @param clazz clazz
     * @return {@link List}<{@link Class}<{@link ?}>>
     */
    public static List<Class<?>> getAllInterfacesAndParentClass(Class<?> clazz) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        Class<?> curClazz = clazz;
        while (true) {
            curClazz = curClazz.getSuperclass();
            if (curClazz != null) {
                classes.add(curClazz);
                getInterfaces(curClazz, classes);
            } else {
                break;
            }
        }
        getInterfaces(clazz, classes);
        return new ArrayList<>(classes);
    }

    /**
     * 获得接口
     *
     * @param clazz   clazz
     * @param classes 类
     */
    private static void getInterfaces(Class<?> clazz, Set<Class<?>> classes) {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            classes.add(anInterface);
            getInterfaces(anInterface, classes);
        }
    }

    /**
     * 获得类的所有声明方法，不包括父类中的
     *
     * @param cl             cl
     * @param methodName     方法名称
     * @param parameterTypes 参数类型
     * @return {@link Method}
     */
    public static Method getSelfDeclaredMethod(Class<?> cl, String methodName, Class<?>... parameterTypes) {
        String key = cl.getName() + "." + methodName + "(" + StringUtils.join(parameterTypes, ",") + ")";
        return SELF_DECLARED_METHOD_CACHE.computeIfAbsent(key, k -> {
            Method method = null;
            try {
                method = cl.getDeclaredMethod(methodName, parameterTypes);
            } catch (Exception ignored) {
            }
            return new AtomicReference<>(method);
        }).get();
    }

    /**
     * 反射调用方法
     *
     * @param method 方法
     * @param obj    obj
     * @param args   arg游戏
     * @return {@link Object}
     */
    public static Object invoke(Method method, Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反射调用default方法(不支持default方法中再次调用本接口的其他default方法)
     *
     * @param method 方法
     * @param args   方法参数
     * @return {@link Object}
     */
    public static Object invokeDefaultMethod(Method method, Object... args) {
        try {
            return invokeDefaultMethod(null, method, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反射调用default方法
     *
     * @param proxy  一般为接口子对象
     * @param method 方法
     * @param args   方法参数
     * @return {@link Object}
     * @throws Throwable throwable
     */
    public static Object invokeDefaultMethod(Object proxy, Method method, Object... args) throws Throwable {
        Class<?> clazz = method.getDeclaringClass();
        Object o;
        try {
            o = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                    (p, m, a) -> MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).unreflectSpecial(m, clazz).bindTo(proxy == null ? p : proxy)
                            .invokeWithArguments(args));
        } catch (Exception e) {
            if (errlog == 0) {
                logger.error("default classloader:{},clazz classloader:{},error:{}", Beans.getDefaultClassLoader(), clazz.getClassLoader(), e.getMessage());
                errlog = 1;
            }
            o = Proxy.newProxyInstance(getDefaultClassLoader(), new Class[]{clazz},
                    (p, m, a) -> MethodHandles.privateLookupIn(clazz, MethodHandles.lookup()).unreflectSpecial(m, clazz).bindTo(proxy == null ? p : proxy)
                            .invokeWithArguments(args));
        }

        return method.invoke(o, args);
    }

    /**
     * 获得类的所有声明字段，包括父类中的
     *
     * @param clazz     clazz
     * @param fieldName 字段名
     * @return {@link Field}
     */
    @SuppressWarnings("unused")
    public static Field getDeclaredField(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            Class<?> cl = clazz;
            Field field = null;
            try {
                field = getSelfDeclaredField(cl, fieldName);
                for (; cl != Object.class && field == null; cl = cl.getSuperclass()) {
                    field = getSelfDeclaredField(cl, fieldName);
                }
            } catch (Exception ignored) {
            }
            return new AtomicReference<>(field);
        }).get();
    }

    /**
     * 获得类的所有声明字段，不包括父类中的
     *
     * @param cl        cl
     * @param fieldName 字段名
     * @return {@link Field}
     */
    public static Field getSelfDeclaredField(Class<?> cl, String fieldName) {
        String key = cl.getName() + "#" + fieldName;
        return SELF_FIELD_CACHE.computeIfAbsent(key, k -> {
            Field field = null;
            try {
                field = cl.getDeclaredField(fieldName);
            } catch (Exception ignored) {
            }
            return new AtomicReference<>(field);
        }).get();
    }

    /**
     * 根据Class获得类信息
     *
     * @param targetClass 目标类
     * @return {@link BeanInfo}
     */
    private static BeanInfo getBeanInfo(Class<?> targetClass) {
        String key = targetClass.getName();
        return BEANINFO_CACHE.computeIfAbsent(key, k -> {
            try {
                return Introspector.getBeanInfo(targetClass);
            } catch (final IntrospectionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 获得类的某个字段属性描述
     *
     * @param targetClass 目标类
     * @param fieldName   字段名
     * @return {@link PropertyDescriptor}
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> targetClass, String fieldName) {
        String key = targetClass.getName() + "#" + fieldName;
        return PROPERTY_DESCRIPTOR_CACHE.computeIfAbsent(key, k -> {
            PropertyDescriptor descriptor;
            BeanInfo beanInfo = getBeanInfo(targetClass);
            descriptor = getPropertyDescriptorFromBeanInfo(beanInfo, fieldName);
            // 解决第二个字母为大写的情况（第二个字母为大写的话，propertyDescriptor有时会出现前两个字母都为大写的情况）
            if (descriptor == null && fieldName.length() >= 2 && Character.isUpperCase(fieldName.charAt(1))) {
                descriptor = getPropertyDescriptorFromBeanInfo(beanInfo, Texts.capitalize(fieldName));
            }
            if (descriptor != null) {
                // 如果用lombok的@Accessors(chain=true)注解的话(链式操作)，writeMethod会为空
                if (descriptor.getWriteMethod() == null) {
                    String methodName = "set" + StringUtils.capitalize(fieldName);
                    Method writeMethod = getDeclaredMethod(targetClass, methodName, descriptor.getPropertyType());
                    try {
                        descriptor.setWriteMethod(writeMethod);
                    } catch (IntrospectionException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            logger.debug("初次获取PropertyDescriptor[{}#{}]", targetClass.getName(), fieldName);
            return new AtomicReference<>(descriptor);
        }).get();
    }

    /**
     * 从BeanInfo中获取字段属性描述器
     *
     * @param beanInfo  bean信息
     * @param fieldName 字段名
     * @return {@link PropertyDescriptor}
     */
    private static PropertyDescriptor getPropertyDescriptorFromBeanInfo(BeanInfo beanInfo, String fieldName) {
        for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
            if (propertyDescriptor.getName().equals(fieldName)) {
                return propertyDescriptor;
            }
        }
        return null;
    }

    /**
     * 获得形参名和形参值的简单对照表（name-value）
     *
     * @param method 方法
     * @param args   实参集合(可为空，MethodParam的value也为空)
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> getFormalParamSimpleMapping(Method method, Object... args) {
        Map<String, Object> result = new HashMap<>();
        String[] names = getMethodParamNames(method);
        if (names == null || names.length == 0) {
            return result;
        }
        Class<?>[] types = method.getParameterTypes();
        boolean existValue = args != null && args.length > 0;
        for (int i = 0; i < types.length; i++) {
            String fname = names[i];
            Object value = null;
            if (existValue) {
                value = args[i];
            }
            result.put(fname, value);
        }
        return result;
    }

    /**
     * 获取方法的形参名集合
     *
     * @param method 方法
     * @return {@link String[]}
     */
    public static String[] getMethodParamNames(final Method method) {
        return DISCOVERER.getParameterNames(method);
    }

    /**
     * 获得所有可访问的字段名（包括父类）集合
     *
     * @param clazz clazz
     * @return {@link List}<{@link String}>
     */
    public static List<String> getAllDeclaredFieldNames(Class<?> clazz) {
        String key = clazz.getName();
        return FIELDNAMES_CACHE.computeIfAbsent(key, k -> {
            BeanInfo beanInfo = getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            List<String> fields = new ArrayList<>(propertyDescriptors.length);
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                String fieldName = descriptor.getName();
                // 去除class字段
                if (!"class".equals(fieldName)) {
                    fields.add(fieldName);
                }
            }
            return fields;
        });
    }

    /**
     * 对比两个对象，获取差异字段集合
     *
     * @param oldObject 旧对象
     * @param newObject 新对象
     * @return {@link List}<{@link FieldDidderence}>
     */
    public static List<FieldDidderence> contrastObject(Object oldObject, Object newObject) {
        List<FieldDidderence> result = new ArrayList<>();
        if (oldObject == null || newObject == null || oldObject.getClass() != newObject.getClass()) {
            return result;
        }
        Field[] noFields = newObject.getClass().getDeclaredFields();
        for (Field noField : noFields) {
            String fieldName = noField.getName();
            Object noValue = getProperty(newObject, fieldName);
            // 如果字段不为空，则表示该字段修改
            if (noValue != null) {
                Object oldValue = getProperty(oldObject, fieldName);
                if (!(noValue.equals(oldValue))) {
                    FieldDidderence didderence = new FieldDidderence();
                    didderence.setFiledName(fieldName);
                    didderence.setNewValue(noValue.toString());
                    if (oldValue != null) {
                        didderence.setOldValue(oldValue.toString());
                    } else {
                        didderence.setOldValue("");
                    }
                    result.add(didderence);
                }
            }
        }
        return result;
    }

    /**
     * 通过反射, 获得Class定义中声明的泛型参数的类型(先找父类，后找接口)。如无法找到, 返回Object.class.
     *
     * @param clazz The class to introspect
     * @return the first generic declaration, or Object.class if you cannot be determined
     * @param <T> 泛型
     */
    public static <T> Class<T> getClassGenericType(final Class<?> clazz) {
        return (Class<T>) getClassGenericType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类(或接口,如果是接口的话，默认获得第一个泛型接口)的泛型参数的类型。如无法找到, 返回Object.class.
     *
     * @param clazz clazz The class to introspect
     * @param index the Index of the generic ddeclaration,start from 0.
     * @return the index generic declaration, or Object.class if you cannot be determined
     */
    public static Class<?> getClassGenericType(final Class<?> clazz, final int index) {
        String key = clazz.getName() + index;
        return CLASSGENERICTYPE_CACHE.computeIfAbsent(key, k -> {
            Class<?> cl;
            Type genType = clazz.getGenericSuperclass();
            Type[] genericInterfaces = clazz.getGenericInterfaces();
            if ((genType == null || genType.equals(Object.class)) && genericInterfaces.length > 0) {
                genType = genericInterfaces[0];
            }
            if (!(genType instanceof ParameterizedType)) {
                logger.warn("{}'s superclass not ParameterizedType", clazz.getSimpleName());
                cl = Object.class;
            } else {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                if ((index >= params.length) || (index < 0)) {
                    logger.warn("Index: {}, Size of {}'s Parameterized Type: {}", index, clazz.getSimpleName(), params.length);
                    cl = Object.class;
                } else if (!(params[index] instanceof Class)) {
                    logger.warn("{} not set the actual class on superclass generic parameter", clazz.getSimpleName());
                    cl = Object.class;
                } else {
                    cl = (Class<?>) params[index];
                }
            }
            return cl;
        });
    }

    /**
     * 获得当前项目（jar）的ClassLoader
     *
     * @return {@link ClassLoader}
     */
    public static ClassLoader getDefaultClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    /**
     * 是否是基本数据类型
     *
     * @param cl cl
     * @return boolean
     */
    public static boolean isBasicType(Class<?> cl) {
        return cl != null && (cl.isPrimitive() || BASIC_TYPE.contains(cl));
    }

    /**
     * 从方法实参中获得对应类型的对象
     *
     * @param methodArgs 方法参数
     * @param clazz      clazz
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T> T getObjcetFromMethodArgs(Object[] methodArgs, Class<T> clazz) {
        return (T) Arrays.stream(methodArgs).filter(o -> clazz.isAssignableFrom(o.getClass())).findFirst().orElse(null);
    }

    /**
     * 获得预期类型的值
     *
     * @param o          o
     * @param returnType 返回类型
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T> T getExpectTypeValue(Object o, Class<T> returnType) {
        if (o == null) {
            if (returnType.isPrimitive()) {
                return DataGenerator.generateDefaultValueByParamType(returnType);
            } else {
                return null;
            }
        }
        if (ConvertUtils.primitiveToWrapper(returnType).equals(ConvertUtils.primitiveToWrapper(o.getClass()))) {
            return (T) o;
        } else {
            return convert(o, returnType);
        }
    }

    /**
     * 获得方法参数化返回值的第一个类型
     *
     * @param defaultClass 默认类型（在获取不到参数化类型时默认取这个值）
     * @param method       方法
     * @return {@link Class}<{@link ?}>
     */
    public static Class<?> getMethodReturnParameterizedTypeFirst(Method method, Class<?> defaultClass) {
        Class<?> result = defaultClass;
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType parameterizedType) {
            Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
            if (actualTypeArgument instanceof WildcardType wildcardType){
                Type[] upperBounds = wildcardType.getUpperBounds();
                result = (Class<?>)upperBounds[0];
            }else {
                result = (Class<?>) actualTypeArgument;
            }
        }
        return result;
    }

    /**
     * 字段差异
     */
    @Data
    public static class FieldDidderence {

        /**
         * 字段名称
         */
        private String filedName;
        /**
         * 字段修改前的值
         */
        private String oldValue;
        /**
         * 字段修改后的值
         */
        private String newValue;

    }
}
