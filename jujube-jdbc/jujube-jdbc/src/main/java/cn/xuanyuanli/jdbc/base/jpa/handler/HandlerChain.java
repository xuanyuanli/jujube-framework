package cn.xuanyuanli.jdbc.base.jpa.handler;

import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * Handler的责任链
 *
 * @author John Li
 */
public interface HandlerChain {

    /**
     * 处理
     *
     * @param method               方法
     * @param spec                 规格
     * @param truncationMethodName 截断后的方法名（By之后的字符）
     * @param args                 方法实参列表
     */
    void handler(DaoMethod method, Spec spec, String truncationMethodName, List<Object> args);

}
