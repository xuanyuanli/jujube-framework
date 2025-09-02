package cn.xuanyuanli.core.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import lombok.Cleanup;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.IOUtils;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.Images.ImageBaseMeta;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.*;
import org.springframework.core.io.Resource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Images 工具类测试
 */
@DisplayName("Images工具类测试")
@SuppressWarnings("DataFlowIssue")
class ImagesTest {

    @Nested
    @DisplayName("图像转换操作")
    class TransformOperations {

        @Test
        @DisplayName("应该成功转换原始图像尺寸")
        void transformOrigin_shouldCreateTransformedImage_whenGivenValidParameters() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/icc.png");
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            Images.transformOrigin(resource.getFile(), file, 1500, 1500);

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功转换图像并添加水印")
        void transform_shouldCreateTransformedImageWithWatermark_whenGivenImageAndWatermark() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
            Resource watermark = Resources.getClassPathResources("META-INF/images/watermark.png");
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            Images.transform(resource.getFile(), file, 1500, 1500, Images.getImage(watermark.getFile()));

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功转换图像并添加透明度和位置的水印")
        void transform_shouldCreateTransformedImageWithWatermarkAndOpacity_whenGivenOpacityAndPosition() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
            Resource watermark = Resources.getClassPathResources("META-INF/images/watermark.png");
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            Images.transform(resource.getFile(), file, 1500, 1500, Images.getImage(watermark.getFile()), 0.7f, Positions.CENTER);

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功随机位置转换图像")
        void transformOfRand_shouldCreateTransformedImageWithRandomWatermark_whenGivenWatermarkPath() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
            Resource watermark = Resources.getClassPathResources("META-INF/images/watermark.png");
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            Images.transformOfRand(Images.getImage(resource.getFile()), file, 1500, 1500, watermark.getFile().getAbsolutePath());

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功修改图像透明度")
        void changeAlpha_shouldCreateImageWithChangedAlpha_whenGivenAlphaValue() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";

            // Act
            Images.changeAlpha(resource.getFile().getAbsolutePath(), filename, 5);

            // Assert
            File file = new File(filename);
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("图像获取和处理")
    class ImageOperations {

        @Test
        @DisplayName("应该成功获取ICC图像")
        void getIccImage_shouldReturnBufferedImage_whenGivenIccImageFile() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/icc.png");

            // Act
            BufferedImage iccImage = Images.getIccImage(resource.getFile());

            // Assert
            assertThat(iccImage).isNotNull();
        }

        @Test
        @DisplayName("应该成功输出图像到流")
        void outputImage_shouldOutputImageToStream_whenGivenValidImage() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            @Cleanup FileOutputStream out = new FileOutputStream(file);
            Images.outputImage(Images.getImage(resource.getFile()), out);

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功生成验证码图像")
        void generateImage_shouldReturnBufferedImage_whenGivenValidCode() {
            // Act
            BufferedImage result = Images.generateImage("45789");

            // Assert
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("应该成功旋转图像")
        void rotate_shouldReturnRotatedImage_whenGivenValidAngle() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");

            // Act
            BufferedImage rotate = Images.rotate(Images.getImage(resource.getFile()), 90);

            // Assert
            assertThat(rotate).isNotNull();
        }

        @Test
        @DisplayName("应该成功获取图像对象")
        void getImage_shouldReturnBufferedImage_whenGivenValidImageFile() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/true.png");

            // Act
            BufferedImage image = Images.getImage(Objects.requireNonNull(resource).getFile());

            // Assert
            assertThat(image).isNotNull();

            // Act & Assert - 验证其他图像文件
            resource = Resources.getClassPathResources("META-INF/images/false.jpg");
            assertThat(Images.isImage(Objects.requireNonNull(resource).getFile())).isNotNull();

            resource = Resources.getClassPathResources("META-INF/images/false1.jpg");
            assertThat(Images.isImage(Objects.requireNonNull(resource).getFile())).isNotNull();
        }
    }

    @Nested
    @DisplayName("颜色空间检测")
    class ColorSpaceOperations {

        @Test
        @DisplayName("应该正确获取文件颜色空间")
        void getColorSpace_shouldReturnCorrectColorSpace_whenGivenFileInput() throws IOException {
            // Arrange
            Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
            Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
            Resource resource3 = Resources.getClassPathResources("META-INF/images/p1.jpg");

            // Act & Assert
            assertThat(Images.getColorSpace(resource1.getFile())).isNull();
            assertThat(Images.getColorSpace(resource2.getFile())).isEqualTo("CMYK");
            assertThat(Images.getColorSpace(resource3.getFile())).isNull();
        }

        @Test
        @DisplayName("应该正确获取输入流颜色空间")
        void getColorSpace_shouldReturnCorrectColorSpace_whenGivenInputStreamInput() throws IOException {
            // Arrange
            Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
            Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");

            // Act & Assert
            assertThat(Images.getColorSpace(resource1.getInputStream())).isNull();
            assertThat(Images.getColorSpace(resource2.getInputStream())).isEqualTo("CMYK");
        }

        @Test
        @DisplayName("应该正确获取文件颜色变换")
        void getColorTransform_shouldReturnCorrectColorTransform_whenGivenFileInput() throws IOException {
            // Arrange
            Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
            Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
            Resource resource3 = Resources.getClassPathResources("META-INF/images/p1.jpg");

            // Act & Assert
            assertThat(Images.getColorTransform(resource1.getFile())).isEqualTo("YCCK");
            assertThat(Images.getColorTransform(resource2.getFile())).isEqualTo("Unknown (RGB or CMYK)");
            assertThat(Images.getColorTransform(resource3.getFile())).isNull();
        }

        @Test
        @DisplayName("应该正确获取输入流颜色变换")
        void getColorTransform_shouldReturnCorrectColorTransform_whenGivenInputStreamInput() throws IOException {
            // Arrange
            Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
            Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");

            // Act & Assert
            assertThat(Images.getColorTransform(resource1.getInputStream())).isEqualTo("YCCK");
            assertThat(Images.getColorTransform(resource2.getInputStream())).isEqualTo("Unknown (RGB or CMYK)");
        }

        @Test
        @DisplayName("应该正确判断是否为CMYK颜色空间")
        void isCmykColorSpace_shouldReturnCorrectResult_whenGivenDifferentColorSpaceImages() throws IOException {
            // Arrange
            Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
            Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");

            // Act & Assert
            assertThat(Images.isCmykColorSpace(resource1.getFile())).isFalse();
            assertThat(Images.isCmykColorSpace(resource2.getFile())).isTrue();
        }

        @Test
        @DisplayName("应该正确判断是否为YCCK颜色变换")
        void isYcckColorTransform_shouldReturnCorrectResult_whenGivenDifferentColorTransformImages() throws IOException {
            // Arrange
            Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
            Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");

            // Act & Assert
            assertThat(Images.isYcckColorTransform(resource1.getFile())).isTrue();
            assertThat(Images.isYcckColorTransform(resource2.getFile())).isFalse();
        }
    }

    @Nested
    @DisplayName("图像验证")
    class ImageValidation {

        @Test
        @DisplayName("应该正确识别图像文件名")
        void isImage_shouldReturnTrue_whenGivenValidImageFileName() {
            // Act & Assert
            assertThat(Images.isImage("a.jpeg")).isTrue();
            assertThat(Images.isImage("a.jpg")).isTrue();
            assertThat(Images.isImage("a.png")).isTrue();
            assertThat(Images.isImage("a.bmp")).isTrue();
            assertThat(Images.isImage("a.webp")).isTrue();

            // 测试大小写不敏感
            assertThat(Images.isImage(".Jpeg")).isTrue();
            assertThat(Images.isImage(".jPg")).isTrue();
            assertThat(Images.isImage(".PNG")).isTrue();
            assertThat(Images.isImage(".bmp")).isTrue();
            assertThat(Images.isImage(".webp")).isTrue();
        }

        @Test
        @DisplayName("应该正确识别真实图像文件")
        void isImage_shouldReturnCorrectResult_whenGivenRealImageFiles() throws IOException {
            // Act & Assert
            assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/heif.jpg").getFile())).isTrue();
            assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/false.jpg").getFile())).isFalse();
            assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/false1.jpg").getFile())).isFalse();
            assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/true.png").getFile())).isTrue();
            assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/1.webp").getFile())).isTrue();
        }

        @Test
        @DisplayName("应该使用Tika正确识别图像")
        void isImageByTika_shouldReturnCorrectResult_whenGivenVariousInputTypes() throws IOException {
            // Act & Assert - InputStream测试
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/heif.jpg").getInputStream())).isTrue();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false.jpg").getInputStream())).isFalse();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/true.png").getInputStream())).isTrue();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/1.webp").getInputStream())).isTrue();

            // Act & Assert - byte数组测试
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/heif.jpg").getContentAsByteArray())).isTrue();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false.jpg").getContentAsByteArray())).isFalse();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/true.png").getContentAsByteArray())).isTrue();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/1.webp").getContentAsByteArray())).isTrue();

            // Act & Assert - FileInputStream测试
            assertThat(Images.isImageByTika(new FileInputStream(Resources.getClassPathResources("META-INF/images/false.jpg").getFile()))).isFalse();

            // 此处显示了和isImageByTika(File)方法的区别
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false1.jpg").getInputStream())).isFalse();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false1.jpg").getContentAsByteArray())).isFalse();
            assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false1.jpg").getFile())).isTrue();
        }
    }

    @Nested
    @DisplayName("图像元数据操作")
    class ImageMetadataOperations {

        @Test
        @DisplayName("应该成功获取HEIF图像元数据")
        void getHeifImageMeta_shouldReturnCorrectMetadata_whenGivenHeifImage() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/heif.jpg");

            // Act
            ImageBaseMeta image = Images.getHeifImageMeta(Objects.requireNonNull(resource).getFile());

            // Assert
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(4032);
            assertThat(image.getHeight()).isEqualTo(3024);

            // Act & Assert - 非HEIF图像应返回null
            Resource pngResource = Resources.getClassPathResources("META-INF/images/true.png");
            ImageBaseMeta pngImage = Images.getHeifImageMeta(Objects.requireNonNull(pngResource).getFile());
            assertThat(pngImage).isNull();
        }

        @Test
        @DisplayName("应该成功获取通用图像基础元数据")
        void getImageBaseMeta_shouldReturnCorrectMetadata_whenGivenDifferentImageTypes() throws IOException {
            // Arrange & Act - HEIF图像测试
            Resource heifResource = Resources.getClassPathResources("META-INF/images/heif.jpg");
            ImageBaseMeta heifImage = Images.getImageBaseMeta(Objects.requireNonNull(heifResource).getFile());

            // Assert
            assertThat(heifImage).isNotNull();
            assertThat(heifImage.getWidth()).isEqualTo(4032);
            assertThat(heifImage.getHeight()).isEqualTo(3024);

            // Arrange & Act - PNG图像测试
            Resource pngResource = Resources.getClassPathResources("META-INF/images/true.png");
            ImageBaseMeta pngImage = Images.getImageBaseMeta(Objects.requireNonNull(pngResource).getInputStream());

            // Assert
            assertThat(pngImage).isNotNull();
            assertThat(pngImage.getWidth()).isEqualTo(206);
            assertThat(pngImage.getHeight()).isEqualTo(65);
        }

        @Test
        @DisplayName("应该成功获取默认图像元数据")
        void getImageMetaDefault_shouldReturnCorrectMetadata_whenGivenImageInputStream() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/true.png");

            // Act
            ImageBaseMeta image = Images.getImageMetaDefault(Objects.requireNonNull(resource).getInputStream());

            // Assert
            assertThat(image).isNotNull();
            assertThat(image.getWidth()).isEqualTo(206);
            assertThat(image.getHeight()).isEqualTo(65);
        }

        @Test
        @DisplayName("应该正确获取文件类型")
        void getFileType_shouldReturnCorrectFileType_whenGivenDifferentImageFiles() {
            // Act & Assert
            assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/1.webp"))).isEqualToIgnoringCase("webp");
            assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/heif.jpg"))).isEqualToIgnoringCase("HEIF");
            assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/cmyk1.jpg"))).isEqualToIgnoringCase("JPEG");
            assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/false.jpg"))).isNull();
            assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/icc.png"))).isEqualToIgnoringCase("PNG");
            assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/true.png"))).isEqualToIgnoringCase("PNG");
        }

        @Test
        @DisplayName("应该正确获取图像方向")
        void getOrientation_shouldReturnCorrectOrientation_whenGivenImagesWithDifferentOrientations() throws IOException {
            // Arrange & Act - 普通图像
            Resource resource = Resources.getClassPathResources("META-INF/images/true.png");
            int normalOrientation = Images.getOrientation(resource.getFile());

            // Assert
            assertThat(normalOrientation).isEqualTo(0);

            // Arrange & Act - 旋转图像
            Resource rotatedResource = Resources.getClassPathResources("META-INF/images/right90.jpeg");
            int rotatedOrientation = Images.getOrientation(rotatedResource.getInputStream());

            // Assert
            assertThat(rotatedOrientation).isEqualTo(6);
        }

        @Test
        @DisplayName("应该成功纠正图像方向")
        void rotateImage_shouldCorrectOrientation_whenGivenRotatedImage() throws IOException {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/images/right90.jpeg");
            File rotateFile = java.nio.file.Files.createTempFile("rotate", ".jpeg").toFile();

            // Act
            Images.rotateImage(resource.getFile(), rotateFile);

            // Assert
            assertThat(Images.getOrientation(rotateFile)).isEqualTo(0);
            rotateFile.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("高级图像操作")
    class AdvancedImageOperations {

        @Test
        @DisplayName("应该成功将HTML保存为图像")
        void saveHtmlToImage_shouldCreateImageFromHtml_whenGivenValidHtmlContent() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);

            // Act
            @Cleanup FileOutputStream outputStream = new FileOutputStream(file);
            Images.saveHtmlToImage("<h1>Hello World</h1>", 300, 300, outputStream);

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功合并图像")
        void merge_shouldMergeImages_whenGivenBackgroundAndOverlayImages() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);
            @Cleanup InputStream is = Resources.getClassPathResourcesInputStream("META-INF/images/p1.jpg");
            BufferedImage backgroundImage = Images.getImage(is);
            BufferedImage zindexImg = QrCodes.encode("https://m.auctionhome.cn/home?managerId=9", 163, 163);

            // Act
            @Cleanup InputStream merge = Images.merge(backgroundImage, zindexImg, 678, 1050);
            @Cleanup FileOutputStream output = new FileOutputStream(file);
            IOUtils.copy(merge, output);

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }

        @Test
        @DisplayName("应该成功将BufferedImage转换为InputStream")
        void bufferedImageToInputStream_shouldConvertBufferedImageToInputStream_whenGivenValidImage() throws IOException {
            // Arrange
            String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
            File file = Files.createFile(filename);
            @Cleanup InputStream is = Resources.getClassPathResourcesInputStream("META-INF/images/p1.jpg");
            BufferedImage image = Images.getImage(is);

            // Act
            @Cleanup FileOutputStream os = new FileOutputStream(file);
            @Cleanup InputStream png = Images.bufferedImageToInputStream(
                    Thumbnails.of(image).size(750, image.getHeight()).asBufferedImage(), "png");
            IOUtils.copy(png, os);

            // Assert
            assertThat(Images.isImage(file)).isTrue();
            assertThat(Images.isImageByTika(file)).isTrue();
            file.deleteOnExit();
        }
    }

    @Nested
    @DisplayName("本地测试 - 已禁用")
    class DisabledLocalTests {

        @Test
        @Disabled("本地测试 - 需要特定本地文件路径")
        @DisplayName("本地图像旋转测试")
        void rotateImageLocal_shouldRotateLocalImage_whenGivenLocalImagePath() throws IOException {
            Images.rotateImage(new File("C:\\Users\\Administrator\\Desktop\\归档\\1.jpeg"), 
                              new File("C:\\Users\\Administrator\\Desktop\\归档\\1-1.jpeg"));
        }
    }
}
