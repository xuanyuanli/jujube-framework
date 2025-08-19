package cn.xuanyuanli.core.util.office;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
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
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Resources;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

    @Test
    void writeExcelWithTemplate() throws IOException {
        List<List<String>> lines = new ArrayList<>();
        lines.add(Lists.newArrayList("title1", "title2", "title3"));
        lines.add(Lists.newArrayList("1", "a", "#"));
        File destFile = new File(SystemProperties.TMPDIR, SnowFlakes.nextId() + "-ExcelWriterTest.xlsx");
        ExcelWriter.writeExcelWithTemplate(TEMPLATE_FILE.getAbsolutePath(), destFile.getAbsolutePath(), 1, lines,
                sheet -> ExcelWriter.setValidation(sheet, new String[]{"op1", "op2"}, 0, 0, 3, 3));
        Assertions.assertThat(destFile).exists();

        ExcelReader reader;
        try (FileInputStream inputStream = FileUtils.openInputStream(destFile)) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        }
        Assertions.assertThat(reader.getRow(0)).hasSize(12);
        Assertions.assertThat(reader.getRow(1)).hasSize(3);
        Assertions.assertThat(reader.getRow(2)).hasSize(3);
        Assertions.assertThat(reader.getRow(1).get(0)).isEqualTo("title1");
        Assertions.assertThat(reader.getRow(1).get(1)).isEqualTo("title2");
        Assertions.assertThat(reader.getRow(1).get(2)).isEqualTo("title3");
        Assertions.assertThat(reader.getRow(2).get(0)).isEqualTo("1");
        Assertions.assertThat(reader.getRow(2).get(1)).isEqualTo("a");
        Assertions.assertThat(reader.getRow(2).get(2)).isEqualTo("#");
        destFile.deleteOnExit();
    }

    @Test
    void generateExcel() throws IOException {
        List<List<String>> lines = new ArrayList<>();
        lines.add(Lists.newArrayList("title1", "title2", "title3"));
        lines.add(Lists.newArrayList("1", "a", "#"));
        File destFile = new File(SystemProperties.TMPDIR, SnowFlakes.nextId() + "-ExcelWriterTest.xlsx");
        ExcelWriter.generateExcel(destFile, lines);
        ExcelReader reader;
        try (FileInputStream inputStream = FileUtils.openInputStream(destFile)) {
            reader = new ExcelReader(inputStream, 0, ExcelReaderConfig.DEFAULT);
        }
        Assertions.assertThat(reader.getRow(0)).hasSize(3);
        Assertions.assertThat(reader.getRow(1)).hasSize(3);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("title1");
        Assertions.assertThat(reader.getRow(0).get(1)).isEqualTo("title2");
        Assertions.assertThat(reader.getRow(0).get(2)).isEqualTo("title3");
        Assertions.assertThat(reader.getRow(1).get(0)).isEqualTo("1");
        Assertions.assertThat(reader.getRow(1).get(1)).isEqualTo("a");
        Assertions.assertThat(reader.getRow(1).get(2)).isEqualTo("#");
        destFile.deleteOnExit();
    }

    @Test
    void generateCsv() {
        List<List<String>> lines = new ArrayList<>();
        lines.add(Lists.newArrayList("title1", "title2", "title3"));
        lines.add(Lists.newArrayList("中国", "a", "#"));
        File destFile = new File(SystemProperties.TMPDIR, SnowFlakes.nextId() + "-ExcelWriterTest.xlsx");
        ExcelWriter.generateCsv(destFile, lines);
        CsvReader reader = new CsvReader(destFile, Charsets.GBK, ExcelReaderConfig.DEFAULT);
        Assertions.assertThat(reader.getRow(0)).hasSize(3);
        Assertions.assertThat(reader.getRow(1)).hasSize(3);
        Assertions.assertThat(reader.getRow(0).get(0)).isEqualTo("title1");
        Assertions.assertThat(reader.getRow(0).get(1)).isEqualTo("title2");
        Assertions.assertThat(reader.getRow(0).get(2)).isEqualTo("title3");
        Assertions.assertThat(reader.getRow(1).get(0)).isEqualTo("中国");
        Assertions.assertThat(reader.getRow(1).get(1)).isEqualTo("a");
        Assertions.assertThat(reader.getRow(1).get(2)).isEqualTo("#");
        destFile.deleteOnExit();
    }

    @Test
    void generateCsvInputStream() throws IOException {
        List<List<String>> lines = new ArrayList<>();
        lines.add(Lists.newArrayList("title1", "title2", "title3"));
        lines.add(Lists.newArrayList("中国", "a", "#"));
        InputStream inputStream = ExcelWriter.generateCsvInputStream(lines);
        Assertions.assertThat(inputStream).isNotNull();
        Assertions.assertThat(IOUtils.toByteArray(inputStream).length).isEqualTo(29);
        inputStream.close();
    }

    @Test
    void getLinesFromEntities() {
        WriterEntity entity = new WriterEntity();
        entity.setAuctioneerName("保利");
        entity.setBeginTime(1602536230L);
        entity.setCommission(52012.63D);
        List<List<String>> lines = ExcelWriter.getLinesFromEntities(Lists.newArrayList(entity), true);
        Assertions.assertThat(lines.toString()).isEqualTo("[[拍卖行名称, 开始时间, 佣金], [保利, 2020-10-13 04:57:10, 52012.63]]");
    }

    @Test
    void setCellImageValue() throws IOException {
        List<List<String>> lines = new ArrayList<>();
        lines.add(List.of("name", "image:https://img.auctionhome.cn/static/images/auctionhome/login.png"));
        try (InputStream inputStream = ExcelWriter.generateExcelInputStream(lines, true)) {
            assertTrue(IOUtils.toByteArray(Objects.requireNonNull(inputStream)).length >= 6119);
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
