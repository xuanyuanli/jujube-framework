package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Is Not Null Handler
 *
 * @author John Li
 */
public class IsNotNullHandler implements Handler {

    /**
     * 是否不null
     */
    public static final String IS_NOT_NULL = "IsNotNull";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IS_NOT_NULL)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IS_NOT_NULL.length());
            field = getDbColumn(method, field);
            spec.isNotNull(field);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
