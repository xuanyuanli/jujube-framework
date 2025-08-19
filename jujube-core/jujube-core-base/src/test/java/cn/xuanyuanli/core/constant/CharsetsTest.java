package cn.xuanyuanli.core.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CharsetsTest {

    @Test
    void test(){
        assertEquals(Charsets.UTF_8, StandardCharsets.UTF_8);
        assertEquals(Charsets.ISO_8859_1, StandardCharsets.ISO_8859_1);
        assertEquals(Charsets.US_ASCII, StandardCharsets.US_ASCII);
        assertEquals(Charsets.UTF_16, StandardCharsets.UTF_16);
        assertEquals(Charsets.UTF_16BE, StandardCharsets.UTF_16BE);
        assertEquals(Charsets.UTF_16LE, StandardCharsets.UTF_16LE);
        assertEquals(Charsets.GBK, Charset.forName("GBK"));
    }
}
