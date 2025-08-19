package cn.xuanyuanli.core.constant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;

import cn.xuanyuanli.core.constant.SystemProperties.SpInnerUtil;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class SystemPropertiesTest {

    @Test
    void testProjectDir() {
        assertNotNull(SystemProperties.PROJECT_DIR);
        assertFalse(SystemProperties.PROJECT_DIR.isEmpty());
    }

    @Test
    void testOsArch() {
        assertNotNull(SystemProperties.OS_ARCH);
        assertFalse(SystemProperties.OS_ARCH.isEmpty());
    }

    @Test
    void testTmpDir() {
        assertNotNull(SystemProperties.TMPDIR);
        assertFalse(SystemProperties.TMPDIR.isEmpty());
    }

    @Test
    void testOsName() {
        assertNotNull(SystemProperties.OS_NAME);
        assertFalse(SystemProperties.OS_NAME.isEmpty());
    }

    @Test
    void testOsEncoding() {
        assertNotNull(SystemProperties.OS_ENCODING);
        assertFalse(SystemProperties.OS_ENCODING.isEmpty());
    }

    @Test
    void testUserHome() {
        assertNotNull(SystemProperties.USER_HOME);
        assertFalse(SystemProperties.USER_HOME.isEmpty());
    }

    @Test
    void testUserName() {
        assertNotNull(SystemProperties.USER_NAME);
        assertFalse(SystemProperties.USER_NAME.isEmpty());
    }

    @Test
    void testClassPath() {
        assertNotNull(SystemProperties.CLASS_PATH);
        assertFalse(SystemProperties.CLASS_PATH.isEmpty());
    }

    @Test
    void testUserLanguage() {
        assertNotNull(SystemProperties.USER_LANGUAGE);
        assertFalse(SystemProperties.USER_LANGUAGE.isEmpty());
    }

    @Test
    void testFileSeparator() {
        assertNotNull(SystemProperties.FILE_SEPARATOR);
        assertFalse(SystemProperties.FILE_SEPARATOR.isEmpty());
    }

    @Test
    void testOsDesktop() {
        if (SystemProperties.WINDOWS) {
            assertNull(SystemProperties.OS_DESKTOP);
        }
    }

    @Test
    void testJvmVendor() {
        assertNotNull(SystemProperties.JVM_VENDOR);
        assertFalse(SystemProperties.JVM_VENDOR.isEmpty());
    }

    @Test
    void testJvmVersion() {
        assertNotNull(SystemProperties.JVM_VERSION);
        assertFalse(SystemProperties.JVM_VERSION.isEmpty());
    }

    @Test
    void testJvmName() {
        assertNotNull(SystemProperties.JVM_NAME);
        assertFalse(SystemProperties.JVM_NAME.isEmpty());
    }

    @Test
    void testJavaVersion() {
        assertNotNull(SystemProperties.JAVA_VERSION);
        assertFalse(SystemProperties.JAVA_VERSION.isEmpty());
    }

    @Test
    void testOsVersion() {
        assertNotNull(SystemProperties.OS_VERSION);
        assertFalse(SystemProperties.OS_VERSION.isEmpty());
    }

    @Test
    void testJavaVendor() {
        assertNotNull(SystemProperties.JAVA_VENDOR);
        assertFalse(SystemProperties.JAVA_VENDOR.isEmpty());
    }

    @Test
    void testJreIs64Bit() {
        assertTrue(SystemProperties.JRE_IS_64BIT);
    }

    @Test
    public void testJreIs64BitThrowsException() {
        // 使用 Mockito 模拟静态方法
        try (MockedStatic<SpInnerUtil> mockedClass = Mockito.mockStatic(SpInnerUtil.class)) {
            // 模拟 方法 抛出 ClassNotFoundException
            mockedClass.when(SpInnerUtil::getAddressSize)
                    .thenThrow(new ClassNotFoundException("sun.misc.Unsafe not found"));

            // 验证异常是否被抛出
            assertTrue(SystemProperties.isIs64Bit());

            // 验证是否被调用
            mockedClass.verify(SpInnerUtil::getAddressSize, atLeastOnce());
        }
    }

    @Test
    void testJreIsMinimumJava8() {
        assertTrue(SystemProperties.JRE_IS_MINIMUM_JAVA8);
    }

    @Test
    public void testJreIsMinimumJava8ThrowsException() {
        // 使用 Mockito 模拟静态方法
        try (MockedStatic<SpInnerUtil> mockedClass = Mockito.mockStatic(SpInnerUtil.class)) {
            // 模拟 方法 抛出 ClassNotFoundException
            mockedClass.when(SpInnerUtil::collectionsHasV8Method)
                    .thenThrow(new NoSuchMethodException("not found"));

            // 验证异常是否被抛出
            assertFalse(SystemProperties.isV8());

            // 验证是否被调用
            mockedClass.verify(SpInnerUtil::collectionsHasV8Method, atLeastOnce());
        }
    }

    @Test
    void testOsSpecificFlags() {
        // These flags should be consistent with the OS_NAME
        if (SystemProperties.OS_NAME.startsWith("Linux")) {
            assertTrue(SystemProperties.LINUX);
        } else if (SystemProperties.OS_NAME.startsWith("Windows")) {
            assertTrue(SystemProperties.WINDOWS);
        } else if (SystemProperties.OS_NAME.startsWith("SunOS")) {
            assertTrue(SystemProperties.SUN_OS);
        } else if (SystemProperties.OS_NAME.startsWith("Mac OS X")) {
            assertTrue(SystemProperties.MAC_OS_X);
        } else if (SystemProperties.OS_NAME.startsWith("FreeBSD")) {
            assertTrue(SystemProperties.FREE_BSD);
        }
    }
}

