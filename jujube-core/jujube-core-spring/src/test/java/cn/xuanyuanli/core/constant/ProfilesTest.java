package cn.xuanyuanli.core.constant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import cn.xuanyuanli.core.spring.ApplicationContextHolder;
import cn.xuanyuanli.core.util.Envs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

@DisplayName("环境配置工具类测试")
class ProfilesTest {

    @Nested
    @DisplayName("设置Spring环境配置到系统属性")
    class SetSpringProfileToSystemProperty {

        @Test
        @DisplayName("应该返回生产环境配置 - 当设置为生产环境时")
        void setSpringProfileToSystemProperty_shouldReturnProduction_whenSetToProduction() {
            // Arrange
            String expectedProfile = Profiles.PRODUCTION;

            // Act
            Profiles.setSpringProfileToSystemProperty(expectedProfile);
            String actualProfile = Profiles.getSpringProfileFromSystemProperty();

            // Assert
            assertThat(actualProfile).isEqualTo(expectedProfile);
        }

        @Test
        @DisplayName("应该返回测试环境配置状态为真 - 当设置为空字符串时")
        void isTestProfile_shouldReturnTrue_whenSetToEmptyString() {
            // Arrange
            Profiles.setSpringProfileToSystemProperty("");

            // Act
            boolean isTestProfile = Profiles.isTestProfile();

            // Assert
            assertThat(isTestProfile).isTrue();
        }

        @Test
        @DisplayName("应该返回生产环境配置状态为假 - 当设置为空字符串时")
        void isProdProfile_shouldReturnFalse_whenSetToEmptyString() {
            // Arrange
            Profiles.setSpringProfileToSystemProperty("");

            // Act
            boolean isProdProfile = Profiles.isProdProfile();

            // Assert
            assertThat(isProdProfile).isFalse();
        }
    }

    @Nested
    @DisplayName("从系统属性获取Spring环境配置")
    class GetSpringProfileFromSystemProperty {

        @Test
        @DisplayName("应该返回测试环境配置 - 当ApplicationContextHolder返回测试环境时")
        void getSpringProfileFromSystemProperty_shouldReturnTest_whenApplicationContextHolderReturnsTest() {
            // Arrange
            Environment environment = Mockito.mock(Environment.class);
            when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

            try (MockedStatic<ApplicationContextHolder> mockedContextHolder = 
                    Mockito.mockStatic(ApplicationContextHolder.class)) {
                
                mockedContextHolder.when(ApplicationContextHolder::getEnvironment).thenReturn(environment);

                // Act
                String result = Profiles.getSpringProfileFromSystemProperty();

                // Assert
                assertThat(result).isEqualTo("test");
            }
        }

        @Test
        @DisplayName("应该返回空字符串 - 当环境变量为空时")
        void getSpringProfileFromSystemProperty_shouldReturnEmpty_whenEnvIsBlank() {
            try (MockedStatic<Envs> mockedEnvs = Mockito.mockStatic(Envs.class)) {
                // Arrange
                mockedEnvs.when(() -> Envs.getEnv(Profiles.SPRING_PROFILES_ACTIVE)).thenReturn("");

                // Act
                String result = Profiles.getSpringProfileFromSystemProperty();

                // Assert
                assertThat(result).isEmpty();
            }
        }
    }
}
