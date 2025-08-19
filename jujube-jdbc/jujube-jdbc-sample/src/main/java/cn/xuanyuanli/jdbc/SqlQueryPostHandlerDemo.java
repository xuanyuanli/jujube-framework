package cn.xuanyuanli.jdbc;

import cn.xuanyuanli.jdbc.base.SqlQueryPostHandler;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import cn.xuanyuanli.core.util.Texts;

import java.util.Arrays;

/**
 * @author John Li
 */
public class SqlQueryPostHandlerDemo implements SqlQueryPostHandler {

    @Override
    public SqlQuery postHandle(String sql, Object[] params) {
        System.out.println(Texts.format("sql: {}", Sqls.realSql(sql, Arrays.asList(params))));
        return new SqlQuery(sql, params);
    }
}
