package cn.xuanyuanli.core.util.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Https 测试")
public class HttpsTest {

    private static final String ERROR_URL = "auctionhome";

    @Nested
    @DisplayName("HTTP状态码获取测试")
    class StatusCodeTests {

        @Test
        @DisplayName("getStatusCode_应该返回200_当访问有效HTTPS URL时")
        void getStatusCode_shouldReturn200_whenAccessingValidHttpsUrl() {
            // Act
            int result = Https.getStatusCode("https://m.auctionhome.cn");

            // Assert
            assertThat(result).isEqualTo(200);
        }

        @Test
        @DisplayName("getStatusCode_应该返回500_当访问无效URL时")
        void getStatusCode_shouldReturn500_whenAccessingInvalidUrl() {
            // Act
            int result = Https.getStatusCode(ERROR_URL);

            // Assert
            assertThat(result).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("字符串内容获取测试")
    class StringContentTests {

        @Test
        @DisplayName("getAsString_应该返回正确内容_当访问文本文件时")
        void getAsString_shouldReturnCorrectContent_whenAccessingTextFile() {
            // Act
            String result = Https.getAsString("https://m.auctionhome.cn/MP_verify_CGYa76e80m7P1V6D.txt");

            // Assert
            assertThat(result).isEqualTo("CGYa76e80m7P1V6D");
        }

        @Test
        @DisplayName("getAsString_应该返回空字符串_当访问无效URL时")
        void getAsString_shouldReturnEmptyString_whenAccessingInvalidUrl() {
            // Act
            String result = Https.getAsString(ERROR_URL);

            // Assert
            assertThat(result).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("POST请求测试")
    class PostRequestTests {

        @Test
        @DisplayName("postAsString_应该返回空字符串_当POST到无效URL时")
        void postAsString_shouldReturnEmptyString_whenPostingToInvalidUrl() {
            // Act
            String result = Https.postAsString(ERROR_URL);

            // Assert
            assertThat(result).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("输入流获取测试")
    class InputStreamTests {

        @Test
        @DisplayName("getAsStream_应该返回非空输入流_当获取有效资源时")
        void getAsStream_shouldReturnNonEmptyStream_whenGettingValidResource() throws IOException {
            // Act
            InputStream inputStream = Https.getAsStream("https://m.auctionhome.cn/MP_verify_CGYa76e80m7P1V6D.txt");

            // Assert
            assertThat(inputStream).isNotNull();
            assertThat(inputStream.readAllBytes()).isNotEmpty();
        }

        @Test
        @DisplayName("getAsStream_应该返回正确的文本内容_当获取文本文件时")
        void getAsStream_shouldReturnCorrectTextContent_whenGettingTextFile() throws IOException {
            // Act
            InputStream inputStream = Https.getAsStream("https://m.auctionhome.cn/MP_verify_CGYa76e80m7P1V6D.txt");

            // Assert
            assertThat(IOUtils.toString(inputStream, StandardCharsets.UTF_8)).isEqualTo("CGYa76e80m7P1V6D");
        }

        @Test
        @DisplayName("getAsStream_应该抛出RuntimeException_当访问无效URL时")
        void getAsStream_shouldThrowRuntimeException_whenAccessingInvalidUrl() {
            // Act & Assert
            assertThatThrownBy(() -> Https.getAsStream(ERROR_URL))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}