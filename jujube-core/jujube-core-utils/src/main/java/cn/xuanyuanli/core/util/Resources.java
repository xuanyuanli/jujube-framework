package cn.xuanyuanli.core.util;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ClassUtils;

/**
 * 资源工具
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Resources {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(Resources.class);
    /**
     * 默认资源模式
     */
    static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";
    /**
     * 资源模式解析器
     */
    private static final ResourcePatternResolver RESOURCE_PATTERN_RESOLVER = new PathMatchingResourcePatternResolver();

    /**
     * 获得包下的所有class
     *
     * @param packageName 包名
     * @return {@link List}<{@link Class}<{@link ?}>>
     */
    public static List<Class<?>> getPackageClasses(String packageName) {
        List<Class<?>> list = new ArrayList<>();
        String packageSearchPath =
                ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(packageName) + "/" + DEFAULT_RESOURCE_PATTERN;
        try {
            Resource[] resources = RESOURCE_PATTERN_RESOLVER.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    String filePath = URLDecoder.decode(resource.getURL().getFile(), StandardCharsets.UTF_8);
                    String className = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length() - 6);
                    if (!className.contains("$")) {
                        try {
                            Class<?> clazz = Thread.currentThread().getContextClassLoader()
                                    .loadClass(getRealPackageName(filePath, packageName) + "." + className);
                            list.add(clazz);
                        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("resourcePatternResolver.getResources", e);
        }
        return list;
    }

    /**
     * 获得真实的文件系统的包路径
     *
     * @param filePath    文件路径
     * @param packageName 包名
     * @return {@link String}
     */
    private static String getRealPackageName(String filePath, String packageName) {
        String pName = packageName.replace(".", "/");
        String tPath = filePath.substring(filePath.indexOf(pName));
        pName = tPath.substring(0, tPath.lastIndexOf("/"));
        return pName.replace("/", ".");
    }

    /**
     * 获取所有classpath下对应名称的Properties文件属性
     *
     * @param fileName 相对于classpath的文件位置
     * @return {@link Properties}
     */
    public static Properties getProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName不能为空");
        }

        Properties properties = new Properties();
        try {
            Resource[] resources = getClassPathAllResources(fileName);
            for (int i = resources.length - 1; i >= 0; i--) {
                Resource resource = resources[i];
                try (InputStream inputStream = resource.getInputStream()) {
                    properties.load(inputStream);
                    logger.debug("loadTreeMap properties:{}", resource.getURL());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    /**
     * 获取当前classpath下对应名称的Properties文件属性
     *
     * @param fileName 相对于classpath的文件位置
     * @return {@link Properties}
     */
    public static Properties getCurrentClasspathProperties(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName不能为空");
        }

        Properties properties = new Properties();
        Resource resource = getClassPathResources(fileName);
        if (resource != null) {
            try (InputStream inputStream = resource.getInputStream()) {
                properties.load(inputStream);
                logger.debug("loadTreeMap properties:{}", resource.getURL());
            } catch (FileNotFoundException f) {
                // ignored
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    /**
     * 获得classpath*下的指定资源
     *
     * @param resourceName 资源名称
     * @return {@link Resource[]}
     */
    public static Resource[] getClassPathAllResources(String resourceName) {
        Resource[] resources = null;
        try {
            resources = RESOURCE_PATTERN_RESOLVER.getResources("classpath*:" + resourceName);
        } catch (IOException ignored) {
        }
        return resources;
    }

    /**
     * 获得classpath下的指定资源
     *
     * @param resourceName 资源名称
     * @return {@link Resource}
     */
    public static Resource getClassPathResources(String resourceName) {
        Resource[] resources = null;
        try {
            resources = RESOURCE_PATTERN_RESOLVER.getResources("classpath:" + resourceName);
        } catch (IOException ignored) {
        }
        return resources != null && resources.length > 0 ? resources[0] : null;
    }

    /**
     * 获得当前的classpath目录
     *
     * @return {@link File}
     */
    public static File getCurrentClasspath() {
        Resource resource = RESOURCE_PATTERN_RESOLVER.getResource("classpath:./");
        try {
            return resource.getFile();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获得当前项目路径（只限于Eclipse或IDEA中管用）
     *
     * @return {@link String}
     */
    public static String getProjectPath() {
        File dir = getCurrentClasspath();
        if (dir != null) {
            // 找到target目录
            while (true) {
                if ("target".equals(dir.getName())) {
                    break;
                } else {
                    dir = dir.getParentFile();
                }
            }
            return dir.getParentFile().getAbsolutePath();
        }
        return null;
    }

    /**
     * 获得某个class所在的jar所在的目录（例如打包A项目为jar，获取的就是A.jar所在的目录）
     *
     * @param cl cl
     * @return {@link String}
     */
    public static String getJarHome(Class<?> cl) {
        String path = cl.getProtectionDomain().getCodeSource().getLocation().getFile();
        File jarFile = new File(path);
        return jarFile.getParentFile().getAbsolutePath();
    }

    /**
     * 文件是否来自于Jar中
     *
     * @param url url
     * @return boolean
     */
    public static boolean isJarFile(URL url) {
        return "jar".equals(url.getProtocol());
    }

    /**
     * 此Class是否从jar中启动
     *
     * @param cl cl
     * @return boolean
     */
    public static boolean isJarStartByClass(Class<?> cl) {
        URL url = cl.getResource(cl.getSimpleName() + ".class");
        return url != null && "jar".equals(url.getProtocol());
    }

    /**
     * 获得classpath下的指定资源--InputStream
     *
     * @param path 路径
     * @return {@link InputStream}
     */
    @SneakyThrows
    public static InputStream getClassPathResourcesInputStream(String path) {
        Resource classPathResources = getClassPathResources(path);
        return classPathResources != null ? classPathResources.getInputStream() : null;
    }
}
