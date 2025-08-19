package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.List;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DbAndEntityFiled;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;

/**
 * @author John Li
 */
public class FindAnyByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_BY2 = "^find(.+?)By(.+)";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, FIND_BY2);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        String mname = method.getName();
        String[] arr = Strings.getGroups(FIND_BY2, mname);
        Spec spec = getSpecOfAllHandler(method, args, arr[2]);
        List<DbAndEntityFiled> selectFields = getDbColumnNames(method, arr[1]);
        return new Query(selectFields, spec);
    }


    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(null, new JavaDaoMethod(method), args);
        return super.query(proxyDao, method, query.getSpec(), query.getSelectDbFields());
    }

}
