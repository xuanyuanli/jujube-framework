package cn.xuanyuanli.jdbc.base.jpa.strategy.query;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author John Li
 * @date 2023/4/20
 */
@Data
@AllArgsConstructor
public class DbAndEntityFiled {

    private String dbField;
    private String entityField;
}
