package cn.xuanyuanli.core.util.office;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * Excel工作表处理器接口
 * 
 * <p>该函数式接口用于处理Excel工作表对象，允许用户自定义对Sheet的各种操作，
 * 包括但不限于：</p>
 * <ul>
 *   <li>设置列宽和行高</li>
 *   <li>设置单元格样式</li>
 *   <li>添加数据验证</li>
 *   <li>设置打印区域</li>
 *   <li>添加图表或图片</li>
 *   <li>其他Excel格式化操作</li>
 * </ul>
 * 
 * <p>作为函数式接口，可以使用Lambda表达式或方法引用来实现。</p>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * ExcelSheetHandler handler = sheet -> {
 *     sheet.setColumnWidth(0, 5000);  // 设置第一列宽度
 *     sheet.createFreezePane(0, 1);   // 冻结首行
 * };
 * }</pre>
 *
 * @author xuanyuanli
 */
@FunctionalInterface
public interface ExcelSheetHandler {

    /**
     * 处理Excel工作表
     * 
     * <p>该方法是函数式接口的核心方法，用于对传入的Sheet对象进行各种处理操作。
     * 实现类可以在此方法中定义具体的Excel格式化逻辑，如设置样式、调整布局、
     * 添加数据验证等。</p>
     * 
     * <p>注意：此方法会直接修改传入的Sheet对象，调用方需要确保Sheet对象
     * 处于可修改状态。</p>
     *
     * @param sheet Excel工作表对象，不能为null
     */
    void handler(Sheet sheet);
}
