package cn.xuanyuanli.core.util.office;

import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

import cn.xuanyuanli.core.util.Resources;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

@DisplayName("Word文档读取器测试")
class WordReaderTest {

    @Nested
    @DisplayName("文档内容读取功能测试")
    class DocumentContentReadingTests {

        @Test
        @DisplayName("读取Word文档内容应返回正确格式的文本")
        void getWordContent_shouldReturnCorrectFormattedText_whenReadingWordDocument() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/office/testWord.docx");
            String expectedContent = """
                    hello
                    world
                    123
                    """;

            // Act
            String actualContent = WordReader.getWordContent(Objects.requireNonNull(resource).getFile().getAbsolutePath());

            // Assert
            assertThat(actualContent).isEqualTo(expectedContent);
        }
    }
}
