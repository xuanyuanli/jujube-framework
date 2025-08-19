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
 *
 * @author 李衡 Email：li15038043160@163.com
 * @date 2021/09/01
 * @since 2015年3月3日 上午11:16:18
 */
@Slf4j
public class WebUploaderUtil {

    /**
     * 合并分片文件
     *
     * @param chunkDirPath 分片文件放置的目录
     * @param destFilePath 合并后的文件路径
     * @return {@link File}
     * @throws IOException ioexception
     */
    public static File mergeChunkFile(String chunkDirPath, String destFilePath) throws IOException {
        return mergeChunkFile(chunkDirPath, destFilePath, true);
    }

    /**
     * 合并分片文件
     *
     * @param chunkDirPath 分片文件放置的目录
     * @param destFilePath 合并后的文件路径
     * @param clean        是否清除原分片目录
     * @return {@link File}
     * @throws IOException ioexception
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
     *
     * @param chunkIndex   分片index
     * @param chunkDirPath 分片文件放置的目录
     * @param inputStream  此次的分片文件流
     * @throws IllegalStateException 非法状态异常
     * @throws IOException           ioexception
     */
    public static void uploadChunkFile(int chunkIndex, String chunkDirPath, InputStream inputStream) throws IllegalStateException, IOException {
        File chunkDir = Files.createDir(chunkDirPath);
        String fileName = "" + chunkIndex;
        File destFile = new File(chunkDir, fileName);
        FileUtils.copyInputStreamToFile(inputStream, destFile);
    }

    /**
     * 分割大文件
     *
     * @param sourcePath  源路径
     * @param size        大小
     * @param destDirPath 目标dir路径
     * @throws Exception 异常
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
