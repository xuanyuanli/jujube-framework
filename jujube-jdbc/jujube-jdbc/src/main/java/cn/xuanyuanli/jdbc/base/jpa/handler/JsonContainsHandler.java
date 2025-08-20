package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * JSON_CONTAINS() Handler
 *
 * @author xuanyuanli
 */
public class JsonContainsHandler implements Handler {

    /**
     * JsonContains
     */
    public static final String JSON_CONTAINS = "JsonContains";
    /**
     * JsonContains$
     */
    public static final String JSON_CONTAINS$ = "JsonContains$";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(JSON_CONTAINS)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - JSON_CONTAINS.length());
            field = getDbColumn(method, field);
            spec.jsonContains(field, args.get(0), null);
            args.remove(0);
        } else if (truncationMethodName.endsWith(JSON_CONTAINS$)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - JSON_CONTAINS$.length());
            field = getDbColumn(method, field);
            spec.jsonContains(field, args.get(0), (String) args.get(1));
            args.remove(0);
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
