package cn.xuanyuanli.core.constant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.util.Collections;

/**
 * 系统属性
 *
 * @author John Li
 */
@SuppressWarnings("unused")
public abstract class SystemProperties {

    /**
     * 项目目录
     */
    public static final String PROJECT_DIR = System.getProperty("user.dir");
    /**
     * 操作系统位数
     */
    public static final String OS_ARCH = System.getProperty("os.arch");
    /**
     * 系统临时目录
     */
    public static final String TMPDIR = System.getProperty("java.io.tmpdir");
    /**
     * 操作系统名称
     */
    public static final String OS_NAME = System.getProperty("os.name");
    /**
     * 系统编码
     */
    public static final String OS_ENCODING = System.getProperty("sun.jnu.encoding");
    /**
     * 用户家目录
     */
    public static final String USER_HOME = System.getProperty("user.home");
    /**
     * 用户名
     */
    public static final String USER_NAME = System.getProperty("user.name");
    /**
     * 项目用到的 class path 集合
     */
    public static final String CLASS_PATH = System.getProperty("java.class.path");
    /**
     * 用户所在地语言
     */
    public static final String USER_LANGUAGE = System.getProperty("user.language");
    /**
     * 文件分隔符
     */
    public static final String FILE_SEPARATOR = FileSystems.getDefault().getSeparator();
    /**
     * 图形桌面名称
     */
    public static final String OS_DESKTOP = System.getProperty("sun.desktop");

    /**
     * JVM vendor info.
     */
    public static final String JVM_VENDOR = System.getProperty("java.vm.vendor");
    /**
     * jvm版本
     */
    public static final String JVM_VERSION = System.getProperty("java.vm.version");
    /**
     * jvm名字
     */
    public static final String JVM_NAME = System.getProperty("java.vm.name");

    /**
     * The value of <tt>System.getProperty("java.version")</tt>.
     **/
    public static final String JAVA_VERSION = System.getProperty("java.version");

    /**
     * True iff running on Linux.
     */
    public static final boolean LINUX = OS_NAME.startsWith("Linux");
    /**
     * True iff running on Windows.
     */
    public static final boolean WINDOWS = OS_NAME.startsWith("Windows");
    /**
     * True iff running on SunOS.
     */
    public static final boolean SUN_OS = OS_NAME.startsWith("SunOS");
    /**
     * True iff running on Mac OS X
     */
    public static final boolean MAC_OS_X = OS_NAME.startsWith("Mac OS X");
    /**
     * True iff running on FreeBSD
     */
    public static final boolean FREE_BSD = OS_NAME.startsWith("FreeBSD");

    /**
     * 操作系统版本
     */
    public static final String OS_VERSION = System.getProperty("os.version");
    /**
     * java厂商
     */
    public static final String JAVA_VENDOR = System.getProperty("java.vendor");

    /**
     * jre是否最低java8
     */
    public static final boolean JRE_IS_MINIMUM_JAVA8;

    /**
     * True iff running on a 64bit JVM
     */
    public static final boolean JRE_IS_64BIT;

    static {
        JRE_IS_64BIT = isIs64Bit();

        // this method only exists in Java 8:
        JRE_IS_MINIMUM_JAVA8 = isV8();
    }

    static boolean isV8() {
        boolean v8 = true;
        try {
            SpInnerUtil.collectionsHasV8Method();
        } catch (NoSuchMethodException nsme) {
            v8 = false;
        }
        return v8;
    }

    static boolean isIs64Bit() {
        boolean is64Bit;
        try {
            final int addressSize = SpInnerUtil.getAddressSize();
            is64Bit = addressSize >= 8;
        } catch (Exception e) {
            final String x = System.getProperty("sun.arch.data.model");
            String str64 = "64";
            if (x != null) {
                is64Bit = x.contains(str64);
            } else {
                is64Bit = OS_ARCH != null && OS_ARCH.contains(str64);
            }
        }
        return is64Bit;
    }

    static class SpInnerUtil {

        @SuppressWarnings("ResultOfMethodCallIgnored")
        static void collectionsHasV8Method() throws NoSuchMethodException {
            Collections.class.getMethod("emptySortedSet");
        }

        static int getAddressSize()
                throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            final Field unsafeField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            final Object unsafe = unsafeField.get(null);
            return ((Number) unsafeClass.getMethod("addressSize").invoke(unsafe)).intValue();
        }
    }
}
