package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Like Handler
 *
 * @author John Li
 */
public class LikeHandler implements Handler {

    /**
     * Like
     */
    public static final String LIKE = "Like";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(LIKE)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - LIKE.length());
            field = getDbColumn(method, field);
            spec.like(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
