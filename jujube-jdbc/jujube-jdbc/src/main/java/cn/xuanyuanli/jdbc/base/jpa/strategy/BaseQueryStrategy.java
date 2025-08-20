package cn.xuanyuanli.jdbc.base.jpa.strategy;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.jdbc.base.BaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.JpaBaseDaoSupport;
import cn.xuanyuanli.jdbc.base.jpa.entity.RecordEntity;
import cn.xuanyuanli.jdbc.base.jpa.handler.DefaultHandlerChain;
import cn.xuanyuanli.jdbc.base.jpa.handler.HandlerContext;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DbAndEntityFiled;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.Query;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.JdbcPojos;
import cn.xuanyuanli.jdbc.base.util.Strings;
import cn.xuanyuanli.jdbc.binding.DaoSqlRegistry;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Pojos;
import cn.xuanyuanli.core.util.Texts;

/**
 * 查询策略
 *
 * @author xuanyuanli
 */
@Slf4j
public abstract class BaseQueryStrategy {

    private static final ConcurrentMap<String, List<DbAndEntityFiled>> QUERY_FIELD_LIST_CACHE = new ConcurrentHashMap<>();

    /**
     * 是否承认
     *
     * @param methodName 方法名称
     * @return boolean
     */
    public abstract boolean accept(String methodName);

    /**
     * 得到查询信息
     *
     * @param tableName 表名
     * @param method    方法
     * @param args      方法实参
     * @return {@link Query}
     */
    @SuppressWarnings("unused")
    public abstract Query getQuery(String tableName, DaoMethod method, Object[] args);

    /**
     * 执行查询
     *
     * @param proxyDao basedao
     * @param method   方法
     * @param args     方法的入参
     * @return 查询结果
     */
    abstract Object query(JpaBaseDaoSupport proxyDao, Method method, Object[] args);

    /**
     * 数据库表对应的列名称
     *
     * @param method          Dao中的方法
     * @param entityFieldName 查询的Entity Class字段名称
     * @return 数据库列名
     */
    public static String getDbColumnName(DaoMethod method, String entityFieldName) {
        return DaoSqlRegistry.getDbColumnName(method.getEntityClass(), entityFieldName);
    }

    /**
     * 获取数据库表列名
     *
     * @param method     方法
     * @param queryField 方法名中的查询字段
     * @return {@link List}<{@link DbAndEntityFiled}>
     */
    public static List<DbAndEntityFiled> getDbColumnNames(DaoMethod method, String queryField) {
        return QUERY_FIELD_LIST_CACHE.computeIfAbsent(method.toString() + "#" + queryField, key -> {
            List<DbAndEntityFiled> fieldList = new ArrayList<>();
            String[] arr = Strings.splitByAnd(queryField);
            for (String field : arr) {
                fieldList.add(new DbAndEntityFiled(getDbColumnName(method, field), field));
            }
            return fieldList;
        });
    }

    /**
     * 用所有的Handler处理当前方法
     */
    Spec getSpecOfAllHandler(DaoMethod method, Object[] args, String tmname) {
        Spec spec = new Spec();
        DefaultHandlerChain handlerChain = new DefaultHandlerChain();
        handlerChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        handlerChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        handlerChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        handlerChain.handler(method, spec, tmname, Lists.newArrayList(args));
        return spec;
    }

    /**
     * 获得要JPA方法要查询的固定字段列表，支持子查询
     *
     * @param method    方法
     * @param tableName 表名
     * @return {@link List}<{@link String}>
     */
    public static List<String> getSelectFieldsByMethod(DaoMethod method, String tableName) {
        List<String> list = new ArrayList<>();
        if (method.hasSelectFieldAnnotation()) {
            list.addAll(Arrays.stream(method.getSelectFieldAnnotationValue()).map(e -> {
                if (e.trim().startsWith("(") || e.contains("$")) {
                    String[] arr = Texts.getGroups("\\$\\{(.*?)\\}", e);
                    String field = arr[1];
                    return StringUtils.replace(e, arr[0], BaseDaoSupport.DIALECT.getSecurityFields(field, tableName));
                } else {
                    return e;
                }
            }).toList());
        }
        return list;
    }

    /**
     * 查询
     *
     * @param proxyDao     代理数据访问
     * @param method       方法
     * @param spec         规范
     * @param selectFields 选择字段
     * @return {@link Object}
     */
    public Object query(JpaBaseDaoSupport proxyDao, Method method, Spec spec, List<String> selectFields) {
        boolean isFindOne = !List.class.equals(method.getReturnType());
        if (selectFields.isEmpty()) {
            selectFields.add("*");
        }
        String fields = String.join(",", selectFields);
        boolean findOneField = selectFields.size() == 1 && !"*".equals(selectFields.get(0));
        if (isFindOne) {
            RecordEntity queryOne = proxyDao.findOne(fields, spec);
            Class<?> returnType =  method.getReturnType();
            if (returnType.equals(proxyDao.getOriginalRealGenericType())) {
                return JdbcPojos.mapping(queryOne, (Class<? extends BaseEntity>)returnType);
            } else if (findOneField) {
                Object val = queryOne != null && !queryOne.isEmpty() ? queryOne.values().iterator().next() : null;
                return Beans.getExpectTypeValue(val, returnType);
            } else {
                // 非Entity类型的映射，直接使用Pojos
                return Pojos.mapping(queryOne, returnType);
            }
        } else {
            List<RecordEntity> queryList = proxyDao.find(fields, spec);
            Class<?> returnParameterizedType1 = Beans.getMethodReturnParameterizedTypeFirst(method, proxyDao.getOriginalRealGenericType());
            if (returnParameterizedType1.equals(proxyDao.getOriginalRealGenericType())) {
                return JdbcPojos.mappingArray(queryList, (Class<? extends BaseEntity>) returnParameterizedType1);
            } else if (findOneField) {
                return queryList.stream().map(e -> Beans.getExpectTypeValue(e.values().iterator().next(), returnParameterizedType1))
                        .collect(Collectors.toList());
            } else {
                return Pojos.mappingArray(queryList, returnParameterizedType1);
            }
        }
    }

}
