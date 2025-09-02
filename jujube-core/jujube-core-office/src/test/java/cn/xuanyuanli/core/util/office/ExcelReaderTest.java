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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import cn.xuanyuanli.core.util.Resources;
import cn.xuanyuanli.core.util.office.ExcelReader.ExcelColumn;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;

@Slf4j
@DisplayName("Excel读取器测试")
public class ExcelReaderTest {

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

    @Nested
    @DisplayName("不同配置的行数统计测试")
    class RowCountWithConfigTests {

        @Test
        @DisplayName("默认配置应返回所有行数")
        void testRealCount_shouldReturnAllRows_whenDefaultConfig() {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/office/testRealCount.xlsx");
            ExcelReader reader = null;

            // Act
            try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
            } catch (IOException e) {
                log.error("read", e);
            }

            // Assert
            assertThat(Objects.requireNonNull(reader).getRowCount()).isEqualTo(9);
            assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        }

        @Test
        @DisplayName("ALL_RIGHT配置应返回有效行数")
        void testRealCount_shouldReturnValidRows_whenAllRightConfig() {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/office/testRealCount.xlsx");
            ExcelReader reader = null;

            // Act
            try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.ALL_RIGHT);
            } catch (IOException e) {
                log.error("read", e);
            }

            // Assert
            assertThat(Objects.requireNonNull(reader).getRowCount()).isEqualTo(4);
            assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        }

        @Test
        @DisplayName("ALL_FALSE配置应返回所有行数")
        void testRealCount_shouldReturnAllRows_whenAllFalseConfig() {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/office/testRealCount.xlsx");
            ExcelReader reader = null;

            // Act
            try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.ALL_FALSE);
            } catch (IOException e) {
                log.error("read", e);
            }

            // Assert
            assertThat(Objects.requireNonNull(reader).getRowCount()).isEqualTo(9);
            assertThat(reader.getRow(0).get(0)).isEqualTo("ID");
        }
    }

    @Nested
    @DisplayName("数值格式处理测试")
    class NumericFormatTests {

        @Test
        @DisplayName("数值单元格应保持完整格式")
        void testNumeric_shouldPreserveNumericFormat_whenReadingPhoneNumbers() {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/office/testNumeric.xlsx");
            ExcelReader reader = null;

            // Act
            try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
            } catch (IOException e) {
                log.error("read", e);
            }

            // Assert
            assertThat(Objects.requireNonNull(reader).getRow(1).get(1)).isEqualTo("13661162128");
            assertThat(reader.getRow(2).get(1)).isEqualTo("13695597788");
            assertThat(reader.getRow(3).get(1)).isEqualTo("13706274444");
        }
    }

    @Nested
    @DisplayName("公式处理测试")
    class FormulaTests {

        @Test
        @DisplayName("公式单元格应返回计算结果")
        void testFormula_shouldReturnCalculatedValue_whenReadingFormulaCells() {
            // Arrange
            Resource resource = Resources.getClassPathResources("META-INF/office/testFormula.xlsx");
            ExcelReader reader = null;

            // Act
            try (FileInputStream inputStream = FileUtils.openInputStream(Objects.requireNonNull(resource).getFile())) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
            } catch (IOException e) {
                log.error("read", e);
            }

            // Assert
            assertThat(Objects.requireNonNull(reader).getRow(1).get(6)).isEqualTo("150");
            assertThat(reader.getRow(4).get(6)).isEqualTo("150.0");
        }
    }

    @Nested
    @DisplayName("基础数据读取功能测试")
    class BasicReadingTests {

        @Test
        @DisplayName("获取第一行数据应返回正确内容")
        void first_shouldReturnCorrectFirstRow_whenReadingData() {
            // Arrange & Act
            List<String> firstRow = READER.first();

            // Assert
            assertThat(firstRow).hasSize(4);
            assertThat(firstRow.get(0)).isEqualTo("拍卖行名称");
            assertThat(firstRow.get(3)).isEqualTo("开拍时间");
        }

        @Test
        @DisplayName("获取最后一行数据应返回正确内容")
        void last_shouldReturnCorrectLastRow_whenReadingData() {
            // Arrange & Act
            List<String> lastRow = READER.last();

            // Assert
            assertThat(lastRow).hasSize(4);
            assertThat(lastRow.get(0)).isEqualTo("Chiswick Auctions");
            assertThat(lastRow.get(3)).isEqualTo("2022-05-16 17:00:00");
        }

        @Test
        @DisplayName("获取所有行数据应返回正确的行数和内容")
        void getRows_shouldReturnCorrectRowsAndContent_whenReadingAllData() {
            // Arrange & Act
            List<List<String>> allRows = READER.getRows();
            int rowCount = READER.getRowCount();

            // Assert
            assertThat(rowCount).isEqualTo(8);
            assertThat(allRows).hasSize(8);
            assertThat(allRows.get(7).get(0)).isEqualTo("Chiswick Auctions");
            assertThat(allRows.get(7).get(3)).isEqualTo("2022-05-16 17:00:00");
        }
    }

    @Nested
    @DisplayName("实体转换功能测试")
    class EntityConversionTests {

        @Test
        @DisplayName("转换为实体对象应返回正确映射结果")
        void toEntity_shouldReturnCorrectMappedEntities_whenConvertingToEntityClass() {
            // Arrange & Act
            List<ReaderEntity> entities = READER.toEntity(ReaderEntity.class, true);

            // Assert
            assertThat(entities).hasSize(7);
            assertThat(entities.get(6).getName1()).isEqualTo("Chiswick Auctions");
            assertThat(entities.get(6).getName2()).isEqualTo("中国单色艺术");
            assertThat(entities.get(6).getName3()).isEqualTo("中国单色艺术");
            assertThat(entities.get(6).getTime()).isEqualTo("2022-05-16 17:00:00");
        }
    }

    @Nested
    @DisplayName("迭代器功能测试")
    class IteratorTests {

        @Test
        @DisplayName("迭代器删除操作应抛出异常")
        void iterator_shouldThrowException_whenRemoveOperationCalled() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> READER.iterator().remove())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("not execute remove");
        }
    }

    @Nested
    @DisplayName("单元格内容处理功能测试")
    class CellContentHandlingTests {

        @Test
        @DisplayName("设置单元格内容处理器应正确处理数据")
        void setCellContentHandle_shouldProcessCellContentCorrectly_whenCustomHandlerSet() {
            // Arrange
            READER.setCellContentHandle(Cell::getStringCellValue);

            // Act
            String cellValue = READER.getRow(0).get(0);

            // Assert
            assertThat(cellValue).isEqualTo("拍卖行名称");

            // Cleanup
            READER.setCellContentHandle(null);
        }
    }

    @Nested
    @DisplayName("单元格样式功能测试")
    class CellStyleTests {

        @Test
        @DisplayName("获取所有单元格样式应返回正确数量")
        void getCellStyles_shouldReturnCorrectStyleCount_whenGettingAllStyles() {
            // Arrange & Act
            List<CellStyle> cellStyles = READER.getCellStyles();

            // Assert
            assertThat(cellStyles).hasSize(4);
        }

        @Test
        @DisplayName("获取指定行单元格样式应返回正确数量")
        void getCellStyles_shouldReturnCorrectStyleCount_whenGettingRowStyles() {
            // Arrange & Act
            List<CellStyle> cellStyles = READER.getCellStyles(0);

            // Assert
            assertThat(cellStyles).hasSize(4);
        }
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
