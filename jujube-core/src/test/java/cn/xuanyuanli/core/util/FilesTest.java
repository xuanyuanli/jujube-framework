package cn.xuanyuanli.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilesTest {

    @Test
    void testGetExtention() {
        Assertions.assertEquals(".jpg", Files.getExtention("1.jpg"));
        Assertions.assertEquals(".jpg", Files.getExtention("/u/89.jpg"));
        Assertions.assertEquals(".JPG", Files.getExtention("1.JPG"));
        Assertions.assertEquals("", Files.getExtention("1jpg"));
        Assertions.assertEquals(".JPG", Files.getExtention("http://o.cn/1.JPG"));
    }

    @Test
    void testGetExtention2() {
        Assertions.assertEquals(".ext", Files.getExtention("123", ".ext"));
        Assertions.assertEquals(".txt", Files.getExtention("123.txt", ".ext"));
    }

    @Test
    void testCreateDir() throws IOException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Files.createDir(""), "文件路径不能为空");
        String path = "/testCreateDir/" + SnowFlakes.nextId();
        File dir = Files.createDir(SystemProperties.TMPDIR + path);
        org.assertj.core.api.Assertions.assertThat(dir).exists().isDirectory();
        FileUtils.deleteDirectory(dir);
    }

    @Test
    void testCreateFile() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Files.createDir(""), "文件路径不能为空");

        String filename = "/" + SnowFlakes.nextId();
        File file = Files.createFile(SystemProperties.TMPDIR + filename);
        org.assertj.core.api.Assertions.assertThat(file).exists().isFile();
        file.deleteOnExit();

        filename = "/testCreateFile/" + SnowFlakes.nextId();
        file = Files.createFile(SystemProperties.TMPDIR + filename);
        org.assertj.core.api.Assertions.assertThat(file).exists().isFile();
        file.deleteOnExit();
    }

    @Test
    void testAppendStringToFile() throws IOException {
        Assertions.assertThrows(IllegalArgumentException.class, () -> Files.appendStringToFile(null, "", null), "文件路径不能为空");
        Assertions.assertThrows(IllegalArgumentException.class, () -> Files.appendStringToFile("a.txt", "", null), "");

        String filename = SystemProperties.TMPDIR + "/testAppendStringToFile/" + SnowFlakes.nextId();
        String data = "data12中文";
        File file = Files.appendStringToFile(filename, data, StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(file).exists().isFile();
        Assertions.assertEquals(data, FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8));
        file.deleteOnExit();

        filename = SystemProperties.TMPDIR + "/testAppendStringToFile/" + SnowFlakes.nextId();
        data = "data12中文";
        file = Files.appendStringToFile(filename, data, StandardCharsets.UTF_8);
        Files.appendStringToFile(filename, data, StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(file).exists().isFile();
        Assertions.assertEquals("data12中文\r\ndata12中文", FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8));
        file.deleteOnExit();

        filename = SystemProperties.TMPDIR + "/testAppendStringToFile/" + SnowFlakes.nextId();
        file = Files.appendStringToFile(filename, null, StandardCharsets.UTF_8);
        org.assertj.core.api.Assertions.assertThat(file).exists().isFile();
        Assertions.assertEquals("", FileUtils.readFileToString(new File(filename), StandardCharsets.UTF_8));
        file.deleteOnExit();
    }

    @Test
    void testBase64ToInputstream() throws IOException {
        String base64 = Base64.getEncoder().encodeToString("test".getBytes());
        InputStream result = Files.base64ToInputstream(base64);
        Assertions.assertEquals("test", IOUtils.toString(result, StandardCharsets.UTF_8));

        Assertions.assertNull(Files.base64ToInputstream(null));
    }

    @Test
    void testBase64ToFile() throws IOException {
        String filename = SystemProperties.TMPDIR + "/testBase64ToFile-" + SnowFlakes.nextId();
        String base64 = Base64.getEncoder().encodeToString("test".getBytes());
        File file = new File(filename);
        Files.base64ToFile(base64, file);
        Assertions.assertEquals("test", FileUtils.readFileToString(file, StandardCharsets.UTF_8));
        file.deleteOnExit();
    }

    @Test
    void testStreamToBase64() {
        String base64 = Base64.getEncoder().encodeToString("test".getBytes());
        InputStream is = Files.base64ToInputstream(base64);
        String result = Files.streamToBase64(is);
        Assertions.assertEquals("dGVzdA==", result);
    }

    @Test
    void testFileToBase64() {
        String filename = SystemProperties.TMPDIR + "/testFileToBase64-" + SnowFlakes.nextId();
        File file = Files.appendStringToFile(filename, "test", StandardCharsets.UTF_8);
        String result = Files.fileToBase64(file);
        Assertions.assertEquals("dGVzdA==", result);
        file.deleteOnExit();
    }

    @Test
    public void testValidSubPath() {
        assertTrue(Files.isValidPath("/base/dir", "subdir/file.txt"));
        assertTrue(Files.isValidPath("/base/dir", "dfdadf"));
    }

    @Test
    public void testValidAbsolutePathWithinBase() {
        assertTrue(Files.isValidPath("/base/dir", "/base/dir/sub/file"));
    }

    @Test
    public void testInvalidPathOutsideBase() {
        assertFalse(Files.isValidPath("/base/dir", "../otherdir"));
    }

    @Test
    public void testInvalidPathAbsoluteOutsideBase() {
        assertFalse(Files.isValidPath("/base/dir", "/other/dir"));
    }

    @Test
    public void testInvalidBasePath() {
        assertFalse(Files.isValidPath("invalid:\0chars", "subdir"));
    }

    @Test
    public void testInvalidUserPath() {
        assertFalse(Files.isValidPath("/base/dir", "invalid:\0chars"));
    }

    @Test
    public void testNullBasePath() {
        assertFalse(Files.isValidPath(null, "subdir"));
    }

    @Test
    public void testNullUserPath() {
        assertFalse(Files.isValidPath("/base/dir", null));
    }

    @Test
    public void testEmptyUserPath() {
        assertTrue(Files.isValidPath("/base/dir", ""));
    }
}
