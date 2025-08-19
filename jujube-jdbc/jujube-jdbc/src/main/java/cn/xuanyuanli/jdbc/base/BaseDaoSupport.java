package cn.xuanyuanli.jdbc.base;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.jdbc.base.dialect.Dialect;
import cn.xuanyuanli.jdbc.base.jpa.entity.RecordEntity;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaEntityClass;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.jdbc.base.util.JdbcPojos;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import cn.xuanyuanli.jdbc.binding.DaoSqlRegistry;
import cn.xuanyuanli.jdbc.binding.SqlBuilder;
import cn.xuanyuanli.jdbc.binding.SqlBuilder.UnionSqlInfo;
import cn.xuanyuanli.jdbc.spring.SpringContextHolder;
import cn.xuanyuanli.jdbc.spring.event.EntitySaveEvent;
import cn.xuanyuanli.jdbc.spring.event.EntityUpdateEvent;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.jdbc.pagination.Pageable;
import cn.xuanyuanli.jdbc.pagination.PageableRequest;
import cn.xuanyuanli.core.lang.Record;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.Collections3;
import cn.xuanyuanli.core.util.DataGenerator;
import cn.xuanyuanli.core.util.Texts;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 基础Dao支持。子类都要重写getTableName()方法 <br> 备注：
 *
 * <pre>
 * 1、查询单行数据，如果为空，则返回null
 * 2、查询单个数据，如果为空，则返回null；如果为int、float、long、double基本类型，则返回默认值
 * 3、查询多行数据，永远不会返回null。判断时用isEmpty()方法即可
 * </pre>
 *
 * @author John Li
 * @date 2022/07/16
 */
@SuppressWarnings({"SqlSourceToSinkFlow", "LoggingSimilarMessage"})
@Slf4j
public class BaseDaoSupport<T extends BaseEntity, PK extends Serializable> implements BaseDao<T, PK> {

    /**
     * 真实的Entity类型
     */
    @Getter
    private final Class<T> realGenericType;
    /**
     * 真实主键的类型
     */
    @Getter
    private final Class<PK> realPrimayKeyType;
    /**
     * 表名
     */
    private final String tableName;
    /**
     * 主键名字
     */
    @Setter
    private String primaryKeyName = "id";

    /**
     * jdbc模板
     */
    @Setter
    @Getter
    private JdbcTemplate jdbcTemplate;

    /**
     * 方言
     */
    public static final Dialect DIALECT = Dialect.DEFAULT;

    /**
     * 列表记录结果设置器
     */
    private final ResultSetExtractor<List<Record>> listRecordResultSetExtractor = rs -> {
        long begin = System.currentTimeMillis();
        List<Record> list = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        List<String> columns = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            columns.add(JdbcUtils.lookupColumnName(rsmd, i));
        }
        while (rs.next()) {
            Record e = new Record(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                e.set(columns.get(i - 1), JdbcUtils.getResultSetValue(rs, i));
            }
            list.add(e);
        }
        if (log.isDebugEnabled()) {
            log.debug("ResultSet转换为Record时间：{}ms", System.currentTimeMillis() - begin);
        }
        return list;
    };

    /**
     * 基地数据访问支持
     *
     * @param realGenericType   真正泛型类型
     * @param realPrimayKeyType 真正primay键类型
     * @param tableName         表名
     */
    public BaseDaoSupport(Class<T> realGenericType, Class<PK> realPrimayKeyType, String tableName) {
        this.realGenericType = realGenericType;
        this.realPrimayKeyType = realPrimayKeyType;
        this.tableName = tableName;
    }

    /**
     * 对象转换为Record
     *
     * @param obj obj
     * @return {@link Record}
     */
    private static <T extends BaseEntity> Record toRecord(T obj) {
        Record record = new Record();
        List<JdbcPojos.FieldColumn> fieldColumns = JdbcPojos.getFieldColumns(new JavaEntityClass(obj.getClass()));
        for (JdbcPojos.FieldColumn fieldColumn : fieldColumns) {
            if (fieldColumn.getVisual()) {
                continue;
            }
            Object property = Beans.getProperty(obj, fieldColumn.getField());
            if (property != null) {
                record.set(fieldColumn.getColumn(), property);
                if (property.equals(BaseEntity.STRING_NULL)) {
                    record.set(fieldColumn.getColumn(), null);
                }
            }
        }
        return record;
    }

    /**
     * 保存
     *
     * @param t t
     * @return {@link PK}
     */
    @Override
    public PK save(T t) {
        Record currenRecord = toRecord(t);

        List<Object> params = new ArrayList<>();
        String sql = DIALECT.forDbSave(getTableName(), currenRecord, params);
        if (sql.isEmpty()) {
            return DataGenerator.generateDefaultValueByParamType(realPrimayKeyType);
        }
        PK id = save(sql, params.toArray());
        id = Beans.getExpectTypeValue(id, getRealPrimayKeyType());
        if (id != null) {
            Beans.setProperty(t, getPrimaryKeyName(), id);
        }
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    SpringContextHolder.getApplicationContext().publishEvent(new EntitySaveEvent(t));
                }
            });
        } else {
            SpringContextHolder.getApplicationContext().publishEvent(new EntitySaveEvent(t));
        }
        return id;
    }

    /**
     * 更新
     *
     * @param t t
     * @return boolean
     */
    @Override
    public boolean update(T t) {
        Record currenRecord = toRecord(t);
        String primaryKeyName = getPrimaryKeyName();
        Object id = currenRecord.get(primaryKeyName);
        if (id == null) {
            throw new IllegalArgumentException("没有id（更新数据库表）");
        }
        currenRecord.remove(primaryKeyName);
        List<Object> paras = new ArrayList<>();
        String sql = DIALECT.forDbUpdate(getTableName(), primaryKeyName, id, currenRecord, paras);
        if (sql.isEmpty()) {
            return false;
        }
        boolean result = getJdbcTemplate().update(sql, paras.toArray()) > 0;
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", Sqls.realSql(sql, Collections.singletonList(paras)), StringUtils.join(paras, ","));
        }
        if (result) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        SpringContextHolder.getApplicationContext().publishEvent(new EntityUpdateEvent(t));
                    }
                });
            } else {
                SpringContextHolder.getApplicationContext().publishEvent(new EntityUpdateEvent(t));
            }
        }

        return result;
    }

    /**
     * 保存或更新
     *
     * @param t t
     * @return {@link PK}
     */
    @Override
    public PK saveOrUpdate(T t) {
        String primaryKeyName = getPrimaryKeyName();
        Object pk = Beans.getProperty(t, primaryKeyName);
        // 保存
        if (pk == null) {
            pk = save(t);
        } else {
            update(t);
        }
        Beans.setProperty(t, getPrimaryKeyName(), pk);
        return Beans.getExpectTypeValue(pk, getRealPrimayKeyType());
    }

    /**
     * 删除通过id
     *
     * @param id id
     * @return boolean
     */
    @Override
    public boolean deleteById(PK id) {
        String sql = DIALECT.forDbDeleteById(getTableName(), getPrimaryKeyName());
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, id);
        }
        return getJdbcTemplate().update(sql, id) > 0;
    }

    /**
     * 根据条件删除数据
     *
     * @param spec 规范
     * @return boolean
     */
    public boolean delete(Spec spec) {
        if (spec.isEmpty()) {
            throw new IllegalArgumentException("此为删除全部，请谨慎操作");
        }
        String sql = DIALECT.forDbDelete(getTableName(), spec.getFilterSql());
        Object[] filterParams = spec.getFilterParams();
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(filterParams, ","));
        }
        return getJdbcTemplate().update(sql, filterParams) > 0;
    }

    /**
     * 批量更新数据
     *
     * @param sql sql
     */
    public void batchUpdate(String sql) {
        getJdbcTemplate().update(sql);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}]", sql);
        }
    }

    /**
     * 批量更新
     *
     * @param sql   sql
     * @param param 参数
     */
    public void batchUpdate(String sql, Object... param) {
        getJdbcTemplate().update(sql, param);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", sql, StringUtils.join(param, ","));
        }
    }

    /**
     * 批量更新
     *
     * @param list 列表
     */
    @Override
    public void batchUpdate(List<T> list) {
        for (T t : list) {
            try {
                saveOrUpdate(t);
            } catch (Exception e) {
                log.error("batchUpdate", e);
            }
        }
    }

    /**
     * 发现通过id
     *
     * @param id id
     * @return {@link T}
     */
    @SuppressWarnings("DaoJpaMethodInspection")
    @Override
    public T findById(PK id) {
        if (id == null) {
            return null;
        }
        return findOne(newSpec().eq(getPrimaryKeyName(), id));
    }

    /**
     * 存在
     *
     * @param id id
     * @return boolean
     */
    @Override
    public boolean exists(PK id) {
        return findById(getPrimaryKeyName(), id) != null;
    }

    /**
     * 发现通过id
     *
     * @param fields 字段
     * @param id     id
     * @return {@link T}
     */
    @SuppressWarnings("DaoJpaMethodInspection")
    @Override
    public T findById(String fields, PK id) {
        if (id == null) {
            return null;
        }
        return findOne(fields, newSpec().eq(getPrimaryKeyName(), id));
    }

    /**
     * 根据sql和params获得数据，用于where后还有order by，group by等的情况.永远不会返回null
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link List}<{@link T}>
     */
    public List<T> find(String sql, Object[] params) {
        List<Record> list = findRecord(sql, params);
        if (RecordEntity.class.isAssignableFrom(realGenericType)) {
            return list.stream().map(r -> (T) new RecordEntity(r)).collect(Collectors.toList());
        } else {
            return JdbcPojos.mappingArray(list, realGenericType);
        }
    }

    /**
     * 找到记录
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link List}<{@link Record}>
     */
    public List<Record> findRecord(String sql, Object[] params) {
        SqlQueryPostHandler.SqlQuery sqlQuery = sqlPostHandle(sql, params);
        sql = sqlQuery.getSql();
        params = sqlQuery.getParams();
        List<Record> list = getJdbcTemplate().query(sql, listRecordResultSetExtractor, params);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", Sqls.realSql(sql, Arrays.asList(params)), StringUtils.join(params, ","));
        }
        return list;
    }

    /**
     * sql后置处理
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link SqlQueryPostHandler.SqlQuery}
     */
    private SqlQueryPostHandler.SqlQuery sqlPostHandle(String sql, Object[] params) {
        List<SqlQueryPostHandler> sqlQueryPostHandlers = DaoSqlRegistry.getSqlQueryPostHandlers();
        if (!sqlQueryPostHandlers.isEmpty()) {
            for (SqlQueryPostHandler sqlQueryPostHandler : sqlQueryPostHandlers) {
                SqlQueryPostHandler.SqlQuery sqlQuery = sqlQueryPostHandler.postHandle(sql, params);
                sql = sqlQuery.getSql();
                params = sqlQuery.getParams();
            }
        }
        return new SqlQueryPostHandler.SqlQuery(sql, params);
    }

    /**
     * 找到记录
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link Record}
     */
    public Record findRecordOne(String sql, Object[] params) {
        List<Record> record = findRecord(sql, params);
        if (record != null && !record.isEmpty()) {
            return record.get(0);
        }
        return null;
    }

    /**
     * 找到所有
     *
     * @return {@link List}<{@link T}>
     */
    @Override
    public List<T> findAll() {
        String sql = DIALECT.forDbSimpleQuery("*", getTableName());
        return find(sql, toArrary());
    }

    /**
     * 找到id
     *
     * @return {@link List}<{@link PK}>
     */
    @Override
    public List<PK> findIds() {
        List<T> list = find(getPrimaryKeyName(), newSpec());
        return Collections3.extractToList(list, getPrimaryKeyName());
    }

    /**
     * 构建查询规格获得数据.永远不会返回null
     *
     * @param spec 规范
     * @return {@link List}<{@link T}>
     */
    @SuppressWarnings("unused")
    public List<T> find(Spec spec) {
        return find("*", spec);
    }

    /**
     * 构建查询规格获得数据，可自定义select与from之间要查询的字段.永远不会返回null
     *
     * @param fields 字段
     * @param spec   规范
     * @return {@link List}<{@link T}>
     */
    public List<T> find(String fields, Spec spec) {
        // 如果查询条件为空，则不进行查询，防止搜索全表(除非fields为主键)
        if (spec.isEmpty() && StringUtils.isBlank(spec.getGroupBy()) && spec.getLimit() <= 0 && spec.sort().isEmpty() && !getPrimaryKeyName().equals(fields)) {
            return new ArrayList<>();
        }
        String securityTableName = DIALECT.getSecurityTableName(getTableName());
        String sql = DIALECT.forDbSimpleQuery(DIALECT.getSecurityFields(fields, securityTableName), getTableName(), spec.getFilterSql(securityTableName));
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + DIALECT.getSecurityFields(spec.getGroupBy(), securityTableName));
        }
        if (StringUtils.isNotBlank(spec.getHaving())) {
            sql += (" having " + spec.getHaving());
        }
        sql += spec.sort().buildSqlSort();
        int begin = Math.max(spec.getLimitBegin(), 0);
        if (spec.getLimit() > 0) {
            sql = DIALECT.forDbPaginationQuery(sql, begin, spec.getLimit());
        }
        return find(sql, spec.getFilterParams());
    }

    /**
     * 构建查询规格获得一条数据
     *
     * @param spec 规范
     * @return {@link T}
     */
    public T findOne(Spec spec) {
        return findOne("*", spec);
    }

    /**
     * 构建查询规格获得一条数据，可自定义select与from之间要查询的字段
     *
     * @param fields 字段
     * @param spec   规范
     * @return {@link T}
     */
    public T findOne(String fields, Spec spec) {
        spec.limit(1);
        List<T> list = find(fields, spec);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 总数的查询
     *
     * @param spec 规范
     * @return long
     */
    public long getCount(Spec spec) {
        spec.limitBegin(0).limit(Integer.MAX_VALUE);
        String securityTableName = DIALECT.getSecurityTableName(getTableName());
        String sql = DIALECT.forDbSimpleQuery("count(*)", getTableName(), spec.getFilterSql(securityTableName));
        Object[] filterParams = spec.getFilterParams();
        SqlQueryPostHandler.SqlQuery sqlQuery = sqlPostHandle(sql, filterParams);
        sql = sqlQuery.getSql();
        filterParams = sqlQuery.getParams();
        if (StringUtils.isNotBlank(spec.getGroupBy())) {
            sql += (" group by " + DIALECT.getSecurityFields(spec.getGroupBy(), securityTableName));
            if (StringUtils.isNotBlank(spec.getHaving())) {
                sql += (" having " + spec.getHaving());
            }
            sql = Sqls.getCountSql(sql);
        }
        return Optional.ofNullable(queryForLong(sql, filterParams)).orElse(0L);
    }

    /**
     * 总数的查询
     *
     * @param sumField 和领域
     * @param spec     规范
     * @return double
     */
    public double getSumOf(String sumField, Spec spec) {
        spec.limitBegin(0).limit(Integer.MAX_VALUE);
        String securityTableName = DIALECT.getSecurityTableName(getTableName());
        String sql = DIALECT.forDbSimpleQuery("sum(" + DIALECT.getSecurityFields(sumField, securityTableName) + ")", getTableName(),
                spec.getFilterSql(securityTableName));
        Object[] filterParams = spec.getFilterParams();
        SqlQueryPostHandler.SqlQuery sqlQuery = sqlPostHandle(sql, filterParams);
        sql = sqlQuery.getSql();
        filterParams = sqlQuery.getParams();
        return Optional.ofNullable(queryForDouble(sql, filterParams)).orElse(0.0);
    }

    /**
     * 根据sql进行分页处理
     *
     * @param sql          sql
     * @param getCountFunc 获得数函数
     * @param request      请求
     * @param filterParams 过滤器参数
     * @return {@link Pageable}<{@link Record}>
     */
    @SuppressWarnings("LoggingSimilarMessage")
    public Pageable<Record> paginationBySql(String sql, Function<String, String> getCountFunc, PageableRequest request, Object... filterParams) {
        if (getCountFunc == null) {
            getCountFunc = Sqls::getCountSql;
        }
        request = PageableRequest.buildPageRequest(request);
        Pageable<Record> pageable = request.newPageable();
        pageable.setTotalElements(request.getTotalElements());

        if (request.getIndex() == 1 || request.getTotalElements() < 1) {
            long begin = System.currentTimeMillis();
            String countSql = getCountFunc.apply(sql);
            Long count = getJdbcTemplate().queryForObject(countSql, Long.class, filterParams);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}], 执行时间：{}ms", Sqls.realSql(countSql, Arrays.asList(filterParams)), StringUtils.join(filterParams, ","),
                        System.currentTimeMillis() - begin);
            }
            count = count == null ? 0 : count;
            pageable.setTotalElements(count);
        }

        long begin = System.currentTimeMillis();
        String cSql = DIALECT.forDbPaginationQuery(sql, pageable.getStart(), pageable.getSize());
        pageable.setData(new ArrayList<>());
        if (pageable.getTotalElements() > 0) {
            List<Record> list = getJdbcTemplate().query(cSql, listRecordResultSetExtractor, filterParams);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}], 执行时间：{}ms", Sqls.realSql(cSql, Arrays.asList(filterParams)), StringUtils.join(filterParams, ","),
                        System.currentTimeMillis() - begin);
            }
            pageable.setData(list);
        }
        return pageable;
    }

    /**
     * 根据sql进行分页处理，用于两个集合union分页
     *
     * @param request       请求
     * @param sql1          sql1
     * @param filterParams1 过滤器params1
     * @param unionSqlInfos 联盟sql信息
     * @return {@link Pageable}<{@link Record}>
     */
    public Pageable<Record> paginationBySqlOfUnion(PageableRequest request, String sql1, Object[] filterParams1, List<SqlBuilder.UnionSqlInfo> unionSqlInfos) {
        request = PageableRequest.buildPageRequest(request);
        Pageable<Record> pageable = request.newPageable();
        long totalCount = 0;

        // 将基础sql封装到集合中做统一处理
        SqlBuilder.UnionSqlInfo baseSql = new SqlBuilder.UnionSqlInfo().setSql(sql1).setFilterParams(filterParams1);
        unionSqlInfos.add(0, baseSql);
        // 计算数据总量
        for (SqlBuilder.UnionSqlInfo sqlInfo : unionSqlInfos) {
            String countSql = Sqls.getCountSql(sqlInfo.getSql());
            Object[] filterParams = sqlInfo.getFilterParams();
            // 如果参数个数大于sql中的参数个数，则截取参数个数（造成这种情况，是因为getCountSql会截取order by部分）
            int actulParamSize = Texts.regQuery("\\?", countSql).size();
            if (actulParamSize < filterParams.length) {
                filterParams = Arrays.copyOf(filterParams, actulParamSize);
            }
            Long count = getJdbcTemplate().queryForObject(countSql, Long.class, filterParams);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", Sqls.realSql(countSql, Arrays.asList(filterParams)), StringUtils.join(filterParams, ","));
            }
            count = count == null ? 0 : count;
            sqlInfo.setSqlCount(count);
            totalCount += count;
        }
        pageable.setTotalElements(totalCount);
        pageable.setData(new ArrayList<>());

        // 若总数据量不为空则查询并封装数据
        if (pageable.getTotalElements() > 0) {
            // 当前查询页结束数量
            long curSizeEnd = pageable.getStart() + pageable.getSize();
            // 累计查询数量
            long cumulativeNnm = 0;
            // 当前sql的起始索引
            long currentSqlIndex = pageable.getStart();
            // 剩余数据数量
            int surplusDataNum = pageable.getSize();
            // 数据集合
            List<Record> list = new ArrayList<>();

            for (SqlBuilder.UnionSqlInfo sqlInfo : unionSqlInfos) {
                cumulativeNnm += sqlInfo.getSqlCount();
                currentSqlIndex = Math.max(currentSqlIndex, 0);
                if (surplusDataNum > 0) {
                    // 如果第一个集合够填满当前分页,只取第一个集合内数据
                    Object[] filterParams = sqlInfo.getFilterParams();
                    if (cumulativeNnm > curSizeEnd) {
                        List<Record> listMap = queryListForPage(currentSqlIndex, surplusDataNum, sqlInfo, filterParams);
                        list.addAll(listMap);
                        surplusDataNum = 0;
                        // 如果第一个集合不足以填满,则将第一个集合数据取完,不足的数据在下次循环中补充
                    } else if (cumulativeNnm > pageable.getStart()) {
                        List<Record> listMap = queryListForPage(currentSqlIndex, surplusDataNum, sqlInfo, filterParams);
                        surplusDataNum -= listMap.size();
                        list.addAll(listMap);
                    }
                }
                curSizeEnd -= sqlInfo.getSqlCount();
                currentSqlIndex -= sqlInfo.getSqlCount();
            }
            // 若数据不为空则封装到分页对象中
            if (!list.isEmpty()) {
                pageable.setData(list);
            }
        }
        return pageable;
    }

    /**
     * 查询列表为分页
     *
     * @param currentSqlIndex 当前sql索引
     * @param surplusDataNum  盈余数据num
     * @param sqlInfo         sql信息
     * @param filterParams    过滤器参数
     * @return {@link List}<{@link Record}>
     */
    private List<Record> queryListForPage(long currentSqlIndex, int surplusDataNum, UnionSqlInfo sqlInfo, Object[] filterParams) {
        String cSql = DIALECT.forDbPaginationQuery(sqlInfo.getSql(), currentSqlIndex, surplusDataNum);
        List<Record> listMap = getJdbcTemplate().query(cSql, listRecordResultSetExtractor, filterParams);
        if (log.isDebugEnabled()) {
            log.debug("sql:[{}], params:[{}]", Sqls.realSql(cSql, Arrays.asList(filterParams)), StringUtils.join(filterParams, ","));
        }
        return listMap;
    }

    /**
     * 长时间查询
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link Long}
     */
    private Long queryForLong(String sql, Object... params) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", Sqls.realSql(sql, Arrays.asList(params)), StringUtils.join(params, ","));
            }
            return getJdbcTemplate().queryForObject(sql, Long.class, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 查询双
     *
     * @param sql    sql
     * @param params 参数
     * @return {@link Double}
     */
    private Double queryForDouble(String sql, Object... params) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", Sqls.realSql(sql, Arrays.asList(params)), StringUtils.join(params, ","));
            }
            return getJdbcTemplate().queryForObject(sql, Double.class, params);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 可以返回id的方法,如果保存失败，会返回-1
     *
     * @param sql  sql
     * @param args 参数
     * @return {@link PK}
     */
    private PK save(final String sql, final Object... args) {
        PK result;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            getJdbcTemplate().update((Connection conn) -> getPreparedStatement(sql, conn, args), keyHolder);
            if (log.isDebugEnabled()) {
                log.debug("sql:[{}], params:[{}]", Sqls.realSql(sql, Arrays.asList(args)), StringUtils.join(args, ","));
            }
            result = (PK) keyHolder.getKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 获得准备声明
     *
     * @param sql  sql
     * @param conn 连接
     * @param args sql参数
     * @return {@link PreparedStatement}
     * @throws SQLException sqlexception异常
     */
    public static PreparedStatement getPreparedStatement(String sql, Connection conn, Object[] args) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        if (args != null && args.length != 0) {
            for (int j = 0; j < args.length; j++) {
                ps.setObject(j + 1, args[j]);
            }
        }
        return ps;
    }

    /**
     * 对易行
     *
     * @param objects 对象
     * @return {@link Object[]}
     */
    public Object[] toArrary(Object... objects) {
        return objects;
    }

    /**
     * 新规范
     *
     * @return {@link Spec}
     */
    public Spec newSpec() {
        return new Spec();
    }

    /**
     * 获得主键名字
     *
     * @return {@link String}
     */
    @Override
    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    /**
     * 获得表名字
     *
     * @return {@link String}
     */
    @Override
    public String getTableName() {
        return tableName;
    }

}
