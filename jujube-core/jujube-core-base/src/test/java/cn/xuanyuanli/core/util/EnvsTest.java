package cn.xuanyuanli.core.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    @DisplayName("获取存在的系统属性")
    void shouldReturnSystemPropertyWhenExists() {
        // 设置系统属性
        System.setProperty(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
        
        String result = Envs.getEnv(TEST_PROPERTY_KEY);
        
        assertEquals(TEST_PROPERTY_VALUE, result);
    }

    @Test
    @DisplayName("当系统属性不存在时，应该尝试获取环境变量")
    void shouldTryEnvironmentVariableWhenSystemPropertyNotExists() {
        // 确保系统属性不存在
        System.clearProperty(TEST_PROPERTY_KEY);
        
        String result = Envs.getEnv(TEST_PROPERTY_KEY);
        
        // 由于我们无法控制环境变量，只能验证结果与 System.getenv 一致
        assertEquals(System.getenv(TEST_PROPERTY_KEY), result);
    }

    @Test
    @DisplayName("当系统属性为空字符串时，应该尝试获取环境变量")
    void shouldTryEnvironmentVariableWhenSystemPropertyIsEmpty() {
        // 设置系统属性为空字符串
        System.setProperty(TEST_PROPERTY_KEY, "");
        
        String result = Envs.getEnv(TEST_PROPERTY_KEY);
        
        // 由于系统属性是空字符串，应该尝试获取环境变量
        assertEquals(System.getenv(TEST_PROPERTY_KEY), result);
    }

    @Test
    @DisplayName("当系统属性和环境变量都不存在时，应该返回null")
    void shouldReturnNullWhenBothSystemPropertyAndEnvironmentVariableAreNull() {
        // 使用一个不存在的键
        String nonExistentKey = "definitely.non.existent.key.12345";
        
        String result = Envs.getEnv(nonExistentKey);
        
        assertNull(result);
    }

    @Test
    @DisplayName("系统属性优先级应该高于环境变量")
    void shouldPreferSystemPropertyOverEnvironmentVariable() {
        // 设置系统属性
        System.setProperty(TEST_PROPERTY_KEY, TEST_PROPERTY_VALUE);
        
        String result = Envs.getEnv(TEST_PROPERTY_KEY);
        
        // 系统属性应该优先
        assertEquals(TEST_PROPERTY_VALUE, result);
    }

    @Test
    @DisplayName("使用真实的系统属性进行集成测试")
    void shouldWorkWithRealSystemProperty() {
        // 使用 Java 内置的系统属性进行测试
        String javaVersion = Envs.getEnv("java.version");
        
        // java.version 应该总是存在
        assertNotNull(javaVersion);
        assertEquals(System.getProperty("java.version"), javaVersion);
    }

    @Test
    @DisplayName("使用PATH环境变量进行测试")
    void shouldWorkWithRealEnvironmentVariable() {
        // PATH 环境变量在所有操作系统上都应该存在
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) {
            // 在某些系统上可能是 Path
            pathEnv = System.getenv("Path");
        }
        
        // 如果系统有 PATH 环境变量，测试我们的方法
        if (pathEnv != null) {
            // 确保系统属性中没有 PATH
            System.clearProperty("PATH");
            String result = Envs.getEnv("PATH");
            assertEquals(pathEnv, result);
        }
    }
}