package cn.xuanyuanli.core;

import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import cn.xuanyuanli.core.util.net.Networks;

@SuppressWarnings("unused")
public class Test {

    public static void main(String[] args) throws IOException, ImageProcessingException, MetadataException {
        System.out.println(Networks.isPortAvailable(12200));
    }

    /**
     * 拆分大文件
     *
     * @param filePath  文件路径
     * @param fileCount 拆分为几块
     */
    public static void splitFile(String filePath, int fileCount) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        FileChannel inputChannel = fis.getChannel();
        final long fileSize = inputChannel.size();
        //平均值
        long average = fileSize / fileCount;
        long bufferSize = 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.parseInt(bufferSize + ""));
        long startPosition = 0;
        long endPosition = average < bufferSize ? 0 : average - bufferSize;
        for (int i = 0; i < fileCount; i++) {
            if (i + 1 != fileCount) {
                int read = inputChannel.read(byteBuffer, endPosition);
                readW:
                while (read != -1) {
                    byteBuffer.flip();//切换读模式
                    byte[] array = byteBuffer.array();
                    for (int j = 0; j < array.length; j++) {
                        byte b = array[j];
                        if (b == 10 || b == 13) {
                            endPosition += j;
                            break readW;
                        }
                    }
                    endPosition += bufferSize;
                    byteBuffer.clear();
                    read = inputChannel.read(byteBuffer, endPosition);
                }
            } else {
                endPosition = fileSize;
            }

            Path path = Paths.get(filePath);
            String outFileName = path.getParent().toString() + File.separator + (i + 1) + "-" + path.getFileName().toString();
            FileOutputStream fos = new FileOutputStream(outFileName);
            FileChannel outputChannel = fos.getChannel();
            inputChannel.transferTo(startPosition, endPosition - startPosition, outputChannel);
            outputChannel.close();
            fos.close();
            startPosition = endPosition + 1;
            endPosition += average;
        }
        inputChannel.close();
        fis.close();
    }
}
