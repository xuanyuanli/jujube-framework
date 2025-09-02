package cn.xuanyuanli.core.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QrCodes 工具类测试
 */
@DisplayName("QrCodes二维码工具类测试")
class QrCodesTest {

    @Nested
    @DisplayName("基本二维码生成")
    class BasicQrCodeGeneration {

        @Test
        @DisplayName("应该成功生成基本二维码")
        void encode_shouldGenerateQrCode_whenGivenValidUrlAndDimensions() throws IOException {
            // Arrange
            String url = "https://m.auctionhome.cn/home?managerId=9";
            int width = 430;
            int height = 430;
            String filename = SystemProperties.TMPDIR + "/createQrCodeWithLogo/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            BufferedImage bufferedImage = QrCodes.encode(url, width, height);
            try (FileOutputStream output = new FileOutputStream(filename)) {
                ImageIO.write(bufferedImage, "png", output);
            }

            // Assert
            assertThat(bufferedImage).isNotNull();
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功生成去除白边的二维码")
        void encode_shouldGenerateQrCodeWithoutWhiteBorder_whenDeleteWhiteIsTrue() throws IOException {
            // Arrange
            String url = "https://m.auctionhome.cn/home?managerId=9";
            int width = 430;
            int height = 430;
            boolean deleteWhite = true;
            String filename = SystemProperties.TMPDIR + "/createQrCodeWithLogo/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            BufferedImage bufferedImage = QrCodes.encode(url, width, height, deleteWhite);
            try (FileOutputStream output = new FileOutputStream(filename)) {
                if (bufferedImage != null) {
                    ImageIO.write(bufferedImage, "png", output);
                }
            }

            // Assert
            assertThat(bufferedImage).isNotNull();
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("带Logo的二维码生成")
    class LogoQrCodeGeneration {

        @Test
        @DisplayName("应该成功生成带Logo的二维码")
        void createQrCodeWithLogo_shouldGenerateQrCodeWithLogo_whenGivenValidParameters() throws IOException {
            // Arrange
            String url = "https://m.auctionhome.cn/home?managerId=9";
            int width = 430;
            int height = 430;
            File logoFile = Objects.requireNonNull(Resources.getClassPathResources("META-INF/qrcode/logo.png")).getFile();
            BufferedImage logoImage = ImageIO.read(logoFile);
            boolean needCompress = true;
            String filename = SystemProperties.TMPDIR + "/createQrCodeWithLogo/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            BufferedImage bufferedImage = QrCodes.createQrCodeWithLogo(url, width, height, logoImage, needCompress);
            try (FileOutputStream output = new FileOutputStream(filename)) {
                ImageIO.write(bufferedImage, "png", output);
            }

            // Assert
            assertThat(bufferedImage).isNotNull();
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("二维码转InputStream")
    class QrCodeToInputStream {

        @Test
        @DisplayName("应该成功将二维码编码为InputStream")
        void encodeToInputStream_shouldGenerateQrCodeInputStream_whenGivenValidParameters() throws IOException {
            // Arrange
            String url = "http://m.jingrui28.cn/auction/match/1001";
            String filename = SystemProperties.TMPDIR + "/encodeToInputStream/" + SnowFlakes.nextId() + ".png";

            // Act & Assert - 测试100x100尺寸
            File file = Files.createFile(filename);
            try (InputStream inputStream = QrCodes.encodeToInputStream(url, 100, 100);
                 FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(inputStream, output);
            }
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();

            // Act & Assert - 测试163x163尺寸
            file = Files.createFile(filename);
            try (InputStream inputStream = QrCodes.encodeToInputStream(url, 163, 163);
                 FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(inputStream, output);
            }
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功将去除白边的二维码编码为InputStream")
        void encodeToInputStream_shouldGenerateQrCodeInputStreamWithoutWhiteBorder_whenDeleteWhiteIsTrue() throws IOException {
            // Arrange
            String url = "http://m.jingrui28.cn/auction/match/1001";
            boolean deleteWhite = true;
            String filename = SystemProperties.TMPDIR + "/encodeToInputStream/" + SnowFlakes.nextId() + ".png";

            // Act & Assert - 测试100x100尺寸，去除白边
            File file = Files.createFile(filename);
            try (InputStream inputStream = QrCodes.encodeToInputStream(url, 100, 100, deleteWhite);
                 FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(inputStream, output);
            }
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();

            // Act & Assert - 测试163x163尺寸，去除白边
            file = Files.createFile(filename);
            try (InputStream inputStream = QrCodes.encodeToInputStream(url, 163, 163, deleteWhite);
                 FileOutputStream output = new FileOutputStream(file)) {
                IOUtils.copy(inputStream, output);
            }
            assertThat(Images.isImage(file)).isTrue();
            file.deleteOnExit();
        }
    }
}
