package cn.xuanyuanli.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import lombok.Cleanup;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

class Zip4jTest {

    @Test
    void containsEntry() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/zip/king.zip");
        @Cleanup ZipFile zipFile = new ZipFile(Objects.requireNonNull(resource).getFile().getAbsoluteFile());
        FileHeader king = zipFile.getFileHeader("king/");
        FileHeader hello = zipFile.getFileHeader("你好");
        assertThat(king.isDirectory()).isTrue();
        assertThat(zipFile.isValidZipFile()).isTrue();
        assertThat(hello).isNull();
    }

    @Test
    void unpackEntry() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/zip/king.zip");
        @Cleanup ZipFile zipFile = new ZipFile(Objects.requireNonNull(resource).getFile().getAbsoluteFile());
        Path tempFile = Files.createTempDirectory(String.valueOf(SnowFlakes.nextId()));
        zipFile.extractFile("king/a.txt", tempFile.toAbsolutePath().toString());
        assertThat(Files.readString(tempFile.resolve("king/a.txt"), StandardCharsets.UTF_8)).isEqualTo("你好");
    }

    @Test
    void unpackNameMapper() throws IOException {
        String filename = SystemProperties.TMPDIR + "/zipstest/" + SnowFlakes.nextId();
        File dir = new File(filename);
        File zip = Objects.requireNonNull(Resources.getClassPathResources("META-INF/zip/king.zip")).getFile();
        @Cleanup ZipFile zipFile = new ZipFile(zip.getAbsoluteFile());
        zipFile.extractAll(dir.getAbsolutePath());
        assertThat(FileUtils.readFileToString(new File(dir, "king/a.txt"), StandardCharsets.UTF_8)).isEqualTo("你好");
        FileUtils.deleteDirectory(dir);
    }

    @Test
    void pack() throws IOException {
        String filename = SystemProperties.TMPDIR + "/zipstest/" + SnowFlakes.nextId() + ".zip";
        File dest = cn.xuanyuanli.core.util.Files.createFile(filename);
        @Cleanup ZipFile zipFile = new ZipFile(filename);
        File source = Objects.requireNonNull(Resources.getClassPathResources("META-INF/zip")).getFile();
        zipFile.addFile(source);
        assertThat(dest).exists();
        dest.deleteOnExit();
    }

}

