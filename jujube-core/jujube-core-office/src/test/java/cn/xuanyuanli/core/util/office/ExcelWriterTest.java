package cn.xuanyuanli.core.util.office;

import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import static org.assertj.core.api.Assertions.assertThat;

import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Resources;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Excel写入器测试")
class ExcelWriterTest {

    static final File TEMPLATE_FILE = new File(SystemProperties.TMPDIR, "ExcelWriterTest-excelTemplate.xlsx");

    @BeforeAll
    static void init() throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(Resources.getClassPathResources("META-INF/office/excelTemplate.xlsx"))
                .getInputStream(); FileOutputStream output = new FileOutputStream(TEMPLATE_FILE)) {
            if (!TEMPLATE_FILE.exists()) {
                //noinspection ResultOfMethodCallIgnored
                TEMPLATE_FILE.createNewFile();
            }
            IOUtils.copy(inputStream, output);
        }
    }

    @AfterAll
    static void destroy() {
        TEMPLATE_FILE.deleteOnExit();
    }

    @Nested
    @DisplayName("Excel文件生成功能测试")
    class ExcelGenerationTests {

        @Test
        @DisplayName("基于模板写入Excel应生成正确内容")
        void writeExcelWithTemplate_shouldGenerateCorrectContent_whenUsingTemplate() throws IOException {
            // Arrange
            List<List<String>> lines = new ArrayList<>();
            lines.add(new ArrayList<>(Arrays.asList("title1", "title2", "title3")));
            lines.add(new ArrayList<>(Arrays.asList("1", "a", "#")));
            File destFile = new File(SystemProperties.TMPDIR, SnowFlakes.nextId() + "-ExcelWriterTest.xlsx");

            // Act
            ExcelWriter.writeExcelWithTemplate(TEMPLATE_FILE.getAbsolutePath(), destFile.getAbsolutePath(), 1, lines,
                    sheet -> ExcelWriter.setValidation(sheet, new String[]{"op1", "op2"}, 0, 0, 3, 3));

            // Assert
            assertThat(destFile).exists();

            ExcelReader reader;
            try (FileInputStream inputStream = FileUtils.openInputStream(destFile)) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
            }
            assertThat(reader.getRow(0)).hasSize(12);
            assertThat(reader.getRow(1)).hasSize(3);
            assertThat(reader.getRow(2)).hasSize(3);
            assertThat(reader.getRow(1).get(0)).isEqualTo("title1");
            assertThat(reader.getRow(1).get(1)).isEqualTo("title2");
            assertThat(reader.getRow(1).get(2)).isEqualTo("title3");
            assertThat(reader.getRow(2).get(0)).isEqualTo("1");
            assertThat(reader.getRow(2).get(1)).isEqualTo("a");
            assertThat(reader.getRow(2).get(2)).isEqualTo("#");
            destFile.deleteOnExit();
        }

        @Test
        @DisplayName("直接生成Excel应创建正确格式")
        void generateExcel_shouldCreateCorrectFormat_whenGeneratingDirectly() throws IOException {
            // Arrange
            List<List<String>> lines = new ArrayList<>();
            lines.add(new ArrayList<>(Arrays.asList("title1", "title2", "title3")));
            lines.add(new ArrayList<>(Arrays.asList("1", "a", "#")));
            File destFile = new File(SystemProperties.TMPDIR, SnowFlakes.nextId() + "-ExcelWriterTest.xlsx");

            // Act
            ExcelWriter.generateExcel(destFile, lines);

            // Assert
            ExcelReader reader;
            try (FileInputStream inputStream = FileUtils.openInputStream(destFile)) {
                reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
            }
            assertThat(reader.getRow(0)).hasSize(3);
            assertThat(reader.getRow(1)).hasSize(3);
            assertThat(reader.getRow(0).get(0)).isEqualTo("title1");
            assertThat(reader.getRow(0).get(1)).isEqualTo("title2");
            assertThat(reader.getRow(0).get(2)).isEqualTo("title3");
            assertThat(reader.getRow(1).get(0)).isEqualTo("1");
            assertThat(reader.getRow(1).get(1)).isEqualTo("a");
            assertThat(reader.getRow(1).get(2)).isEqualTo("#");
            destFile.deleteOnExit();
        }

        @Test
        @DisplayName("设置图片单元格值应生成包含图片的Excel")
        void setCellImageValue_shouldGenerateExcelWithImage_whenImageUrlProvided() throws IOException {
            // Arrange
            List<List<String>> lines = new ArrayList<>();
            lines.add(List.of("name", "image:https://img.auctionhome.cn/static/images/auctionhome/login.png"));

            // Act & Assert
            try (InputStream inputStream = ExcelWriter.generateExcelInputStream(lines, true)) {
                assertThat(IOUtils.toByteArray(Objects.requireNonNull(inputStream)).length).isGreaterThanOrEqualTo(6119);
            }
        }
    }

    @Nested
    @DisplayName("CSV文件生成功能测试")
    class CsvGenerationTests {

        @Test
        @DisplayName("生成CSV文件应正确处理中文内容")
        void generateCsv_shouldHandleChineseContent_whenGeneratingCsv() {
            // Arrange
            List<List<String>> lines = new ArrayList<>();
            lines.add(new ArrayList<>(Arrays.asList("title1", "title2", "title3")));
            lines.add(new ArrayList<>(Arrays.asList("中国", "a", "#")));
            File destFile = new File(SystemProperties.TMPDIR, SnowFlakes.nextId() + "-ExcelWriterTest.xlsx");

            // Act
            ExcelWriter.generateCsv(destFile, lines);

            // Assert
            CsvReader reader = new CsvReader(destFile, Charsets.GBK, ExcelReaderConfig.DEFAULT);
            assertThat(reader.getRow(0)).hasSize(3);
            assertThat(reader.getRow(1)).hasSize(3);
            assertThat(reader.getRow(0).get(0)).isEqualTo("title1");
            assertThat(reader.getRow(0).get(1)).isEqualTo("title2");
            assertThat(reader.getRow(0).get(2)).isEqualTo("title3");
            assertThat(reader.getRow(1).get(0)).isEqualTo("中国");
            assertThat(reader.getRow(1).get(1)).isEqualTo("a");
            assertThat(reader.getRow(1).get(2)).isEqualTo("#");
            destFile.deleteOnExit();
        }

        @Test
        @DisplayName("生成CSV输入流应返回正确字节长度")
        void generateCsvInputStream_shouldReturnCorrectByteLength_whenGeneratingInputStream() throws IOException {
            // Arrange
            List<List<String>> lines = new ArrayList<>();
            lines.add(new ArrayList<>(Arrays.asList("title1", "title2", "title3")));
            lines.add(new ArrayList<>(Arrays.asList("中国", "a", "#")));

            // Act
            InputStream inputStream = ExcelWriter.generateCsvInputStream(lines);

            // Assert
            assertThat(inputStream).isNotNull();
            assertThat(IOUtils.toByteArray(inputStream).length).isEqualTo(29);
            inputStream.close();
        }
    }

    @Nested
    @DisplayName("实体转换功能测试")
    class EntityConversionTests {

        @Test
        @DisplayName("从实体对象获取行数据应正确映射字段")
        void getLinesFromEntities_shouldCorrectlyMapFields_whenConvertingFromEntities() {
            // Arrange
            WriterEntity entity = new WriterEntity();
            entity.setAuctioneerName("保利");
            entity.setBeginTime(1602536230L);
            entity.setCommission(52012.63D);

            // Act
            List<List<String>> lines = ExcelWriter.getLinesFromEntities(new ArrayList<>(List.of(entity)), true);

            // Assert
            assertThat(lines.toString()).isEqualTo("[[拍卖行名称, 开始时间, 佣金], [保利, 2020-10-13 04:57:10, 52012.63]]");
        }
    }

    @Data
    @Accessors(chain = true)
    public static class WriterEntity implements BaseEntity {

        @ExcelField("拍卖行名称")
        private String auctioneerName;
        @ExcelField(value = "开始时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
        private Long beginTime;
        @ExcelField(value = "佣金", numberFormat = "##.##")
        private Double commission;
    }
}
