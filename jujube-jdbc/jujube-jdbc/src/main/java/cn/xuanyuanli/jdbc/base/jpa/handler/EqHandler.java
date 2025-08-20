package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Eq Handler(因为是默认，所以在Chain中一定要是最后一个，且不再继续调用责任链)
 *
 * @author xuanyuanli
 */

public class EqHandler implements Handler {

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        String field = getDbColumn(method, truncationMethodName);
        Object value = args.get(0);
        spec.eq(field, value);
        args.remove(0);
    }

}
