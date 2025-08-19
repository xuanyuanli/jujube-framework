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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    @Test
    void testMergeChunkFile_SingleChunk() throws IOException {
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
    void testMergeChunkFile_MultipleChunks() throws IOException {
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
    void testMergeChunkFile_MultipleChunks2() throws IOException {
        // Act
        WebUploaderUtil.uploadChunkFile(0, chunkDir.getAbsolutePath(), new ByteArrayInputStream("Chunk".getBytes()));
        WebUploaderUtil.uploadChunkFile(1, chunkDir.getAbsolutePath(), new ByteArrayInputStream("Chunk".getBytes()));
        WebUploaderUtil.uploadChunkFile(2, chunkDir.getAbsolutePath(), new ByteArrayInputStream("Chunk".getBytes()));

        // Act
        File mergedFile = WebUploaderUtil.mergeChunkFile(chunkDir.getAbsolutePath(), destFile.getAbsolutePath());

        // Assert
        assertThat(mergedFile).exists();
        assertThat(FileUtils.readFileToString(mergedFile, "UTF-8")).isEqualTo("ChunkChunkChunk");
    }

    @Test
    void testUploadChunkFile() throws IOException {
        // Arrange
        InputStream inputStream = new ByteArrayInputStream("Chunk content".getBytes());

        // Act
        WebUploaderUtil.uploadChunkFile(0, chunkDir.getAbsolutePath(), inputStream);

        // Assert
        File uploadedFile = new File(chunkDir, "0");
        assertThat(uploadedFile).exists();
        assertThat(FileUtils.readFileToString(uploadedFile, "UTF-8")).isEqualTo("Chunk content");
    }

    @Test
    void testSplitFile() throws Exception {
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
