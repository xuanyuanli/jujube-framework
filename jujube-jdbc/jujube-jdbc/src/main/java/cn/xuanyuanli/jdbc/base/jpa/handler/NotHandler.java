package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Not Handler
 *
 * @author xuanyuanli
 */
public class NotHandler implements Handler {

    /**
     * Not
     */
    public static final String NOT = "Not";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(NOT)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - NOT.length());
            field = getDbColumn(method, field);
            spec.not(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
