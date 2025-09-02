package cn.xuanyuanli.core.util.office;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.Validate;
import cn.xuanyuanli.core.constant.Charsets;

/**
 * CSV文件读取器，提供对CSV文件的读取和遍历功能
 * 
 * <p>支持以下功能：
 * <ul>
 *     <li>从文件或输入流读取CSV数据</li>
 *     <li>支持自定义字符编码和CSV格式</li>
 *     <li>提供迭代器接口，支持逐行遍历</li>
 *     <li>支持获取指定行或所有行的数据</li>
 *     <li>支持单元格内容的自动去空格处理</li>
 * </ul>
 * 
 * <p>使用示例：
 * <pre>{@code
 * File csvFile = new File("data.csv");
 * ExcelReaderConfig config = new ExcelReaderConfig();
 * CsvReader reader = new CsvReader(csvFile, StandardCharsets.UTF_8, config);
 * 
 * // 遍历所有行
 * for (List<String> row : reader) {
 *     System.out.println(row);
 * }
 * }</pre>
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@Slf4j
public class CsvReader implements Iterable<List<String>> {

    /**
     * Excel读取器配置，包含读取规则和选项
     */
    private final ExcelReaderConfig config;
    
    /**
     * CSV文件的总行数（包含数据的行数）
     */
    @Getter
    private int rowCount;
    
    /**
     * 解析后的CSV记录列表，每个CSVRecord对应CSV文件中的一行数据
     */
    private List<CSVRecord> csvRecords;
    
    /**
     * 默认字符编码集，使用GBK编码以支持中文字符
     */
    public static final Charset DEFAULT_CHARSET = Charsets.GBK;

    /**
     * 构造CSV读取器（使用默认CSV格式）
     *
     * @param file    CSV文件对象，必须存在且可读
     * @param charset 文件字符编码，如UTF-8、GBK等
     * @param config  Excel读取器配置，包含读取规则和选项
     * @throws IllegalArgumentException 如果文件不存在
     * @throws NullPointerException 如果config为null
     */
    public CsvReader(File file, Charset charset, ExcelReaderConfig config) {
        this(file, charset, config, CSVFormat.DEFAULT);
    }

    /**
     * 构造CSV读取器（自定义CSV格式）
     *
     * @param file      CSV文件对象，必须存在且可读
     * @param charset   文件字符编码，如UTF-8、GBK等
     * @param config    Excel读取器配置，包含读取规则和选项
     * @param csvFormat CSV格式配置，定义分隔符、引号字符等
     * @throws IllegalArgumentException 如果文件不存在
     * @throws NullPointerException 如果config为null
     */
    public CsvReader(File file, Charset charset, ExcelReaderConfig config, CSVFormat csvFormat) {
        Objects.requireNonNull(config);
        Validate.isTrue(file.exists(), "file not exists");
        this.config = config;
        try {
            CSVParser csvParser = CSVParser.parse(file, charset, csvFormat);
            csvRecords = csvParser.getRecords();
            rowCount = (int) csvParser.getRecordNumber();
            csvParser.close();
        } catch (IOException e) {
            log.error("CsvReader", e);
        }
    }

    /**
     * 构造CSV读取器（从输入流读取，使用默认CSV格式）
     *
     * @param is      CSV数据输入流
     * @param charset 数据字符编码，如UTF-8、GBK等
     * @param config  Excel读取器配置，包含读取规则和选项
     * @throws NullPointerException 如果config为null
     */
    public CsvReader(InputStream is, Charset charset, ExcelReaderConfig config) {
        this(is, charset, config, CSVFormat.DEFAULT);
    }

    /**
     * 构造CSV读取器（从输入流读取，自定义CSV格式）
     *
     * @param is        CSV数据输入流
     * @param charset   数据字符编码，如UTF-8、GBK等
     * @param config    Excel读取器配置，包含读取规则和选项
     * @param csvFormat CSV格式配置，定义分隔符、引号字符等
     * @throws NullPointerException 如果config为null
     */
    public CsvReader(InputStream is, Charset charset, ExcelReaderConfig config, CSVFormat csvFormat) {
        Objects.requireNonNull(config);
        this.config = config;
        try {
            CSVParser csvParser = CSVParser.parse(is, charset, csvFormat);
            csvRecords = csvParser.getRecords();
            rowCount = (int) csvParser.getRecordNumber();
            csvParser.close();
        } catch (Exception e) {
            log.error("CsvReader", e);
        }
    }

    /**
     * 获取指定行的数据
     *
     * @param rowNo 行号，从0开始
     * @return 该行的所有单元格数据列表，每个元素对应一个单元格的内容
     * @throws IndexOutOfBoundsException 如果行号超出范围
     */
    public List<String> getRow(int rowNo) {
        return recordToStringList(csvRecords.get(rowNo));
    }

    /**
     * 返回用于遍历CSV文件所有行的迭代器
     * 
     * <p>每次迭代返回一行数据，以字符串列表的形式表示
     *
     * @return 行数据迭代器，每个元素是一行的所有单元格数据
     */
    @Override
    public Iterator<List<String>> iterator() {
        return new Itr();
    }


    /**
     * 将CSVRecord转换为字符串列表
     * 
     * <p>根据配置决定是否对单元格内容进行去空格处理
     *
     * @param record CSV记录对象
     * @return 该记录中所有单元格内容的字符串列表
     */
    private List<String> recordToStringList(CSVRecord record) {
        List<String> list = new ArrayList<>();
        for (String cellContent : record) {
            if (config.isTrimCellContent()) {
                cellContent = cellContent.trim();
            }
            list.add(cellContent);
        }
        return list;
    }

    /**
     * 获取CSV文件的所有行数据
     *
     * @return 所有行的数据列表，外层列表的每个元素代表一行，内层列表代表该行的所有单元格内容
     */
    public List<List<String>> getRows() {
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            data.add(getRow(i));
        }
        return data;
    }

    /**
     * 内部迭代器实现类，用于遍历CSV文件的所有行
     */
    private class Itr implements Iterator<List<String>> {

        /**
         * 当前迭代的行号，从0开始
         */
        private int currentNum;

        /**
         * 检查是否还有下一行数据
         *
         * @return 如果还有下一行数据则返回true，否则返回false
         */
        @Override
        public boolean hasNext() {
            return currentNum != rowCount;
        }

        /**
         * 获取下一行数据并移动到下一位置
         *
         * @return 下一行的所有单元格数据列表
         * @throws NoSuchElementException 如果没有更多数据
         */
        @Override
        public List<String> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return getRow(currentNum++);
        }

        /**
         * 不支持的删除操作
         *
         * @throws UnsupportedOperationException 始终抛出此异常，因为不支持删除操作
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("CSV读取器不支持删除操作");
        }

    }
}
