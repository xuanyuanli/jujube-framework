package cn.xuanyuanli.core.util.office;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import kong.unirest.core.Unirest;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFDrawing;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import cn.xuanyuanli.core.constant.Charsets;
import cn.xuanyuanli.core.constant.SystemProperties;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Dates;
import cn.xuanyuanli.core.util.Files;
import cn.xuanyuanli.core.util.Numbers;
import cn.xuanyuanli.core.util.Texts;
import cn.xuanyuanli.core.util.snowflake.SnowFlakes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Excel写入工具类
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 */
public class ExcelWriter {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(ExcelWriter.class);
    /**
     * 最大长度
     */
    private static final int MAXIMUM_LENGTH = 32767;
    /**
     * 图像前缀
     */
    private static final String IMAGE_PREFIX = "image:";

    /**
     * 优秀作家
     */
    private ExcelWriter() {
    }

    /**
     * 根据模板写入Excel
     *
     * @param templateFilePath 模板文件路径
     * @param destFileName     要写入的文件路径
     * @param copyLineIndex    设定保留模板前几行
     * @param lines            数据
     * @param sheetHandler     单处理程序
     */
    public static void writeExcelWithTemplate(String templateFilePath, String destFileName, int copyLineIndex, List<List<String>> lines,
            ExcelSheetHandler sheetHandler) {
        FileOutputStream out = null;
        FileInputStream templateInput = null;
        File destFile;
        // 解决线程同步问题，创建一个不可能冲突的文件
        File templateFile = new File(SystemProperties.TMPDIR, "/ExcelWriterTemplate-" + SnowFlakes.nextId() + Files.getExtention(templateFilePath));
        try {
            FileUtils.copyFile(new File(templateFilePath), templateFile);
            destFile = new File(destFileName);
            templateInput = new FileInputStream(templateFile);
            Workbook workbook = WorkbookFactory.create(templateInput);
            Sheet sheet = workbook.getSheetAt(0);
            if (sheetHandler != null) {
                sheetHandler.handler(sheet);
            }
            for (int i = 0; i < lines.size(); i++) {
                List<String> vRow = lines.get(i);
                if (vRow != null && !vRow.isEmpty()) {
                    Row row = sheet.createRow(copyLineIndex + i);
                    for (int j = 0; j < vRow.size(); j++) {
                        String cValue = vRow.get(j);
                        row.createCell(j).setCellValue(getReasonableValue(cValue));
                    }
                }
            }
            out = new FileOutputStream(destFile);
            workbook.write(out);
            workbook.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (templateInput != null) {
                try {
                    templateInput.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            templateFile.deleteOnExit();
        }
    }

    /**
     * 获得合理的单元格内容
     *
     * @param cValue c值
     * @return {@link String}
     */
    private static String getReasonableValue(String cValue) {
        if (cValue != null && cValue.length() > MAXIMUM_LENGTH) {
            cValue = cValue.substring(0, MAXIMUM_LENGTH);
        }
        return cValue;
    }

    /**
     * 生成excle
     *
     * @param destFile excel文件地址
     * @param lines    数据
     */
    public static void generateExcel(File destFile, List<List<String>> lines) {
        generateExcel(destFile, lines, false);
    }

    /**
     * 生成excle
     *
     * @param destFile    excel文件地址
     * @param lines       数据
     * @param handleImage 是否处理图片--把图片嵌入到excel。规则：单元格内容以"image:"开头则进行嵌入
     */
    public static void generateExcel(File destFile, List<List<String>> lines, boolean handleImage) {
        if (!destFile.exists()) {
            destFile = Files.createFile(destFile.getAbsolutePath());
        }
        try (FileOutputStream outputStream = new FileOutputStream(destFile);
                InputStream input = generateExcelInputStream(lines, handleImage)) {
            IOUtils.copy(Objects.requireNonNull(input), outputStream);
            logger.info("生成Excle：{},共{}行数据", destFile.getAbsolutePath(), lines.size());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 生成excle
     *
     * @param lines 数据
     * @return {@link InputStream}
     */
    public static InputStream generateExcelInputStream(List<List<String>> lines) {
        return generateExcelInputStream(lines, false);
    }

    /**
     * 生成excle
     *
     * @param lines       数据
     * @param handleImage 是否处理图片--把图片嵌入到excel。规则：单元格内容以"image:"开头则进行嵌入
     * @return {@link InputStream}
     */
    public static InputStream generateExcelInputStream(List<List<String>> lines, boolean handleImage) {
        // 解决内存溢出问题，每2000行会先flush到磁盘上
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); SXSSFWorkbook workbook = new SXSSFWorkbook(2000)) {
            Sheet sheet = workbook.createSheet();
            for (int rowIndex = 0; rowIndex < lines.size(); rowIndex++) {
                List<String> vRow = lines.get(rowIndex);
                if (vRow != null && !vRow.isEmpty()) {
                    Row row = sheet.createRow(rowIndex);
                    for (int colIndex = 0; colIndex < vRow.size(); colIndex++) {
                        String cValue = vRow.get(colIndex);
                        Cell cell = row.createCell(colIndex);
                        if (handleImage && cValue.startsWith(IMAGE_PREFIX)) {
                            setCellImageValue(sheet, rowIndex, row, colIndex, cValue);
                        } else {
                            cell.setCellValue(getReasonableValue(cValue));
                        }
                    }
                }
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 设置单元格的值为图片
     *
     * @param sheet    表
     * @param rowIndex 行索引
     * @param row      行
     * @param colIndex 列索引
     * @param imgUrl   图片地址
     */
    public static void setCellImageValue(Sheet sheet, int rowIndex, Row row, int colIndex, String imgUrl) {
        sheet.setColumnWidth(colIndex, 4800);
        row.setHeight((short) 2200);
        byte[] bytes = Unirest.get(imgUrl.substring(IMAGE_PREFIX.length())).asBytes().getBody();
        int myPictureId = sheet.getWorkbook().addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
        XSSFClientAnchor myAnchor = new XSSFClientAnchor();
        myAnchor.setCol1(colIndex);
        myAnchor.setRow1(rowIndex);
        myAnchor.setCol2(colIndex + 1);
        myAnchor.setRow2(rowIndex + 1);
        SXSSFDrawing drawing = (SXSSFDrawing) sheet.createDrawingPatriarch();
        drawing.createPicture(myAnchor, myPictureId);
    }

    /**
     * 生成csv文件
     *
     * @param dest 目标路径
     * @param data 数据
     */
    public static void generateCsv(File dest, List<List<String>> data) {
        try {
            FileUtils.writeLines(dest, Charsets.GBK.name(), escapeCsv(data), "\n");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 生成csv输入流
     *
     * @param data 数据
     * @return {@link InputStream}
     */
    public static InputStream generateCsvInputStream(List<List<String>> data) {
        try {
            List<String> rows = escapeCsv(data);
            return IOUtils.toInputStream(StringUtils.join(rows, "\n"), Charsets.GBK.name());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 逃避csv
     *
     * @param lines 行
     * @return {@link List}<{@link String}>
     */
    private static List<String> escapeCsv(List<List<String>> lines) {
        List<String> result = new ArrayList<>();
        for (List<String> line : lines) {
            StringBuilder sbLine = new StringBuilder();
            if (line == null) {
                continue;
            }
            for (int i = 0; i < line.size(); i++) {
                String cell = StringEscapeUtils.escapeCsv(line.get(i));
                if (i != line.size() - 1) {
                    sbLine.append(cell).append(",");
                } else {
                    sbLine.append(cell);
                }
            }
            result.add(sbLine.toString());
        }
        return result;
    }

    /**
     * 设置某些列的值只能输入预制的数据,显示下拉框.
     *
     * @param sheet    要设置的sheet.
     * @param textlist 下拉框显示的内容
     * @param firstRow 开始行
     * @param endRow   结束行
     * @param firstCol 开始列
     * @param endCol   结束列
     */
    public static void setValidation(Sheet sheet, String[] textlist, int firstRow, int endRow, int firstCol, int endCol) {
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        DataValidation dataValidationList =
                sheet instanceof XSSFSheet ?
                        new XSSFDataValidationHelper((XSSFSheet) sheet).createValidation(new XSSFDataValidationConstraint(textlist), regions) :
                        new HSSFDataValidation(regions, DVConstraint.createExplicitListConstraint(textlist));
        sheet.addValidationData(dataValidationList);
    }

    /**
     * 从实体类集合中获取excel数据
     *
     * @param extractHead 是否提取并添加头部
     * @param entities    实体
     * @return {@link List}<{@link List}<{@link String}>>
     */
    public static List<List<String>> getLinesFromEntities(List<? extends BaseEntity> entities, boolean extractHead) {
        List<List<String>> lines = new ArrayList<>(entities.size());
        AtomicInteger incr = new AtomicInteger(0);
        List<Col> fields = Arrays.stream(entities.get(0).getClass().getDeclaredFields()).filter(e -> e.isAnnotationPresent(ExcelField.class)).map(e -> {
            int index = incr.getAndIncrement();
            ExcelField annotation = e.getAnnotation(ExcelField.class);
            return new Col(e.getName(), annotation.value(), annotation.colIndex() > 0 ? annotation.colIndex() : index, annotation.dateFormat(),
                    annotation.numberFormat(), annotation.customizeFormat());
        }).sorted(Comparator.comparing(Col::getColIndex)).toList();
        if (extractHead) {
            lines.add(fields.stream().map(Col::getColName).collect(Collectors.toList()));
        }
        lines.addAll(entities.stream().map(e -> {
            List<String> row = new ArrayList<>();
            for (Col field : fields) {
                Object property = Beans.getProperty(e, field.getFieldName());
                String val;
                if (property == null) {
                    val = "";
                } else {
                    if (!field.getDateFormat().isEmpty()) {
                        val = Dates.formatTimeMillis((Long) property, field.getDateFormat());
                    } else if (!field.getNumberFormat().isEmpty()) {
                        val = Numbers.numberFormat((Number) property, field.getNumberFormat());
                    } else if (!field.getCustomizeFormat().isEmpty()) {
                        val = Texts.format(field.getCustomizeFormat(), property);
                    } else {
                        val = String.valueOf(property);
                    }
                }
                row.add(val);
            }
            return row;
        }).toList());
        return lines;
    }

    @Data
    @AllArgsConstructor
    static class Col {

        private String fieldName;
        private String colName;
        private int colIndex;
        private String dateFormat;
        private String numberFormat;
        private String customizeFormat;
    }
}
