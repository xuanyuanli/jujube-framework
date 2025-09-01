package cn.xuanyuanli.core.constant;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.util.Collections;

/**
 * 系统属性常量类
 * <p>
 * 该类提供了常用的系统属性常量，包括操作系统信息、JVM信息、文件路径等。
 * 所有属性都是静态常量，在类加载时初始化，可以直接通过类名访问。
 * </p>
 * 
 * <p>主要功能：</p>
 * <ul>
 * <li>操作系统相关信息：名称、版本、架构等</li>
 * <li>JVM相关信息：版本、厂商、名称等</li>
 * <li>用户环境信息：用户名、家目录、语言等</li>
 * <li>文件系统信息：分隔符、临时目录等</li>
 * <li>平台判断：是否为特定操作系统或JVM版本</li>
 * </ul>
 *
 * @author xuanyuanli
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

    /**
     * 检查当前JRE是否为Java 8或更高版本
     * <p>
     * 通过检查Collections类是否存在emptySortedSet()方法来判断，
     * 该方法是在Java 8中引入的。
     * </p>
     * 
     * @return 如果是Java 8或更高版本返回true，否则返回false
     */
    static boolean isV8() {
        boolean v8 = true;
        try {
            SpInnerUtil.collectionsHasV8Method();
        } catch (NoSuchMethodException nsme) {
            v8 = false;
        }
        return v8;
    }

    /**
     * 检查当前JVM是否运行在64位系统上
     * <p>
     * 首先尝试通过sun.misc.Unsafe获取地址大小来判断，
     * 如果失败则通过系统属性sun.arch.data.model或os.arch来判断。
     * </p>
     * 
     * @return 如果运行在64位JVM上返回true，否则返回false
     */
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

    /**
     * 系统属性检测工具内部类
     * <p>
     * 提供底层的系统检测方法，用于支持SystemProperties类的功能。
     * 包含Java版本检测和JVM位数检测的具体实现。
     * </p>
     */
    static class SpInnerUtil {

        /**
         * 检查Collections类是否包含Java 8新增的方法
         * <p>
         * 通过反射检查Collections.emptySortedSet()方法是否存在，
         * 该方法在Java 8中新增，用于判断当前Java版本。
         * </p>
         * 
         * @throws NoSuchMethodException 如果方法不存在（即Java版本低于8）
         */
        @SuppressWarnings("ResultOfMethodCallIgnored")
        static void collectionsHasV8Method() throws NoSuchMethodException {
            Collections.class.getMethod("emptySortedSet");
        }

        /**
         * 获取JVM的地址大小
         * <p>
         * 通过sun.misc.Unsafe类获取JVM的地址大小，用于判断是否为64位JVM。
         * 地址大小大于等于8字节表示64位JVM。
         * </p>
         * 
         * @return JVM地址大小（字节数）
         * @throws ClassNotFoundException 如果找不到Unsafe类
         * @throws NoSuchFieldException 如果找不到theUnsafe字段
         * @throws IllegalAccessException 如果无法访问字段
         * @throws InvocationTargetException 如果方法调用失败
         * @throws NoSuchMethodException 如果找不到addressSize方法
         */
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
