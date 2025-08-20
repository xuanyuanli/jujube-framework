package cn.xuanyuanli.core.util.support.freemarker;

import freemarker.cache.URLTemplateLoader;
import java.net.URL;
import java.util.Objects;
import org.springframework.util.ClassUtils;

/**
 * 为解决不能读取jar中目录的问题，拓展Freemarker的TemplateLoader
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
public class ClassloaderTemplateLoader extends URLTemplateLoader {

    /**
     * 路径
     */
    private final String path;

    /**
     * 模板类加载器加载程序
     *
     * @param path 路径
     */
    public ClassloaderTemplateLoader(String path) {
        super();
        this.path = canonicalizePrefix(path);
    }

    /**
     * 获得url
     *
     * @param name 名字
     * @return {@link URL}
     */
    @Override
    protected URL getURL(String name) {
        name = path + name;
        return Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).getResource(name);
    }

}
