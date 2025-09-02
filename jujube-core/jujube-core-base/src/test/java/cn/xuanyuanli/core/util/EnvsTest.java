package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Envs 环境变量获取工具测试")
class EnvsTest {

    private static final String TEST_PROPERTY_KEY = "test.property.key";
    private static final String TEST_PROPERTY_VALUE = "test.property.value";
    private String originalPropertyValue;

    @BeforeEach
    void setUp() {
        // 保存原始值以便恢复
        originalPropertyValue = System.getProperty(TEST_PROPERTY_KEY);
    }

    @AfterEach
    void tearDown() {
        // 恢复原始值
        if (originalPropertyValue != null) {
            System.setProperty(TEST_PROPERTY_KEY, originalPropertyValue);
        } else {
            System.clearProperty(TEST_PROPERTY_KEY);
        }
    }

    @Nested
    @DisplayName("系统属性获取测试")
    class SystemPropertyTests {

        @Test
        @DisplayName("getEnv_应该返回系统属性值_当系统属性存在时")
        void getEnv_shouldReturnSystemPropertyValue_whenSystemPropertyExists() {
            // Arrange
            System.setProperty(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);

            // Act
            String result = Envs.getEnv(TEST_PROPERTY_KEY);

            // Assert
            assertThat(result).isEqualTo(TEST_PROPERTY_VALUE);
        }

        @Test
        @DisplayName("getEnv_应该尝试获取环境变量_当系统属性不存在时")
        void getEnv_shouldTryEnvironmentVariable_whenSystemPropertyNotExists() {
            // Arrange
            System.clearProperty(TEST_PROPERTY_KEY);

            // Act
            String result = Envs.getEnv(TEST_PROPERTY_KEY);

            // Assert
            // 由于我们无法控制环境变量，只能验证结果与 System.getenv 一致
            assertThat(result).isEqualTo(System.getenv(TEST_PROPERTY_KEY));
        }

        @Test
        @DisplayName("getEnv_应该尝试获取环境变量_当系统属性为空字符串时")
        void getEnv_shouldTryEnvironmentVariable_whenSystemPropertyIsEmpty() {
            // Arrange
            System.setProperty(TEST_PROPERTY_KEY, "");

            // Act
            String result = Envs.getEnv(TEST_PROPERTY_KEY);

            // Assert
            // 由于系统属性是空字符串，应该尝试获取环境变量
            assertThat(result).isEqualTo(System.getenv(TEST_PROPERTY_KEY));
        }

        @Test
        @DisplayName("getEnv_应该返回null_当系统属性和环境变量都不存在时")
        void getEnv_shouldReturnNull_whenBothSystemPropertyAndEnvironmentVariableAreNull() {
            // Arrange
            String nonExistentKey = "definitely.non.existent.key.12345";

            // Act
            String result = Envs.getEnv(nonExistentKey);

            // Assert
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("优先级测试")
    class PriorityTests {

        @Test
        @DisplayName("getEnv_应该优先返回系统属性_当系统属性和环境变量都存在时")
        void getEnv_shouldPreferSystemProperty_whenBothSystemPropertyAndEnvironmentVariableExist() {
            // Arrange
            System.setProperty(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);

            // Act
            String result = Envs.getEnv(TEST_PROPERTY_KEY);

            // Assert
            // 系统属性应该优先
            assertThat(result).isEqualTo(TEST_PROPERTY_VALUE);
        }
    }

    @Nested
    @DisplayName("集成测试")
    class IntegrationTests {

        @Test
        @DisplayName("getEnv_应该正常工作_当使用真实系统属性时")
        void getEnv_shouldWorkCorrectly_whenUsingRealSystemProperty() {
            // Act
            String javaVersion = Envs.getEnv("java.version");

            // Assert
            // java.version 应该总是存在
            assertThat(javaVersion).isNotNull().isNotEmpty();
            assertThat(javaVersion).isEqualTo(System.getProperty("java.version"));
        }

        @Test
        @DisplayName("getEnv_应该正常工作_当使用PATH环境变量时")
        void getEnv_shouldWorkCorrectly_whenUsingPATHEnvironmentVariable() {
            // Arrange
            String pathEnv = System.getenv("PATH");
            if (pathEnv == null) {
                // 在某些系统上可能是 Path
                pathEnv = System.getenv("Path");
            }

            // 如果系统有 PATH 环境变量，测试我们的方法
            if (pathEnv != null) {
                // Arrange
                System.clearProperty("PATH");

                // Act
                String result = Envs.getEnv("PATH");

                // Assert
                assertThat(result).isEqualTo(pathEnv);
            }
        }
    }
}