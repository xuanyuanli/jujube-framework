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
     * 模板总目录
     */
    private static final String FTL_DIR = "templates";

    /**
     * ftl
     */
    private Ftls() {
    }

    /**
     * 文件模板
     */
    private static final Configuration FILE_TEMPLATE_CONFIGURATION;
    /**
     * 字符串模板
     */
    private static final Configuration STRING_TEMPLATE_CONFIGURATION;

    /**
     * 字符串模板载入器
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
     * 生成模板到文件
     *
     * @param templateName 模板名称
     * @param outputPath   输出路径(绝对路径)
     * @param root         FreeMarker数据模型
     */
    public static void processFileTemplateToFile(String templateName, String outputPath, Map<String, Object> root) {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(Files.createFile(outputPath)), Charsets.UTF_8)) {
            processFileTemplateTo(templateName, root, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成模板，输出到控制台
     *
     * @param templateName 模板名称
     * @param root         FreeMarker数据模型
     */
    @SuppressWarnings("unused")
    public static void processFileTemplateToConsole(String templateName, Map<String, Object> root) {
        processFileTemplateTo(templateName, root, new OutputStreamWriter(System.out));
    }

    /**
     * 生成模板，输出String
     *
     * @param templateName 模板名称
     * @param root         FreeMarker数据模型
     * @return {@link String}
     */
    public static String processFileTemplateToString(String templateName, Map<String, Object> root) {
        return processTemplateToString(getFileTemplate(templateName), root);
    }

    /**
     * 工艺文件模板
     *
     * @param templateName 模板名称
     * @param root         根
     * @param out          出
     */
    private static void processFileTemplateTo(String templateName, Map<String, Object> root, Writer out) {
        processTemplateTo(getFileTemplate(templateName), root, out);
    }

    /**
     * 处理模板源文件，生成内容
     *
     * @param ftlSource 模板源码
     * @param map       root
     * @return {@link String}
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
     * 获得文件模板
     *
     * @param templateName 模板名称
     * @return {@link Template}
     */
    private static Template getFileTemplate(String templateName) {
        try {
            return FILE_TEMPLATE_CONFIGURATION.getTemplate(templateName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理模板到字符串中
     *
     * @param template 模板
     * @param map      map
     * @return {@link String}
     */
    private static String processTemplateToString(Template template, Map<String, Object> map) {
        StringWriter result = new StringWriter();
        processTemplateTo(template, map, result);
        return result.toString();
    }

    /**
     * 生成模板，输出到...
     *
     * @param template 模板
     * @param root     根
     * @param out      出
     * @author xuanyuanli Email：xuanyuanli999@gmail.com
     */
    private static void processTemplateTo(Template template, Map<String, Object> root, Writer out) {
        try {
            template.process(root, out);
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 静态模型
     */
    private final static TemplateHashModel STATIC_MODELS = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().getStaticModels();

    /**
     * 导入Class类的静态方法到Freemarker
     *
     * @param clazz clazz
     * @return {@link TemplateHashModel}
     */
    public static TemplateHashModel useStaticPackage(Class<?> clazz) {
        try {
            return (TemplateHashModel) STATIC_MODELS.get(clazz.getName());
        } catch (Exception e) {
            return null;
        }
    }

}
