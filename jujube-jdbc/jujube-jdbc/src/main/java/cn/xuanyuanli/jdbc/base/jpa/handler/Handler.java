package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.BaseQueryStrategy;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * 方法名到Spec的处理
 *
 * @author John Li
 */
public interface Handler {

    /**
     * 处理
     *
     * @param method               方法
     * @param spec                 规格
     * @param truncationMethodName 被截断的方法名
     * @param args                 方法实参列表
     * @param chain                Handler责任链
     */
    void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain);

    /**
     * 获得db列
     *
     * @param method     方法
     * @param queryField 查询字段
     * @return {@link String}
     * @see BaseQueryStrategy#getDbColumnName(DaoMethod, String)
     */
    default String getDbColumn(DaoMethod method, String queryField) {
        return BaseQueryStrategy.getDbColumnName(method, queryField);
    }
}
