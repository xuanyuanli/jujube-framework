package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Like Handler
 *
 * @author John Li
 */
public class LtHandler implements Handler {

    /**
     * lt
     */
    public static final String LT = "Lt";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(LT)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - LT.length());
            field = getDbColumn(method, field);
            spec.lt(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
