package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;

/**
 * @author xuanyuanli
 */
public class AndHandler implements Handler {

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args, HandlerChain chain) {
        String[] sarr = Strings.splitByAnd(truncationMethodName);
        if (sarr.length > 1) {
            Spec[] specArr = new Spec[sarr.length];
            int i = 0;
            for (String field : sarr) {
                Spec tSpec = new Spec();
                DefaultHandlerChain selfChain = new DefaultHandlerChain();
                selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
                selfChain.handler(method, tSpec, field, args);
                specArr[i++] = tSpec;
            }
            spec.and(specArr);
        } else {
            chain.handler(method, spec, truncationMethodName, args);
        }
    }

}
