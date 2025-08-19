package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.handler.GroupByHandler;
import cn.xuanyuanli.jdbc.base.jpa.handler.OrderByHandler;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DbAndEntityFiled;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;

/**
 * find*ById方法
 *
 * @author John Li
 */

public class FindAnyByIdQuery extends BaseQueryStrategy {

    private static final String FIND_BY_ID2_PREFIX = "^find(.+?)ById$";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, FIND_BY_ID2_PREFIX) && !methodName.contains(OrderByHandler.ORDER_BY) && !methodName.contains(GroupByHandler.GROUP_BY);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        String mname = method.getName();
        String[] arr = Strings.getGroups(FIND_BY_ID2_PREFIX, mname);
        List<DbAndEntityFiled> fieldList = getDbColumnNames(method, arr[1]);
        return new Query(fieldList, null);
    }


    @Override
    public Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(null, new JavaDaoMethod(method), args);
        Serializable id = (Serializable) args[0];
        if (id != null) {
            return super.query(proxyDao, method, new Spec().eq(proxyDao.getPrimaryKeyName(), id), query.getSelectDbFields());
        } else {
            return null;
        }
    }

}
