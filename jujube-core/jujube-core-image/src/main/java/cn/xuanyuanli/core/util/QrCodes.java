package cn.xuanyuanli.core.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import cn.xuanyuanli.core.constant.Charsets;

/**
 * 二维码工具类
 * 
 * <p>该工具类提供了二维码的生成和处理功能，包括：</p>
 * <ul>
 *     <li>基础二维码生成：支持指定宽高的二维码生成</li>
 *     <li>白边处理：可选择是否移除二维码周围的白边</li>
 *     <li>Logo嵌入：支持在二维码中心嵌入Logo图片</li>
 *     <li>格式转换：支持将生成的二维码转换为BufferedImage或InputStream</li>
 *     <li>高精度渲染：通过自定义的QrCodeWriterSelf内部类，解决原生库白边过多的问题</li>
 * </ul>
 * 
 * <p><b>使用示例：</b></p>
 * <pre>{@code
 * // 生成基础二维码
 * BufferedImage qrImage = QrCodes.encode("https://example.com", 300, 300);
 * 
 * // 生成无白边的二维码
 * BufferedImage qrImageNoWhite = QrCodes.encode("Hello World", 200, 200, true);
 * 
 * // 生成带Logo的二维码
 * BufferedImage logoImage = ImageIO.read(new File("logo.png"));
 * BufferedImage qrWithLogo = QrCodes.createQrCodeWithLogo("https://example.com", 
 *     300, 300, logoImage, true);
 * }</pre>
 * 
 * <p><b>技术特点：</b></p>
 * <ul>
 *     <li>使用ZXing库作为核心二维码引擎</li>
 *     <li>默认使用GBK字符编码，支持中文内容</li>
 *     <li>默认采用L级别的纠错等级，平衡码密度和纠错能力</li>
 *     <li>内置图片处理功能，支持Logo自动缩放和居中放置</li>
 * </ul>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QrCodes {

    /**
     * 生成二维码
     *
     * @param contents 内容
     * @param width    宽度
     * @param height   高度
     * @return {@link BufferedImage}
     */
    public static BufferedImage encode(String contents, int width, int height) {
        return encode(contents, width, height, false);
    }

    /**
     * 生成二维码
     *
     * @param contents    内容
     * @param width       宽度
     * @param height      高度
     * @param deleteWhite 删除白
     * @return {@link BufferedImage}
     */
    public static BufferedImage encode(String contents, int width, int height, boolean deleteWhite) {
        Map<EncodeHintType, Object> hints = new Hashtable<>();
        // 指定纠错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 指定编码格式
        hints.put(EncodeHintType.CHARACTER_SET, Charsets.GBK.name());
        if (deleteWhite) {
            hints.put(EncodeHintType.MARGIN, 0);
        }
        try {
            Writer writer;
            if (deleteWhite) {
                writer = new QrCodeWriterSelf();
            } else {
                writer = new QRCodeWriter();
            }
            BitMatrix bitMatrix = writer.encode(contents, BarcodeFormat.QR_CODE, width, height, hints);
            return deleteWhite ? QrCodeWriterSelf.toBufferedImage(bitMatrix, width, height) : MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            log.error("encode", e);
        }
        return null;
    }

    /**
     * 生成二维码
     *
     * @param contents    内容
     * @param width       宽度
     * @param height      高度
     * @param deleteWhite 删除白
     * @return {@link InputStream}
     */
    public static InputStream encodeToInputStream(String contents, int width, int height, boolean deleteWhite) {
        BufferedImage image = encode(contents, width, height, deleteWhite);
        return Images.bufferedImageToInputStream(image, "jpg");
    }

    /**
     * 生成二维码
     *
     * @param contents 内容
     * @param width    宽度
     * @param height   高度
     * @return {@link InputStream}
     */
    public static InputStream encodeToInputStream(String contents, int width, int height) {
        BufferedImage image = encode(contents, width, height);
        return Images.bufferedImageToInputStream(image, "jpg");
    }

    /**
     * 带有logo的二维码
     *
     * @param contents           二维码内容
     * @param width              宽
     * @param height             高
     * @param logoImage          logo图片
     * @param isCompressLogImage 是否压缩logo图片到合适的尺寸
     * @param deleteWhite        删除白
     * @return {@link BufferedImage}
     */
    public static BufferedImage createQrCodeWithLogo(String contents, int width, int height, BufferedImage logoImage, boolean isCompressLogImage,
            boolean deleteWhite) {
        try {
            if (isCompressLogImage) {
                int newWidth = (int) (width / 6.8);
                int newHeight = (int) (height / 6.8);
                logoImage = Thumbnails.of(logoImage).size(newWidth, newHeight).asBufferedImage();
            }
            BufferedImage qrcode = encode(contents, width, height, deleteWhite);
            int deltaHeight = height - logoImage.getHeight();
            int deltaWidth = width - logoImage.getWidth();
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) combined.getGraphics();
            g.drawImage(qrcode, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.drawImage(logoImage, (int) Math.round(deltaWidth / 2.0), (int) Math.round(deltaHeight / 2.0), null);
            return combined;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 带有logo的二维码
     *
     * @param contents           二维码内容
     * @param width              宽
     * @param height             高
     * @param logoImage          logo图片
     * @param isCompressLogImage 是否压缩logo图片到合适的尺寸
     * @return {@link BufferedImage}
     */
    public static BufferedImage createQrCodeWithLogo(String contents, int width, int height, BufferedImage logoImage, boolean isCompressLogImage) {
        return createQrCodeWithLogo(contents, width, height, logoImage, isCompressLogImage, false);
    }

    /**
     * 为了解决QRCodeWriter宽高非整数情况下，还出现白边的问题，重写了QRCodeWriter
     * 
     * <p>该内部类继承自Writer接口，主要解决原生QRCodeWriter在处理宽高时产生过多白边的问题。
     * 通过重新实现encode方法和renderResult方法，能够更精确地控制二维码的尺寸和边距。</p>
     * 
     * @author xuanyuanli
     * @since 1.0
     */
    private static class QrCodeWriterSelf implements Writer {

        private static final int QUIET_ZONE_SIZE = 4;
        private static final MatrixToImageConfig DEFAULT_CONFIG = new MatrixToImageConfig();

        /**
         * 编码二维码
         * 
         * @param contents 二维码内容
         * @param format 二维码格式
         * @param width 宽度
         * @param height 高度
         * @return BitMatrix对象
         * @throws WriterException 编码异常
         */
        @Override
        public BitMatrix encode(String contents, BarcodeFormat format, int width, int height)
                throws WriterException {
            return encode(contents, format, width, height, null);
        }

        /**
         * 编码二维码（带提示参数）
         * 
         * @param contents 二维码内容
         * @param format 二维码格式
         * @param width 宽度
         * @param height 高度
         * @param hints 编码提示参数，包含纠错等级、字符集、边距等配置
         * @return BitMatrix对象
         * @throws WriterException 编码异常
         */
        @Override
        public BitMatrix encode(String contents,
                BarcodeFormat format,
                int width,
                int height,
                Map<EncodeHintType, ?> hints) throws WriterException {

            if (contents.isEmpty()) {
                throw new IllegalArgumentException("Found empty contents");
            }

            if (format != BarcodeFormat.QR_CODE) {
                throw new IllegalArgumentException("Can only encode QR_CODE, but got " + format);
            }

            if (width < 0 || height < 0) {
                throw new IllegalArgumentException("Requested dimensions are too small: " + width + 'x' +
                        height);
            }

            ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;
            int quietZone = QUIET_ZONE_SIZE;
            if (hints != null) {
                if (hints.containsKey(EncodeHintType.ERROR_CORRECTION)) {
                    errorCorrectionLevel = ErrorCorrectionLevel.valueOf(hints.get(EncodeHintType.ERROR_CORRECTION).toString());
                }
                if (hints.containsKey(EncodeHintType.MARGIN)) {
                    quietZone = Integer.parseInt(hints.get(EncodeHintType.MARGIN).toString());
                }
            }

            QRCode code = Encoder.encode(contents, errorCorrectionLevel, hints);
            return renderResult(code, width, height, quietZone);
        }

        /**
         * 对 zxing 的 QRCodeWriter 进行扩展, 解决白边过多的问题
         * 
         * <p>该方法重新实现了二维码的渲染逻辑，通过动态计算缩放比例和边距，
         * 有效减少了二维码周围的白边，使生成的二维码更加紧凑。</p>
         * 
         * @param code QRCode对象，包含编码后的二维码数据
         * @param width 期望的输出宽度
         * @param height 期望的输出高度
         * @param quietZone 静默区域大小（白边大小）
         * @return 渲染后的BitMatrix对象
         * @throws IllegalStateException 当二维码矩阵为空时抛出
         */
        private static BitMatrix renderResult(QRCode code, int width, int height, int quietZone) {
            ByteMatrix input = code.getMatrix();
            if (input == null) {
                throw new IllegalStateException();
            }

            // xxx 二维码宽高相等, 即 qrWidth == qrHeight
            int inputWidth = input.getWidth();
            int inputHeight = input.getHeight();
            int qrWidth = inputWidth + (quietZone * 2);
            int qrHeight = inputHeight + (quietZone * 2);

            // 白边过多时, 缩放
            int minSize = Math.min(width, height);
            int scale = calculateScale(qrWidth, minSize);
            if (scale > 0) {
                int padding, tmpValue;
                // 计算边框留白
                padding = (minSize - qrWidth * scale) / QUIET_ZONE_SIZE * quietZone;
                tmpValue = qrWidth * scale + padding;
                if (width == height) {
                    width = tmpValue;
                    height = tmpValue;
                } else if (width > height) {
                    width = width * tmpValue / height;
                    height = tmpValue;
                } else {
                    height = height * tmpValue / width;
                    width = tmpValue;
                }
            }

            int outputWidth = Math.max(width, qrWidth);
            int outputHeight = Math.max(height, qrHeight);

            int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);
            int leftPadding = (outputWidth - (inputWidth * multiple)) / 2;
            int topPadding = (outputHeight - (inputHeight * multiple)) / 2;

            BitMatrix output = new BitMatrix(outputWidth, outputHeight);

            for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
                // Write the contents of this row of the barcode
                for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                    if (input.get(inputX, inputY) == 1) {
                        output.setRegion(outputX, outputY, multiple, multiple);
                    }
                }
            }

            return output;
        }


        /**
         * 如果留白超过1% , 则需要缩放 (1% 可以根据实际需要进行修改)
         *
         * @param qrCodeSize 二维码大小
         * @param expectSize 期望输出大小
         * @return 返回缩放比例, <= 0 则表示不缩放, 否则指定缩放参数
         */
        private static int calculateScale(int qrCodeSize, int expectSize) {
            if (qrCodeSize >= expectSize) {
                return 0;
            }

            int scale = expectSize / qrCodeSize;
            int abs = expectSize - scale * qrCodeSize;
            if (abs < expectSize * 0.01) {
                return 0;
            }

            return scale;
        }


        /**
         * 将BitMatrix转换为BufferedImage
         * 
         * <p>将二维码的位矩阵数据转换为可显示的图像对象。如果实际生成的二维码尺寸
         * 与期望尺寸不一致，会进行缩放处理以确保输出图像符合预期大小。</p>
         * 
         * @param matrix 二维码位矩阵
         * @param width 期望的图像宽度
         * @param height 期望的图像高度
         * @return 转换后的BufferedImage对象
         */
        public static BufferedImage toBufferedImage(BitMatrix matrix, int width, int height) {
            int qrCodeWidth = matrix.getWidth();
            int qrCodeHeight = matrix.getHeight();
            BufferedImage qrCode = new BufferedImage(qrCodeWidth, qrCodeHeight, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < qrCodeWidth; x++) {
                for (int y = 0; y < qrCodeHeight; y++) {
                    qrCode.setRGB(x, y, matrix.get(x, y) ? DEFAULT_CONFIG.getPixelOnColor() : DEFAULT_CONFIG.getPixelOffColor());
                }
            }

            // 若二维码的实际宽高和预期的宽高不一致, 则缩放
            if (qrCodeWidth != width || qrCodeHeight != height) {
                BufferedImage tmp = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                tmp.getGraphics().drawImage(
                        qrCode.getScaledInstance(width, height,
                                java.awt.Image.SCALE_SMOOTH), 0, 0, null);
                qrCode = tmp;
            }

            return qrCode;
        }


    }
}
