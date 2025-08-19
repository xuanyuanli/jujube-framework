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

/**
 * @author John Li
 */
public class FindByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_ONE_BY = "findOneBy";
    private static final String FIND_BY = "findBy";

    @Override
    public boolean accept(String methodName) {
        return methodName.startsWith(FIND_ONE_BY) || methodName.startsWith(FIND_BY);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        String mname = method.getName();
        String tmname = mname.startsWith(FIND_ONE_BY) ? mname.substring(FIND_ONE_BY.length()) : mname.substring(FIND_BY.length());
        Spec spec = getSpecOfAllHandler(method, args, tmname);
        return new Query(Stream.of("*").map(e -> new DbAndEntityFiled(e, null)).collect(Collectors.toList()), spec);
    }


    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(null, new JavaDaoMethod(method), args);
        return super.query(proxyDao, method, query.getSpec(), query.getSelectDbFields());
    }

}
