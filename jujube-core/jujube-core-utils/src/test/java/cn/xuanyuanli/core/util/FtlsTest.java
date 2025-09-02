package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Ftls FreeMarker模板工具测试")
class FtlsTest {

    @Nested
    @DisplayName("文件模板处理测试")
    class FileTemplateProcessTests {

        @Test
        @DisplayName("processFileTemplateToFile_应该抛出运行时异常_当模板数据不足时")
        void processFileTemplateToFile_shouldThrowRuntimeException_whenTemplateDataInsufficent() {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/processFileTemplateToFile-" + SnowFlakes.nextId();
            Map<String, Object> root = new HashMap<>();
            
            // Act & Assert
            RuntimeException exception = catchThrowableOfType(RuntimeException.class, 
                () -> Ftls.processFileTemplateToFile("1.ftl", filename, root));
            assertThat(exception).isNotNull();
        }

        @Test
        @DisplayName("processFileTemplateToFile_应该成功生成文件_当模板数据充足时")
        void processFileTemplateToFile_shouldGenerateFileSuccessfully_whenTemplateDataSufficient() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/processFileTemplateToFile-" + SnowFlakes.nextId();
            File file = new File(filename);
            Map<String, Object> root = new HashMap<>();
            root.put("test", true);
            
            // Act
            Ftls.processFileTemplateToFile("1.ftl", filename, root);
            
            // Assert
            assertThat(FileUtils.readFileToString(file, StandardCharsets.UTF_8)).isEqualTo("    Who am i?\r\n");
            
            // Cleanup
            file.deleteOnExit();
        }

        @Test
        @DisplayName("processFileTemplateToConsole_应该输出到控制台_当模板数据充足时")
        void processFileTemplateToConsole_shouldOutputToConsole_whenTemplateDataSufficient() {
            // Arrange
            Map<String, Object> root = new HashMap<>();
            root.put("test", true);
            
            // Act & Assert (此方法主要用于控制台输出，无法直接断言，但确保不抛异常)
            Ftls.processFileTemplateToConsole("1.ftl", root);
        }
    }

    @Nested
    @DisplayName("字符串模板处理测试")
    class StringTemplateProcessTests {

        @Test
        @DisplayName("processFileTemplateToString_应该返回处理后的字符串_当使用文件模板时")
        void processFileTemplateToString_shouldReturnProcessedString_whenUsingFileTemplate() {
            // Arrange
            Map<String, Object> root = new HashMap<>();
            root.put("test", true);
            
            // Act
            String result = Ftls.processFileTemplateToString("1.ftl", root);
            
            // Assert
            assertThat(result).isEqualTo("    Who am i?\r\n");
        }

        @Test
        @DisplayName("processStringTemplateToString_应该返回处理后的字符串_当使用字符串模板时")
        void processStringTemplateToString_shouldReturnProcessedString_whenUsingStringTemplate() {
            // Arrange
            Map<String, Object> root = new HashMap<>();
            root.put("test", true);
            root.put("ids", List.of(2, 3, 4));
            String template = """
                    <#if test>
                        Who am i?
                    </#if>${ids?join(',')}""";
            
            // Act
            String result = Ftls.processStringTemplateToString(template, root);
            
            // Assert
            assertThat(result).isEqualTo("    Who am i?\n2,3,4");
        }
    }

    @Nested
    @DisplayName("静态方法包使用测试")
    class StaticPackageUsageTests {

        @Test
        @DisplayName("useStaticPackage_应该正确调用静态方法_当在模板中使用时")
        void useStaticPackage_shouldCallStaticMethodCorrectly_whenUsedInTemplate() {
            // Arrange
            Map<String, Object> root = new HashMap<>();
            root.put("texts", Ftls.useStaticPackage(Texts.class));
            
            // Act
            String result = Ftls.processStringTemplateToString("${texts.capitalize('test')}", root);
            
            // Assert
            assertThat(result).isEqualTo("Test");
        }
    }
}
