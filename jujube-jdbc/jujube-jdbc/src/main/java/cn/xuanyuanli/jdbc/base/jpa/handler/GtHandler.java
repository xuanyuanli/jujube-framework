package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Like Handler
 *
 * @author xuanyuanli
 */
public class GtHandler implements Handler {

    /**
     * gt
     */
    public static final String GT = "Gt";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(GT)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - GT.length());
            field = getDbColumn(method, field);
            spec.gt(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
