package cn.xuanyuanli.core.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.adobe.AdobeJpegDirectory;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.icc.IccDirectory;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import cn.xuanyuanli.core.constant.SystemProperties;
import org.xhtmlrenderer.context.AWTFontResolver;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xhtmlrenderer.util.FSImageWriter;

/**
 * 图像处理工具类
 *
 * <pre>
 * 用到开源库： <a href="https://github.com/haraldk/TwelveMonkeys">imageio-jpeg</a>
 * Thumbnailator(https://github.com/coobird/thumbnailator/wiki/Examples)
 * </pre>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Images {

    /**
     * FSImageWriter的实例
     */
    public static final FSImageWriter FS_IMAGE_WRITER = new FSImageWriter("jpg");
    /**
     * 支持的图片格式
     */
    private static final String[] IMAGE_EXTENTIONS = {".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp", ".ico", ".svg", ".tif", ".tiff", ".jfif", ".pjpeg",
            ".avif", ".svgz", ".heic", ".heif"};
    /**
     * 默认的水印透明度
     */
    private static final float DEFAULF_OPACITY = 0.45f;
    private static final Tika TIKA = new Tika();

    /**
     * 等比压缩图像(原图纯压缩，不要水印)
     *
     * @param sourceFile 源图像文件
     * @param destFile   压缩后要存放的目标文件
     * @param maxWidth   压缩后允许的最大宽度
     * @param maxHeight  压缩后允许的最大高度
     * @throws IOException ioexception
     */
    public static void transformOrigin(File sourceFile, File destFile, int maxWidth, int maxHeight) throws IOException {
        BufferedImage srcImage = getImage(sourceFile);
        Thumbnails.of(srcImage).size(maxWidth, maxHeight).toFile(destFile);
    }

    /**
     * 等比压缩图像(默认带水印)
     *
     * @param sourceFile     源图像文件
     * @param destFile       压缩后要存放的目标文件
     * @param maxWidth       压缩后允许的最大宽度
     * @param maxHeight      压缩后允许的最大高度
     * @param waterMarkImage 水印图像
     * @throws IOException ioexception
     */
    public static void transform(File sourceFile, File destFile, int maxWidth, int maxHeight, BufferedImage waterMarkImage) throws IOException {
        transform(sourceFile, destFile, maxWidth, maxHeight, waterMarkImage, DEFAULF_OPACITY, Positions.CENTER);
    }

    /**
     * 等比压缩图像(默认带水印)
     *
     * @param sourceFile     源图像文件
     * @param destFile       压缩后要存放的目标文件
     * @param maxWidth       压缩后允许的最大宽度
     * @param maxHeight      压缩后允许的最大高度
     * @param opacity        水印透明度
     * @param position       水印位置信息
     * @param waterMarkImage 水印图像
     * @throws IOException ioexception
     */
    public static void transform(File sourceFile, File destFile, int maxWidth, int maxHeight, BufferedImage waterMarkImage, float opacity, Position position)
            throws IOException {
        rotateImage(sourceFile);
        try {
            BufferedImage image = Thumbnails.of(sourceFile).scale(1).rotate(0).asBufferedImage();
            innerTransform(image, destFile, maxWidth, maxHeight, waterMarkImage, opacity, position);
        } catch (Exception e) { // 有时用常规的读取会报错，所以加下面一层
            log.error("transform常规处理出错，使用扩展图片读取器再试一遍。sourceFile:{},destFile:{}", sourceFile.getAbsolutePath(), destFile.getAbsolutePath());
            BufferedImage image = getImage(sourceFile);
            innerTransform(image, destFile, maxWidth, maxHeight, waterMarkImage, opacity, position);
        }
    }

    /**
     * 获得图片方向
     *
     * @param sourceFile 源文件
     * @return int
     */
    public static int getOrientation(File sourceFile) {
        try (InputStream inputStream = java.nio.file.Files.newInputStream(sourceFile.toPath())) {
            return getOrientation(inputStream);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * 获得图片方向
     *
     * @param inputStream 输入流
     * @return int
     */
    public static int getOrientation(InputStream inputStream) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(byteArrayInputStream);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory == null) {
                return 0;
            }
            Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            return orientation != null ? orientation : 0;
        } catch (ImageProcessingException | IOException e) {
            // nothing
        }
        return 0;
    }

    /**
     * 本来应该根据图片旋转的角度来进行更正，更正后的图片会覆盖源文件
     *
     * @param sourceFile 源文件
     * @throws IOException ioexception
     */
    public static void rotateImage(File sourceFile) throws IOException {
        rotateImage(sourceFile, sourceFile);
    }

    /**
     * 本来应该根据图片旋转的角度来进行更正
     *
     * @param sourceFile 源文件
     * @param destFile   目标文件
     * @throws IOException ioexception
     */
    public static void rotateImage(File sourceFile, File destFile) throws IOException {
        try (InputStream inputStream = java.nio.file.Files.newInputStream(
                sourceFile.toPath()); ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(byteArrayInputStream);
            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory == null) {
                return;
            }
            Integer orientation = directory.getInteger(ExifIFD0Directory.TAG_ORIENTATION);
            // 进行图片旋转
            if (orientation != null) {
                int turn = 0;
                int i3 = 3;
                int i6 = 6;
                int i8 = 8;
                if (orientation == i3) {
                    turn = 180;
                } else if (orientation == i6) {
                    turn = 90;
                } else if (orientation == i8) {
                    turn = 270;
                }

                if (turn > 0) {
                    if (directory.containsTag(ExifIFD0Directory.TAG_MAKE)) {
                        String mark = directory.getDescription(ExifIFD0Directory.TAG_MAKE);
                        // 测试发现，对于佳能来说，只有旋转360°才回复正常
                        String canon = "Canon";
                        if (canon.equalsIgnoreCase(mark)) {
                            turn = 360;
                        }
                        // 对于iphone，这里不处理，交由下一步
                        String regEx = "[iI][pP]hone";
                        if (Texts.find(mark, regEx)) {
                            return;
                        }
                    }
                    BufferedImage image = rotate(ImageIO.read(sourceFile), turn);
                    ImageIO.write(image, "jpg", destFile);
                }
            }
        } catch (ImageProcessingException e1) {
            // nothing
        }
    }

    /**
     * 对角线的水印
     *
     * @param sourceImage    源图像
     * @param destFile       目标文件
     * @param maxWidth       最大宽度
     * @param maxHeight      最大高度
     * @param watermarkImage 水印图像
     * @param opacity        不透明度
     * @param position       位置
     * @throws IOException ioexception
     */
    static void innerTransform(BufferedImage sourceImage, File destFile, int maxWidth, int maxHeight, BufferedImage watermarkImage, float opacity,
            Position position) throws IOException {
        if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
            maxHeight = sourceImage.getHeight();
            maxWidth = sourceImage.getWidth();
        }

        Builder<BufferedImage> builder = Thumbnails.of(sourceImage).size(maxWidth, maxHeight);
        BufferedImage image = builder.asBufferedImage();
        // 给网站的图片打上水印
        if (watermarkImage != null) {
            double ratio = (image.getWidth() / 8.0) / watermarkImage.getWidth();
            watermarkImage = Thumbnails.of(watermarkImage).scale(ratio).asBufferedImage();
            builder.watermark(position, watermarkImage, opacity);
        }
        builder.toFile(destFile);
    }

    /**
     * 随机的水印
     *
     * @param sourceImage        源图像
     * @param destFile           目标文件
     * @param maxWidth           最大宽度
     * @param maxHeight          最大高度
     * @param waterMarkImagePath 水印图像路径
     * @throws IOException ioexception
     */
    public static void transformOfRand(BufferedImage sourceImage, File destFile, int maxWidth, int maxHeight, String waterMarkImagePath) throws IOException {
        if (sourceImage.getWidth() <= maxWidth && sourceImage.getHeight() <= maxHeight) {
            maxHeight = sourceImage.getHeight();
            maxWidth = sourceImage.getWidth();
        }
        Builder<BufferedImage> builder = Thumbnails.of(sourceImage).size(maxWidth, maxHeight).rotate(0);
        // 给网站的图片打上水印
        if (StringUtils.isNotBlank(waterMarkImagePath)) {
            float opacity = 0.4f;
            BufferedImage image = builder.asBufferedImage();
            BufferedImage watermarkImage = ImageIO.read(new File(waterMarkImagePath));
            double ratio = (image.getWidth() / 8.0) / watermarkImage.getWidth();
            watermarkImage = Thumbnails.of(watermarkImage).scale(ratio).asBufferedImage();

            Position position = getPosition();
            builder.watermark(position, watermarkImage, opacity).watermark(position, watermarkImage, opacity).watermark(position, watermarkImage, opacity);
        }
        builder.toFile(destFile);
    }

    /**
     * 获取随机水印位置
     *
     * @return {@link Position} 水印位置对象
     */
    private static Position getPosition() {
        List<Integer> xList = new ArrayList<>();
        List<Integer> yList = new ArrayList<>();
        return (enclosingWidth, enclosingHeight, width, height, insetLeft, insetRight, insetTop, insetBottom) -> {
            int x = getPosition(enclosingWidth, width, xList);
            xList.add(x);
            // noinspection SuspiciousNameCombination
            int y = getPosition(enclosingHeight, height, yList);
            yList.add(y);
            return new Point(x, y);
        };
    }

    /**
     * 根据原图尺寸和水印尺寸获得水印坐标
     *
     * @param enclosingWidth 封闭宽度
     * @param width          宽度
     * @param listPosition   列表位置
     * @return int
     */
    private static int getPosition(int enclosingWidth, int width, List<Integer> listPosition) {
        final int x = Randoms.randomInt(width / 2, enclosingWidth - (width + width / 2));
        int width2 = width + width / 2;
        // 如果相距过近，则重新计算
        if (listPosition.stream().anyMatch(t -> x < t + width2 && x > t - width2)) {
            return getPosition(enclosingWidth, width, listPosition);
        }
        return x;
    }

    /**
     * 获得图片真实文件类型
     *
     * @param inputStream 输入流
     * @return WebP、JPEG等，如果读取失败，则返回null
     */
    public static String getFileType(InputStream inputStream) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(byteArrayInputStream);
            FileTypeDirectory typeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
            return typeDirectory.getString(FileTypeDirectory.TAG_DETECTED_FILE_TYPE_NAME);
        } catch (ImageProcessingException | IOException ignored) {
        }
        return null;
    }

    /**
     * 获得文件类型
     *
     * @param file 文件
     * @return {@link String}
     * @see #getFileType(InputStream)
     */
    public static String getFileType(File file) {
        try (InputStream fileInputStream = java.nio.file.Files.newInputStream(file.toPath())) {
            return getFileType(fileInputStream);
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * 获得图像
     *
     * @param fileInputStream 文件输入流
     * @return {@link BufferedImage}
     */
    @SneakyThrows
    public static BufferedImage getImage(InputStream fileInputStream) {
        try (ImageInputStream input = ImageIO.createImageInputStream(fileInputStream)) {
            // Find potential readers
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            // For each reader: try to read
            while (readers != null && readers.hasNext()) {
                ImageReader reader = readers.next();
                BufferedImage image;
                try {
                    reader.setInput(input);
                    image = reader.read(0);
                    return image;
                } catch (IIOException e) {
                    // Try next reader, ignore.
                } finally {
                    // Close reader resources
                    reader.dispose();
                }
            }
            // Couldn't resize with any of the readers
            throw new IIOException("Unable to resize image");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据文件获得BufferedImage(ICC头文件兼容版本，为了防止图片变红)
     * <br>
     * 必须开启 System.setProperty("java.awt.headless", "false"); 不过headless默认是true，开启这个开关在某些情况下造成未知风险。建议使用oss的图片类型转换，或者使用simpleimage库
     *
     * @param sourceFile 源文件
     * @return {@link BufferedImage}
     */
    public static BufferedImage getIccImage(File sourceFile) {
        Image src = Toolkit.getDefaultToolkit().getImage(sourceFile.getPath());
        return iccImageToBufferedImage(src);
    }

    /**
     * icc图像缓冲图像
     *
     * @param image 图像
     * @return {@link BufferedImage}
     */
    private static BufferedImage iccImageToBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        BufferedImage bimage;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency = Transparency.OPAQUE;
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            throw new RuntimeException(e);
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }

    /**
     * 根据文件获得BufferedImage
     *
     * @param sourceFile 源文件
     * @return {@link BufferedImage}
     * @throws IOException ioexception
     */
    public static BufferedImage getImage(File sourceFile) throws IOException {
        try (InputStream is = java.nio.file.Files.newInputStream(sourceFile.toPath())) {
            return getImage(is);
        }
    }

    /**
     * 对图片进行旋转(顺时针)
     *
     * @param src   图片
     * @param angel 角度，一般是90的倍数
     * @return {@link BufferedImage}
     */
    public static BufferedImage rotate(Image src, int angel) {
        int srcWidth = src.getWidth(null);
        int srcHeight = src.getHeight(null);
        // calculate the new image size
        Rectangle rectDes = calcRotatedSize(new Rectangle(new Dimension(srcWidth, srcHeight)), angel);

        BufferedImage res;
        res = new BufferedImage(rectDes.width, rectDes.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = res.createGraphics();
        // transform
        g2.translate((rectDes.width - srcWidth) / 2.0, (rectDes.height - srcHeight) / 2.0);
        g2.rotate(Math.toRadians(angel), srcWidth / 2.0, srcHeight / 2.0);

        g2.drawImage(src, null, null);
        return res;
    }

    /**
     * calc旋转大小
     *
     * @param src   src
     * @param angel 天使
     * @return {@link Rectangle}
     */
    private static Rectangle calcRotatedSize(Rectangle src, int angel) {
        int num = 90;
        if (angel >= num) {
            boolean bool = angel / num % 2 == 1;
            if (bool) {
                int temp = src.height;
                // noinspection SuspiciousNameCombination
                src.height = src.width;
                src.width = temp;
            }
            angel = angel % num;
        }

        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2.0;
        double len = 2 * Math.sin(Math.toRadians(angel) / 2.0) * r;
        double angelAlpha = (Math.PI - Math.toRadians(angel)) / 2.0;
        double angelDaltaWidth = Math.atan((double) src.height / src.width);
        double angelDaltaHeight = Math.atan((double) src.width / src.height);

        int lenDaltaWidth = (int) (len * Math.cos(Math.PI - angelAlpha - angelDaltaWidth));
        int lenDaltaHeight = (int) (len * Math.cos(Math.PI - angelAlpha - angelDaltaHeight));
        int desWidth = src.width + lenDaltaWidth * 2;
        int desHeight = src.height + lenDaltaHeight * 2;
        return new Rectangle(new Dimension(desWidth, desHeight));
    }

    /**
     * 往页面输出的方法
     *
     * @param image 图像
     * @param out   出
     * @throws IOException          ioexception
     * @throws NullPointerException 空指针异常
     */
    public static void outputImage(BufferedImage image, OutputStream out) throws IOException, NullPointerException {
        // 下面进行对图片格式的一些修改
        ImageTypeSpecifier type = ImageTypeSpecifier.createFromRenderedImage(image);
        ImageWriter writer = ImageIO.getImageWriters(type, "jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // 控制图片质量，1.0最高
        param.setCompressionQuality(1.0F);
        // 创建输出流
        ImageOutputStream outputStream = ImageIO.createImageOutputStream(out);
        // 将构建好的图片输出流写入到页面中
        writer.setOutput(outputStream);
        writer.write(null, new IIOImage(image, null, null), param);
    }

    /**
     * 根据文件名后缀判断是否是图片文件
     *
     * @param fileName 文件名称
     * @return boolean
     */
    public static boolean isImage(String fileName) {
        if (!fileName.startsWith(".")) {
            fileName = Files.getExtention(fileName);
        }
        String exten = fileName;
        return Arrays.stream(IMAGE_EXTENTIONS).anyMatch(s -> s.equalsIgnoreCase(exten));
    }

    /**
     * 判断文件是否是图片
     * <br>
     * 此方法很浪费内存，一个1MB大小的图片需要100MB内存. 推荐使用{@link #isImageByTika(File)}方法
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isImage(File file) {
        try {
            // 针对heif格式（apple特有格式），目前要转换为BufferedImage比较麻烦，这里直接判断类型
            if (isHeifImage(file)) {
                return true;
            }
            BufferedImage bi = getImage(file);
            if (bi == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 使用Apache Tika库检测输入流是否为图像文件
     */
    public static boolean isImageByTika(InputStream inputStream) {
        try {
            String mimeType = TIKA.detect(inputStream);
            return mimeType.startsWith("image/");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 使用Apache Tika库检测输入流是否为图像文件
     */
    public static boolean isImageByTika(byte[] bytes) {
        try {
            String mimeType = TIKA.detect(bytes);
            return mimeType.startsWith("image/");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 使用Apache Tika库检测输入流是否为图像文件
     * <p>
     * 注意：此方法的准确率高于{@link #isImageByTika(InputStream)}，和{@link #isImage(File)}准确率相当。参见测试用例
     * </p>
     */
    public static boolean isImageByTika(File file) {
        try {
            String mimeType = TIKA.detect(file);
            return mimeType.startsWith("image/");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 是否是heif格式（apple特有格式）
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isHeifImage(File file) {
        String fileType = getFileType(file);
        return "HEIF".equalsIgnoreCase(fileType) || "heic".equalsIgnoreCase(fileType);
    }

    /**
     * 获取HEIF格式图片的基本元数据信息
     *
     * @param file HEIF格式图片文件
     * @return {@link ImageBaseMeta} 图片基本元数据，包含宽度和高度信息
     */
    @SneakyThrows
    static ImageBaseMeta getHeifImageMeta(File file) {
        Metadata metadata = getMetadata(file);
        int width = getExifSubIfdTagIntVal(metadata, 40962);
        if (width > -1) {
            int height = getExifSubIfdTagIntVal(metadata, 40963);
            return new ImageBaseMeta(width, height);
        }
        return null;
    }

    /**
     * 获取元数据
     *
     * @param file 文件
     * @return {@link Metadata }
     * @throws IOException              ioexception
     * @throws ImageProcessingException 图像处理异常
     */
    private static Metadata getMetadata(File file) throws IOException, ImageProcessingException {
        @Cleanup InputStream inputStream = java.nio.file.Files.newInputStream(file.toPath());
        @Cleanup ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream));
        return ImageMetadataReader.readMetadata(byteArrayInputStream);
    }

    /**
     * 获取图片的基本信息--宽高
     *
     * @param imgFile 图片文件
     * @return {@link ImageBaseMeta}
     */
    public static ImageBaseMeta getImageBaseMeta(File imgFile) {
        ImageBaseMeta baseMeta = null;
        if (Images.isHeifImage(imgFile)) {
            baseMeta = getHeifImageMeta(imgFile);
        } else {
            try (FileInputStream fileInputStream = new FileInputStream(imgFile)) {
                baseMeta = getImageMetaDefault(fileInputStream);
            } catch (Exception e) {
                log.error("getImage", e);
            }
        }
        return baseMeta;
    }

    /**
     * 获取图片的基本信息--宽高。同时对于cmyk格式进行色彩还原
     *
     * @param stream 流
     * @return {@link ImageBaseMeta}
     */
    @SneakyThrows
    public static ImageBaseMeta getImageBaseMeta(InputStream stream) {
        Path tempFile = java.nio.file.Files.createTempFile("getImageBaseMeta-", ".tmp");
        IOUtils.copy(stream, java.nio.file.Files.newOutputStream(tempFile));
        try {
            return getImageBaseMeta(tempFile.toFile());
        } finally {
            java.nio.file.Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 使用默认方式获取图片的基本元数据信息
     *
     * @param fileInputStream 图片文件输入流
     * @return {@link ImageBaseMeta} 图片基本元数据，包含宽度和高度信息
     * @throws RuntimeException 当无法读取图片时抛出运行时异常
     */
    @SneakyThrows
    static ImageBaseMeta getImageMetaDefault(InputStream fileInputStream) {
        try (ImageInputStream input = ImageIO.createImageInputStream(fileInputStream)) {
            // Find potential readers
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            // For each reader: try to read
            while (readers != null && readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(input);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    return new ImageBaseMeta(width, height);
                } catch (IIOException e) {
                    // Try next reader, ignore.
                } finally {
                    // Close reader resources
                    reader.dispose();
                }
            }
            // Couldn't resize with any of the readers
            throw new IIOException("Unable to resize image");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得exif子ifdtagint值
     *
     * @param metadata 元数据
     * @param tagType  标签类型
     * @return int
     */
    private static int getExifSubIfdTagIntVal(Metadata metadata, int tagType) {
        Directory directory = metadata.getFirstDirectoryOfType((Class<? extends Directory>) ExifSubIFDDirectory.class);
        if (directory != null) {
            try {
                return directory.getInt(tagType);
            } catch (Exception ignored) {
            }
        }
        return -1;
    }

    /**
     * 生成图片验证码
     *
     * @param code 需为四位的验证码，如果超出四位，则只取前四位
     * @return {@link BufferedImage}
     */
    public static BufferedImage generateImage(String code) {
        // 设置图片信息，宽，高，具有 8 位 RGB 颜色分量的图像
        BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
        // 得到画笔
        Graphics g = image.getGraphics();
        // 产生背景图片
        g.setColor(Color.white);
        // 画一个矩形框
        g.fillRect(1, 1, 98, 28);
        // 添加一些干扰的线条
        for (int i = 0; i < 20; i++) {
            g.setColor(generateColor());
            int x1 = Randoms.randomInt(0, 100);
            int y1 = Randoms.randomInt(0, 30);
            int x2 = Randoms.randomInt(0, 100);
            int y2 = Randoms.randomInt(0, 30);
            g.drawLine(x1, y1, x2, y2);
        }
        // 画数字
        // 为了得到不同效果的随机字符串，这里采用一个一个字符串的画。
        // 这样可以使其颜色或者其他信息有所不同

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(0) + "", 5, 28);

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(1) + "", 30, 28);

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(2) + "", 55, 28);

        g.setFont(new Font("IMPACT", Font.PLAIN, 20 + Randoms.randomInt(0, 10)));
        g.setColor(generateColor());
        g.drawString(code.charAt(3) + "", 80, 28);

        // 返回制作好的图像
        return image;
    }

    /**
     * 生成随机的颜色
     *
     * @return {@link Color}
     */
    static Color generateColor() {
        int r = Randoms.randomInt(0, 180);
        int g = Randoms.randomInt(0, 180);
        int b = Randoms.randomInt(0, 180);
        return new Color(r, g, b);
    }

    /**
     * 获得颜色空间
     *
     * @param imageFile 图像文件
     * @return {@link String}
     * @see #getColorSpace(InputStream)
     */
    public static String getColorSpace(File imageFile) {
        try (FileInputStream inputStream = new FileInputStream(imageFile)) {
            return getColorSpace(inputStream);
        } catch (IOException e) {
            log.error("getColorSpace", e);
        }
        return null;
    }

    /**
     * 获得图片的Color Space<br> <a href="https://neucrack.com/p/294">颜色空间</a>
     *
     * @param inputStream 输入流
     * @return 如CMYK等。如果读取出错，则返回null
     */
    public static String getColorSpace(InputStream inputStream) {
        // 获取metadata可能会改变文件，所以这里做一个处理
        try (ByteArrayInputStream stream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(stream);
            IccDirectory iccDirectory = metadata.getFirstDirectoryOfType(IccDirectory.class);
            if (iccDirectory != null) {
                return iccDirectory.getDescription(IccDirectory.TAG_COLOR_SPACE);
            }
        } catch (ImageProcessingException | IOException ignored) {
        }
        return null;
    }

    /**
     * 获得颜色变换
     *
     * @param imageFile 图像文件
     * @return {@link String}
     * @see #getColorSpace(InputStream)
     */
    public static String getColorTransform(File imageFile) {
        try (FileInputStream inputStream = new FileInputStream(imageFile)) {
            return getColorTransform(inputStream);
        } catch (IOException e) {
            log.error("getColorTransform", e);
        }
        return null;
    }

    /**
     * 获得图片的Color Transform
     *
     * @param inputStream 输入流
     * @return 如YCCK等。如果读取出错，则返回null
     */
    public static String getColorTransform(InputStream inputStream) {
        // 获取metadata可能会改变文件，所以这里做一个处理
        try (ByteArrayInputStream stream = new ByteArrayInputStream(IOUtils.toByteArray(inputStream))) {
            Metadata metadata = ImageMetadataReader.readMetadata(stream);
            AdobeJpegDirectory adobeJpegDirectory = metadata.getFirstDirectoryOfType(AdobeJpegDirectory.class);
            if (adobeJpegDirectory != null) {
                return adobeJpegDirectory.getDescription(AdobeJpegDirectory.TAG_COLOR_TRANSFORM);
            }
        } catch (ImageProcessingException | IOException ignored) {
        }
        return null;
    }

    /**
     * 是否是CMYK颜色空间的图片
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isCmykColorSpace(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return "CMYK".equalsIgnoreCase(getColorSpace(inputStream));
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * 是否是YCCK颜色空间的图片
     *
     * @param file 文件
     * @return boolean
     */
    public static boolean isYcckColorTransform(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return "YCCK".equalsIgnoreCase(getColorTransform(inputStream));
        } catch (IOException ignored) {
        }
        return false;
    }

    /**
     * 保存html为图片。仅支持CSS2.1的渲染<br> 注意非Windows操作系统可能不支持中文，需要在对应项目的resources下添加typeface/MicrosoftYaHei.ttf文件
     *
     * @param html         内容
     * @param htmlWidth    宽
     * @param htmlHeight   高，-1的话=auto
     * @param outputStream 输出目的地
     */
    public static void saveHtmlToImage(String html, int htmlWidth, int htmlHeight, OutputStream outputStream) {
        File tempFile = null;
        try {
            tempFile = java.nio.file.Files.createTempFile("saveHtmlToImage", "").toFile();
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(html.getBytes())) {
                FileUtils.copyInputStreamToFile(byteArrayInputStream, tempFile);
            }
            Java2DRenderer renderer = new Java2DRenderer(tempFile, htmlWidth, htmlHeight);
            if (!SystemProperties.WINDOWS) {
                AWTFontResolver fontResolver = (AWTFontResolver) renderer.getSharedContext().getFontResolver();
                fontResolver.setFontMapping("Microsoft YaHei",
                        Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(Resources.getClassPathResourcesInputStream("typeface/MicrosoftYaHei.ttf"))));
            }
            FS_IMAGE_WRITER.write(renderer.getImage(), outputStream);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    /**
     * 调整图片透明度
     *
     * @param path    源路径
     * @param tarPath 生成路径
     * @param alpha   透明度 （0不透明---10全透明）
     */
    public static void changeAlpha(String path, String tarPath, int alpha) {
        // 检查透明度是否越界
        if (alpha < 0) {
            alpha = 0;
        } else if (alpha > 10) {
            alpha = 10;
        }
        try {
            BufferedImage image = ImageIO.read(new File(path));
            int weight = image.getWidth();
            int height = image.getHeight();
            BufferedImage output = new BufferedImage(weight, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = output.createGraphics();
            output = g2.getDeviceConfiguration().createCompatibleImage(weight, height, Transparency.TRANSLUCENT);
            g2.dispose();
            g2 = output.createGraphics();
            // 调制透明度
            for (int j1 = output.getMinY(); j1 < output.getHeight(); j1++) {
                for (int j2 = output.getMinX(); j2 < output.getWidth(); j2++) {
                    int rgb = output.getRGB(j2, j1);
                    rgb = ((alpha * 255 / 10) << 24) | (rgb & 0x00ffffff);
                    output.setRGB(j2, j1, rgb);
                }
            }
            g2.setComposite(AlphaComposite.SrcIn);
            g2.drawImage(image, 0, 0, weight, height, null);
            g2.dispose();
            ImageIO.write(output, "png", Files.createFile(tarPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 图片合并<br> 图片合并中遇到合并后发红的情况，主要是getImage()方法无法处理icc表头导致的，使用getIccImage()可以解决这个问题，但注意java.awt.headless开关修改后的影响
     *
     * @param backgroundImage 背景图片
     * @param zindexImg       层级图片
     * @param x               x坐标
     * @param y               y坐标
     * @return {@link InputStream}
     */
    public static InputStream merge(BufferedImage backgroundImage, BufferedImage zindexImg, int x, int y) {
        Graphics2D g = backgroundImage.createGraphics();
        g.drawImage(zindexImg, x, y, zindexImg.getWidth(), zindexImg.getHeight(), null);
        g.dispose();
        return bufferedImageToInputStream(backgroundImage, "jpg");
    }

    /**
     * backgroundImage 转换为输出流
     *
     * @param backgroundImage 图片流
     * @param format          图片后缀
     * @return {@link InputStream}
     */
    public static InputStream bufferedImageToInputStream(BufferedImage backgroundImage, String format) {
        try (ByteArrayOutputStream bs = new ByteArrayOutputStream(); ImageOutputStream imOut = ImageIO.createImageOutputStream(bs)) {
            ImageIO.write(backgroundImage, format, imOut);
            return new ByteArrayInputStream(bs.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class ImageBaseMeta {

        private int width;
        private int height;
    }
}
