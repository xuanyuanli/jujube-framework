package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Is Null Handler
 *
 * @author xuanyuanli
 */
public class IsNullHandler implements Handler {

    /**
     * 是否null
     */
    public static final String IS_NULL = "IsNull";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IS_NULL)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IS_NULL.length());
            field = getDbColumn(method, field);
            spec.isNull(field);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
