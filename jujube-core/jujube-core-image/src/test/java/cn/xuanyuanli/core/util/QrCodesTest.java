package cn.xuanyuanli.core.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.Test;

class QrCodesTest {

    @Test
    void encode() throws IOException {
        String filename = SystemProperties.TMPDIR + "/createQrCodeWithLogo/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        BufferedImage bufferedImage = QrCodes.encode("https://m.auctionhome.cn/home?managerId=9", 430, 430);
        try (FileOutputStream output = new FileOutputStream(filename)) {
            ImageIO.write(bufferedImage, "png", output);
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void encodeDeleteWhite() throws IOException {
        String filename = SystemProperties.TMPDIR + "/createQrCodeWithLogo/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        BufferedImage bufferedImage = QrCodes.encode("https://m.auctionhome.cn/home?managerId=9", 430, 430, true);
        try (FileOutputStream output = new FileOutputStream(filename)) {
            if (bufferedImage != null) {
                ImageIO.write(bufferedImage, "png", output);
            }
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void createQrCodeWithLogo() throws IOException {
        String filename = SystemProperties.TMPDIR + "/createQrCodeWithLogo/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        File logoFile = Objects.requireNonNull(Resources.getClassPathResources("META-INF/qrcode/logo.png")).getFile();
        BufferedImage bufferedImage = QrCodes.createQrCodeWithLogo("https://m.auctionhome.cn/home?managerId=9", 430, 430,
                ImageIO.read(logoFile), true);
        try (FileOutputStream output = new FileOutputStream(filename)) {
            ImageIO.write(bufferedImage, "png", output);
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void encodeToInputStream() throws IOException {
        String filename = SystemProperties.TMPDIR + "/encodeToInputStream/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        try (InputStream inputStream = QrCodes.encodeToInputStream("http://m.jingrui28.cn/auction/match/1001", 100, 100);
                FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, output);
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();

        file = Files.createFile(filename);
        try (InputStream inputStream = QrCodes.encodeToInputStream("http://m.jingrui28.cn/auction/match/1001", 163, 163);
                FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, output);
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void encodeToInputStreamDeleteWhite() throws IOException {
        String filename = SystemProperties.TMPDIR + "/encodeToInputStream/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        try (InputStream inputStream = QrCodes.encodeToInputStream("http://m.jingrui28.cn/auction/match/1001", 100, 100, true);
                FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, output);
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();

        file = Files.createFile(filename);
        try (InputStream inputStream = QrCodes.encodeToInputStream("http://m.jingrui28.cn/auction/match/1001", 163, 163, true);
                FileOutputStream output = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, output);
        }
        Assertions.assertThat(Images.isImage(file)).isTrue();
        file.deleteOnExit();
    }
}
