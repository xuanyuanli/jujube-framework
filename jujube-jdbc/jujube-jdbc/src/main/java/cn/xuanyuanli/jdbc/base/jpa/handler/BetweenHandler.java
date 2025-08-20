package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Between Handler
 *
 * @author xuanyuanli
 */
public class BetweenHandler implements Handler {

    /**
     * 之间
     */
    public static final String BETWEEN = "Between";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(BETWEEN)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - BETWEEN.length());
            field = getDbColumn(method, field);
            spec.between(field, args.get(0), args.get(1));
            args.remove(0);
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
