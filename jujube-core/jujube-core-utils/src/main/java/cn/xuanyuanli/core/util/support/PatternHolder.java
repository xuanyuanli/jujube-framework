package cn.xuanyuanli.core.util.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import cn.xuanyuanli.core.util.Texts;

/**
 * 因为Pattern会在初始化的时候进行编译，此类提供了Pattern缓存
 * <br>
 * 注意：此类仅适用于固定的regex缓存。对于动态的regex，请直接使用Pattern.compile，否则将出现OOM
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class PatternHolder {

    /**
     * 模式
     */
    private static final ConcurrentMap<String, Pattern> PATTERNS = new ConcurrentHashMap<>();

    /**
     * 获得模式
     *
     * @param regex 正则表达式
     * @return {@link Pattern}
     */
    public static Pattern getPattern(String regex) {
        return getPattern(regex, false);
    }

    /**
     * 获得模式
     *
     * @param regex      正则表达式
     * @param ignoreCase 忽略大小写
     * @return {@link Pattern}
     */
    public static Pattern getPattern(String regex, boolean ignoreCase) {
        String key = regex + ignoreCase;
        return PATTERNS.computeIfAbsent(key, k -> {
            Pattern pattern;
            if (ignoreCase) {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            } else {
                pattern = Pattern.compile(regex);
            }
            return pattern;
        });
    }

    /**
     * {@link Texts#escapeExprSpecialWord(String)}
     *
     * @param keyword 关键字
     * @return {@link String}
     */
    public static String escapeExprSpecialWord(String keyword) {
        return Texts.escapeExprSpecialWord(keyword);
    }

    /***
     * @see Pattern#compile(String, int)
     * @param regex 正则表达式
     * @param flags Match flags, a bit mask that may include CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, CANON_EQ, UNIX_LINES, LITERAL, UNICODE_CHARACTER_CLASS and COMMENTS
     * @return {@link Pattern}
     */
    public static Pattern compile(String regex, int flags) {
        String key = regex + flags;
        Pattern pattern = PATTERNS.get(key);
        if (pattern == null) {
            //noinspection MagicConstant
            pattern = Pattern.compile(regex, flags);
            PATTERNS.putIfAbsent(key, pattern);
        }
        return pattern;
    }
}
