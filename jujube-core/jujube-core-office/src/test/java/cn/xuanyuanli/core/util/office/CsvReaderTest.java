package cn.xuanyuanli.core.util.office;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.util.Resources;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

    @Test
    public void getRow() {
        Assertions.assertThat(fileReader.getRow(1)).hasSize(4);
        Assertions.assertThat(inputstreamReader.getRow(1)).hasSize(4);
    }

    @Test
    public void getRows() {
        Assertions.assertThat(fileReader.getRows()).hasSize(8);
        Assertions.assertThat(fileReader.getRows().get(7)).hasSize(4);
        Assertions.assertThat(inputstreamReader.getRows()).hasSize(8);
        Assertions.assertThat(inputstreamReader.getRows().get(7)).hasSize(4);
    }

    @Test
    public void iterator() {
        Assertions.assertThat(fileReader.iterator().next()).hasSize(4);
        Assertions.assertThatThrownBy(() -> fileReader.iterator().remove()).isInstanceOf(RuntimeException.class).hasMessage("not execute remove");
    }

    @Test
    public void getRowCount() {
        Assertions.assertThat(fileReader.getRowCount()).isEqualTo(8);
        Assertions.assertThat(inputstreamReader.getRowCount()).isEqualTo(8);
    }

    @Test
    public void testConstructorWithNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.csv");
        Assertions.assertThatThrownBy(() -> new CsvReader(nonExistentFile, Charsets.GBK, ExcelReaderConfig.DEFAULT))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("file not exists");
    }

    @Test
    public void testConstructorWithInvalidInputStream() {
        InputStream invalidInputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated IO error");
            }
        };
        Assertions.assertThat(new CsvReader(invalidInputStream, Charsets.GBK, ExcelReaderConfig.DEFAULT).getRowCount()).isEqualTo(0);
    }

    @Test
    public void testTrimCellContentFalse() throws IOException {
        ExcelReaderConfig config = new ExcelReaderConfig(false, false);
        config.setTrimCellContent(false);
        CsvReader reader = new CsvReader(Objects.requireNonNull(Resources.getClassPathResources("META-INF/office/testTrimCell.csv")).getFile(), Charsets.GBK, config);
        List<String> row = reader.getRow(0);
        Assertions.assertThat(row).containsExactly("  cell1  ", "cell2", "", "");
    }

    @Test
    public void testIteratorHasNextFalse() {
        Iterator<List<String>> iterator = fileReader.iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }
        Assertions.assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }

}
