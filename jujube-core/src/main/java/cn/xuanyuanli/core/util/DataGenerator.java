package cn.xuanyuanli.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;


/**
 * 数据生成者
 *
 * @author John Li
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DataGenerator {

    /**
     * 填充一个对象（一般用于测试）
     *
     * @param cl  cl
     * @param <T> 泛型
     * @return {@link T}
     */
    public static <T> T fullObject(Class<T> cl) {
        if (cl == null) {
            throw new IllegalArgumentException("参数不能为null");
        }
        if (Beans.isBasicType(cl)) {
            return generateRandomValueByParamType(cl);
        }
        if (Map.class.isAssignableFrom(cl)) {
            return (T) fullMap();
        }
        if (cl.isEnum()) {
            T[] enumConstants = cl.getEnumConstants();
            return enumConstants[Randoms.randomInt(0, enumConstants.length - 1)];
        }
        T t;
        try {
            t = Beans.getInstance(cl);
        } catch (Exception e) {
            return null;
        }
        Method[] methods = cl.getMethods();
        for (Method method : methods) {
            // 如果是set方法,进行随机数据的填充
            if (method.getName().indexOf("set") == 0 && method.getParameterTypes().length == 1) {
                Class<?> paramClass = method.getParameterTypes()[0];
                try {
                    Object val;
                    if (List.class.isAssignableFrom(paramClass)) {
                        val = fullListBean((Class<?>) ((ParameterizedType) method.getGenericParameterTypes()[0]).getActualTypeArguments()[0],
                                Randoms.randomInt(1, 5));
                    } else {
                        val = fullObject(paramClass);
                    }
                    method.invoke(t, val);
                } catch (Exception ignored) {
                }
            }
        }
        return t;
    }

    /**
     * 根据类型生成对应类型的随机数据
     *
     * @param paramClass 参数类
     * @param <T>        泛型
     * @return {@link T}
     */
    public static <T> T generateRandomValueByParamType(Class<T> paramClass) {
        Object result = null;
        if (paramClass.equals(String.class)) {
            int i = Randoms.randomInt(1, 16);
            result = i <= 8 ? Randoms.randomChinese(i) : Randoms.randomCodes(i);
        } else if (paramClass.equals(Short.class) || paramClass.equals(Short.TYPE)) {
            result = (short) new Random().nextInt(5);
        } else if (paramClass.equals(Boolean.class) || paramClass.equals(Boolean.TYPE)) {
            result = Randoms.randomInt(0, 1) > 0;
        } else if (paramClass.equals(Double.class) || paramClass.equals(Double.TYPE)) {
            int i = new Random().nextInt(6);
            result = Calcs.mul(new Random().nextDouble(), Math.pow(10, i), 2).doubleValue();
        } else if (paramClass.equals(Float.class) || paramClass.equals(Float.TYPE)) {
            int i = new Random().nextInt(3);
            result = Calcs.mul(new Random().nextFloat(), Math.pow(10, i), 2).floatValue();
        } else if (paramClass.equals(Long.class) || paramClass.equals(Long.TYPE)) {
            result = (long) new Random().nextInt(99999999);
        } else if (paramClass.equals(Integer.class) || paramClass.equals(Integer.TYPE)) {
            result = new Random().nextInt(999);
        } else if (paramClass.equals(Date.class)) {
            result = new Date();
        } else if (paramClass.equals(Timestamp.class)) {
            result = new Timestamp(System.currentTimeMillis());
        } else if (paramClass.equals(java.sql.Date.class)) {
            result = new java.sql.Date(System.currentTimeMillis());
        }
        return (T) result;
    }

    /**
     * 根据类型生成对应类型的默认数据
     *
     * @param paramClass 参数类
     * @param <T>        泛型
     * @return {@link T}
     */
    public static <T> T generateDefaultValueByParamType(Class<T> paramClass) {
        Object result = null;
        if (paramClass.equals(String.class)) {
            result = "";
        } else if (paramClass.equals(Short.class) || paramClass.equals(Short.TYPE)) {
            result = Short.valueOf("0");
        } else if (paramClass.equals(Boolean.class) || paramClass.equals(Boolean.TYPE)) {
            result = false;
        } else if (paramClass.equals(Double.class) || paramClass.equals(Double.TYPE)) {
            result = 0.0d;
        } else if (paramClass.equals(Float.class) || paramClass.equals(Float.TYPE)) {
            result = 0.0f;
        } else if (paramClass.equals(Long.class) || paramClass.equals(Long.TYPE)) {
            result = 0L;
        } else if (paramClass.equals(Integer.class) || paramClass.equals(Integer.TYPE)) {
            result = 0;
        } else if (paramClass.equals(Byte.class) || paramClass.equals(Byte.TYPE)) {
            result = Byte.valueOf("0");
        } else if (paramClass.equals(Character.class) || paramClass.equals(Character.TYPE)) {
            result = '\u0000';
        }
        return (T) result;
    }

    /**
     * 填充一个Map（一般用于测试）
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    public static Map<String, Object> fullMap() {
        Map<String, Object> map = new HashMap<>(8);
        map.put("string", generateRandomValueByParamType(String.class));
        map.put("short", generateRandomValueByParamType(short.class));
        map.put("float", generateRandomValueByParamType(float.class));
        map.put("double", generateRandomValueByParamType(double.class));
        map.put("int", generateRandomValueByParamType(int.class));
        map.put("long", generateRandomValueByParamType(long.class));
        map.put("date", generateRandomValueByParamType(Date.class));
        map.put("array", Arrays.asList(1, 2, 3, 4, 5));
        return map;
    }

    /**
     * 填充一个对应类型的List
     *
     * @param cl   cl
     * @param size 大小
     * @param <T>  泛型
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> fullListBean(Class<T> cl, int size) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(fullObject(cl));
        }
        return list;
    }

}
