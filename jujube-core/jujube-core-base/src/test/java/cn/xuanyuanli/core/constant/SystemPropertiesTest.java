package cn.xuanyuanli.core.constant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;

import cn.xuanyuanli.core.constant.SystemProperties.SpInnerUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * SystemProperties 系统属性常量测试
 * 验证系统属性、用户相关属性、Java/JVM相关属性和操作系统标识的正确性
 */
@DisplayName("SystemProperties 系统属性常量测试")
class SystemPropertiesTest {

    @Nested
    @DisplayName("基本系统属性测试")
    class BasicSystemPropertiesTests {

        @Test
        @DisplayName("PROJECT_DIR_应该不为空_当获取项目目录时")
        void PROJECT_DIR_shouldNotBeEmpty_whenGettingProjectDirectory() {
            // Assert
            assertThat(SystemProperties.PROJECT_DIR).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("OS_ARCH_应该不为空_当获取操作系统架构时")
        void OS_ARCH_shouldNotBeEmpty_whenGettingOsArchitecture() {
            // Assert
            assertThat(SystemProperties.OS_ARCH).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("TMPDIR_应该不为空_当获取临时目录时")
        void TMPDIR_shouldNotBeEmpty_whenGettingTempDirectory() {
            // Assert
            assertThat(SystemProperties.TMPDIR).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("OS_NAME_应该不为空_当获取操作系统名称时")
        void OS_NAME_shouldNotBeEmpty_whenGettingOsName() {
            // Assert
            assertThat(SystemProperties.OS_NAME).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("OS_ENCODING_应该不为空_当获取操作系统编码时")
        void OS_ENCODING_shouldNotBeEmpty_whenGettingOsEncoding() {
            // Assert
            assertThat(SystemProperties.OS_ENCODING).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("FILE_SEPARATOR_应该不为空_当获取文件分隔符时")
        void FILE_SEPARATOR_shouldNotBeEmpty_whenGettingFileSeparator() {
            // Assert
            assertThat(SystemProperties.FILE_SEPARATOR).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("OS_DESKTOP_应该为null_当运行在Windows系统时")
        void OS_DESKTOP_shouldBeNull_whenRunningOnWindows() {
            // Act & Assert
            if (SystemProperties.WINDOWS) {
                assertThat(SystemProperties.OS_DESKTOP).isNull();
            }
        }

        @Test
        @DisplayName("OS_VERSION_应该不为空_当获取操作系统版本时")
        void OS_VERSION_shouldNotBeEmpty_whenGettingOsVersion() {
            // Assert
            assertThat(SystemProperties.OS_VERSION).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("用户相关属性测试")
    class UserPropertiesTests {

        @Test
        @DisplayName("USER_HOME_应该不为空_当获取用户主目录时")
        void USER_HOME_shouldNotBeEmpty_whenGettingUserHome() {
            // Assert
            assertThat(SystemProperties.USER_HOME).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("USER_NAME_应该不为空_当获取用户名时")
        void USER_NAME_shouldNotBeEmpty_whenGettingUserName() {
            // Assert
            assertThat(SystemProperties.USER_NAME).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("USER_LANGUAGE_应该不为空_当获取用户语言时")
        void USER_LANGUAGE_shouldNotBeEmpty_whenGettingUserLanguage() {
            // Assert
            assertThat(SystemProperties.USER_LANGUAGE).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Java相关属性测试")
    class JavaPropertiesTests {

        @Test
        @DisplayName("CLASS_PATH_应该不为空_当获取类路径时")
        void CLASS_PATH_shouldNotBeEmpty_whenGettingClassPath() {
            // Assert
            assertThat(SystemProperties.CLASS_PATH).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("JAVA_VERSION_应该不为空_当获取Java版本时")
        void JAVA_VERSION_shouldNotBeEmpty_whenGettingJavaVersion() {
            // Assert
            assertThat(SystemProperties.JAVA_VERSION).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("JAVA_VENDOR_应该不为空_当获取Java供应商时")
        void JAVA_VENDOR_shouldNotBeEmpty_whenGettingJavaVendor() {
            // Assert
            assertThat(SystemProperties.JAVA_VENDOR).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("JRE_IS_MINIMUM_JAVA8_应该为true_当JRE版本不低于Java8时")
        void JRE_IS_MINIMUM_JAVA8_shouldBeTrue_whenJreVersionIsAtLeastJava8() {
            // Assert
            assertThat(SystemProperties.JRE_IS_MINIMUM_JAVA8).isTrue();
        }

        @Test
        @DisplayName("isV8_应该返回false_当方法检查抛出异常时")
        void isV8_shouldReturnFalse_whenMethodCheckThrowsException() {
            // Arrange & Act
            try (MockedStatic<SpInnerUtil> mockedClass = Mockito.mockStatic(SpInnerUtil.class)) {
                mockedClass.when(SpInnerUtil::collectionsHasV8Method)
                        .thenThrow(new NoSuchMethodException("not found"));

                // Act
                boolean result = SystemProperties.isV8();

                // Assert
                assertThat(result).isFalse();
                mockedClass.verify(SpInnerUtil::collectionsHasV8Method, atLeastOnce());
            }
        }
    }

    @Nested
    @DisplayName("JVM相关属性测试")
    class JvmPropertiesTests {

        @Test
        @DisplayName("JVM_VENDOR_应该不为空_当获取JVM供应商时")
        void JVM_VENDOR_shouldNotBeEmpty_whenGettingJvmVendor() {
            // Assert
            assertThat(SystemProperties.JVM_VENDOR).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("JVM_VERSION_应该不为空_当获取JVM版本时")
        void JVM_VERSION_shouldNotBeEmpty_whenGettingJvmVersion() {
            // Assert
            assertThat(SystemProperties.JVM_VERSION).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("JVM_NAME_应该不为空_当获取JVM名称时")
        void JVM_NAME_shouldNotBeEmpty_whenGettingJvmName() {
            // Assert
            assertThat(SystemProperties.JVM_NAME).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("JRE_IS_64BIT_应该为true_当JRE为64位时")
        void JRE_IS_64BIT_shouldBeTrue_whenJreIs64Bit() {
            // Assert
            assertThat(SystemProperties.JRE_IS_64BIT).isTrue();
        }

        @Test
        @DisplayName("isIs64Bit_应该返回true_当无法获取地址大小时")
        void isIs64Bit_shouldReturnTrue_whenAddressSizeUnavailable() {
            // Arrange & Act
            try (MockedStatic<SpInnerUtil> mockedClass = Mockito.mockStatic(SpInnerUtil.class)) {
                mockedClass.when(SpInnerUtil::getAddressSize)
                        .thenThrow(new ClassNotFoundException("sun.misc.Unsafe not found"));

                // Act
                boolean result = SystemProperties.isIs64Bit();

                // Assert
                assertThat(result).isTrue();
                mockedClass.verify(SpInnerUtil::getAddressSize, atLeastOnce());
            }
        }
    }

    @Nested
    @DisplayName("操作系统标识测试")
    class OperatingSystemFlagsTests {

        @Test
        @DisplayName("操作系统标识_应该与操作系统名称一致_当检查各种OS时")
        void osFlags_shouldBeConsistentWithOsName_whenCheckingVariousOS() {
            // Act & Assert
            if (SystemProperties.OS_NAME.startsWith("Linux")) {
                assertThat(SystemProperties.LINUX).isTrue();
            } else if (SystemProperties.OS_NAME.startsWith("Windows")) {
                assertThat(SystemProperties.WINDOWS).isTrue();
            } else if (SystemProperties.OS_NAME.startsWith("SunOS")) {
                assertThat(SystemProperties.SUN_OS).isTrue();
            } else if (SystemProperties.OS_NAME.startsWith("Mac OS X")) {
                assertThat(SystemProperties.MAC_OS_X).isTrue();
            } else if (SystemProperties.OS_NAME.startsWith("FreeBSD")) {
                assertThat(SystemProperties.FREE_BSD).isTrue();
            }
        }
    }
}

