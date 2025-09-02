package cn.xuanyuanli.core.util.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AES加密工具测试")
public class AesUtilTest {

    @Nested
    @DisplayName("加密功能测试")
    class EncryptTests {

        @Test
        @DisplayName("加密和解密正常流程")
        void encrypt_shouldReturnEncryptedStringAndDecryptCorrectly_whenValidDataAndKey() {
            // Arrange
            String data = "123456";
            String key = "abcde123";
            String expectedEncrypted = "u2TDMZTBmDacbHWR132xqg==";

            // Act
            String encrypted = AesUtil.encrypt(data, key);
            String decrypted = AesUtil.decrypt(expectedEncrypted, key);

            // Assert
            assertThat(encrypted).isEqualTo(expectedEncrypted);
            assertThat(decrypted).isEqualTo(data);
        }
    }

    @Nested
    @DisplayName("解密功能测试")
    class DecryptTests {

        @Test
        @DisplayName("解密无效数据应返回空字符串")
        void decrypt_shouldReturnEmpty_whenInvalidEncryptedData() {
            // Arrange
            String key = "abcde123";
            String invalidData = "45";

            // Act
            String result1 = AesUtil.decrypt(invalidData, key);
            String result2 = AesUtil.decrypt(invalidData, key);

            // Assert
            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
        }

        @Test
        @DisplayName("解密有效数据应返回正确结果")
        void decrypt_shouldReturnCorrectValue_whenValidEncryptedData() {
            // Arrange
            String validEncryptedData = "5QARST5wOTpvSCT6wN/eoA==";
            String key = "aes-share-token";
            String expected = "144";

            // Act
            String result = AesUtil.decrypt(validEncryptedData, key);

            // Assert
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("解密URL编码的数据应返回空字符串")
        void decrypt_shouldReturnEmpty_whenUrlEncodedData() {
            // Arrange
            String urlEncodedData = "5QARST5wOTpvSCT6wN%2FeoA%3D%3D";
            String key = "aes-share-token";

            // Act
            String result = AesUtil.decrypt(urlEncodedData, key);

            // Assert
            assertThat(result).isEmpty();
        }
    }
}
