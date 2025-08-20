package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DbAndEntityFiled;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.Strings;
import cn.xuanyuanli.core.util.Beans;

/**
 * @author xuanyuanli
 */
public class GetSumOfByAnyQuery extends BaseQueryStrategy {

    private static final String GET_SUM_OF = "^getSumOf(.+?)By(.+)";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, GET_SUM_OF);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        String mname = method.getName();
        String[] groups = Strings.getGroups(GET_SUM_OF, mname);
        String fieldName = groups[1];
        String sumField = getDbColumnName(method, fieldName);
        Spec spec = getSpecOfAllHandler(method, args, groups[2]);
        return new Query(Stream.of(sumField).map(e -> new DbAndEntityFiled(e, fieldName)).collect(Collectors.toList()), spec);
    }


    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(null, new JavaDaoMethod(method), args);
        double sum = proxyDao.getSumOf(query.getSelectDbFields().get(0), query.getSpec());
        return Beans.getExpectTypeValue(sum, method.getReturnType());
    }

}
