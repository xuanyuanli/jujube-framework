package cn.xuanyuanli.core.util.office;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.util.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CSV读取器测试")
public class CsvReaderTest {

    private static CsvReader fileReader;
    private static CsvReader inputstreamReader;

    @BeforeAll
    static void init() throws IOException {
        String path = "META-INF/office/testRead.csv";
        fileReader = new CsvReader(Objects.requireNonNull(Resources.getClassPathResources(path)).getFile(), Charsets.GBK, ExcelReaderConfig.DEFAULT);
        try (InputStream classPathResourcesInputStream = Resources.getClassPathResourcesInputStream(path)) {
            inputstreamReader = new CsvReader(classPathResourcesInputStream, Charsets.GBK, ExcelReaderConfig.DEFAULT);
        }
    }

    @Nested
    @DisplayName("数据读取功能测试")
    class DataReadingTests {

        @Test
        @DisplayName("获取指定行数据应返回正确行数")
        void getRow_shouldReturnCorrectRowSize_whenValidRowIndex() {
            // Arrange & Act
            List<String> fileRow = fileReader.getRow(1);
            List<String> inputStreamRow = inputstreamReader.getRow(1);

            // Assert
            assertThat(fileRow).hasSize(4);
            assertThat(inputStreamRow).hasSize(4);
        }

        @Test
        @DisplayName("获取所有行数据应返回正确的行数和列数")
        void getRows_shouldReturnCorrectRowsAndColumns_whenReadingAllData() {
            // Arrange & Act
            List<List<String>> fileRows = fileReader.getRows();
            List<List<String>> inputStreamRows = inputstreamReader.getRows();

            // Assert
            assertThat(fileRows).hasSize(8);
            assertThat(fileRows.get(7)).hasSize(4);
            assertThat(inputStreamRows).hasSize(8);
            assertThat(inputStreamRows.get(7)).hasSize(4);
        }

        @Test
        @DisplayName("获取行数应返回正确的总行数")
        void getRowCount_shouldReturnCorrectCount_whenCountingAllRows() {
            // Arrange & Act
            int fileRowCount = fileReader.getRowCount();
            int inputStreamRowCount = inputstreamReader.getRowCount();

            // Assert
            assertThat(fileRowCount).isEqualTo(8);
            assertThat(inputStreamRowCount).isEqualTo(8);
        }
    }

    @Nested
    @DisplayName("迭代器功能测试")
    class IteratorTests {

        @Test
        @DisplayName("迭代器应正确返回数据并禁止删除操作")
        void iterator_shouldReturnDataAndThrowExceptionOnRemove_whenIterating() {
            // Arrange
            Iterator<List<String>> iterator = fileReader.iterator();

            // Act & Assert
            assertThat(iterator.next()).hasSize(4);
            assertThatThrownBy(() -> iterator.remove())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not execute remove");
        }

        @Test
        @DisplayName("迭代器遍历完成后应抛出异常")
        void iterator_shouldThrowException_whenIteratingBeyondEnd() {
            // Arrange
            Iterator<List<String>> iterator = fileReader.iterator();
            while (iterator.hasNext()) {
                iterator.next();
            }

            // Act & Assert
            assertThatThrownBy(iterator::next)
                .isInstanceOf(NoSuchElementException.class);
        }
    }

    @Nested
    @DisplayName("构造器异常测试")
    class ConstructorExceptionTests {

        @Test
        @DisplayName("使用不存在的文件应抛出异常")
        void constructor_shouldThrowException_whenFileNotExists() {
            // Arrange
            File nonExistentFile = new File("non_existent_file.csv");

            // Act & Assert
            assertThatThrownBy(() -> new CsvReader(nonExistentFile, Charsets.GBK, ExcelReaderConfig.DEFAULT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("file not exists");
        }

        @Test
        @DisplayName("使用无效输入流应返回空数据")
        void constructor_shouldReturnEmptyData_whenInvalidInputStream() {
            // Arrange
            InputStream invalidInputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("Simulated IO error");
                }
            };

            // Act
            CsvReader reader = new CsvReader(invalidInputStream, Charsets.GBK, ExcelReaderConfig.DEFAULT);
            int rowCount = reader.getRowCount();

            // Assert
            assertThat(rowCount).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("配置功能测试")
    class ConfigTests {

        @Test
        @DisplayName("关闭单元格内容修剪功能应保留原始空格")
        void trimCellContent_shouldPreserveWhitespace_whenTrimDisabled() throws IOException {
            // Arrange
            ExcelReaderConfig config = new ExcelReaderConfig(false, false);
            config.setTrimCellContent(false);
            CsvReader reader = new CsvReader(
                Objects.requireNonNull(Resources.getClassPathResources("META-INF/office/testTrimCell.csv")).getFile(),
                Charsets.GBK,
                config
            );

            // Act
            List<String> row = reader.getRow(0);

            // Assert
            assertThat(row).containsExactly("  cell1  ", "cell2", "", "");
        }
    }
}
