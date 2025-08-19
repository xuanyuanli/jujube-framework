package cn.xuanyuanli.core.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 编码常量定义
 *
 * @author John Li
 * @date 2021/09/01
 */
@SuppressWarnings("unused")
public interface Charsets {

    /**
     * iso 8859 - 1
     */
    Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;

    /**
     * us ascii
     */
    Charset US_ASCII = StandardCharsets.US_ASCII;

    /**
     * utf 16
     */
    Charset UTF_16 = StandardCharsets.UTF_16;

    /**
     * utf 16BE
     */
    Charset UTF_16BE = StandardCharsets.UTF_16BE;

    /**
     * utf 16LE
     */
    Charset UTF_16LE = StandardCharsets.UTF_16LE;

    /**
     * utf 8
     */
    Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * gbk
     */
    Charset GBK = Charset.forName("GBK");
}
