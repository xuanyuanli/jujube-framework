package cn.xuanyuanli.jdbc.base.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import cn.xuanyuanli.jdbc.exception.DaoInitializeException;
import cn.xuanyuanli.core.util.Texts;

/**
 * 带有缓存的字符串处理方法集合
 *
 * @author xuanyuanli
 * @date 2022/07/16
 */
public class Strings {

    /**
     * 分割缓存
     */
    final static ConcurrentMap<String, String[]> SPLIT_BY_AND_CACHE = new ConcurrentHashMap<>();
    /**
     * reg找到缓存
     */
    private static final ConcurrentMap<String, Boolean> REG_FIND_CACHE = new ConcurrentHashMap<>();
    /**
     * reg getgroups缓存
     */
    private static final ConcurrentMap<String, String[]> REG_GETGROUPS_CACHE = new ConcurrentHashMap<>();
    /**
     * 和
     */
    private final static String AND = "And";

    /**
     * 根据And关键字分割字符串
     *
     * @param text 文本
     * @return {@link String[]}
     */
    public static String[] splitByAnd(String text) {
        return SPLIT_BY_AND_CACHE.computeIfAbsent(text, k -> {
            String mname = text;
            List<String> list = new ArrayList<>();
            int index, num = 0;
            int andLength = AND.length();
            while ((index = mname.indexOf(AND)) > -1) {
                if (num > 500) {
                    throw new DaoInitializeException("Jpa方法名异常：" + text+"。请检查：1、字段名是否使用了sql的关键字；2、sql关键字在方法中是否保持了独立性，也就是前后没有歧义。例如And前面不能出现大写字母");
                }
                if (mname.startsWith(AND)) {
                    int secondAndIndex = mname.substring(andLength).indexOf(AND);
                    if (secondAndIndex > -1) {
                        int beginIndex = secondAndIndex + andLength;
                        list.add(mname.substring(0, beginIndex));
                        mname = mname.substring(beginIndex + andLength);
                    } else {
                        break;
                    }
                } else {
                    boolean beforeIsNotUpperCase = !Character.isUpperCase(mname.substring(index - 1, index).charAt(0));
                    boolean afterIsNotLowerCase = !Character.isLowerCase(mname.substring(index, index + 1).charAt(0));
                    if (beforeIsNotUpperCase && afterIsNotLowerCase) {
                        list.add(mname.substring(0, index));
                        mname = mname.substring(index + andLength);
                    }
                }
                num++;
            }
            if (!mname.isEmpty()) {
                list.add(mname);
            }
            return list.toArray(new String[]{});
        });
    }

    /**
     * 用正则匹配，查找字符串中有没有相应字符
     *
     * @param source 源
     * @param regEx  注册前
     * @return boolean
     */
    public static boolean find(String source, String regEx) {
        return REG_FIND_CACHE.computeIfAbsent(source + ":" + regEx, k -> Texts.find(source, regEx));
    }

    /**
     * 获得组
     *
     * @param regex  正则表达式
     * @param source 源
     * @return {@link String[]}
     * @see Texts#getGroups(String, String, boolean)
     */
    public static String[] getGroups(String regex, String source) {
        return REG_GETGROUPS_CACHE.computeIfAbsent(source + ":" + regex, k -> Texts.getGroups(regex, source));
    }
}
