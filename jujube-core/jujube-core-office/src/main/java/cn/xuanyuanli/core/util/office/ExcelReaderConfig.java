package cn.xuanyuanli.core.util.office;

import lombok.Data;

/**
 * Excel读取器的配置类
 * <p>
 * 用于配置Excel文件读取过程中的各种参数，包括是否修剪单元格内容、
 * 是否在遇到空行时终止读取等选项。提供了多种预定义的配置实例供快速使用。
 * </p>
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 */
@Data
public class ExcelReaderConfig {

    /**
     * 是否修剪单元格中的内容
     * <p>
     * 当设置为true时，会自动去除单元格内容前后的空白字符（包括空格、制表符、换行符等）。
     * 默认值为false，保持单元格原始内容。
     * </p>
     */
    private boolean trimCellContent;
    
    /**
     * 是否在遇到空行时终止读取
     * <p>
     * 当设置为true时，在计算Excel总行数或读取过程中遇到完全空白的行时会停止继续读取。
     * 当设置为false时，会跳过空行继续读取后续内容。默认值取决于具体的配置实例。
     * </p>
     */
    private boolean blankLineTerminated;

    /**
     * 默认配置实例
     * <p>
     * 启用单元格内容修剪(trimCellContent=true)，禁用空行终止(blankLineTerminated=false)。
     * 适用于大多数常见的Excel读取场景，既能清理数据又能读取完整内容。
     * </p>
     */
    public static final ExcelReaderConfig DEFAULT = new ExcelReaderConfig(true, false);
    
    /**
     * 全部启用配置实例
     * <p>
     * 启用所有功能选项(trimCellContent=true, blankLineTerminated=true)。
     * 适用于需要清理数据且只读取连续数据区域的场景，遇到空行即停止读取。
     * </p>
     */
    public static final ExcelReaderConfig ALL_RIGHT = new ExcelReaderConfig(true, true);

    /**
     * 全部禁用配置实例
     * <p>
     * 禁用所有功能选项(trimCellContent=false, blankLineTerminated=false)。
     * 适用于需要保持原始数据格式且读取全部内容的场景，包括空行和未修剪的单元格内容。
     * </p>
     */
    public static final ExcelReaderConfig ALL_FALSE = new ExcelReaderConfig(false, false);

    /**
     * 构造一个Excel读取器配置实例
     * <p>
     * 通过指定的参数创建一个配置实例，用于控制Excel读取过程中的行为。
     * </p>
     *
     * @param trimCellContent     是否修剪单元格内容，true表示去除前后空白字符，false表示保持原样
     * @param blankLineTerminated 是否在遇到空行时终止读取，true表示遇到空行即停止，false表示跳过空行继续读取
     */
    public ExcelReaderConfig(boolean trimCellContent, boolean blankLineTerminated) {
        super();
        this.trimCellContent = trimCellContent;
        this.blankLineTerminated = blankLineTerminated;
    }

}
