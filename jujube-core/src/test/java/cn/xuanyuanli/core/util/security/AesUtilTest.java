package cn.xuanyuanli.core.util.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class AesUtilTest {

    @Test
    public void encrypt() {
        String data = "123456";
        String key = "abcde123";
        String expected = "u2TDMZTBmDacbHWR132xqg==";
        assertThat(AesUtil.encrypt(data, key)).isEqualTo(expected);
        assertThat(AesUtil.decrypt(expected, key)).isEqualTo(data);
    }

    @Test
    public void decryptErr() {
        String key = "abcde123";
        String expected = "45";
        assertThat(AesUtil.decrypt(expected, key)).isEmpty();
        assertThat(AesUtil.decrypt(expected, key)).isEmpty();
        assertThat(AesUtil.decrypt("5QARST5wOTpvSCT6wN/eoA==", "aes-share-token")).isEqualTo("144");
        assertThat(AesUtil.decrypt("5QARST5wOTpvSCT6wN%2FeoA%3D%3D", "aes-share-token")).isEmpty();
    }
}
