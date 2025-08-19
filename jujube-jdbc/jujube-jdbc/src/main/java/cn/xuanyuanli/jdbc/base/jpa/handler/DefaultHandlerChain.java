package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * @author John Li
 */
public class DefaultHandlerChain implements HandlerChain {

    @Getter
    private final List<Handler> chain = new ArrayList<>();
    private int pos = 0;

    @Override
    public void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args) {
        if (pos < chain.size()) {
            chain.get(pos++).handler(method, spec, truncationMethodName, args, this);
        }
    }

    /**
     * 添加处理程序
     *
     * @param handlers 处理程序
     */
    public void addHandlers(List<Handler> handlers) {
        this.chain.addAll(handlers);
    }

}
