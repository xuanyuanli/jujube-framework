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
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.util.Images.ImageBaseMeta;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

@SuppressWarnings("DataFlowIssue")
public class ImagesTest {


    @Test
    void transformOrigin() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/icc.png");
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        Images.transformOrigin(resource.getFile(), file, 1500, 1500);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void transform5() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
        Resource watermark = Resources.getClassPathResources("META-INF/images/watermark.png");
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        Images.transform(resource.getFile(), file, 1500, 1500, Images.getImage(watermark.getFile()));
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void transform7() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
        Resource watermark = Resources.getClassPathResources("META-INF/images/watermark.png");
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        Images.transform(resource.getFile(), file, 1500, 1500, Images.getImage(watermark.getFile()), 0.7f, Positions.CENTER);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void getIccImage() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/icc.png");
        BufferedImage iccImage = Images.getIccImage(resource.getFile());
        Assertions.assertThat(iccImage).isNotNull();
    }

    @Test
    void outputImage() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        @Cleanup FileOutputStream out = new FileOutputStream(file);
        Images.outputImage(Images.getImage(resource.getFile()), out);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void generateImage() {
        Assertions.assertThat(Images.generateImage("45789")).isNotNull();
    }

    @Test
    void transformOfRand() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
        Resource watermark = Resources.getClassPathResources("META-INF/images/watermark.png");
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        Images.transformOfRand(Images.getImage(resource.getFile()), file, 1500, 1500, watermark.getFile().getAbsolutePath());
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void rotate() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
        BufferedImage rotate = Images.rotate(Images.getImage(resource.getFile()), 90);
        Assertions.assertThat(rotate).isNotNull();
    }

    @Test
    void getColorSpaceFile() throws IOException {
        Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
        Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
        Resource resource3 = Resources.getClassPathResources("META-INF/images/p1.jpg");
        Assertions.assertThat(Images.getColorSpace(resource1.getFile())).isNull();
        Assertions.assertThat(Images.getColorSpace(resource2.getFile())).isEqualTo("CMYK");
        Assertions.assertThat(Images.getColorSpace(resource3.getFile())).isNull();
    }

    @Test
    void getColorSpaceInputstream() throws IOException {
        Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
        Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
        Assertions.assertThat(Images.getColorSpace(resource1.getInputStream())).isNull();
        Assertions.assertThat(Images.getColorSpace(resource2.getInputStream())).isEqualTo("CMYK");
    }

    @Test
    void getColorTransformFile() throws IOException {
        Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
        Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
        Resource resource3 = Resources.getClassPathResources("META-INF/images/p1.jpg");
        Assertions.assertThat(Images.getColorTransform(resource1.getFile())).isEqualTo("YCCK");
        Assertions.assertThat(Images.getColorTransform(resource2.getFile())).isEqualTo("Unknown (RGB or CMYK)");
        Assertions.assertThat(Images.getColorTransform(resource3.getFile())).isNull();
    }

    @Test
    void getColorTransformInputstream() throws IOException {
        Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
        Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
        Assertions.assertThat(Images.getColorTransform(resource1.getInputStream())).isEqualTo("YCCK");
        Assertions.assertThat(Images.getColorTransform(resource2.getInputStream())).isEqualTo("Unknown (RGB or CMYK)");
    }

    @Test
    void isCmykColorSpace() throws IOException {
        Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
        Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
        Assertions.assertThat(Images.isCmykColorSpace(resource1.getFile())).isFalse();
        Assertions.assertThat(Images.isCmykColorSpace(resource2.getFile())).isTrue();
    }

    @Test
    void isYcckColorTransform() throws IOException {
        Resource resource1 = Resources.getClassPathResources("META-INF/images/ycck1.jpg");
        Resource resource2 = Resources.getClassPathResources("META-INF/images/cmyk1.jpg");
        Assertions.assertThat(Images.isYcckColorTransform(resource1.getFile())).isTrue();
        Assertions.assertThat(Images.isYcckColorTransform(resource2.getFile())).isFalse();
    }

    @Test
    void saveHtmlToImage() throws IOException {
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        @Cleanup FileOutputStream outputStream = new FileOutputStream(file);
        Images.saveHtmlToImage("<h1>Hello World</h1>", 300, 300, outputStream);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void changeAlpha() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/p1.jpg");
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        Images.changeAlpha(resource.getFile().getAbsolutePath(), filename, 5);
        File file = new File(filename);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    public void isImage() {
        String fileName = "a.jpeg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.jpg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.png";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.bmp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = "a.webp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();

        fileName = ".Jpeg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".jPg";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".PNG";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".bmp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
        fileName = ".webp";
        Assertions.assertThat(Images.isImage(fileName)).isTrue();
    }

    @Test
    public void isImageFile() throws IOException {
        Assertions.assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/heif.jpg").getFile())).isTrue();
        Assertions.assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/false.jpg").getFile())).isFalse();
        Assertions.assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/false1.jpg").getFile())).isFalse();
        Assertions.assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/true.png").getFile())).isTrue();
        Assertions.assertThat(Images.isImage(Resources.getClassPathResources("META-INF/images/1.webp").getFile())).isTrue();
    }

    @Test
    public void isImageByTika() throws IOException {
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/heif.jpg").getInputStream())).isTrue();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/heif.jpg").getContentAsByteArray())).isTrue();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false.jpg").getInputStream())).isFalse();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false.jpg").getContentAsByteArray())).isFalse();
        Assertions.assertThat(Images.isImageByTika(new FileInputStream(Resources.getClassPathResources("META-INF/images/false.jpg").getFile()))).isFalse();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/true.png").getInputStream())).isTrue();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/true.png").getContentAsByteArray())).isTrue();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/1.webp").getInputStream())).isTrue();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/1.webp").getContentAsByteArray())).isTrue();
        // 此处显示了和isImageByTika(File)方法的区别
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false1.jpg").getInputStream())).isFalse();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false1.jpg").getContentAsByteArray())).isFalse();
        Assertions.assertThat(Images.isImageByTika(Resources.getClassPathResources("META-INF/images/false1.jpg").getFile())).isTrue();
    }

    @Test
    public void getImage() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/true.png");
        BufferedImage image = Images.getImage(Objects.requireNonNull(resource).getFile());
        Assertions.assertThat(image).isNotNull();

        resource = Resources.getClassPathResources("META-INF/images/false.jpg");
        Assertions.assertThat(Images.isImage(Objects.requireNonNull(resource).getFile())).isNotNull();

        resource = Resources.getClassPathResources("META-INF/images/false1.jpg");
        Assertions.assertThat(Images.isImage(Objects.requireNonNull(resource).getFile())).isNotNull();
    }

    @Test
    void getHeifImageMeta() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/heif.jpg");
        ImageBaseMeta image = Images.getHeifImageMeta(Objects.requireNonNull(resource).getFile());
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getWidth()).isEqualTo(4032);
        Assertions.assertThat(image.getHeight()).isEqualTo(3024);

        resource = Resources.getClassPathResources("META-INF/images/true.png");
        image = Images.getHeifImageMeta(Objects.requireNonNull(resource).getFile());
        Assertions.assertThat(image).isNull();
    }

    @Test
    void getImageBaseMeta() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/heif.jpg");
        ImageBaseMeta image = Images.getImageBaseMeta(Objects.requireNonNull(resource).getFile());
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getWidth()).isEqualTo(4032);
        Assertions.assertThat(image.getHeight()).isEqualTo(3024);

        resource = Resources.getClassPathResources("META-INF/images/true.png");
        image = Images.getImageBaseMeta(Objects.requireNonNull(resource).getInputStream());
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getWidth()).isEqualTo(206);
        Assertions.assertThat(image.getHeight()).isEqualTo(65);
    }

    @Test
    void getImageMetaDefault() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/true.png");
        ImageBaseMeta image = Images.getImageMetaDefault(Objects.requireNonNull(resource).getInputStream());
        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getWidth()).isEqualTo(206);
        Assertions.assertThat(image.getHeight()).isEqualTo(65);
    }

    @Test
    void merge() throws IOException {
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        @Cleanup InputStream is = Resources.getClassPathResourcesInputStream("META-INF/images/p1.jpg");
        BufferedImage backgroundImage = Images.getImage(is);
        BufferedImage zindexImg = QrCodes.encode("https://m.auctionhome.cn/home?managerId=9", 163, 163);
        @Cleanup InputStream merge = Images.merge(backgroundImage, zindexImg, 678, 1050);
        @Cleanup FileOutputStream output = new FileOutputStream(file);
        IOUtils.copy(merge, output);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void bufferedImageToInputStream() throws IOException {
        String filename = SystemProperties.TMPDIR + "/imagetest/" + SnowFlakes.nextId() + ".png";
        File file = Files.createFile(filename);
        @Cleanup InputStream is = Resources.getClassPathResourcesInputStream("META-INF/images/p1.jpg");
        BufferedImage image = Images.getImage(is);
        @Cleanup FileOutputStream os = new FileOutputStream(file);
        @Cleanup InputStream png = Images.bufferedImageToInputStream(Thumbnails.of(image).size(750, image.getHeight()).asBufferedImage(), "png");
        IOUtils.copy(png, os);
        Assertions.assertThat(Images.isImage(file)).isTrue();
        Assertions.assertThat(Images.isImageByTika(file)).isTrue();
        file.deleteOnExit();
    }

    @Test
    void getFileType() {
        Assertions.assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/1.webp"))).isEqualToIgnoringCase("webp");
        Assertions.assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/heif.jpg"))).isEqualToIgnoringCase("HEIF");
        Assertions.assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/cmyk1.jpg"))).isEqualToIgnoringCase("JPEG");
        Assertions.assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/false.jpg"))).isNull();
        Assertions.assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/icc.png"))).isEqualToIgnoringCase("PNG");
        Assertions.assertThat(Images.getFileType(Resources.getClassPathResourcesInputStream("META-INF/images/true.png"))).isEqualToIgnoringCase("PNG");
    }

    @Test
    void getOrientation() throws IOException {
        Resource resource = Resources.getClassPathResources("META-INF/images/true.png");
        Assertions.assertThat(Images.getOrientation(resource.getFile())).isEqualTo(0);

        Resource resource2 = Resources.getClassPathResources("META-INF/images/right90.jpeg");
        Assertions.assertThat(Images.getOrientation(resource2.getInputStream())).isEqualTo(6);
    }

    @Test
    void rotateImage() throws IOException {
        Resource resource2 = Resources.getClassPathResources("META-INF/images/right90.jpeg");
        File rotate = java.nio.file.Files.createTempFile("rotate", ".jpeg").toFile();
        Images.rotateImage(resource2.getFile(), rotate);
        Assertions.assertThat(Images.getOrientation(rotate)).isEqualTo(0);
        rotate.deleteOnExit();
    }

    @Test
    @Disabled
    void rotateImageLocal() throws IOException {
        Images.rotateImage(new File("C:\\Users\\Administrator\\Desktop\\归档\\1.jpeg"), new File("C:\\Users\\Administrator\\Desktop\\归档\\1-1.jpeg"));
    }
}
