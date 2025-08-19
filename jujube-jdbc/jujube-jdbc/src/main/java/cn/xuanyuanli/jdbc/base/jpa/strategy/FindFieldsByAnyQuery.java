package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
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
public class FindFieldsByAnyQuery extends BaseQueryStrategy {

    private static final String FIND_ANY_BY = "^findAny(\\d*?)By(.+)";

    @Override
    public boolean accept(String methodName) {
        return Strings.find(methodName, FIND_ANY_BY);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        String mname = method.getName();
        String[] arr = Strings.getGroups(FIND_ANY_BY, mname);
        Spec spec = getSpecOfAllHandler(method, args, arr[2]);
        List<String> selectFields = getSelectFieldsByMethod(method, tableName);
        return new Query(selectFields.stream().map(e -> new DbAndEntityFiled(e, null)).collect(Collectors.toList()), spec);
    }


    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(proxyDao.getTableName(), new JavaDaoMethod(method), args);
        return super.query(proxyDao, method, query.getSpec(), query.getSelectDbFields());
    }

}
