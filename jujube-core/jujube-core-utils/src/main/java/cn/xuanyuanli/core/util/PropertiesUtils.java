package cn.xuanyuanli.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import cn.xuanyuanli.core.constant.Charsets;

/**
 * Properties 文件处理工具类
 * 
 * <p>提供对 Properties 文件的加载、保存、格式转换等功能，支持：</p>
 * <ul>
 *   <li>从 classpath 或文件系统加载 properties 文件</li>
 *   <li>保存 properties 内容到文件，支持 Unicode 转义</li>
 *   <li>键值对的转义处理，确保特殊字符正确保存和加载</li>
 *   <li>返回有序的键值对映射（TreeMap）</li>
 * </ul>
 * 
 * <p>该工具类遵循 Properties 文件的 ISO-8859-1 编码标准，
 * 对中文等非 ASCII 字符自动进行 Unicode 转义处理。</p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesUtils {

    /**
     * 将 Properties 内容保存到指定路径的文件中
     * 
     * <p>该方法会将字符串列表形式的 properties 内容保存到文件，
     * 自动处理键值对的格式化和转义。文件将以 ISO-8859-1 编码保存。</p>
     *
     * @param descPath      目标文件路径，如果路径不存在会自动创建
     * @param content       Properties 内容，每个字符串代表一行，格式为 "key=value" 或注释行
     * @param escapeUnicode 是否将非 ASCII 字符转换为 Unicode 转义序列
     * @throws IOException 当文件写入失败时抛出
     */
    @SneakyThrows
    public static void saveProperties(String descPath, List<String> content, boolean escapeUnicode)  {
        List<String> data = new ArrayList<>();
        for (String line : content) {
            String newLine = line;
            if (line != null && line.contains("=")) {
                String[] kv = line.split("=");
                if (kv.length == 2) {
                    String key = kv[0];
                    String val = kv[1];
                    key = saveConvert(key, true, escapeUnicode);
                    val = saveConvert(val, false, escapeUnicode);
                    newLine = key + "=" + val;
                }
            }
            data.add(newLine);
        }
        FileUtils.writeLines(new File(descPath), Charsets.ISO_8859_1.name(), data);
    }

    /**
     * 对 Properties 的键进行转义处理
     * 
     * <p>将键中的特殊字符进行转义，确保在 Properties 文件中能够正确保存和读取。
     * 会对空格、等号、冒号等特殊字符进行转义，并将非 ASCII 字符转换为 Unicode 转义序列。</p>
     *
     * @param key 需要转义的键
     * @return 转义后的键字符串
     * @see #saveConvert(String, boolean, boolean)
     */
    public static String keySaveConvert(String key) {
        return saveConvert(key, true, true);
    }

    /**
     * 对 Properties 的值进行转义处理
     * 
     * <p>将值中的特殊字符进行转义，确保在 Properties 文件中能够正确保存和读取。
     * 会对换行符、制表符等控制字符进行转义，并将非 ASCII 字符转换为 Unicode 转义序列。</p>
     *
     * @param val 需要转义的值
     * @return 转义后的值字符串
     * @see #saveConvert(String, boolean, boolean)
     */
    public static String valueSaveConvert(String val) {
        return saveConvert(val, false, true);
    }

    /**
     * 对字符串中的特殊字符进行转义处理
     * 
     * <p>该方法参考了 {@link java.util.Properties} 中的同名方法实现，
     * 用于将字符串中的特殊字符转义为 Properties 文件格式要求的形式。</p>
     * 
     * <p>主要处理以下转义：</p>
     * <ul>
     *   <li>控制字符：\t、\n、\r、\f</li>
     *   <li>特殊字符：=、:、#、!、\</li>
     *   <li>空格字符（当 escapeSpace 为 true 时）</li>
     *   <li>非 ASCII 字符的 Unicode 转义（当 escapeUnicode 为 true 时）</li>
     * </ul>
     *
     * @param theString     待转义的字符串
     * @param escapeSpace   是否转义空格字符，对于键通常为 true，对于值通常为 false
     * @param escapeUnicode 是否将非 ASCII 字符转换为 Unicode 转义序列
     * @return 转义后的字符串
     */
    private static String saveConvert(String theString, boolean escapeSpace, boolean escapeUnicode) {
        int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        StringBuilder outBuffer = new StringBuilder(bufLen);

        for (int x = 0; x < len; x++) {
            char aChar = theString.charAt(x);
            // Handle common case first, selecting the largest block that
            // avoids the specials below
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    outBuffer.append('\\');
                    outBuffer.append('\\');
                    continue;
                }
                outBuffer.append(aChar);
                continue;
            }
            switch (aChar) {
                case ' ':
                    if (x == 0 || escapeSpace) {
                        outBuffer.append('\\');
                    }
                    outBuffer.append(' ');
                    break;
                case '\t':
                    outBuffer.append('\\');
                    outBuffer.append('t');
                    break;
                case '\n':
                    outBuffer.append('\\');
                    outBuffer.append('n');
                    break;
                case '\r':
                    outBuffer.append('\\');
                    outBuffer.append('r');
                    break;
                case '\f':
                    outBuffer.append('\\');
                    outBuffer.append('f');
                    break;
                case '=':
                case ':':
                case '#':
                case '!':
                    outBuffer.append('\\');
                    outBuffer.append(aChar);
                    break;
                default:
                    if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >> 8) & 0xF));
                        outBuffer.append(toHex((aChar >> 4) & 0xF));
                        outBuffer.append(toHex(aChar & 0xF));
                    } else {
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /**
     * 将半字节（4位）转换为对应的十六进制字符
     * 
     * <p>用于 Unicode 转义序列的生成，将 0-15 的数值转换为 '0'-'9', 'A'-'F' 的字符。</p>
     *
     * @param nibble 待转换的半字节值（0-15）
     * @return 对应的十六进制字符
     */
    private static char toHex(int nibble) {
        return HEX_DIGIT[(nibble & 0xF)];
    }

    /**
     * 十六进制数字字符表
     * 用于将数值转换为对应的十六进制字符
     */
    private static final char[] HEX_DIGIT = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 加载时转换转义字符为原始字符
     * 
     * <p>该方法参考了 {@link java.util.Properties#loadConvert(char[], int, int, StringBuilder)} 的实现，
     * 用于将 Properties 文件中的转义序列转换回原始字符。</p>
     * 
     * <p>主要处理以下转义序列：</p>
     * <ul>
     *   <li>\\uxxxx - Unicode 转义序列</li>
     *   <li>\t - 制表符</li>
     *   <li>\r - 回车符</li>
     *   <li>\n - 换行符</li>
     *   <li>\f - 换页符</li>
     *   <li>其他被转义的字符</li>
     * </ul>
     *
     * @param in       输入字符数组
     * @param off      开始位置偏移量
     * @param len      需要转换的字符长度
     * @param convtBuf 用于转换的缓冲区字符数组
     * @return 转换后的字符串
     * @throws IllegalArgumentException 当遇到格式错误的 Unicode 转义序列时抛出
     */
    private static String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        value = switch (aChar) {
                            case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (value << 4) + aChar - '0';
                            case 'a', 'b', 'c', 'd', 'e', 'f' -> (value << 4) + 10 + aChar - 'a';
                            case 'A', 'B', 'C', 'D', 'E', 'F' -> (value << 4) + 10 + aChar - 'A';
                            default -> throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        };
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }

    /**
     * 从 classpath 加载 Properties 文件并返回有序的键值对映射
     * 
     * <p>该方法会从类路径中加载指定的 Properties 文件，
     * 自动处理文件中的转义序列，并返回按键排序的 TreeMap。</p>
     * 
     * <p>特点：</p>
     * <ul>
     *   <li>支持 ISO-8859-1 编码的 Properties 文件</li>
     *   <li>自动处理 Unicode 转义序列</li>
     *   <li>返回的 Map 按键的自然顺序排序</li>
     *   <li>支持注释行和空行</li>
     * </ul>
     *
     * @param propertiesPath Properties 文件在 classpath 中的路径
     * @return 包含所有键值对的有序映射，如果文件不存在则抛出异常
     * @throws IOException 当文件读取失败时抛出
     * @throws NullPointerException 当找不到指定的资源文件时抛出
     */
    @SneakyThrows
    public static TreeMap<String, String> loadTreeMapFromClasspath(String propertiesPath) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        @Cleanup InputStream inputStream = Objects.requireNonNull(Resources.getClassPathResources(propertiesPath)).getInputStream();
        load0(new LineReader(inputStream), treeMap);
        return treeMap;
    }

    /**
     * 从文件系统加载 Properties 文件并返回有序的键值对映射
     * 
     * <p>该方法会从文件系统中加载指定路径的 Properties 文件，
     * 自动处理文件中的转义序列，并返回按键排序的 TreeMap。</p>
     * 
     * <p>特点：</p>
     * <ul>
     *   <li>支持 ISO-8859-1 编码的 Properties 文件</li>
     *   <li>自动处理 Unicode 转义序列</li>
     *   <li>返回的 Map 按键的自然顺序排序</li>
     *   <li>支持注释行和空行</li>
     * </ul>
     *
     * @param propertiesPath Properties 文件的绝对路径或相对路径
     * @return 包含所有键值对的有序映射
     * @throws IOException 当文件不存在或读取失败时抛出
     */
    @SneakyThrows
    public static TreeMap<String, String> loadTreeMapFromFile(String propertiesPath)  {
        TreeMap<String, String> treeMap = new TreeMap<>();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(propertiesPath);
        load0(new LineReader(fileInputStream), treeMap);
        return treeMap;
    }

    /**
     * 加载 Properties 文件内容的核心方法
     * 
     * <p>该方法参考了 {@link java.util.Properties#load0} 的实现，
     * 负责解析 Properties 文件的格式，处理键值对分离、注释行、续行等。</p>
     * 
     * <p>支持的特性：</p>
     * <ul>
     *   <li>键值分隔符：= 或 : 或空白字符</li>
     *   <li>注释行：以 # 或 ! 开头的行</li>
     *   <li>续行：以 \ 结尾的行</li>
     *   <li>转义序列：自动处理各种转义字符</li>
     * </ul>
     *
     * @param lr      行读取器，用于逐行读取文件内容
     * @param treeMap 用于存储解析结果的有序映射
     * @throws IOException 当读取文件发生错误时抛出
     */
    private static void load0(LineReader lr, TreeMap<String, String> treeMap) throws IOException {
        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                // need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' && c != '\f') {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
            treeMap.put(key, value);
        }
    }

    /**
     * Properties 文件行读取器
     * 
     * <p>该类参考了 {@link java.util.Properties.LineReader} 的实现，
     * 专门用于读取和解析 Properties 文件格式的文本行。</p>
     * 
     * <p>主要功能：</p>
     * <ul>
     *   <li>按行读取输入流内容</li>
     *   <li>处理续行（以反斜杠结尾的行）</li>
     *   <li>跳过注释行和空白行</li>
     *   <li>支持 ISO-8859-1 字符编码</li>
     * </ul>
     */
    static class LineReader {

        /**
         * 构造一个行读取器
         *
         * @param inStream 输入流，通常是 Properties 文件的输入流
         */
        public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        /** 输入字节缓冲区 */
        final byte[] inByteBuf;
        /** 输入字符缓冲区 */
        char[] inCharBuf;
        /** 行缓冲区，用于存储当前正在处理的行 */
        char[] lineBuf = new char[1024];
        /** 输入缓冲区的有效数据长度 */
        int inLimit = 0;
        /** 输入缓冲区的当前读取位置 */
        int inOff = 0;
        /** 输入流 */
        final InputStream inStream;
        /** 字符读取器 */
        Reader reader;

        /**
         * 读取一个逻辑行
         * 
         * <p>该方法会读取输入流中的一个逻辑行，处理以下情况：</p>
         * <ul>
         *   <li>跳过前导空白字符</li>
         *   <li>跳过注释行（以 # 或 ! 开头）</li>
         *   <li>处理续行（以 \ 结尾的行会与下一行合并）</li>
         *   <li>自动扩展行缓冲区以容纳长行</li>
         * </ul>
         *
         * @return 读取到的逻辑行的字符数，如果到达文件末尾则返回 -1
         * @throws IOException 当读取输入流发生错误时抛出
         */
        int readLine() throws IOException {
            int len = 0;
            char c;
            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLf = false;
            while (true) {
                if (inOff >= inLimit) {
                    inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        if (precedingBackslash) {
                            len--;
                        }
                        return len;
                    }
                }
                if (inStream != null) {
                    // The line below is equivalent to calling a
                    // ISO8859-1 decoder.
                    c = (char) (0xff & inByteBuf[inOff++]);
                } else {
                    c = inCharBuf[inOff++];
                }
                if (skipLf) {
                    skipLf = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                        continue;
                    }
                }
                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    // flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                } else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            if (precedingBackslash) {
                                len--;
                            }
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        // skip the leading whitespace characters in following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLf = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }
}
