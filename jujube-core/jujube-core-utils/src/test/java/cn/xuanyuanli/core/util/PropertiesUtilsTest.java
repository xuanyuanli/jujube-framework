package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PropertiesUtils 属性工具类测试")
class PropertiesUtilsTest {

    @Nested
    @DisplayName("属性文件保存测试")
    class PropertiesSaveTests {

        @Test
        @DisplayName("saveProperties_应该正确保存和读取属性文件_当包含中文内容时")
        void saveProperties_shouldSaveAndLoadPropertiesCorrectly_whenContainsChineseContent() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "propertiesutilstest" + File.separator + SnowFlakes.nextId() + ".properties";
            List<String> properties = new ArrayList<>(List.of("name=中国"));
            
            // Act
            PropertiesUtils.saveProperties(filename, properties, true);
            
            // Assert
            File file = new File(filename);
            assertThat(FileUtils.readFileToString(file, StandardCharsets.UTF_8).replace("\r", "")).isEqualTo("name=\\u4E2D\\u56FD\n");
            
            TreeMap<String, String> map = PropertiesUtils.loadTreeMapFromFile(filename);
            assertThat(map.get("name")).isEqualTo("中国");
            
            // Cleanup
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("键值转换测试")
    class KeyValueConversionTests {

        @Test
        @DisplayName("keySaveConvert_应该正确转换键名_当包含中文字符时")
        void keySaveConvert_shouldConvertKeysCorrectly_whenContainsChineseCharacters() {
            // Act & Assert
            assertThat(PropertiesUtils.keySaveConvert("name")).isEqualTo("name");
            assertThat(PropertiesUtils.keySaveConvert("name中")).isEqualTo("name\\u4E2D");
        }

        @Test
        @DisplayName("valueSaveConvert_应该正确转换属性值_当包含中文字符时")
        void valueSaveConvert_shouldConvertValuesCorrectly_whenContainsChineseCharacters() {
            // Act & Assert
            assertThat(PropertiesUtils.valueSaveConvert("name")).isEqualTo("name");
            assertThat(PropertiesUtils.valueSaveConvert("name中")).isEqualTo("name\\u4E2D");
        }
    }

    @Nested
    @DisplayName("类路径属性加载测试")
    class ClasspathPropertiesLoadTests {

        @Test
        @DisplayName("loadTreeMapFromClasspath_应该正确加载类路径中的属性文件_当文件存在时")
        void loadTreeMapFromClasspath_shouldLoadPropertiesFromClasspath_whenFileExists() {
            // Act
            TreeMap<String, String> map = PropertiesUtils.loadTreeMapFromClasspath("test.properties");
            
            // Assert
            assertThat(map.get("name")).isEqualTo("中国");
            assertThat(map.get("age")).isEqualTo("45");
        }
    }
}
