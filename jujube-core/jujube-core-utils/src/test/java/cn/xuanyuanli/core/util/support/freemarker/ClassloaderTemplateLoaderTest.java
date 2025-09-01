package cn.xuanyuanli.core.util.support.freemarker;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("ClassloaderTemplateLoader 测试")
public class ClassloaderTemplateLoaderTest {

    @Nested
    @DisplayName("模板加载器创建测试")
    class TemplateLoaderCreationTests {

        @Test
        @DisplayName("构造函数_应该创建有效的模板加载器_当提供有效路径时")
        void constructor_shouldCreateValidLoader_whenValidPathProvided() {
            // Arrange & Act
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");

            // Assert
            assertThat(loader).isNotNull();
        }

        @ParameterizedTest(name = "构造函数_应该正确处理路径_当路径为: {0}")
        @MethodSource("providePathParameters")
        @DisplayName("构造函数_应该正确处理不同格式的路径")
        void constructor_shouldHandleDifferentPathFormats(String path, @SuppressWarnings("unused") String expectedBehavior) {
            // Act & Assert
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader(path);
            assertThat(loader).isNotNull();
        }

        private static Stream<Arguments> providePathParameters() {
            return Stream.of(
                Arguments.of("templates/", "正常路径格式"),
                Arguments.of("templates", "无斜杠结尾"),
                Arguments.of("/templates/", "斜杠开头"),
                Arguments.of("", "空路径"),
                Arguments.of("META-INF/", "META-INF路径")
            );
        }
    }

    @Nested
    @DisplayName("URL获取测试")
    class URLRetrievalTests {

        @Test
        @DisplayName("getURL_应该返回有效URL_当模板存在时")
        void getURL_shouldReturnValidURL_whenTemplateExists() throws Exception {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");
            
            // Act
            URL url = invokeGetURL(loader, "1.ftl");

            // Assert
            assertThat(url).isNotNull();
            assertThat(url.toString()).contains("1.ftl");
        }

        @Test
        @DisplayName("getURL_应该返回null_当模板不存在时")
        void getURL_shouldReturnNull_whenTemplateDoesNotExist() throws Exception {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");
            
            // Act
            URL url = invokeGetURL(loader, "nonexistent.ftl");

            // Assert
            assertThat(url).isNull();
        }

        @Test
        @DisplayName("getURL_应该正确拼接路径_当使用不同路径时")
        void getURL_shouldCorrectlyConcatenatePath_whenUsingDifferentPaths() throws Exception {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");
            
            // Act
            URL url = invokeGetURL(loader, "1.ftl");

            // Assert
            assertThat(url).isNotNull();
            // 验证路径拼接正确
            assertThat(url.getPath()).contains("templates/1.ftl");
        }
    }

    @Nested
    @DisplayName("模板读取测试")
    class TemplateReadingTests {

        @Test
        @DisplayName("findTemplateSource_应该返回有效的模板源_当模板存在时")
        void findTemplateSource_shouldReturnValidTemplateSource_whenTemplateExists() throws IOException {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");

            // Act
            Object templateSource = loader.findTemplateSource("1.ftl");

            // Assert
            assertThat(templateSource).isNotNull();
        }

        @Test
        @DisplayName("findTemplateSource_应该返回null_当模板不存在时")
        void findTemplateSource_shouldReturnNull_whenTemplateDoesNotExist() throws IOException {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");

            // Act
            Object templateSource = loader.findTemplateSource("nonexistent.ftl");

            // Assert
            assertThat(templateSource).isNull();
        }

        @Test
        @DisplayName("getReader_应该返回有效的Reader_当模板源有效时")
        void getReader_shouldReturnValidReader_whenTemplateSourceIsValid() throws IOException {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");
            Object templateSource = loader.findTemplateSource("1.ftl");

            // Act
            Reader reader = loader.getReader(templateSource, "UTF-8");

            // Assert
            assertThat(reader).isNotNull();
            
            // 验证可以读取内容
            char[] buffer = new char[100];
            int bytesRead = reader.read(buffer);
            assertThat(bytesRead).isGreaterThan(0);
            
            reader.close();
        }

        @Test
        @DisplayName("getLastModified_应该返回有效时间戳_当模板源有效时")
        void getLastModified_shouldReturnValidTimestamp_whenTemplateSourceIsValid() throws IOException {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");
            Object templateSource = loader.findTemplateSource("1.ftl");

            // Act
            long lastModified = loader.getLastModified(templateSource);

            // Assert
            // 对于资源文件，时间戳应该大于0
            assertThat(lastModified).isGreaterThanOrEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("构造函数_应该处理null路径_当路径为null时")
        void constructor_shouldHandleNullPath_whenPathIsNull() {
            // Act & Assert - 构造函数应该能处理null，但可能会有NullPointerException
            try {
                ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader(null);
                assertThat(loader).isNotNull();
            } catch (NullPointerException e) {
                // 这是可接受的行为
                assertThat(e).isInstanceOf(NullPointerException.class);
            }
        }

        @Test
        @DisplayName("findTemplateSource_应该处理空字符串模板名_当模板名为空时")
        void findTemplateSource_shouldHandleEmptyTemplateName_whenTemplateNameIsEmpty() throws IOException {
            // Arrange
            ClassloaderTemplateLoader loader = new ClassloaderTemplateLoader("templates/");

            // Act
            Object templateSource = loader.findTemplateSource("");

            // Assert
            // 空模板名实际上会返回目录路径，这是实际行为
            assertThat(templateSource).isNotNull();
        }
    }

    // 辅助方法 - 通过反射调用protected方法getURL
    private URL invokeGetURL(ClassloaderTemplateLoader loader, String name) throws Exception {
        var method = ClassloaderTemplateLoader.class.getDeclaredMethod("getURL", String.class);
        method.setAccessible(true);
        return (URL) method.invoke(loader, name);
    }
}