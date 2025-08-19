package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Notin Handler
 *
 * @author John Li
 */
public class NotInHandler implements Handler {

    /**
     * notin
     */
    public static final String NOTIN = "NotIn";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(NOTIN)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - NOTIN.length());
            field = getDbColumn(method, field);
            spec.notin(field, (Iterable<?>) args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
