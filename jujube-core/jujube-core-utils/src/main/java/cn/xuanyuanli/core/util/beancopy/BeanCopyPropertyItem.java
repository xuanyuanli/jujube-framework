package cn.xuanyuanli.core.util.beancopy;

import java.beans.PropertyDescriptor;
import lombok.Data;

/**
 * @author xuanyuanli
 */
@Data
public class BeanCopyPropertyItem {

    private boolean sourceIsMap;
    private String sourcePropertyName;
    private PropertyDescriptor sourceProperty;
    private PropertyDescriptor targetProperty;


}
