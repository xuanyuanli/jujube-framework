package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Like Handler
 *
 * @author xuanyuanli
 */
public class NotLikeHandler implements Handler {

    /**
     * 不喜欢
     */
    public static final String NOT_LIKE = "NotLike";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(NOT_LIKE)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - NOT_LIKE.length());
            field = getDbColumn(method, field);
            spec.notlike(field, args.get(0));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
