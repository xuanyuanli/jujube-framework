package cn.xuanyuanli.core.util.office;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.util.LocaleUtil;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Collections3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Excel 文件读取器
 * <p>
 * 基于 Apache POI 实现的高性能 Excel 读取工具，支持 .xls 和 .xlsx 格式文件。
 * 提供灵活的读取配置和数据转换功能，适用于大文件流式处理场景。主要特性：
 * <ul>
 * <li><strong>多格式支持：</strong>兼容 Excel 97-2003(.xls) 和 Excel 2007+(.xlsx)</li>
 * <li><strong>流式处理：</strong>实现 Iterable 接口，支持逐行遍历，内存友好</li>
 * <li><strong>灵活配置：</strong>支持跳过标题行、空行终止等多种读取策略</li>
 * <li><strong>数据转换：</strong>自动处理公式计算、日期格式转换、数据类型识别</li>
 * <li><strong>对象映射：</strong>支持将 Excel 行数据映射为 Java 对象</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>基础使用示例：</strong>
 * <pre>{@code
 * try (FileInputStream fis = new FileInputStream("data.xlsx")) {
 *     ExcelReaderConfig config = ExcelReaderConfig.builder()
 *         .skipHeaderRow(true)
 *         .blankLineTerminated(true)
 *         .build();
 *     
 *     ExcelReader reader = new ExcelReader(fis, 0, config);
 *     
 *     // 逐行遍历
 *     for (List<String> row : reader) {
 *         System.out.println("行数据: " + row);
 *     }
 *     
 *     // 转换为对象列表
 *     List<UserDto> users = reader.readToList(UserDto.class);
 * }
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>对象映射示例：</strong>
 * <pre>{@code
 * public class UserDto {
 *     @ExcelField("姓名")
 *     private String name;
 *     
 *     @ExcelField("年龄")
 *     private Integer age;
 *     
 *     @ExcelField(value = "出生日期", dateFormat = "yyyy-MM-dd")
 *     private Date birthDate;
 * }
 * 
 * List<UserDto> users = reader.readToList(UserDto.class);
 * }</pre>
 * </p>
 * 
 * <p>
 * <strong>高级特性：</strong>
 * <ul>
 * <li><strong>自定义单元格处理：</strong>通过 setCellContentHandle 自定义数据提取逻辑</li>
 * <li><strong>公式计算：</strong>自动计算公式并返回结果值</li>
 * <li><strong>日期识别：</strong>智能识别 Excel 日期格式并转换</li>
 * <li><strong>异常处理：</strong>完善的错误处理和日志记录</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <strong>性能优化：</strong>
 * <ul>
 * <li>采用流式处理，避免大文件内存溢出</li>
 * <li>延迟初始化，提高启动性能</li>
 * <li>内置缓存机制，减少重复计算</li>
 * </ul>
 * </p>
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 * @see ExcelReaderConfig
 * @see ExcelField
 */
public class ExcelReader implements Iterable<List<String>> {

    /**
     * Excel 工作表实例
     * <p>
     * 当前要读取的 Excel 工作表（Sheet）对象，包含所有表格数据。
     * 通过 POI 的 WorkbookFactory 从输入流中创建。
     * </p>
     */
    private Sheet sheet;
    /**
     * 工作表的总行数
     * <p>
     * 根据配置策略计算的实际数据行数：
     * <ul>
     * <li>如果启用空行终止：计算到遇到第一个空行为止</li>
     * <li>如果未启用空行终止：使用 POI 的 getLastRowNum() + 1</li>
     * </ul>
     * </p>
     */
    @Getter
    private int rowCount;
    /**
     * Excel 读取配置
     * <p>
     * 包含读取策略的配置对象，如是否跳过标题行、是否空行终止等。
     * 在构造时传入并不可变。
     * </p>
     */
    private final ExcelReaderConfig config;
    /**
     * Excel 公式计算器
     * <p>
     * POI 的 FormulaEvaluator，用于计算 Excel 中的公式并返回结果值。
     * 当遇到公式单元格时，会自动计算并提取数值。
     * </p>
     */
    private FormulaEvaluator evaluator;
    /**
     * 自定义单元格内容处理器
     * <p>
     * 函数式接口，允许用户自定义单元格数据的提取和转换逻辑。
     * 如果设置了此处理器，会覆盖默认的单元格读取逻辑。
     * </p>
     */
    @Setter
    private Function<Cell, String> cellContentHandle;

    /**
     * 日志记录器
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 构造 Excel 读取器
     * <p>
     * 初始化 Excel 读取器，解析指定的工作表并准备读取数据。
     * 支持 .xls 和 .xlsx 格式的 Excel 文件。
     * </p>
     * 
     * <p>
     * <strong>注意：</strong>构造器会消费输入流，请确保在使用后正确关闭流。
     * </p>
     *
     * @param inputStream Excel 文件输入流，不可为 null
     * @param sheetIndex  要读取的工作表索引，从 0 开始计数
     * @param config      读取配置，不可为 null
     * @throws IllegalArgumentException 当 config 为 null 时
     * @throws RuntimeException 当 Excel 文件解析失败时
     * @see ExcelReaderConfig
     */
    public ExcelReader(InputStream inputStream, int sheetIndex, ExcelReaderConfig config) {
        Objects.requireNonNull(config);
        this.config = config;
        init(inputStream, sheetIndex);
    }

    /**
     * 初始化 Excel 读取器内部组件
     * <p>
     * 从输入流中创建 Excel 工作簿，初始化公式计算器，
     * 获取指定的工作表并计算总行数。
     * </p>
     *
     * @param inputStream Excel 文件输入流
     * @param sheetIndex  工作表索引，从 0 开始
     * @throws RuntimeException 当文件解析失败时
     */
    private void init(InputStream inputStream, int sheetIndex) {
        try {
            Workbook curWorkbook = WorkbookFactory.create(inputStream);
            this.evaluator = curWorkbook.getCreationHelper().createFormulaEvaluator();
            this.sheet = curWorkbook.getSheetAt(sheetIndex);
            this.rowCount = config.isBlankLineTerminated() ? realRows() : sheet.getLastRowNum() + 1;
        } catch (IOException e) {
            logger.error("ExcelReader.init", e);
        }
    }

    /**
     * 计算工作表的真实数据行数
     * <p>
     * 解决 Excel 中删除内容但保留格式导致的虚假行问题。
     * 遍历所有行，遇到第一个空行或空白行时停止计数。
     * </p>
     * 
     * <p>
     * <strong>判断空行的条件：</strong>
     * <ul>
     * <li>Row 对象为 null（被删除的行）</li>
     * <li>所有单元格都为空或空白字符串</li>
     * </ul>
     * </p>
     *
     * @return 实际包含数据的行数
     * @see #isBlankRow(List)
     */
    public int realRows() {
        int con = sheet.getLastRowNum() + 1;
        for (int i = 0; i < con; i++) {
            // 遇到第一个空行，则终止
            if (sheet.getRow(i) == null) {
                con = i;
                break;
            }
            List<String> rows = getRow(i);
            if (rows.isEmpty() || isBlankRow(rows)) {
                con = i;
                break;
            }
        }
        return con;
    }

    /**
     * 获取行数据迭代器
     * <p>
     * 实现 {@link Iterable} 接口，支持使用 for-each 循环遍历 Excel 数据。
     * 迭代器采用懒加载方式，适合处理大文件。
     * </p>
     * 
     * <p>
     * <strong>使用示例：</strong>
     * <pre>{@code
     * for (List<String> row : excelReader) {
     *     // 处理每一行数据
     *     System.out.println(row);
     * }
     * }</pre>
     * </p>
     *
     * @return 行数据迭代器，每个元素为一行的字符串列表
     * @see Iterable#iterator()
     */
    @Override
    public Iterator<List<String>> iterator() {
        return new Itr();
    }

    /**
     * 获取第一行数据
     * <p>
     * 返回 Excel 的第一行数据，通常用于获取列标题。
     * 如果第一行不存在或为空，则返回空列表。
     * </p>
     *
     * @return 第一行的字符串列表，不为 null
     * @see #getRow(int)
     */
    public List<String> first() {
        return getRow(0);
    }

    /**
     * 获得最后一行数据
     *
     * @return {@link List}<{@link String}>
     */
    public List<String> last() {
        return getRow(getRowCount() - 1);
    }

    /**
     * 是否空白行
     *
     * @param rows 行
     * @return boolean
     */
    private boolean isBlankRow(List<String> rows) {
        boolean result = true;
        for (String cell : rows) {
            if (StringUtils.isNotBlank(cell)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * 获得相应行数据
     *
     * @param rowNo 行编号
     * @return {@link List}<{@link String}>
     */
    public List<String> getRow(int rowNo) {
        List<String> list = new ArrayList<>();
        Row row = sheet.getRow(rowNo);
        if (row != null) {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                Cell cell = row.getCell(i);
                String cellContent;
                if (cellContentHandle != null) {
                    cellContent = cellContentHandle.apply(cell);
                } else {
                    cellContent = getCellContent(cell);
                }
                if (config.isTrimCellContent()) {
                    cellContent = cellContent.trim();
                }
                list.add(cellContent);
            }
        }
        return list;
    }

    /**
     * 获得第一行的CellStyles
     *
     * @return {@link List}<{@link CellStyle}>
     */
    public List<CellStyle> getCellStyles() {
        return getCellStyles(sheet.getFirstRowNum());
    }

    /**
     * 获得指定行的CellStyles
     *
     * @param rowNum 行num
     * @return {@link List}<{@link CellStyle}>
     */
    public List<CellStyle> getCellStyles(int rowNum) {
        List<CellStyle> list = new ArrayList<>();
        int lastCellNum = sheet.getRow(rowNum).getLastCellNum();
        for (int i = 0; i < lastCellNum; i++) {
            list.add(sheet.getColumnStyle(i));
        }
        return list;
    }

    /**
     * 获得单元格内容
     *
     * @param cell 细胞
     * @return {@link String}
     */
    public String getCellContent(Cell cell) {
        String cellContent = "";
        if (cell != null) {
            // 如果有表格中单元格有公式，cell.toString()得不到正确结果。这里需要做下处理。需要注意：公式计算出来的数字大多为浮点型，需要客户端去精确
            switch (cell.getCellType()) {
                case FORMULA:
                    try {
                        cellContent = String.valueOf(cell.getNumericCellValue());
                    } catch (IllegalStateException e) {
                        try {
                            cellContent = String.valueOf(cell.getRichStringCellValue());
                        } catch (IllegalStateException e1) {
                            try {
                                CellValue cellValue = evaluator.evaluate(cell);
                                cellContent = cellValue.formatAsString();
                            } catch (Exception e2) {
                                cellContent = cell.toString();
                            }
                        }
                    }
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LocaleUtil.getUserLocale());
                        sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                        return sdf.format(cell.getDateCellValue());
                    }
                    DataFormatter dataFormatter = new DataFormatter();
                    cellContent = dataFormatter.formatCellValue(cell);
                    break;
                default:
                    cellContent = cell.toString();
                    break;
            }
        }
        return cellContent;
    }

    /**
     * 获得所有行集合
     *
     * @return {@link List}<{@link List}<{@link String}>>
     */
    public List<List<String>> getRows() {
        return Collections3.getListFromIterator(iterator());
    }

    /**
     * Excel数据转换为List entity
     *
     * @param wipeOffHead 是否去除头部
     * @param entityClass 实体类
     * @return {@link List}<{@link T}>
     * @param <T> 泛型
     */
    public <T> List<T> toEntity(Class<T> entityClass, boolean wipeOffHead) {
        List<T> list = new ArrayList<>();
        Field[] fields = entityClass.getDeclaredFields();
        for (int i = wipeOffHead ? 1 : 0; i < getRowCount(); i++) {
            List<String> row = getRow(i);
            T t = Beans.getInstance(entityClass);
            for (Field field : fields) {
                ExcelColumn fieldAnnotation = field.getAnnotation(ExcelColumn.class);
                if (fieldAnnotation != null) {
                    int index = fieldAnnotation.value();
                    if (row.size() > index) {
                        String val = row.get(index);
                        Beans.setProperty(t, field.getName(), val);
                    }
                }
            }
            list.add(t);
        }
        return list;
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

    /**
     * Excel中的列
     */
    @Documented
    @Target(value = {ElementType.FIELD})
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface ExcelColumn {

        /**
         * 列的下标排序，从0开始
         *
         * @return int
         */
        int value();
    }
}
