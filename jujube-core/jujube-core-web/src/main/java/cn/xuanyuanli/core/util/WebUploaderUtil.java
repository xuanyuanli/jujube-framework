package cn.xuanyuanli.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * WebUploader组件工具类
 * <p>
 * 提供文件上传相关的工具方法，包括分片文件合并、分片文件上传和大文件分割等功能。
 * 主要用于支持WebUploader组件的大文件分片上传功能。
 * </p>
 *
 * @author 李衡 Email：li15038043160@163.com
 * @date 2021/09/01
 */
@Slf4j
public class WebUploaderUtil {

    /**
     * 合并分片文件
     * <p>
     * 将指定目录下的所有分片文件按照文件名数字顺序合并成一个完整的文件。
     * 如果分片目录中只有一个文件，则直接复制该文件；如果有多个文件，则按文件名排序后依次合并。
     * 合并完成后会自动清理原分片目录。
     * </p>
     *
     * @param chunkDirPath 分片文件放置的目录路径，该目录下应包含以数字命名的分片文件
     * @param destFilePath 合并后的目标文件路径，如果文件不存在会自动创建
     * @return {@link File} 合并后的文件对象
     * @throws IOException 当读取分片文件或写入目标文件时发生I/O错误
     */
    public static File mergeChunkFile(String chunkDirPath, String destFilePath) throws IOException {
        return mergeChunkFile(chunkDirPath, destFilePath, true);
    }

    /**
     * 合并分片文件
     * <p>
     * 将指定目录下的所有分片文件按照文件名数字顺序合并成一个完整的文件。
     * 支持自定义是否在合并完成后清理原分片目录。分片文件按文件名的数字大小进行排序，
     * 然后使用NIO的FileChannel进行高效的文件合并操作。
     * </p>
     *
     * @param chunkDirPath 分片文件放置的目录路径，该目录下应包含以数字命名的分片文件
     * @param destFilePath 合并后的目标文件路径，如果文件不存在会自动创建
     * @param clean        是否在合并完成后清除原分片目录，true表示清除，false表示保留
     * @return {@link File} 合并后的文件对象
     * @throws IOException 当读取分片文件或写入目标文件时发生I/O错误
     */
    public static File mergeChunkFile(String chunkDirPath, String destFilePath, boolean clean) throws IOException {
        File destFile = Files.createFile(destFilePath);
        File chunkDir = new File(chunkDirPath);
        List<File> chunkFileList = Arrays.asList(Objects.requireNonNull(chunkDir.listFiles()));
        // 如果只有一个文件，则进行移动即可
        if (chunkFileList.size() == 1) {
            FileUtils.copyFile(chunkFileList.get(0), destFile);
        }
        // 多个文件，则进行合并
        if (chunkFileList.size() > 1) {
            chunkFileList.sort(Comparator.comparingLong(o -> NumberUtils.toLong(o.getName())));

            // 合并
            try (FileOutputStream fileOutputStream = new FileOutputStream(destFile); FileChannel outStreamChannel = fileOutputStream.getChannel()) {
                for (File file : chunkFileList) {
                    try (FileInputStream fileInputStream = new FileInputStream(file); FileChannel inStreamChannel = fileInputStream.getChannel()) {
                        inStreamChannel.transferTo(0, inStreamChannel.size(), outStreamChannel);
                    }
                }
            }
        }
        if (clean) {
            try {
                // 清除临时文件
                FileUtils.deleteDirectory(chunkDir);
            } catch (IOException e) {
                log.error("删除目录[{}]出错", chunkDir.getAbsolutePath());
            }
        }
        return destFile;
    }

    /**
     * 上传分片文件
     * <p>
     * 将输入流中的分片文件数据保存到指定的分片目录中，文件名为分片索引。
     * 如果目标目录不存在会自动创建。该方法主要用于处理客户端上传的分片文件。
     * </p>
     *
     * @param chunkIndex   分片索引，用作文件名，通常从0开始递增
     * @param chunkDirPath 分片文件存放的目录路径，如果目录不存在会自动创建
     * @param inputStream  包含分片文件数据的输入流
     * @throws IllegalStateException 当无法创建目录或文件时抛出此异常
     * @throws IOException           当读取输入流或写入文件时发生I/O错误
     */
    public static void uploadChunkFile(int chunkIndex, String chunkDirPath, InputStream inputStream) throws IllegalStateException, IOException {
        File chunkDir = Files.createDir(chunkDirPath);
        String fileName = "" + chunkIndex;
        File destFile = new File(chunkDir, fileName);
        FileUtils.copyInputStreamToFile(inputStream, destFile);
    }

    /**
     * 分割大文件
     * <p>
     * 将指定的大文件按照指定的大小分割成多个小文件片段。
     * 分割后的文件片段保存在目标目录中，文件名为从0开始的连续数字。
     * 该方法适用于需要将大文件分割后进行传输或处理的场景。
     * </p>
     *
     * @param sourcePath  源文件的完整路径
     * @param size        每个分片文件的大小（字节数）
     * @param destDirPath 分片文件存放的目标目录路径，如果目录不存在会自动创建
     * @throws Exception 当文件读取、写入或创建目录时发生任何异常
     */
    @SuppressWarnings("unused")
    public static void splitFile(String sourcePath, int size, String destDirPath) throws Exception {
        File sourceFile = new File(sourcePath);
        long num = sourceFile.length() % size == 0 ? sourceFile.length() / size : sourceFile.length() / size + 1;
        @Cleanup FileInputStream reader = new FileInputStream(sourceFile);
        long beginIndex = 0, endIndex = 0;
        int readcount;
        // 可以设置文件在读取时一次读取文件的大小
        int length = 1024;
        byte[] buffer = new byte[length];
        for (int i = 0; i < num; i++) {
            File destDir = Files.createDir(destDirPath);
            File partFile = new File(destDir, "" + i);
            @Cleanup FileOutputStream writer = new FileOutputStream(partFile);
            endIndex = Math.min((endIndex + size), sourceFile.length());
            while (beginIndex < endIndex) {
                if (endIndex - beginIndex >= length) {
                    readcount = reader.read(buffer);
                    beginIndex += readcount;
                    writer.write(buffer);
                } else {
                    // 下面的就不能直接读取1024个字节了,就要一个一个字节的读取了
                    for (; beginIndex < endIndex; beginIndex++) {
                        writer.write(reader.read());
                    }
                }
            }
        }
    }
}
