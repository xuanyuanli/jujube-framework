package cn.xuanyuanli.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("WebUploaderUtil 测试")
public class WebUploaderUtilTest {

    @TempDir
    Path tempDir;

    private File chunkDir;
    private File destFile;

    @BeforeEach
    void setUp() throws IOException {
        chunkDir = tempDir.resolve("chunks").toFile();
        destFile = tempDir.resolve("mergedFile.txt").toFile();
        Files.createDirectory(chunkDir.toPath());
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.deleteDirectory(chunkDir);
        if (destFile.exists()) {
            FileUtils.delete(destFile);
        }
    }

    @Nested
    @DisplayName("文件块合并测试")
    class ChunkMergeTests {

        @Test
        @DisplayName("mergeChunkFile_应该成功合并单个文件块_当只有一个块时")
        void mergeChunkFile_shouldSuccessfullyMergeSingleChunk_whenOnlyOneChunkExists() throws IOException {
            // Arrange
            File chunkFile = new File(chunkDir, "0");
            FileUtils.writeStringToFile(chunkFile, "Single chunk content", "UTF-8");

            // Act
            File mergedFile = WebUploaderUtil.mergeChunkFile(chunkDir.getAbsolutePath(), destFile.getAbsolutePath());

            // Assert
            assertThat(mergedFile).exists();
            assertThat(FileUtils.readFileToString(mergedFile, "UTF-8")).isEqualTo("Single chunk content");
        }

        @Test
        @DisplayName("mergeChunkFile_应该成功合并多个文件块_当存在多个块时")
        void mergeChunkFile_shouldSuccessfullyMergeMultipleChunks_whenMultipleChunksExist() throws IOException {
            // Arrange
            File chunkFile1 = new File(chunkDir, "0");
            FileUtils.writeStringToFile(chunkFile1, "Chunk 1 content", "UTF-8");
            File chunkFile2 = new File(chunkDir, "1");
            FileUtils.writeStringToFile(chunkFile2, "Chunk 2 content", "UTF-8");

            // Act
            File mergedFile = WebUploaderUtil.mergeChunkFile(chunkDir.getAbsolutePath(), destFile.getAbsolutePath());

            // Assert
            assertThat(mergedFile).exists();
            assertThat(FileUtils.readFileToString(mergedFile, "UTF-8")).isEqualTo("Chunk 1 contentChunk 2 content");
        }

        @Test
        @DisplayName("mergeChunkFile_应该正确合并上传的文件块_当使用uploadChunkFile方法时")
        void mergeChunkFile_shouldCorrectlyMergeUploadedChunks_whenUsingUploadChunkFileMethod() throws IOException {
            // Arrange
            WebUploaderUtil.uploadChunkFile(0, chunkDir.getAbsolutePath(), new ByteArrayInputStream("Chunk".getBytes()));
            WebUploaderUtil.uploadChunkFile(1, chunkDir.getAbsolutePath(), new ByteArrayInputStream("Chunk".getBytes()));
            WebUploaderUtil.uploadChunkFile(2, chunkDir.getAbsolutePath(), new ByteArrayInputStream("Chunk".getBytes()));

            // Act
            File mergedFile = WebUploaderUtil.mergeChunkFile(chunkDir.getAbsolutePath(), destFile.getAbsolutePath());

            // Assert
            assertThat(mergedFile).exists();
            assertThat(FileUtils.readFileToString(mergedFile, "UTF-8")).isEqualTo("ChunkChunkChunk");
        }
    }

    @Nested
    @DisplayName("文件块上传测试")
    class ChunkUploadTests {

        @Test
        @DisplayName("uploadChunkFile_应该成功上传文件块_当提供有效输入流时")
        void uploadChunkFile_shouldSuccessfullyUploadChunk_whenValidInputStreamProvided() throws IOException {
            // Arrange
            InputStream inputStream = new ByteArrayInputStream("Chunk content".getBytes());

            // Act
            WebUploaderUtil.uploadChunkFile(0, chunkDir.getAbsolutePath(), inputStream);

            // Assert
            File uploadedFile = new File(chunkDir, "0");
            assertThat(uploadedFile).exists();
            assertThat(FileUtils.readFileToString(uploadedFile, "UTF-8")).isEqualTo("Chunk content");
        }
    }

    @Nested
    @DisplayName("文件分割测试")
    class FileSplitTests {

        @Test
        @DisplayName("splitFile_应该正确分割文件_当指定块大小时")
        void splitFile_shouldCorrectlySplitFile_whenChunkSizeSpecified() throws Exception {
            // Arrange
            File sourceFile = tempDir.resolve("sourceFile.txt").toFile();
            FileUtils.writeStringToFile(sourceFile, "This is a large file content", "UTF-8");
            File destDir = tempDir.resolve("splitFiles").toFile();

            // Act
            WebUploaderUtil.splitFile(sourceFile.getAbsolutePath(), 10, destDir.getAbsolutePath());

            // Assert
            File[] splitFiles = destDir.listFiles();
            assertThat(splitFiles).isNotNull().hasSize(3);
            Arrays.sort(splitFiles, Comparator.comparing(File::getName));
            assertThat(FileUtils.readFileToString(splitFiles[0], "UTF-8")).isEqualTo("This is a ");
            assertThat(FileUtils.readFileToString(splitFiles[1], "UTF-8")).isEqualTo("large file");
            assertThat(FileUtils.readFileToString(splitFiles[2], "UTF-8")).isEqualTo(" content");
        }
    }
}