package cn.xuanyuanli.core.util;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Data;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.text.StringEscapeUtils;
import cn.xuanyuanli.core.exception.RepeatException;
import cn.xuanyuanli.core.util.support.PatternHolder;

/**
 * 文本字符工具
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2021/09/01
 */
@SuppressWarnings("unused")
public class Texts {

    /**
     * 文本
     */
    private Texts() {
    }

    /**
     * 转义正则特殊字符 （$()*+.[]?\^{},|）
     *
     * @param keyword 关键字
     * @return {@link String}
     */
    public static String escapeExprSpecialWord(String keyword) {
        if (StringUtils.isNotBlank(keyword)) {
            String[] fbsArr = {"\\", "$", "(", ")", "*", "+", ".", "[", "]", "?", "^", "{", "}", "|"};
            for (String key : fbsArr) {
                if (keyword.contains(key)) {
                    keyword = keyword.replace(key, "\\" + key);
                }
            }
        }
        return keyword;
    }

    /**
     * 判断是否为合法IP
     *
     * @param ipAddress ip地址
     * @return boolean
     */
    public static boolean isTrueIp(String ipAddress) {
        return find(ipAddress, "((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)($|(?!\\.$)\\.)){4}");
    }

    /**
     * 是否仅包含中文、英文和数字
     *
     * @param name 名字
     * @return boolean
     */
    public static boolean isOnlyContainCnAndNumAndEn(String name) {
        return getCnAndNumAndEn(name).equals(name);
    }

    /**
     * 获得中英文和数字
     *
     * @param source 源
     * @return {@link String}
     */
    public static String getCnAndNumAndEn(String source) {
        if (source == null) {
            return null;
        }
        return Texts.regQuery("[a-zA-Z0-9\\u4e00-\\u9fa5]+", source).stream().map(RegexQueryInfo::getGroup).collect(Collectors.joining(""));
    }

    /**
     * 正则验证密码为数字和字母的组合，且为6位以上
     *
     * @param passWord 通过单词
     * @return boolean
     */
    public static boolean checkPassWord(String passWord) {
        String regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,}$";
        return passWord.matches(regex);
    }

    /**
     * 清除特殊字符
     *
     * @param str str
     * @return {@link String}
     */
    public static String cleanSpecialChar(String str) {
        if (str == null) {
            return null;
        }
        Pattern p = PatternHolder.getPattern("[`~!@#$%^&*()+=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]");
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    /**
     * 过滤掉超过3个字节的UTF8字符
     *
     * @param text 文本
     * @return {@link String}
     */
    public static String filterOffUtf8Mb4(String text) {
        if (text == null) {
            return null;
        }
        byte[] bytes;
        bytes = text.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        int i = 0;
        while (i < bytes.length) {
            short b = bytes[i];
            if (b > 0) {
                buffer.put(bytes[i++]);
                continue;
            }

            // 去掉符号位
            b += 256;

            if (((b >> 5) ^ 0x6) == 0) {
                buffer.put(bytes, i, 2);
                i += 2;
            } else if (((b >> 4) ^ 0xE) == 0) {
                buffer.put(bytes, i, 3);
                i += 3;
            } else if (((b >> 3) ^ 0x1E) == 0) {
                i += 4;
            } else if (((b >> 2) ^ 0x3E) == 0) {
                i += 5;
            } else if (((b >> 1) ^ 0x7E) == 0) {
                i += 6;
            } else {
                buffer.put(bytes[i++]);
            }
        }
        buffer.flip();
        String result = new String(buffer.array(), StandardCharsets.UTF_8);
        return result.replaceAll("\u0000","");
    }

    /**
     * 邮箱验证
     *
     * @param email 电子邮件
     * @return boolean
     */
    public static boolean emailValidate(String email) {
        String mailRegex = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        return find(email, mailRegex);
    }

    /**
     * 手机验证
     *
     * @param mobile 移动
     * @return boolean
     */
    public static boolean mobileValidate(String mobile) {
        String mobileRegex = "^[1]\\d{10}$";
        return find(mobile, mobileRegex);
    }

    /**
     * 替换所有空白字符
     *
     * @param str str
     * @return {@link String}
     */
    public static String replaceBlank(String str) {
        Pattern compile = PatternHolder.getPattern("\\s*|\t|\r|\n");
        return compile.matcher(str).replaceAll("");
    }

    /**
     * 替换换行符为空格
     *
     * @param str str
     * @return {@link String}
     */
    public static String replaceLineBreakToSpacing(String str) {
        if (str != null) {
            return str.lines().collect(Collectors.joining(" "));
        }
        return null;
    }

    /**
     * 替换utf-8中的空格，以免造成编码转换出现？的情况
     *
     * @param text 文本
     * @return {@link String}
     */
    public static String replaceUtf8Blank(String text) {
        if (text == null) {
            return null;
        }
        // utf-8空格替换;
        return text.replace(" ", " ");
    }

    /**
     * 去CSS
     *
     * @param content 内容
     * @return {@link String}
     */
    public static String clearCss(String content) {
        content = content.replaceAll("<\\s*?style[^>]*?>[\\s\\S]*?<\\s*?/\\s*?style\\s*?>", "");
        content = content.replaceAll("[style|STYLE]\\s*?=\\s*?\".*?\"", "");
        return content;
    }

    /**
     * 正则替换封装（忽略大小写）
     *
     * <pre>
     *     举例：regReplace("@+","","@@@123@") = 123
     *     需要注意的是，正则表达式中如果出现特殊字符，需要进行转义。比如：*.$等。"\\$"进行转义
     * </pre>
     *
     * @param reg    正则表达式
     * @param repstr 要替换为的字符
     * @param instr  原始字符串
     * @return 完成替换的字符串
     */
    public static String regReplace(String reg, String repstr, String instr) {
        return regReplace(reg, repstr, instr, true);
    }

    /**
     * 正则替换封装
     *
     * @param reg        正则表达式
     * @param repstr     要替换为的字符
     * @param instr      原始字符串
     * @param ignoreCase 是否忽略大小写
     * @return {@link String}
     */
    public static String regReplace(String reg, String repstr, String instr, boolean ignoreCase) {
        Pattern pattern = Pattern.compile(reg, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        Matcher matcher = pattern.matcher(instr);
        return matcher.replaceAll(repstr);
    }

    /**
     * 注册查询
     *
     * @param reg   注册
     * @param instr instr
     * @return {@link List}<{@link RegexQueryInfo}>
     * @see #regQuery(String, String, boolean)
     */
    public static List<RegexQueryInfo> regQuery(String reg, String instr) {
        return regQuery(reg, instr, true);
    }

    /**
     * 正则查询
     *
     * @param reg        正则表达式
     * @param instr      原始字符串
     * @param ignoreCase 是否忽略大小写
     * @return 返回自身与多个子匹配的信息
     */
    public static List<RegexQueryInfo> regQuery(String reg, String instr, boolean ignoreCase) {
        List<RegexQueryInfo> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(reg, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        Matcher matcher = pattern.matcher(instr);
        while (matcher.find()) {
            RegexQueryInfo info = new RegexQueryInfo();
            info.setEnd(matcher.end());
            info.setStart(matcher.start());
            info.setGroup(matcher.group());
            List<String> groups = new ArrayList<>(matcher.groupCount());
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
            info.setGroups(groups);
            list.add(info);
        }
        return list;
    }

    /**
     * 替换字符串str中的中文为str2
     *
     * @param str  str
     * @param str2 str2
     * @return {@link String}
     */
    public static String replaceChinese(String str, String str2) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("[\\u4e00-\\u9fa5]", str2);
    }

    /**
     * 全角转半角
     *
     * @param input 输入
     * @return {@link String}
     */
    public static String toDbc(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '\u3000') {
                c[i] = ' ';
            } else if (c[i] > '\uFF00' && c[i] < '｟') {
                c[i] = (char) (c[i] - 65248);
            }
        }
        return new String(c);
    }

    /**
     * 适用于大型字符串分割，可以设置多个分隔符
     *
     * <pre>
     *     举例：
     *     String[] s = stringTokenizer("wo; are, student", " ,;");
     *     //s={wo,are,student}
     * </pre>
     *
     * @param srcString       要分割的字符串
     * @param tokenizerString 分隔符
     * @return 分割后的数组
     */
    public static String[] stringTokenizer(String srcString, String tokenizerString) {
        return org.springframework.util.StringUtils.tokenizeToStringArray(srcString, tokenizerString);
    }

    /**
     * 不忽略大小写
     *
     * @param source 源
     * @param regEx  注册前
     * @return boolean
     * @see #find(String, String, boolean)
     */
    public static boolean find(String source, String regEx) {
        return find(source, regEx, false);
    }

    /**
     * 用正则匹配，查找字符串中有没有相应字符
     *
     * <pre>
     * 举例：find("zfa_999_ic", "zfa_\\d+_ic") = true
     * </pre>
     *
     * @param source     原字符串
     * @param regEx      正则表达式
     * @param ignoreCase 是否忽略大小写
     * @return 是否找到
     */
    public static boolean find(String source, String regEx, boolean ignoreCase) {
        if (StringUtils.isBlank(source)) {
            return false;
        }
        Pattern pat = Pattern.compile(regEx, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        Matcher mat = pat.matcher(source);
        return mat.find();
    }

    /**
     * 高亮显示关键字(所有匹配的字符都替换)
     *
     * @param source      原文本
     * @param keyWord     关键字
     * @param styleBefore 样式前，例如&lt;font class='red'&gt;
     * @param styleAfter  样式后,例如&lt;/font&gt;
     * @return {@link String}
     */
    public static String highlight(String source, String keyWord, String styleBefore, String styleAfter) {
        int begin;
        // 加上样式之后的关键字长度
        int len = styleAfter.length() + styleBefore.length() + keyWord.length();
        StringBuilder sb = new StringBuilder(source.length() + len * 5);
        String tag = source;
        while (true) {
            // 不区分大小写，找到关键字
            begin = tag.toUpperCase().indexOf(keyWord.toUpperCase());
            // 如果找到关键字，则关键字替换为高亮样式
            if (begin != -1) {
                int end = begin + keyWord.length();
                // 原文本中的关键字（保持其大小写状态）
                String red = tag.substring(begin, end);
                // 此次查找的字符串
                String result = tag.substring(0, end);
                // 对文本中关键字进行高亮替换
                result = result.replace(red, (styleBefore + red + styleAfter));
                // 保存已经替换完成的那一段
                sb.append(result);
                // 截取字符串，在后面继续寻找关键字，进行高亮替换
                tag = tag.substring(end);
            } else {
                // 如果没有找到关键字，把文本遗落的一段放入结果中
                sb.append(tag);
                break;
            }
        }
        return sb.toString();
    }

    /**
     * 获得字符串第一个字母
     * <p>
     * 如果为第一个字符为英文，则取出第一个字母<br> 如果为第一个字符为汉字，则汉字转化为拼音，取第一个拼音字母<br> 如果第一个字符（或之后）是符号，则从第二个字符开始，按照上面规则处理。后面字符还是符号，则递归处理<br> 如果全是符号，则返回null
     *
     * @param str str
     * @return {@link String}
     */
    public static String getFirstLetter(String str) {
        Validate.notBlank(str);

        String result = null;
        char firstLetter = str.charAt(0);
        if (isEn(firstLetter)) {
            result = String.valueOf(firstLetter);
        } else if (isChinese(firstLetter)) {
            result = PinyinHelper.toHanyuPinyinStringArray(firstLetter)[0].substring(0, 1);
        } else if (str.length() > 1) {
            result = getFirstLetter(str.substring(1));
        }
        return result;
    }

    /**
     * 判断字符串中是否包含汉字
     *
     * @param s 年代
     * @return boolean
     */
    public static boolean containsChinese(String s) {
        boolean result = false;
        if (StringUtils.isNotBlank(s)) {
            for (int i = 0; i < s.length(); i++) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(s.charAt(i));
                if (pinyinArray != null && pinyinArray.length > 0) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 获得文本中的所有中文
     *
     * @param content 内容
     * @return {@link String}
     */
    public static String getChinese(String content) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (isChinese(ch)) {
                result.append(ch);
            }
        }
        return result.toString();
    }

    /**
     * 获取汉字的首字母集合(如果字符串中有非中文，将被舍弃)
     *
     * @param str str
     * @return {@link String}
     */
    public static String getFirstLetterArr(String str) {
        Validate.notBlank(str);
        StringBuilder sBuffer = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (isChinese(c)) {
                sBuffer.append(getFirstLetter(String.valueOf(c)));
            }
        }
        return sBuffer.toString();
    }

    /**
     * 得到汉字的拼音
     *
     * @param content 内容
     * @return 拼音都为小写不带声调
     */
    public static String getHanyupinyin(String content) {
        return getHanyupinyin(content, 2);
    }

    /**
     * 得到汉字的拼音
     *
     * @param content 汉字
     * @param type    1、拼音都为小写且带声调；2、拼音都为小写不带声调；3、拼音首字母大写不带声调；
     * @return {@link String}
     */
    public static String getHanyupinyin(String content, int type) {
        if (content == null) {
            return null;
        }
        if (type < 1 || type > 3) {
            type = 1;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            String[] arr = PinyinHelper.toHanyuPinyinStringArray(c);
            if (arr != null && arr.length > 0) {
                String pinyin = arr[0];
                if (type == 2) {
                    pinyin = pinyin.substring(0, pinyin.length() - 1);
                } else if (type == 3) {
                    pinyin = pinyin.substring(0, pinyin.length() - 1);
                    pinyin = StringUtils.capitalize(pinyin);
                }
                result.append(pinyin);
            }
        }
        return result.toString();
    }

    /**
     * 是否是英文字母
     *
     * @param c c
     * @return boolean
     */
    public static boolean isEn(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /**
     * 是否是数字
     *
     * @param c c
     * @return boolean
     */
    public static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }

    /**
     * 字符是否是中文
     *
     * @param c c
     * @return boolean
     */
    public static boolean isChinese(char c) {
        return containsChinese(String.valueOf(c));
    }

    /**
     * 是否是科学计数法
     *
     * @param str str
     * @return boolean
     */
    public static boolean isScientificNotation(String str) {
        try {
            BigDecimal bd = new BigDecimal(str);
            String s = bd.toPlainString();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获得组
     *
     * @param regex  正则表达式
     * @param source 源
     * @return {@link String[]}
     * @see #getGroups(String, String, boolean)
     */
    public static String[] getGroups(String regex, String source) {
        return getGroups(regex, source, false);
    }

    /**
     * 和JavaScript中RegExp对象的exec()方法一样<br> 只返回第一个匹配的结果，数组中第一个元素包含正则表达式匹配的字符串，余下的元素是与圆括号内的子表达式相匹配的子串
     *
     * @param regex      正则表达式
     * @param source     源字符串
     * @param ignoreCase 是否区分大小写
     * @return {@link String[]}
     */
    public static String[] getGroups(String regex, String source, boolean ignoreCase) {
        Pattern pattern = Pattern.compile(regex, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        Matcher matcher = pattern.matcher(source);
        String[] groups = new String[0];
        if (matcher.find()) {
            int count = matcher.groupCount();
            groups = new String[count + 1];
            for (int i = 0; i <= count; i++) {
                groups[i] = matcher.group(i);
            }
        }
        return groups;
    }

    /**
     * 获取匹配到的文本
     *
     * @param regex  正则表达式
     * @param source 源
     * @return {@link String}
     */
    public static String getGroup(String regex, String source) {
        return getGroup(regex, source, false);
    }

    /**
     * 获取匹配到的文本
     *
     * @param regex      正则表达式
     * @param source     源
     * @param ignoreCase 忽略大小写
     * @return {@link String}
     */
    public static String getGroup(String regex, String source, boolean ignoreCase) {
        Pattern pattern = Pattern.compile(regex, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        Matcher matcher = pattern.matcher(source);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * 对lines进行分组处理
     *
     * @param lines         数据
     * @param groupFunction 获得group name的function：入参为line，返回groupName
     * @return 分组后的lines
     */
    public static Map<String, List<String>> group(List<String> lines, Function<String, String> groupFunction) {
        return group(lines, groupFunction, true);
    }

    /**
     * 对lines进行分组处理
     *
     * @param lines         数据
     * @param groupFunction 获得group name的function：入参为line，返回groupName
     * @param allowRepeat   是否允许重复，如果为false，有重复元素的话会抛出RepeatException
     * @return 分组后的lines
     */
    public static Map<String, List<String>> group(List<String> lines, Function<String, String> groupFunction, boolean allowRepeat) {
        Map<String, List<String>> group = new LinkedHashMap<>();
        List<String> part = new ArrayList<>();
        // 先找出第一个groupName
        String groupName = "";
        int i = 0;
        for (; i < lines.size(); i++) {
            String line = lines.get(i);
            String curGroupName = groupFunction.apply(line);
            if (StringUtils.isNotBlank(curGroupName)) {
                groupName = curGroupName;
                break;
            }
        }
        for (int j = i + 1; j < lines.size(); j++) {
            String line = lines.get(j);
            String curGroupName = groupFunction.apply(line);
            if (StringUtils.isNotBlank(curGroupName)) {
                if (!allowRepeat && group.containsKey(groupName)) {
                    throw new RepeatException(groupName);
                }
                group.put(groupName, part);
                // 因为group中元素都在group下面，so...
                groupName = curGroupName;
                part = new ArrayList<>();
            } else if (StringUtils.isNotBlank(line)) {
                part.add(line);
            }
        }
        if (!allowRepeat && group.containsKey(groupName)) {
            throw new RepeatException(groupName);
        }
        group.put(groupName, part);
        return group;
    }

    /**
     * 对分组后的数据进行再分组
     *
     * @param group         分组后的数据
     * @param groupFunction 获得sub group name的function：入参为组下元素，返回sub group name
     * @return 再分组后的数据
     */
    public static Map<String, Map<String, List<String>>> groupAgain(Map<String, List<String>> group, Function<String, String> groupFunction) {
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (String groupName : group.keySet()) {
            List<String> groupEles = group.get(groupName);
            Map<String, List<String>> subGroup = new LinkedHashMap<>();
            result.put(groupName, subGroup);
            // 对group进行再分组
            for (String groupEle : groupEles) {
                String subGoupName = groupFunction.apply(groupEle);
                if (!subGroup.containsKey(subGoupName)) {
                    List<String> part = new ArrayList<>();
                    part.add(groupEle);
                    subGroup.put(subGoupName, part);
                } else {
                    subGroup.get(subGoupName).add(groupEle);
                }
            }
        }
        return result;
    }

    /**
     * 合并分组
     *
     * @param group                 集团
     * @param moduleCommentFunction 评论功能模块
     * @return {@link List}<{@link String}>
     */
    public static List<String> mergeGroup(Map<String, Map<String, List<String>>> group, Function<String, String> moduleCommentFunction) {
        List<String> lines = new ArrayList<>();
        for (String groupName : group.keySet()) {
            Map<String, List<String>> subGroup = group.get(groupName);
            String moduleComment = moduleCommentFunction.apply(groupName);
            lines.add(moduleComment);
            for (Entry<String, List<String>> ele : subGroup.entrySet()) {
                lines.addAll(ele.getValue());
                lines.add("");
            }
            lines.add("");
        }
        return lines;
    }

    /**
     * 格式化字符串，仿C#。或直接用{}，仿log.info()
     * <p>进阶版的MessageFormat#format()</p>
     * <pre>
     * str = Hello {0}
     * result = format(str,"World!")
     * #result = Hello World!
     * </pre>
     *
     * @param pattern 待匹配字符串
     * @param params  参数数组
     * @return {@link String}
     */
    public static String format(String pattern, Object... params) {
        if (pattern == null) {
            return null;
        }
        String regex = "\\{\\d+\\}";
        int count = regQuery(regex, pattern).size();
        String symbol = "{}";
        // 类似{0}这种形式
        if (count > 0) {
            if (params.length != count) {
                throw new IllegalArgumentException("模式匹配跟参数个数不对应");
            }
            String result = pattern;
            for (int i = 0; i < count; i++) {
                String replacement = params[i] == null ? "" : params[i].toString();
                if (replacement == null) {
                    replacement = "";
                }
                result = result.replace("{" + i + "}", replacement);
            }
            return result;
        }
        // 或者直接{}这种形式
        else if (pattern.contains(symbol)) {
            StringBuilder result = new StringBuilder();
            // 防止{}出现在最后一行
            pattern += " ";
            String[] arr = pattern.split("\\{}");
            for (int i = 0; i < arr.length - 1; i++) {
                result.append(arr[i]).append(params.length <= i ? "" : params[i] == null ? "" : params[i].toString());
                if (i == arr.length - 2) {
                    result.append(arr[i + 1]);
                }
            }
            return result.substring(0, result.length() - 1);
        } else {
            return pattern;
        }
    }

    /**
     * 生成a标签
     *
     * @param href  href
     * @param title 标题
     * @param blank 空白
     * @return {@link String}
     */
    public static String generateAtag(String href, String title, boolean blank) {
        return format("<a href=\"{0}\" target=\"{1}\">{2}</a>", href, blank ? "_blank" : "", title);
    }

    /**
     * 字符截断。如果超出某个长度，则后跟‘...’。默认英文字符按照0.5个长度计算，中文字符按照一个长度计算
     *
     * @param source        原始字符
     * @param trancationNum 截断的字符数
     * @return {@link String}
     */
    public static String truncate(String source, int trancationNum) {
        return truncate(source, trancationNum, true);
    }

    /**
     * 字符截断。如果超出trancationNum，则后跟‘...’
     *
     * @param source        原始字符
     * @param trancationNum 截断的字符数
     * @param calcEnLen     是否英文字符按照0.5个长度计算
     * @return {@link String}
     */
    public static String truncate(String source, int trancationNum, boolean calcEnLen) {
        if (source == null) {
            return null;
        }
        String result;
        int slen = source.length();
        if (calcEnLen) {
            float len = 0.0f;
            int i = 0;
            for (; i < source.length(); i++) {
                if (isChinese(source.charAt(i))) {
                    len += 1;
                } else {
                    len += 0.5F;
                }
                if (len >= trancationNum) {
                    break;
                }
            }
            int cur = i + 1;
            if (cur >= slen) {
                result = source;
            } else {
                result = source.substring(0, cur) + "...";
            }
        } else {
            if (slen > trancationNum) {
                result = source.substring(0, trancationNum) + "...";
            } else {
                result = source;
            }
        }
        return result;
    }

    /**
     * 长
     *
     * @param source 源
     * @return long
     * @see NumberUtils#toLong(String)
     */
    public static long toLong(String source) {
        return NumberUtils.toLong(source);
    }

    /**
     * 增加一倍
     *
     * @param source 源
     * @return double
     * @see NumberUtils#toDouble(String)
     */
    public static double toDouble(String source) {
        return NumberUtils.toDouble(source);
    }

    /**
     * 到int
     *
     * @param source 源
     * @return int
     * @see NumberUtils#toInt(String)
     */
    public static int toInt(String source) {
        return NumberUtils.toInt(source);
    }

    /**
     * 只取字符串中的数字
     *
     * @param source 源
     * @return int
     */
    public static int parseInt(String source) {
        return Numbers.parseInt(source);
    }

    /**
     * 只取字符串中的数字
     *
     * @param source 源
     * @return int
     */
    public static long parseLong(String source) {
        return Numbers.parseLong(source);
    }

    /**
     * 首字母大写
     *
     * @param str str
     * @return {@link String}
     */
    public static String capitalize(String str) {
        return StringUtils.capitalize(str);
    }

    /**
     * 获得遮掩的名称，用**表示中间的字符（用于脱敏）
     *
     * @param source 源
     * @return {@link String}
     */
    public static String getHideName(String source) {
        int len = source.length() / 2;
        int num = Math.min(len, 4);
        int prefixNum = Math.min(len, 3);
        int suffixNum = Math.min(len, 4);
        return getHideName(source, num, prefixNum, suffixNum);
    }

    /**
     * 获得遮掩的名称，用**表示中间的字符（用于脱敏）
     *
     * @param num：中间*的个数
     * @param prefixNum：*前面显示的个数
     * @param suffixNum：*后面显示的个数
     * @param source             源
     * @return {@link String}
     */
    public static String getHideName(String source, int num, int prefixNum, int suffixNum) {
        String result = source;
        if (num < 0) {
            num = 3;
        }
        if (prefixNum < 0) {
            prefixNum = 1;
        }
        if (suffixNum < 0) {
            suffixNum = prefixNum;
        }

        StringBuilder middle = new StringBuilder();
        middle.append("*".repeat(num));
        if (StringUtils.isBlank(source)) {
            return result;
        }
        int len = source.length();
        if (len > prefixNum) {
            int endLen = len - suffixNum;
            if (endLen > suffixNum) {
                endLen = suffixNum;
            }
            result = source.substring(0, prefixNum) + middle + source.substring(len - endLen);
        } else {
            result = source + middle;
        }
        return result;
    }

    /**
     * 判断输入项是否为Ip地址
     *
     * @param ipAddress ip地址
     * @return boolean
     */
    public static boolean isIp(String ipAddress) {
        String regex = "^([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$";
        return Pattern.matches(regex, ipAddress);
    }


    /**
     * 正则查询的信息
     * <p>
     * 类中三个字段参考Matcher类
     */
    @Data
    public static class RegexQueryInfo {

        /**
         * 整个正则匹配到的字符
         */
        private String group;
        private int start;
        private int end;
        /**
         * 正则中每个括号匹配到的字符集合，从0开始。例如用(a)(b)去匹配'ab'，groups的size=2
         */
        private List<String> groups;

    }

    /**
     * 反替换html中的转义字符
     *
     * @param str str
     * @return {@link String}
     */
    public static String unescapeHtml(String str) {
        return StringEscapeUtils.unescapeHtml4(str);
    }

    /**
     * 版本号比较
     *
     * @param version1 version1
     * @param version2 version2
     * @return 0 相等，1 大于，-1 小于
     */
    public static int compareVersion(String version1, String version2) {
        if (StringUtils.isBlank(version1) && StringUtils.isBlank(version2)) {
            return 0;
        }
        if (StringUtils.isBlank(version1)) {
            return -1;
        }
        if (StringUtils.isBlank(version2)) {
            return 1;
        }
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        // 取两个版本号最长的长度
        int len = Math.max(v1.length, v2.length);
        for (int i = 0; i < len; i++) {
            // 如果版本号位数不够，补0
            int num1 = i < v1.length ? Integer.parseInt(v1[i]) : 0;
            int num2 = i < v2.length ? Integer.parseInt(v2[i]) : 0;
            if (num1 != num2) {
                return num1 > num2 ? 1 : -1;
            }
        }
        // 版本号相同
        return 0;
    }
}
