package cn.xuanyuanli.core.util.office;

import lombok.Data;

/**
 * ExcelReader的配置器
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2021/09/01
 */
@Data
public class ExcelReaderConfig {

    /**
     * 设置 是否trim单元格中的内容。默认为false
     */
    private boolean trimCellContent;
    /**
     * 计算excel总行数时，是否遇到空行，则终止
     */
    private boolean blankLineTerminated;

    /**
     * 默认
     */
    public static final ExcelReaderConfig DEFAULT = new ExcelReaderConfig(true, false);
    /**
     * 所有参数默认为ture
     */
    public static final ExcelReaderConfig ALL_RIGHT = new ExcelReaderConfig(true, true);

    /**
     * 所有参数默认为false
     */
    public static final ExcelReaderConfig ALL_FALSE = new ExcelReaderConfig(false, false);

    /**
     * excel读者配置
     *
     * @param trimCellContent     修剪单元内容
     * @param blankLineTerminated 空行终止
     */
    public ExcelReaderConfig(boolean trimCellContent, boolean blankLineTerminated) {
        super();
        this.trimCellContent = trimCellContent;
        this.blankLineTerminated = blankLineTerminated;
    }

}
