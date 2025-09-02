package cn.xuanyuanli.core.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@DisplayName("应用上下文持有者测试")
class ApplicationContextHolderTest {

    private ApplicationContext applicationContext;
    private Environment environment;

    @BeforeEach
    void setUp() {
        applicationContext = Mockito.mock(ApplicationContext.class);
        environment = Mockito.mock(Environment.class);
        when(applicationContext.getEnvironment()).thenReturn(environment);
    }

    @Nested
    @DisplayName("设置应用上下文")
    class SetApplicationContext {

        @Test
        @DisplayName("应该设置应用上下文成功 - 当通过ApplicationContextAware接口设置时")
        void setApplicationContext_shouldSetSuccessfully_whenSetThroughInterface() {
            // Arrange
            ApplicationContextHolder holder = new ApplicationContextHolder();

            // Act
            holder.setApplicationContext(applicationContext);
            ApplicationContext actual = ApplicationContextHolder.getApplicationContext();

            // Assert
            assertThat(actual).isEqualTo(applicationContext);
        }

        @Test
        @DisplayName("应该手动设置应用上下文成功 - 当通过静态方法设置时")
        void manualSetApplicationContext_shouldSetSuccessfully_whenSetThroughStaticMethod() {
            // Arrange & Act
            ApplicationContextHolder.manualSetApplicationContext(applicationContext);
            ApplicationContext actual = ApplicationContextHolder.getApplicationContext();

            // Assert
            assertThat(actual).isEqualTo(applicationContext);
        }
    }

    @Nested
    @DisplayName("获取环境配置")
    class GetEnvironment {

        @Test
        @DisplayName("应该返回环境配置对象 - 当应用上下文已设置时")
        void getEnvironment_shouldReturnEnvironment_whenApplicationContextIsSet() {
            // Arrange
            ApplicationContextHolder.manualSetApplicationContext(applicationContext);

            // Act
            Environment actual = ApplicationContextHolder.getEnvironment();

            // Assert
            assertThat(actual).isEqualTo(environment);
        }
    }

    @Nested
    @DisplayName("获取配置属性")
    class GetProperty {

        @Test
        @DisplayName("应该返回配置属性值 - 当属性存在时")
        void getProperty_shouldReturnPropertyValue_whenPropertyExists() {
            // Arrange
            ApplicationContextHolder.manualSetApplicationContext(applicationContext);
            when(environment.getProperty("test.property2")).thenReturn("testValue");

            // Act
            String actual = ApplicationContextHolder.getProperty("test.property2");

            // Assert
            assertThat(actual).isEqualTo("testValue");
        }

        @Test
        @DisplayName("应该返回默认值 - 当属性不存在且有默认值时")
        void getProperty_shouldReturnDefaultValue_whenPropertyNotExistsAndDefaultValueProvided() {
            // Arrange
            ApplicationContextHolder.manualSetApplicationContext(applicationContext);
            when(environment.getProperty("test.property")).thenReturn(null);

            // Act
            String actual = ApplicationContextHolder.getProperty("test.property", "defaultValue");

            // Assert
            assertThat(actual).isEqualTo("defaultValue");
        }

        @Test
        @DisplayName("应该返回null - 当环境配置为null且无默认值时")
        void getProperty_shouldReturnNull_whenEnvironmentIsNullAndNoDefaultValue() {
            // Arrange
            ApplicationContextHolder.manualSetApplicationContext(null);

            // Act
            String actual = ApplicationContextHolder.getProperty("test.property");

            // Assert
            assertThat(actual).isNull();
        }

        @Test
        @DisplayName("应该返回默认值 - 当环境配置为null但有默认值时")
        void getProperty_shouldReturnDefaultValue_whenEnvironmentIsNullButDefaultValueProvided() {
            // Arrange
            ApplicationContextHolder.manualSetApplicationContext(null);

            // Act
            String actual = ApplicationContextHolder.getProperty("test.property", "defaultValue");

            // Assert
            assertThat(actual).isEqualTo("defaultValue");
        }
    }
}
