package cn.xuanyuanli.core.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.util.support.freemarker.ClassloaderTemplateLoader;

/**
 * 项目的FreeMarker总体配置类。直接调用其中方法生成模板
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 */
public class Ftls {

    /**
     * FreeMarker模板文件存放的根目录路径
     */
    private static final String FTL_DIR = "templates";

    /**
     * 私有构造函数，防止实例化工具类
     */
    private Ftls() {
    }

    /**
     * 文件模板配置对象，用于加载和处理模板文件
     */
    private static final Configuration FILE_TEMPLATE_CONFIGURATION;
    
    /**
     * 字符串模板配置对象，用于处理字符串形式的模板
     */
    private static final Configuration STRING_TEMPLATE_CONFIGURATION;

    /**
     * 字符串模板加载器，用于动态加载字符串模板
     */
    private static final StringTemplateLoader STRING_TEMPLATE_LOADER;

    static {
        Properties props = new Properties();
        props.put("tag_syntax", "auto_detect");
        props.put("template_update_delay", "5");
        props.put("defaultEncoding", Charsets.UTF_8.name());
        props.put("url_escaping_charset", Charsets.UTF_8.name());
        props.put("boolean_format", "true,false");
        props.put("datetime_format", "yyyy-MM-dd HH:mm:ss");
        props.put("date_format", "yyyy-MM-dd");
        props.put("time_format", "HH:mm:ss");
        props.put("number_format", "0.######");
        props.put("whitespace_stripping", "true");

        FILE_TEMPLATE_CONFIGURATION = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        FILE_TEMPLATE_CONFIGURATION.setTemplateLoader(new ClassloaderTemplateLoader(FTL_DIR));
        try {
            FILE_TEMPLATE_CONFIGURATION.setSettings(props);
        } catch (TemplateException ignored) {
        }

        STRING_TEMPLATE_CONFIGURATION = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        STRING_TEMPLATE_CONFIGURATION.setDefaultEncoding(Charsets.UTF_8.name());
        STRING_TEMPLATE_LOADER = new StringTemplateLoader();
        STRING_TEMPLATE_CONFIGURATION.setTemplateLoader(STRING_TEMPLATE_LOADER);
        try {
            STRING_TEMPLATE_CONFIGURATION.setSettings(props);
        } catch (TemplateException ignored) {
        }
    }

    /**
     * 根据模板名称生成内容并输出到指定文件
     *
     * @param templateName 模板文件名称，相对于templates目录
     * @param outputPath   输出文件的绝对路径
     * @param root         FreeMarker数据模型，包含模板渲染所需的数据
     * @throws RuntimeException 当文件IO操作失败时抛出
     */
    public static void processFileTemplateToFile(String templateName, String outputPath, Map<String, Object> root) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(Files.createFile(outputPath)), Charsets.UTF_8)) {
            processFileTemplateTo(templateName, root, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据模板名称生成内容并输出到控制台
     *
     * @param templateName 模板文件名称，相对于templates目录
     * @param root         FreeMarker数据模型，包含模板渲染所需的数据
     */
    @SuppressWarnings("unused")
    public static void processFileTemplateToConsole(String templateName, Map<String, Object> root) {
        processFileTemplateTo(templateName, root, new OutputStreamWriter(System.out));
    }

    /**
     * 根据模板名称生成内容并返回字符串
     *
     * @param templateName 模板文件名称，相对于templates目录
     * @param root         FreeMarker数据模型，包含模板渲染所需的数据
     * @return 生成的内容字符串
     */
    public static String processFileTemplateToString(String templateName, Map<String, Object> root) {
        return processTemplateToString(getFileTemplate(templateName), root);
    }

    /**
     * 处理文件模板并输出到指定的Writer
     *
     * @param templateName 模板文件名称，相对于templates目录
     * @param root         FreeMarker数据模型，包含模板渲染所需的数据
     * @param out          输出流写入器
     */
    private static void processFileTemplateTo(String templateName, Map<String, Object> root, Writer out) {
        processTemplateTo(getFileTemplate(templateName), root, out);
    }

    /**
     * 根据字符串模板源码生成内容并返回字符串
     *
     * @param ftlSource 模板源代码字符串
     * @param map       FreeMarker数据模型，包含模板渲染所需的数据
     * @return 生成的内容字符串
     * @throws RuntimeException 当模板处理失败时抛出
     */
    public static String processStringTemplateToString(String ftlSource, Map<String, Object> map) {
        String defaultFtlName = "default_" + ftlSource.hashCode();
        STRING_TEMPLATE_LOADER.putTemplate(defaultFtlName, ftlSource);
        try {
            Template template = STRING_TEMPLATE_CONFIGURATION.getTemplate(defaultFtlName);
            return processTemplateToString(template, map);
        } catch (Exception e) {
            throw new RuntimeException(Texts.format("ftl内容：{}，root：{}", ftlSource, map), e);
        }
    }

    /**
     * 根据模板名称获取文件模板对象
     *
     * @param templateName 模板文件名称，相对于templates目录
     * @return FreeMarker模板对象
     * @throws RuntimeException 当模板文件读取失败时抛出
     */
    private static Template getFileTemplate(String templateName) {
        try {
            return FILE_TEMPLATE_CONFIGURATION.getTemplate(templateName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理模板对象并返回生成的字符串内容
     *
     * @param template FreeMarker模板对象
     * @param map      FreeMarker数据模型，包含模板渲染所需的数据
     * @return 生成的内容字符串
     */
    private static String processTemplateToString(Template template, Map<String, Object> map) {
        StringWriter result = new StringWriter();
        processTemplateTo(template, map, result);
        return result.toString();
    }

    /**
     * 处理模板对象并输出到指定的Writer
     *
     * @param template FreeMarker模板对象
     * @param root     FreeMarker数据模型，包含模板渲染所需的数据
     * @param out      输出流写入器
     * @throws RuntimeException 当模板处理失败时抛出
     */
    private static void processTemplateTo(Template template, Map<String, Object> root, Writer out) {
        try {
            template.process(root, out);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * FreeMarker静态模型对象，用于在模板中访问Java类的静态方法和字段
     */
    private final static TemplateHashModel STATIC_MODELS = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().getStaticModels();

    /**
     * 获取指定类的静态模型，用于在FreeMarker模板中调用该类的静态方法
     *
     * @param clazz 需要导入静态方法的Java类
     * @return 该类对应的FreeMarker静态模型对象，如果获取失败则返回null
     */
    public static TemplateHashModel useStaticPackage(Class<?> clazz) {
        try {
            return (TemplateHashModel) STATIC_MODELS.get(clazz.getName());
        } catch (Exception e) {
            return null;
        }
    }

}
