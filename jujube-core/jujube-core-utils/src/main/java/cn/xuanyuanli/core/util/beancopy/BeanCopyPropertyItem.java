package cn.xuanyuanli.core.util.beancopy;

import java.beans.PropertyDescriptor;
import lombok.Data;

/**
 * Bean 复制属性项配置类
 * 
 * <p>用于封装 Bean 属性复制过程中涉及的源属性和目标属性的相关信息，
 * 支持普通对象属性和 Map 类型属性的复制配置。</p>
 * 
 * <p>该类主要用于 Bean 复制工具中，记录每个待复制属性的详细信息，
 * 包括属性名称、属性描述符等，以便在复制过程中进行正确的类型转换和赋值操作。</p>
 * 
 * @author xuanyuanli
 */
@Data
public class BeanCopyPropertyItem {

    /**
     * 标识源对象是否为 Map 类型
     * 
     * <p>当源对象是 Map 类型时，属性的获取方式与普通 Bean 对象不同，
     * 需要通过 Map 的 get 方法来获取属性值。</p>
     */
    private boolean sourceIsMap;
    
    /**
     * 源属性名称
     * 
     * <p>表示在源对象中对应的属性名称，用于从源对象中获取属性值。
     * 当 sourceIsMap 为 true 时，该名称作为 Map 的 key 使用。</p>
     */
    private String sourcePropertyName;
    
    /**
     * 源属性描述符
     * 
     * <p>包含源对象属性的详细信息，如 getter/setter 方法、属性类型等。
     * 当源对象不是 Map 类型时使用该描述符进行属性访问。</p>
     */
    private PropertyDescriptor sourceProperty;
    
    /**
     * 目标属性描述符
     * 
     * <p>包含目标对象属性的详细信息，用于向目标对象设置属性值。
     * 提供了属性的 setter 方法和类型信息，确保正确的类型转换和赋值。</p>
     */
    private PropertyDescriptor targetProperty;


}
