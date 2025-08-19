package cn.xuanyuanli.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.Test;

class FtlsTest {

    @Test
    void processFileTemplateToFile() throws IOException {
        String filename = SystemProperties.TMPDIR + "/processFileTemplateToFile-" + SnowFlakes.nextId();
        File file = new File(filename);
        Map<String, Object> root = new HashMap<>();
        //noinspection ResultOfMethodCallIgnored
        Assertions.catchThrowableOfType(() -> Ftls.processFileTemplateToFile("1.ftl", filename, root), RuntimeException.class);
        root.put("test", true);
        Ftls.processFileTemplateToFile("1.ftl", filename, root);
        Assertions.assertThat(FileUtils.readFileToString(file, StandardCharsets.UTF_8)).isEqualTo("    Who am i?\r\n");
        file.deleteOnExit();
    }

    @Test
    void processFileTemplateToConsole() {
        Map<String, Object> root = new HashMap<>();
        root.put("test", true);
        Ftls.processFileTemplateToConsole("1.ftl", root);
    }

    @Test
    void processFileTemplateToString() {
        Map<String, Object> root = new HashMap<>();
        root.put("test", true);
        Assertions.assertThat(Ftls.processFileTemplateToString("1.ftl", root)).isEqualTo("    Who am i?\r\n");
    }

    @Test
    void processStringTemplateToString() {
        Map<String, Object> root = new HashMap<>();
        root.put("test", true);
        root.put("ids", List.of(2,3,4));
        Assertions.assertThat(Ftls.processStringTemplateToString("<#if test>\n"
                + "    Who am i?\n"
                + "</#if>${ids?join(',')}", root)).isEqualTo("    Who am i?\n2,3,4");
    }

    @Test
    void useStaticPackage() {
        Map<String, Object> root = new HashMap<>();
        root.put("texts", Ftls.useStaticPackage(Texts.class));
        Assertions.assertThat(Ftls.processStringTemplateToString("${texts.capitalize('test')}", root)).isEqualTo("Test");
    }
}
