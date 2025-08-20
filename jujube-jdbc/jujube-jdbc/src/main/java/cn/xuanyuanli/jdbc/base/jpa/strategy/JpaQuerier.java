package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.event.JpaQueryPreEvent;
import cn.xuanyuanli.jdbc.spring.SpringContextHolder;

/**
 * Query Context
 *
 * @author xuanyuanli
 */
public class JpaQuerier {

    private static final List<BaseQueryStrategy> STRATEGIES = new ArrayList<>();

    static {
        // Query类必须为线程安全的，也就是无状态的
        STRATEGIES.add(new GetCountByAnyQuery());
        STRATEGIES.add(new GetSumOfByAnyQuery());
        STRATEGIES.add(new FindAllQuery());
        STRATEGIES.add(new FindFieldsByAnyQuery());
        STRATEGIES.add(new FindAnyByIdQuery());
        STRATEGIES.add(new FindByAnyQuery());
        STRATEGIES.add(new FindAnyByAnyQuery());
    }

    /**
     * 查询
     *
     * @param proxyDao 代理数据访问
     * @param method   方法
     * @param args     参数
     * @return {@link Object}
     */
    public static Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        for (BaseQueryStrategy strategy : STRATEGIES) {
            if (strategy.accept(method.getName())) {
                if (args == null) {
                    args = new Object[0];
                }
                SpringContextHolder.getApplicationContext().publishEvent(new JpaQueryPreEvent(method, args));
                return strategy.query(proxyDao, method, args);
            }
        }
        return null;
    }

    public static List<BaseQueryStrategy> getStrategies() {
        return STRATEGIES;
    }

}
