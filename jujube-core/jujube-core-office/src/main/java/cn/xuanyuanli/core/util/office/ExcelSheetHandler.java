package cn.xuanyuanli.core.util.office;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Sheet处理，可进行excel格式设置
 *
 * @author xuanyuanli
 */
@FunctionalInterface
public interface ExcelSheetHandler {

    /**
     * handler
     *
     * @param sheet 表
     */
    void handler(Sheet sheet);
}
