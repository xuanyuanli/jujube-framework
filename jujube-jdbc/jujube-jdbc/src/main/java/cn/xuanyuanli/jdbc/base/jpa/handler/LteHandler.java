package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Like Handler
 *
 * @author xuanyuanli
 */
public class LteHandler implements Handler {

    /**
     * lte
     */
    public static final String LTE = "Lte";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(LTE)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - LTE.length());
            field = getDbColumn(method, field);
            spec.lte(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
