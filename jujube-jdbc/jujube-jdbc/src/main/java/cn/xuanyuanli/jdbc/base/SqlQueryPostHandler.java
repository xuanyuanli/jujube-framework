package cn.xuanyuanli.jdbc.base;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * sql查询后置处理器
 *
 * @author xuanyuanli
 */
public interface SqlQueryPostHandler {

    /**
     * 处理后
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link SqlQuery}
     */
    SqlQuery postHandle(String sql, Object[] params);

    @Data
    @AllArgsConstructor
    class SqlQuery {
        private String sql;
        private Object[] params;
    }
}
