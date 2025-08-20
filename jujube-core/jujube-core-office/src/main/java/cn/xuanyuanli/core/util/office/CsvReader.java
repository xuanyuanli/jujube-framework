package cn.xuanyuanli.core.util.office;

import com.google.common.collect.Lists;
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
 * Csv读取器
 *
 * @author xuanyuanli
 * @date 2021/09/01
 */
@Slf4j
public class CsvReader implements Iterable<List<String>> {

    /**
     * 配置
     */
    private final ExcelReaderConfig config;
    /**
     * 工作薄的总行数
     */
    @Getter
    private int rowCount;
    /**
     * csv记录
     */
    private List<CSVRecord> csvRecords;
    /**
     * 默认字符集
     */
    public static final Charset DEFAULT_CHARSET = Charsets.GBK;

    /**
     * 构造
     *
     * @param file    csv文件
     * @param charset 文件编码
     * @param config  文件读取的一些配置规则
     */
    public CsvReader(File file, Charset charset, ExcelReaderConfig config) {
        this(file, charset, config, CSVFormat.DEFAULT);
    }

    /**
     * 构造
     *
     * @param file      csv文件
     * @param charset   文件编码
     * @param config    文件读取的一些配置规则
     * @param csvFormat csv格式
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
     * 构造
     *
     * @param charset 文件编码
     * @param config  文件读取的一些配置规则
     * @param is      是否
     */
    public CsvReader(InputStream is, Charset charset, ExcelReaderConfig config) {
        this(is, charset, config, CSVFormat.DEFAULT);
    }

    /**
     * 构造
     *
     * @param charset   文件编码
     * @param config    文件读取的一些配置规则
     * @param is        是否
     * @param csvFormat csv格式
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
     * 获得行
     *
     * @param rowNo 行编号
     * @return {@link List}<{@link String}>
     */
    public List<String> getRow(int rowNo) {
        return recordToStringList(csvRecords.get(rowNo));
    }

    /**
     * 迭代器
     *
     * @return {@link Iterator}<{@link List}<{@link String}>>
     */
    @Override
    public Iterator<List<String>> iterator() {
        return new Itr();
    }


    /**
     * 记录字符串列表
     *
     * @param record 记录
     * @return {@link List}<{@link String}>
     */
    private List<String> recordToStringList(CSVRecord record) {
        List<String> list = Lists.newArrayList();
        for (String cellContent : record) {
            if (config.isTrimCellContent()) {
                cellContent = cellContent.trim();
            }
            list.add(cellContent);
        }
        return list;
    }

    /**
     * 获得行
     *
     * @return {@link List}<{@link List}<{@link String}>>
     */
    public List<List<String>> getRows() {
        List<List<String>> data = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            data.add(getRow(i));
        }
        return data;
    }

    private class Itr implements Iterator<List<String>> {

        /**
         * 工作薄的当前行数
         */
        private int currentNum;

        @Override
        public boolean hasNext() {
            return currentNum != rowCount;
        }

        @Override
        public List<String> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return getRow(currentNum++);
        }

        @Override
        public void remove() {
            throw new RuntimeException("not execute remove");
        }

    }
}
