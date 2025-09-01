package cn.xuanyuanli.core.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * 文件操作工具类
 * <p>
 * 提供全面的文件操作功能，包括但不限于：
 * <ul>
 * <li>文件和目录的创建与管理</li>
 * <li>文件扩展名获取和处理</li>
 * <li>文件内容的读写操作</li>
 * <li>Base64 编码解码与文件互转</li>
 * <li>路径安全性验证</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>核心功能：</strong>
 * <ul>
 * <li><strong>文件创建：</strong>支持递归创建目录和文件，自动处理父目录</li>
 * <li><strong>Base64 处理：</strong>支持文件与Base64字符串的双向转换</li>
 * <li><strong>安全路径检查：</strong>防止路径遍历攻击，确保文件操作安全</li>
 * <li><strong>编码支持：</strong>支持指定字符编码的文件读写</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>使用示例：</strong>
 * <pre>{@code
 * // 创建文件和目录
 * File dir = Files.createDir("/path/to/directory");
 * File file = Files.createFile("/path/to/file.txt");
 * 
 * // 获取文件扩展名
 * String ext = Files.getExtention("document.pdf"); // ".pdf"
 * 
 * // 向文件追加内容
 * Files.appendStringToFile("log.txt", "新日志内容", StandardCharsets.UTF_8);
 * 
 * // Base64 与文件互转
 * String base64 = Files.fileToBase64(new File("image.jpg"));
 * Files.base64ToFile(base64Data, new File("output.jpg"));
 * 
 * // 验证路径安全性
 * boolean safe = Files.isValidPath("/base/path", "../../../etc/passwd");
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>安全注意事项：</strong>
 * <ul>
 * <li>使用 {@link #isValidPath(String, String)} 验证用户输入路径，防止路径遍历攻击</li>
 * <li>Base64 处理会自动去除数据URL前缀（如 "data:image/jpeg;base64,"）</li>
 * <li>所有文件操作都包含适当的异常处理和资源管理</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>依赖：</strong>基于 Apache Commons IO 和 Java NIO 实现，提供高性能的文件操作。
 * </p>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@SuppressWarnings("unused")
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Files {

    /**
     * 获得文件扩展名
     *
     * @param fileName 文件名称
     * @return {@link String}
     */
    public static String getExtention(String fileName) {
        return getExtention(fileName, "");
    }

    /**
     * 获得文件扩展名（如果扩展名为空，则默认为.${defaultExtension})
     *
     * @param fileName         文件名称
     * @param defaultExtension 默认扩展
     * @return {@link String}
     */
    public static String getExtention(String fileName, String defaultExtension) {
        String extension = FilenameUtils.getExtension(fileName);
        return (StringUtils.isEmpty(extension) ? defaultExtension : "." + extension);
    }

    /**
     * 创建目录
     *
     * @param filePath 文件路径
     * @return {@link File}
     */
    public static File createDir(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        File myFile = new File(filePath);
        if (!myFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            myFile.mkdirs();
        }
        return myFile;
    }

    /**
     * 创建文件。如果上级路径不存在，则创建路径；如果文件不存在，则创建文件
     *
     * @param filePath 文件绝对路径
     * @return {@link File}
     */
    public static File createFile(String filePath) {
        Validate.isTrue(StringUtils.isNotBlank(filePath), "文件路径不能为空");
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            parentFile.mkdirs();
        }
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    /**
     * 向文件末尾写入内容，如果不存在此文件，则新建
     *
     * @param fileName 文件名称
     * @param data     数据
     * @param encoding 编码
     * @return {@link File}
     */
    public static File appendStringToFile(String fileName, String data, Charset encoding) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (encoding == null) {
            throw new IllegalArgumentException();
        }
        File file = createFile(fileName);
        if (StringUtils.isBlank(data)) {
            return file;
        }
        try (BufferedWriter bufferedWriter = java.nio.file.Files.newBufferedWriter(file.toPath(), encoding, StandardOpenOption.APPEND)){
            bufferedWriter.write((file.length() == 0 ? "" : "\r\n") + data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * base64转成inputStream流
     *
     * @param base64Text base64文本
     * @return {@link InputStream}
     */
    public static InputStream base64ToInputstream(String base64Text) {
        InputStream inputStream = null;
        if (base64Text == null) {
            // 图像数据为空
            return null;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        try {
            // 对字符串进行处理
            int j = base64Text.indexOf(',');
            if (j != -1) {
                base64Text = base64Text.substring(j + 1);
            }
            // Base64解码
            byte[] bytes = decoder.decode(base64Text);
            for (int i = 0; i < bytes.length; ++i) {
                if (bytes[i] < 0) {
                    bytes[i] += (byte) 256;
                }
            }
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            log.error("base64ToInputstream", e);
        }
        return null;
    }

    /**
     * base64转存到文件
     *
     * @param base64Text base64文本
     * @param destFile   目标文件
     */
    public static void base64ToFile(String base64Text, File destFile) {
        try (InputStream inputStream = base64ToInputstream(base64Text);
                FileOutputStream outputStream = new FileOutputStream(destFile)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * inputstream转换为base64
     *
     * @param inputStream 输入流
     * @return {@link String}
     */
    public static String streamToBase64(InputStream inputStream) {
        try {
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 文件转换为base64
     *
     * @param file 文件
     * @return {@link String}
     */
    public static String fileToBase64(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return streamToBase64(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 是有效路径
     *
     * @param baseDir  基本方向
     * @param userPath 用户路径
     * @return boolean
     */
    public static boolean isValidPath(String baseDir, String userPath) {
        try {
            Path base = Paths.get(baseDir).normalize();
            Path resolved = base.resolve(userPath).normalize();
            return resolved.startsWith(base);
        } catch (Exception e) {
            return false;
        }
    }
}
