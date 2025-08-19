package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.core.util.Beans;

/**
 * @author John Li
 */
public class GetCountByAnyQuery extends BaseQueryStrategy {

    private static final String GET_COUNT_BY = "getCountBy";

    @Override
    public boolean accept(String methodName) {
        return methodName.startsWith(GET_COUNT_BY);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        String mname = method.getName();
        String tmname = mname.substring(GET_COUNT_BY.length());
        Spec spec = getSpecOfAllHandler(method, args, tmname);
        return new Query(null, spec);
    }


    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(null, new JavaDaoMethod(method), args);
        long count = proxyDao.getCount(query.getSpec());
        return Beans.getExpectTypeValue(count, method.getReturnType());
    }

}
