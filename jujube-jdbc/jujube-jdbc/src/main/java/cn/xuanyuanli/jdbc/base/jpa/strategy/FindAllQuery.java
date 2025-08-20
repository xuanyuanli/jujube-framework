package cn.xuanyuanli.jdbc.base.jpa.strategy;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.handler.DefaultHandlerChain;
import cn.xuanyuanli.jdbc.base.jpa.handler.HandlerContext;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DbAndEntityFiled;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;

/**
 * @author xuanyuanli
 */
public class FindAllQuery extends BaseQueryStrategy {

    /**
     * 找到所有
     */
    static final String FIND_ALL = "findAll";

    @Override
    public boolean accept(String methodName) {
        return methodName.startsWith(FIND_ALL);
    }

    @Override
    public Query getQuery(String tableName, DaoMethod method, Object[] args) {
        if (args == null) {
            args = new Object[0];
        }
        String mname = method.getName();
        String tmname = mname.substring(FIND_ALL.length());
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.handler(method, spec, tmname, Lists.newArrayList(args));
        List<String> selectFields = getSelectFieldsByMethod(method, tableName);
        return new Query(selectFields.stream().map(e -> new DbAndEntityFiled(e, null)).collect(Collectors.toList()), spec);
    }


    @Override
    Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args) {
        Query query = getQuery(proxyDao.getTableName(), new JavaDaoMethod(method), args);
        return super.query(proxyDao, method, query.getSpec(), query.getSelectDbFields());
    }

}
