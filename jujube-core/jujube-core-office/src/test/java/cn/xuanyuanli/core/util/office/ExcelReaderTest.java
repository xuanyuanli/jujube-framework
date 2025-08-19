package cn.xuanyuanli.core.util.office;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.util.Resources;
import cn.xuanyuanli.core.util.office.ExcelReader.ExcelColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

@Slf4j
public class ExcelReaderTest {

    @Test
    public void testRealCount() {
        Resource resource = Resources.getClassPathResources("META-INF/office/testRealCount.xlsx");
        ExcelReader reader = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(Objects.requireNonNull(reader).getRowCount()).isEqualTo(9);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.ALL_RIGHT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRowCount()).isEqualTo(4);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        try (FileInputStream inputStream = FileUtils.openInputStream(resource.getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.ALL_FALSE);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(reader.getRowCount()).isEqualTo(9);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
    }

    @Test
    public void testNumeric() {
        Resource resource = Resources.getClassPathResources("META-INF/office/testNumeric.xlsx");
        ExcelReader reader = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(Objects.requireNonNull(reader).getRow(1).get(1)).isEqualTo("13661162128");
        Assertions.assertThat(reader.getRow(2).get(1)).isEqualTo("13695597788");
        Assertions.assertThat(reader.getRow(3).get(1)).isEqualTo("13706274444");
    }

    @Test
    public void testFormula() {
        Resource resource = Resources.getClassPathResources("META-INF/office/testFormula.xlsx");
        ExcelReader reader = null;
        try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
        Assertions.assertThat(Objects.requireNonNull(reader).getRow(1).get(6)).isEqualTo("150");
        Assertions.assertThat(reader.getRow(4).get(6)).isEqualTo("150.0");
    }

    static ExcelReader READER;

    @BeforeAll
    static void init() {
        Resource resource = Resources.getClassPathResources("META-INF/office/testRead.xlsx");
        try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
            READER = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        } catch (IOException e) {
            log.error("read", e);
        }
    }

    @Test
    void first() {
        Assertions.assertThat(READER.first()).hasSize(4);
        Assertions.assertThat(READER.first().get(0)).isEqualTo("拍卖行名称");
        Assertions.assertThat(READER.first().get(3)).isEqualTo("开拍时间");
    }

    @Test
    void last() {
        Assertions.assertThat(READER.last()).hasSize(4);
        Assertions.assertThat(READER.last().get(0)).isEqualTo("Chiswick Auctions");
        Assertions.assertThat(READER.last().get(3)).isEqualTo("2022-05-16 17:00:00");
    }

    @Test
    void getRows() {
        Assertions.assertThat(READER.getRowCount()).isEqualTo(8);
        Assertions.assertThat(READER.getRows()).hasSize(8);
        Assertions.assertThat(READER.getRows().get(7).get(0)).isEqualTo("Chiswick Auctions");
        Assertions.assertThat(READER.getRows().get(7).get(3)).isEqualTo("2022-05-16 17:00:00");
    }

    @Test
    void toEntity() {
        List<ReaderEntity> entities = READER.toEntity(ReaderEntity.class, true);
        Assertions.assertThat(entities).hasSize(7);
        Assertions.assertThat(entities.get(6).getName1()).isEqualTo("Chiswick Auctions");
        Assertions.assertThat(entities.get(6).getName2()).isEqualTo("中国单色艺术");
        Assertions.assertThat(entities.get(6).getName3()).isEqualTo("中国单色艺术");
        Assertions.assertThat(entities.get(6).getTime()).isEqualTo("2022-05-16 17:00:00");
    }

    @Test
    void iterator() {
        Assertions.assertThatThrownBy(()-> READER.iterator().remove()).isInstanceOf(RuntimeException.class ).hasMessage("not execute remove");
    }

    @Test
    void setCellContentHandle(){
        READER.setCellContentHandle(Cell::getStringCellValue);
        Assertions.assertThat(READER.getRow(0).get(0)).isEqualTo("拍卖行名称");
        READER.setCellContentHandle(null);
    }

    @Test
    void getCellStyles(){
        List<CellStyle> cellStyles = READER.getCellStyles();
        Assertions.assertThat(cellStyles).hasSize(4);
    }

    @Test
    void getCellStyleRow(){
        List<CellStyle> cellStyles = READER.getCellStyles(0);
        Assertions.assertThat(cellStyles).hasSize(4);
    }

    @Data
    public static class ReaderEntity {

        @ExcelColumn(0)
        private String name1;
        @ExcelColumn(1)
        private String name2;
        @ExcelColumn(2)
        private String name3;
        @ExcelColumn(3)
        private String time;
    }
}
