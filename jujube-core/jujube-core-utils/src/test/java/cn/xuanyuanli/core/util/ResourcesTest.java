package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

@DisplayName("Resources 资源工具类测试")
class ResourcesTest {

    @Nested
    @DisplayName("包扫描测试")
    class PackageScanTests {

        @Test
        @DisplayName("getPackageClasses_应该返回指定包下的类_当包存在时")
        void getPackageClasses_shouldReturnClassesFromPackage_whenPackageExists() {
            // Act
            List<Class<?>> packageClasses = Resources.getPackageClasses("cn.xuanyuanli.core.util.beancopy")
                    .stream()
                    .filter(e -> !e.getName().endsWith("Test"))
                    .toList();

            // Assert
            assertThat(packageClasses).hasSize(4);
        }
    }

    @Nested
    @DisplayName("属性文件加载测试")
    class PropertiesLoadingTests {

        @Test
        @DisplayName("getProperties_应该返回有效属性对象_当属性文件存在时")
        void getProperties_shouldReturnValidProperties_whenPropertiesFileExists() {
            // Act
            Properties properties = Resources.getProperties("test.properties");

            // Assert
            assertThat(properties).isNotNull();
        }

        @Test
        @DisplayName("getCurrentClasspathProperties_应该返回有效属性对象_当类路径中存在属性文件时")
        void getCurrentClasspathProperties_shouldReturnValidProperties_whenPropertiesFileExistsInClasspath() {
            // Act
            Properties properties = Resources.getCurrentClasspathProperties("test.properties");

            // Assert
            assertThat(properties).isNotNull();
        }
    }

    @Nested
    @DisplayName("路径获取测试")
    class PathRetrievalTests {

        @Test
        @DisplayName("getCurrentClasspath_应该返回存在的路径_当获取当前类路径时")
        void getCurrentClasspath_shouldReturnExistingPath_whenGettingCurrentClasspath() {
            // Act & Assert
            assertThat(Resources.getCurrentClasspath()).exists();
        }

        @Test
        @DisplayName("getProjectPath_应该返回非空项目路径_当获取项目路径时")
        void getProjectPath_shouldReturnNonEmptyProjectPath_whenGettingProjectPath() {
            // Act & Assert
            assertThat(Resources.getProjectPath()).isNotEmpty();
        }

        @Test
        @DisplayName("getJarHome_应该返回非空JAR主目录_当提供类时")
        void getJarHome_shouldReturnNonEmptyJarHome_whenProvidingClass() {
            // Act & Assert
            assertThat(Resources.getJarHome(ResourcesTest.class)).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("JAR文件检测测试")
    class JarFileDetectionTests {

        @Test
        @DisplayName("isJarFile_应该返回false_当URL不是JAR文件时")
        void isJarFile_shouldReturnFalse_whenURLIsNotJarFile() throws IOException {
            // Act & Assert
            assertThat(Resources.isJarFile(
                    Objects.requireNonNull(Resources.getClassPathResources("test.properties")).getURL()))
                    .isFalse();
        }

        @Test
        @DisplayName("isJarStartByClass_应该返回false_当类不是从JAR启动时")
        void isJarStartByClass_shouldReturnFalse_whenClassIsNotStartedFromJar() {
            // Act & Assert
            assertThat(Resources.isJarStartByClass(ResourcesTest.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("资源流获取测试")
    class ResourceStreamTests {

        @Test
        @DisplayName("getClassPathResourcesInputStream_应该返回有效输入流_当资源存在时")
        void getClassPathResourcesInputStream_shouldReturnValidInputStream_whenResourceExists() throws IOException {
            // Act & Assert
            try (InputStream inputStream = Resources.getClassPathResourcesInputStream("test.properties")) {
                assertThat(inputStream).isNotNull();
            }
        }

        @Test
        @DisplayName("getClassPathResources_应该返回正确文件名的资源_当资源存在时")
        void getClassPathResources_shouldReturnResourceWithCorrectFilename_whenResourceExists() {
            // Act
            Resource classPathResources = Resources.getClassPathResources("META-INF/office/testRealCount.xlsx");

            // Assert
            if (classPathResources != null) {
                assertThat(classPathResources.getFilename()).isEqualTo("testRealCount.xlsx");
            }
        }
    }
}
