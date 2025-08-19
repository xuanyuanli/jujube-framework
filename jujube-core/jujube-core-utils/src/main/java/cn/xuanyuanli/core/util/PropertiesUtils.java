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
 * properties工具
 *
 * @author John Li
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesUtils {

    /**
     * 保存内容到相应路径
     *
     * @param descPath      目标路径
     * @param content       内容
     * @param escapeUnicode val是否转换为unicode
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
     * 键保存转换
     *
     * @param key 键
     * @return {@link String}
     * @see #saveConvert(String, boolean, boolean)
     */
    public static String keySaveConvert(String key) {
        return saveConvert(key, true, true);
    }

    /**
     * 值保存转换
     *
     * @param val 值
     * @return {@link String}
     * @see #saveConvert(String, boolean, boolean)
     */
    public static String valueSaveConvert(String val) {
        return saveConvert(val, false, true);
    }

    /**
     * 对key和value特殊字符进行转义，参考Properties中的此方法
     *
     * @param theString     字符串
     * @param escapeSpace   逃避空间
     * @param escapeUnicode 逃避unicode
     * @return {@link String}
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
     * Convert a nibble to a hex character
     *
     * @param nibble the nibble to convert.
     * @return char
     */
    private static char toHex(int nibble) {
        return HEX_DIGIT[(nibble & 0xF)];
    }

    /**
     * A table of hex digits
     */
    private static final char[] HEX_DIGIT = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * from java.util.Properties#loadConvert copy
     *
     * @param in       在
     * @param off      从
     * @param len      len
     * @param convtBuf convt缓冲区
     * @return {@link String}
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
     * 加载classpath下的properties文件，返回有序的map
     *
     * @param propertiesPath 属性路径
     * @return {@link TreeMap}<{@link String}, {@link String}>
     */
    @SneakyThrows
    public static TreeMap<String, String> loadTreeMapFromClasspath(String propertiesPath) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        @Cleanup InputStream inputStream = Objects.requireNonNull(Resources.getClassPathResources(propertiesPath)).getInputStream();
        load0(new LineReader(inputStream), treeMap);
        return treeMap;
    }

    /**
     * 加载classpath下的properties文件，返回有序的map
     *
     * @param propertiesPath 属性路径
     * @return {@link TreeMap}<{@link String}, {@link String}>
     */
    @SneakyThrows
    public static TreeMap<String, String> loadTreeMapFromFile(String propertiesPath)  {
        TreeMap<String, String> treeMap = new TreeMap<>();
        @Cleanup FileInputStream fileInputStream = new FileInputStream(propertiesPath);
        load0(new LineReader(fileInputStream), treeMap);
        return treeMap;
    }

    /**
     * from java.util.Properties#load0(Properties.LineReader) copy
     *
     * @param lr      lr
     * @param treeMap 树map
     * @throws IOException ioexception
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
     * from java.util.Properties.LineReader copy
     */
    static class LineReader {

        public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        final byte[] inByteBuf;
        char[] inCharBuf;
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        final InputStream inStream;
        Reader reader;

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
