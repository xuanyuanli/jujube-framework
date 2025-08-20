package cn.xuanyuanli.jdbc.binding;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.jdbc.exception.DaoProxyException;
import cn.xuanyuanli.jdbc.base.BaseDao;
import cn.xuanyuanli.jdbc.base.BaseDaoSupport;
import cn.xuanyuanli.jdbc.base.annotation.GetCountStrategy;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.strategy.JpaQuerier;
import cn.xuanyuanli.jdbc.spring.SpringContextHolder;
import cn.xuanyuanli.jdbc.pagination.Pageable;
import cn.xuanyuanli.jdbc.pagination.PageableRequest;
import cn.xuanyuanli.core.lang.Record;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Exceptions;
import cn.xuanyuanli.core.util.Pojos;
import org.springframework.beans.BeansException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;

/**
 * Dao接口代理类
 *
 * @author xuanyuanli
 */
@Slf4j
public class DaoProxy<T extends BaseDao<?, ?>> implements InvocationHandler {

    private static final ConcurrentMap<String, JpaBaseDaoSupport> JPA_BASEDAO_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, BaseDaoSupport<?, ?>> BASEDAO_CACHE = new ConcurrentHashMap<>();

    private final Class<T> daoInterfaceClass;

    /**
     * 数据访问代理
     *
     * @param daoInterfaceClass 数据访问接口类
     */
    public DaoProxy(Class<T> daoInterfaceClass) {
        this.daoInterfaceClass = daoInterfaceClass;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            if (method.isDefault()) {
                return Beans.invokeDefaultMethod(proxy, method, args);
            }
            BaseDaoSupport<?, ?> baseDaoSupport = getBaseDaoSupport(daoInterfaceClass);
            // 先看方法是否在BaseDaoSupport中，如果在，则直接调用
            Method declaredMethod = Beans.getSelfDeclaredMethod(baseDaoSupport.getClass(), method.getName(), method.getParameterTypes());
            if (declaredMethod != null) {
                return Beans.invoke(declaredMethod, baseDaoSupport, args);
            } else if (DaoSqlRegistry.isJpaMethod(method.getName())) {
                // 如果以find开头，则属于jpa查询，调用JpaQueryProxyDao
                JpaBaseDaoSupport recordEntityBaseDaoSupport = getJpaBaseDao(daoInterfaceClass);
                return JpaQuerier.query(recordEntityBaseDaoSupport, method, args);
            } else {
                // 以上两种情况都不符合，则属于sql查询，关联sql文件进行查询
                return sqlQuery(baseDaoSupport, proxy, method, args);
            }
        } catch (Exception e) {
            String builder = "Proxy class:" + declaringClass.getName() + ",method:" + method.getName() + ",args:" + StringUtils.join(args, ",") + ",error:"
                             + Exceptions.exceptionToString(e);
            throw new DaoProxyException(builder);
        } finally {
            if (log.isDebugEnabled()) {
                stopWatch.stop();
                log.debug("{},执行时间：{}ms", declaringClass.getSimpleName() + "." + method.getName(), stopWatch.lastTaskInfo().getTimeMillis());
            }
        }
    }

    /**
     * sql查询
     */
    private Object sqlQuery(BaseDaoSupport<?, ?> baseDaoSupport, Object proxy, Method method, Object[] args) {
        SqlBuilder sqlBuilder = DaoSqlRegistry.getSqlBuilder(method);
        Class<?> parameterizedReturnType =  Beans.getMethodReturnParameterizedTypeFirst(method,
                Record.class);
        // 分页
        if (method.getReturnType().equals(Pageable.class)) {
            GetCountStrategy countStrategy = method.getAnnotation(GetCountStrategy.class);
            PageableRequest pageableRequest = Beans.getObjcetFromMethodArgs(args, PageableRequest.class);
            Map<String, Object> queryMap = (Map<String, Object>) Beans.getObjcetFromMethodArgs(args, Map.class);
            SqlBuilder.SqlResult sqlResult = sqlBuilder.builder(queryMap);
            Pageable<?> pageable;
            Object[] filterParams = sqlResult.getFilterParams();
            if (sqlResult.isUnion()) {
                pageable = baseDaoSupport.paginationBySqlOfUnion(pageableRequest, sqlResult.getSql(), filterParams, sqlResult.getUnionAfterSqlInfo());
            } else {
                Function<String, String> getCountFunc = countStrategy == null ? null : s -> {
                    try {
                        Method declaredMethod = Beans.getDeclaredMethod(method.getDeclaringClass(), countStrategy.value(), String.class);
                        return (String) Beans.invokeDefaultMethod(proxy, declaredMethod, s);
                    } catch (Throwable e) {
                        throw new DaoProxyException("调用getCount方法出错，sql：" + s);
                    }
                };
                pageable = baseDaoSupport.paginationBySql(sqlResult.getSql(), getCountFunc, pageableRequest, filterParams);
            }
            return parameterizedReturnType.equals(Record.class) ? pageable : pageable.toGenericType((Class<? extends Serializable>)parameterizedReturnType);
        }
        // 普通查询
        else {
            Map<String, Object> queryMap = Beans.getFormalParamSimpleMapping(method, args);
            SqlBuilder.SqlResult sqlResult = sqlBuilder.builder(queryMap);
            if (method.getReturnType().equals(List.class)) {
                List<Record> list = baseDaoSupport.findRecord(sqlResult.getSql(), sqlResult.getFilterParams());
                if (parameterizedReturnType.equals(Record.class)) {
                    return list;
                } else if (Beans.isBasicType(parameterizedReturnType)) {
                    return list.stream().map(r -> Beans.getExpectTypeValue(r.values().iterator().next(), parameterizedReturnType)).collect(Collectors.toList());
                } else {
                    return Pojos.mappingArray(list, parameterizedReturnType);
                }
            } else if (method.getReturnType().equals(Record.class)) {
                return baseDaoSupport.findRecordOne(sqlResult.getSql(), sqlResult.getFilterParams());
            } else if (Beans.isBasicType(method.getReturnType())) {
                Record one = baseDaoSupport.findRecordOne(sqlResult.getSql(), sqlResult.getFilterParams());
                Object firstVal = one != null ? new ArrayList<>(one.values()).get(0) : null;
                return Beans.getExpectTypeValue(firstVal, method.getReturnType());
            } else {
                Record one = baseDaoSupport.findRecordOne(sqlResult.getSql(), sqlResult.getFilterParams());
                return Pojos.mapping(one, method.getReturnType());
            }
        }
    }

    /**
     * 缓存中获取Dao class对应的DaoSupport
     */
    private static JpaBaseDaoSupport getJpaBaseDao(Class<? extends BaseDao<?, ?>> daoInterfaceClass) {
        return JPA_BASEDAO_CACHE.computeIfAbsent(daoInterfaceClass.getName(), k -> {
            BaseDaoSupport<?, ?> baseDaoSupport = getBaseDaoSupport(daoInterfaceClass);
            JpaBaseDaoSupport jpaBaseDaoSupport = new JpaBaseDaoSupport(baseDaoSupport.getRealGenericType(), baseDaoSupport.getRealPrimayKeyType(),
                    baseDaoSupport.getTableName());
            jpaBaseDaoSupport.setPrimaryKeyName(baseDaoSupport.getPrimaryKeyName());
            jpaBaseDaoSupport.setJdbcTemplate(baseDaoSupport.getJdbcTemplate());
            return jpaBaseDaoSupport;
        });
    }

    private static BaseDaoSupport<?, ?> getBaseDaoSupport(Class<? extends BaseDao<?, ?>> daoInterfaceClass) {
        return BASEDAO_CACHE.computeIfAbsent(daoInterfaceClass.getName(), k -> {
            Class<?> realGenericType = Beans.getClassGenericType(daoInterfaceClass, 0);
            Class<?> realPrimayKeyType = Beans.getClassGenericType(daoInterfaceClass, 1);
            Method getTableNameMethod = Beans.getSelfDeclaredMethod(daoInterfaceClass, "getTableName");
            if (getTableNameMethod == null) {
                throw new RuntimeException("Dao必须用default覆写getTableName方法");
            }
            String tableName = (String) Beans.invokeDefaultMethod(getTableNameMethod);
            @SuppressWarnings({"rawtypes", "unchecked"}) BaseDaoSupport<?, ?> baseDaoSupport = new BaseDaoSupport(realGenericType, realPrimayKeyType,
                    tableName);
            Method getPrimaryKeyNameMethod = Beans.getSelfDeclaredMethod(daoInterfaceClass, "getPrimaryKeyName");
            if (getPrimaryKeyNameMethod != null) {
                String primaryKeyName = (String) Beans.invokeDefaultMethod(getPrimaryKeyNameMethod);
                baseDaoSupport.setPrimaryKeyName(primaryKeyName);
            }
            baseDaoSupport.setJdbcTemplate(getJdbcTemplate());
            return baseDaoSupport;
        });
    }

    private static JdbcTemplate getJdbcTemplate() {
        try {
            return SpringContextHolder.getApplicationContext().getBean("jdbcTemplate", JdbcTemplate.class);
        } catch (BeansException e) {
            return SpringContextHolder.getApplicationContext().getBean(JdbcTemplate.class);
        }
    }
}
