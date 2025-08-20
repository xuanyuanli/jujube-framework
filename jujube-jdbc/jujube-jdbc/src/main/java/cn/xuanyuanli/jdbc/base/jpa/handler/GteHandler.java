package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Like Handler
 *
 * @author xuanyuanli
 */
public class GteHandler implements Handler {

    /**
     * Gte
     */
    public static final String GTE = "Gte";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(GTE)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - GTE.length());
            field = getDbColumn(method, field);
            spec.gte(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
