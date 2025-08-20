package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Is Empty Handler
 *
 * @author xuanyuanli
 */
public class IsEmptyHandler implements Handler {

    /**
     * 是否空
     */
    public static final String IS_EMPTY = "IsEmpty";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IS_EMPTY)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IS_EMPTY.length());
            field = getDbColumn(method, field);
            spec.isEmpty(field);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
