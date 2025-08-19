package cn.xuanyuanli.jdbc.base.spec;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author John Li
 * @date 2023/4/20
 */
@Data
@AllArgsConstructor
public class QueryCondition {
    private String field;

    private Object[] values;

    /**
     * 对应的参数数量
     */
    private Integer paramNum;
}
