package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * In Handler
 *
 * @author John Li
 */
public class InHandler implements Handler {

    /**
     * In
     */
    public static final String IN = "In";

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        if (truncationMethodName.endsWith(IN)) {
            String field = truncationMethodName.substring(0, truncationMethodName.length() - IN.length());
            field = getDbColumn(method, field);
            spec.in(field, toIterable(args.get(0)));
            args.remove(0);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

    Iterable<?> toIterable(Object o) {
        if (o instanceof Iterable) {
            return (Iterable<?>) o;
        }
        List<Object> list = new ArrayList<>();
        if (o != null) {
            if (o.getClass().isArray()) {
                int length = Array.getLength(o);
                for (int i = 0; i < length; i++) {
                    list.add(Array.get(o, i));
                }
            }
        }
        return list;
    }

}
