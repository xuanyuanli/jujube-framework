package cn.xuanyuanli.jdbc.base.jpa.strategy.query;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * 查询信息（包含了查询字段和条件）
 *
 * @author xuanyuanli
 * @date 2023/04/13
 */
@Data
@AllArgsConstructor
public class Query {

    private List<DbAndEntityFiled> selectFields;
    private Spec spec;

    public List<String> getSelectDbFields() {
        return selectFields.stream().map(DbAndEntityFiled::getDbField).collect(Collectors.toList());
    }
}
