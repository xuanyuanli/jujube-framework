package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

public class ResourcesTest {

    @Test
    void getPackageClasses() {
        List<Class<?>> packageClasses = Resources.getPackageClasses("cn.xuanyuanli.core.util.beancopy").stream().filter(e -> !e.getName().endsWith("Test"))
                .toList();
        assertThat(packageClasses).hasSize(4);
    }

    @Test
    void getProperties() {
        Properties properties = Resources.getProperties("test.properties");
        assertThat(properties).isNotNull();
    }

    @Test
    void getCurrentClasspathProperties() {
        Properties properties = Resources.getCurrentClasspathProperties("test.properties");
        assertThat(properties).isNotNull();
    }

    @Test
    void getClassPathAllResources() {
        Resource[] resources = Resources.getClassPathAllResources("META-INF/office/testRealCount.xlsx");
        assertThat(resources).hasSize(1);
    }

    @Test
    void getCurrentClasspath() {
        assertThat(Resources.getCurrentClasspath()).exists();
    }

    @Test
    void getProjectPath() {
        assertThat(Resources.getProjectPath()).isNotEmpty();
    }

    @Test
    void getJarHome() {
        assertThat(Resources.getJarHome(ResourcesTest.class)).isNotEmpty();
    }

    @Test
    void isJarFile() throws IOException {
        assertThat(Resources.isJarFile(Objects.requireNonNull(Resources.getClassPathResources("test.properties")).getURL())).isFalse();
    }

    @Test
    void isJarStartByClass() {
        assertThat(Resources.isJarStartByClass(ResourcesTest.class)).isFalse();
    }

    @Test
    void getClassPathResourcesInputStream() throws IOException {
        try (InputStream inputStream = Resources.getClassPathResourcesInputStream("test.properties")) {
            assertThat(inputStream).isNotNull();
        }
    }

    @Test
    public void getClassPathResources() {
        Resource classPathResources = Resources.getClassPathResources("META-INF/office/testRealCount.xlsx");
        if (classPathResources != null) {
            Assertions.assertThat(classPathResources.getFilename()).isEqualTo("testRealCount.xlsx");
        }
    }
}
