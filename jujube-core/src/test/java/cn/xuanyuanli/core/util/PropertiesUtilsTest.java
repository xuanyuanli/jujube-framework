package cn.xuanyuanli.core.util;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.Test;

class PropertiesUtilsTest {

    @Test
    void saveProperties() throws IOException {
        String filename = SystemProperties.TMPDIR + "propertiesutilstest" + File.separator + SnowFlakes.nextId() + ".properties";
        PropertiesUtils.saveProperties(filename, Lists.newArrayList("name=中国"), true);
        File file = new File(filename);
        Assertions.assertThat(FileUtils.readFileToString(file, StandardCharsets.UTF_8).replace("\r", "")).isEqualTo("name=\\u4E2D\\u56FD\n");
        TreeMap<String, String> map = PropertiesUtils.loadTreeMapFromFile(filename);
        Assertions.assertThat(map.get("name")).isEqualTo("中国");
        file.deleteOnExit();
    }

    @Test
    void keySaveConvert() {
        Assertions.assertThat(PropertiesUtils.keySaveConvert("name")).isEqualTo("name");
        Assertions.assertThat(PropertiesUtils.keySaveConvert("name中")).isEqualTo("name\\u4E2D");
    }

    @Test
    void valueSaveConvert() {
        Assertions.assertThat(PropertiesUtils.valueSaveConvert("name")).isEqualTo("name");
        Assertions.assertThat(PropertiesUtils.valueSaveConvert("name中")).isEqualTo("name\\u4E2D");
    }

    @Test
    void loadTreeMapFromClasspath(){
        TreeMap<String, String> map = PropertiesUtils.loadTreeMapFromClasspath("test.properties");
        Assertions.assertThat(map.get("name")).isEqualTo("中国");
        Assertions.assertThat(map.get("age")).isEqualTo("45");
    }
}
