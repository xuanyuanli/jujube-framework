package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Is Not Empty Handler
 *
 * @author xuanyuanli
 */
public class IsNotEmptyHandler implements Handler {

    /**
     * 是否不空
     */
    public static final String IS_NOT_EMPTY = "IsNotEmpty";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IS_NOT_EMPTY)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IS_NOT_EMPTY.length());
            field = getDbColumn(method, field);
            spec.isNotEmpty(field);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
