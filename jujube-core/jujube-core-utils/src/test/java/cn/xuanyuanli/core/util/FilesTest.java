package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Files 文件工具类测试")
class FilesTest {

    @Nested
    @DisplayName("文件扩展名获取测试")
    class FileExtensionTests {

        @Test
        @DisplayName("getExtention_应该返回正确扩展名_当输入带扩展名的文件时")
        void getExtention_shouldReturnCorrectExtension_whenInputFileWithExtension() {
            // Act & Assert
            assertThat(Files.getExtention("1.jpg")).isEqualTo(".jpg");
            assertThat(Files.getExtention("/u/89.jpg")).isEqualTo(".jpg");
            assertThat(Files.getExtention("1.JPG")).isEqualTo(".JPG");
            assertThat(Files.getExtention("http://o.cn/1.JPG")).isEqualTo(".JPG");
        }

        @Test
        @DisplayName("getExtention_应该返回空字符串_当输入文件无扩展名时")
        void getExtention_shouldReturnEmptyString_whenInputFileWithoutExtension() {
            // Act & Assert
            assertThat(Files.getExtention("1jpg")).isEqualTo("");
        }

        @Test
        @DisplayName("getExtention_应该返回指定扩展名_当使用默认扩展名参数时")
        void getExtention_shouldReturnSpecifiedExtension_whenUsingDefaultExtensionParameter() {
            // Act & Assert
            assertThat(Files.getExtention("123", ".ext")).isEqualTo(".ext");
            assertThat(Files.getExtention("123.txt", ".ext")).isEqualTo(".txt");
        }
    }

    @Nested
    @DisplayName("目录创建测试")
    class DirectoryCreationTests {

        @Test
        @DisplayName("createDir_应该抛出非法参数异常_当路径为空时")
        void createDir_shouldThrowIllegalArgumentException_whenPathIsEmpty() {
            // Act & Assert
            assertThatThrownBy(() -> Files.createDir(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("文件路径不能为空");
        }

        @Test
        @DisplayName("createDir_应该成功创建目录_当路径有效时")
        void createDir_shouldCreateDirectorySuccessfully_whenPathIsValid() throws IOException {
            // Arrange
            String path = "/testCreateDir/" + SnowFlakes.nextId();
            
            // Act
            File dir = Files.createDir(SystemProperties.TMPDIR + path);
            
            // Assert
            assertThat(dir).exists().isDirectory();
            
            // Cleanup
            FileUtils.deleteDirectory(dir);
        }
    }

    @Nested
    @DisplayName("文件创建测试")
    class FileCreationTests {

        @Test
        @DisplayName("createFile_应该抛出非法参数异常_当路径为空时")
        void createFile_shouldThrowIllegalArgumentException_whenPathIsEmpty() {
            // Act & Assert
            assertThatThrownBy(() -> Files.createFile(""))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("createFile_应该成功创建文件_当路径为根目录时")
        void createFile_shouldCreateFileSuccessfully_whenPathIsRoot() {
            // Arrange
            String filename = "/" + SnowFlakes.nextId();
            
            // Act
            File file = Files.createFile(SystemProperties.TMPDIR + filename);
            
            // Assert
            assertThat(file).exists().isFile();
            
            // Cleanup
            file.deleteOnExit();
        }

        @Test
        @DisplayName("createFile_应该成功创建文件_当路径含有子目录时")
        void createFile_shouldCreateFileSuccessfully_whenPathHasSubdirectory() {
            // Arrange
            String filename = "/testCreateFile/" + SnowFlakes.nextId();
            
            // Act
            File file = Files.createFile(SystemProperties.TMPDIR + filename);
            
            // Assert
            assertThat(file).exists().isFile();
            
            // Cleanup
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("字符串追加写入文件测试")
    class StringAppendToFileTests {

        @Test
        @DisplayName("appendStringToFile_应该抛出非法参数异常_当路径为null时")
        void appendStringToFile_shouldThrowIllegalArgumentException_whenPathIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> Files.appendStringToFile(null, "", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("文件路径不能为空");
        }

        @Test
        @DisplayName("appendStringToFile_应该抛出非法参数异常_当编码为null时")
        void appendStringToFile_shouldThrowIllegalArgumentException_whenCharsetIsNull() {
            // Act & Assert
            assertThatThrownBy(() -> Files.appendStringToFile("a.txt", "", null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("appendStringToFile_应该成功写入字符串_当第一次写入时")
        void appendStringToFile_shouldWriteStringSuccessfully_whenFirstTimeWrite() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/testAppendStringToFile/" + SnowFlakes.nextId();
            String data = "data12中文";
            
            // Act
            File file = Files.appendStringToFile(filename, data, StandardCharsets.UTF_8);
            
            // Assert
            assertThat(file).exists().isFile();
            assertThat(FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8)).isEqualTo(data);
            
            // Cleanup
            file.deleteOnExit();
        }

        @Test
        @DisplayName("appendStringToFile_应该追加字符串_当多次写入时")
        void appendStringToFile_shouldAppendString_whenWritingMultipleTimes() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/testAppendStringToFile/" + SnowFlakes.nextId();
            String data = "data12中文";
            
            // Act
            File file = Files.appendStringToFile(filename, data, StandardCharsets.UTF_8);
            Files.appendStringToFile(filename, data, StandardCharsets.UTF_8);
            
            // Assert
            assertThat(file).exists().isFile();
            assertThat(FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8))
                .isEqualTo("data12中文\r\ndata12中文");
            
            // Cleanup
            file.deleteOnExit();
        }

        @Test
        @DisplayName("appendStringToFile_应该创建空文件_当数据为null时")
        void appendStringToFile_shouldCreateEmptyFile_whenDataIsNull() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/testAppendStringToFile/" + SnowFlakes.nextId();
            
            // Act
            File file = Files.appendStringToFile(filename, null, StandardCharsets.UTF_8);
            
            // Assert
            assertThat(file).exists().isFile();
            assertThat(FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8)).isEqualTo("");
            
            // Cleanup
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("Base64与InputStream转换测试")
    class Base64InputStreamTests {

        @Test
        @DisplayName("base64ToInputstream_应该转换为InputStream_当输入有效Base64时")
        void base64ToInputstream_shouldConvertToInputStream_whenInputValidBase64() throws IOException {
            // Arrange
            String base64 = Base64.getEncoder().encodeToString("test".getBytes());
            
            // Act
            InputStream result = Files.base64ToInputstream(base64);
            
            // Assert
            assertThat(IOUtils.toString(result, StandardCharsets.UTF_8)).isEqualTo("test");
        }

        @Test
        @DisplayName("base64ToInputstream_应该返回null_当输入为null时")
        void base64ToInputstream_shouldReturnNull_whenInputIsNull() {
            // Act & Assert
            assertThat(Files.base64ToInputstream(null)).isNull();
        }
    }

    @Nested
    @DisplayName("Base64与File转换测试")
    class Base64FileTests {

        @Test
        @DisplayName("base64ToFile_应该成功写入文件_当输入有效Base64时")
        void base64ToFile_shouldWriteFileSuccessfully_whenInputValidBase64() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/testBase64ToFile-" + SnowFlakes.nextId();
            String base64 = Base64.getEncoder().encodeToString("test".getBytes());
            File file = new File(filename);
            
            // Act
            Files.base64ToFile(base64, file);
            
            // Assert
            assertThat(FileUtils.readFileToString(file, StandardCharsets.UTF_8)).isEqualTo("test");
            
            // Cleanup
            file.deleteOnExit();
        }

        @Test
        @DisplayName("fileToBase64_应该转换为Base64_当输入有效文件时")
        void fileToBase64_shouldConvertToBase64_whenInputValidFile() {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/testFileToBase64-" + SnowFlakes.nextId();
            File file = Files.appendStringToFile(filename, "test", StandardCharsets.UTF_8);
            
            // Act
            String result = Files.fileToBase64(file);
            
            // Assert
            assertThat(result).isEqualTo("dGVzdA==");
            
            // Cleanup
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("Stream与Base64转换测试")
    class StreamBase64Tests {

        @Test
        @DisplayName("streamToBase64_应该转换为Base64_当输入有效Stream时")
        void streamToBase64_shouldConvertToBase64_whenInputValidStream() {
            // Arrange
            String base64 = Base64.getEncoder().encodeToString("test".getBytes());
            InputStream is = Files.base64ToInputstream(base64);
            
            // Act
            String result = Files.streamToBase64(is);
            
            // Assert
            assertThat(result).isEqualTo("dGVzdA==");
        }
    }

    @Nested
    @DisplayName("路径验证测试")
    class PathValidationTests {

        @Test
        @DisplayName("isValidPath_应该返回true_当输入有效子路径时")
        void isValidPath_shouldReturnTrue_whenInputValidSubPath() {
            // Act & Assert
            assertThat(Files.isValidPath("/base/dir", "subdir/file.txt")).isTrue();
            assertThat(Files.isValidPath("/base/dir", "dfdadf")).isTrue();
        }

        @Test
        @DisplayName("isValidPath_应该返回true_当输入有效绝对路径时")
        void isValidPath_shouldReturnTrue_whenInputValidAbsolutePath() {
            // Act & Assert
            assertThat(Files.isValidPath("/base/dir", "/base/dir/sub/file")).isTrue();
        }

        @Test
        @DisplayName("isValidPath_应该返回true_当输入空用户路径时")
        void isValidPath_shouldReturnTrue_whenInputEmptyUserPath() {
            // Act & Assert
            assertThat(Files.isValidPath("/base/dir", "")).isTrue();
        }

        @Test
        @DisplayName("isValidPath_应该返回false_当用户路径在基础路径外时")
        void isValidPath_shouldReturnFalse_whenUserPathOutsideBasePath() {
            // Act & Assert
            assertThat(Files.isValidPath("/base/dir", "../otherdir")).isFalse();
            assertThat(Files.isValidPath("/base/dir", "/other/dir")).isFalse();
        }

        @Test
        @DisplayName("isValidPath_应该返回false_当基础路径包含非法字符时")
        void isValidPath_shouldReturnFalse_whenBasePathContainsInvalidCharacters() {
            // Act & Assert
            assertThat(Files.isValidPath("invalid:\0chars", "subdir")).isFalse();
        }

        @Test
        @DisplayName("isValidPath_应该返回false_当用户路径包含非法字符时")
        void isValidPath_shouldReturnFalse_whenUserPathContainsInvalidCharacters() {
            // Act & Assert
            assertThat(Files.isValidPath("/base/dir", "invalid:\0chars")).isFalse();
        }

        @Test
        @DisplayName("isValidPath_应该返回false_当基础路径为null时")
        void isValidPath_shouldReturnFalse_whenBasePathIsNull() {
            // Act & Assert
            assertThat(Files.isValidPath(null, "subdir")).isFalse();
        }

        @Test
        @DisplayName("isValidPath_应该返回false_当用户路径为null时")
        void isValidPath_shouldReturnFalse_whenUserPathIsNull() {
            // Act & Assert
            assertThat(Files.isValidPath("/base/dir", null)).isFalse();
        }
    }
}
