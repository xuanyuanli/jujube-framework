package cn.xuanyuanli.jdbc.base.spec;

import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import cn.xuanyuanli.jdbc.exception.DaoQueryException;
import cn.xuanyuanli.jdbc.base.spec.SpecSupport.Op;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import cn.xuanyuanli.core.util.Texts;

/**
 * 构建查询规格（Specification）
 *
 * @author xuanyuanli Email：xuanyuanli999@gmail.com
 * @date 2022/07/16
 */
@EqualsAndHashCode
public final class Spec implements Cloneable {

    /**
     * 条件
     */
    private final List<QueryCondition> conditions = new ArrayList<>();
    /**
     * 规范map
     */
    private Map<String, Object> specMap = Maps.newLinkedHashMap();
    /**
     * 排序
     */
    private Sort sort = new Sort(this);
    /**
     * 分组
     */
    private String groupBy;
    /**
     * having
     */
    private String having;

    /**
     * 限制
     */
    private int limit;
    /**
     * 限制开始
     */
    private int limitBegin;

    /**
     * 获得规范map
     *
     * @return {@link Map}<{@link String}, {@link Object}>
     */
    Map<String, Object> getSpecMap() {
        return specMap;
    }

    /**
     * 等于
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec eq(String fieldName, Object value) {
        // 不为null；如果为String类型，不为空
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.EQ, fieldName), value);
        } else {
            throw new DaoQueryException("eq查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 是否不空白
     *
     * @param value 价值
     * @return boolean
     */
    private boolean isNotBlank(Object value) {
        if (value == null) {
            return false;
        }
        return (!(value instanceof String) || ((String) value).length() != 0);
    }

    /**
     * like
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec like(String fieldName, Object value) {
        if (verifyLikeValue(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.LIKE, fieldName), value);
        } else {
            throw new DaoQueryException("like查询的值不符合规则。字段：" + fieldName + "，值为空或仅有%符号");
        }
        return this;
    }

    /**
     * JSON_CONTAINS
     *
     * @param fieldName 字段名
     * @param value     价值
     * @param path      路径
     * @return {@link Spec}
     */
    @SuppressWarnings("UnusedReturnValue")
    public Spec jsonContains(String fieldName, Object value, String path) {
        specMap.put(SpecSupport.Op.join(Op.JSON_CONTAINS, fieldName), new Object[]{value, path});
        return this;
    }

    /**
     * 验证等价值
     *
     * @param value 价值
     * @return boolean
     */
    private boolean verifyLikeValue(Object value) {
        String symbol1 = "%";
        String symbol2 = "%%";
        return isNotBlank(value) && !symbol1.equals(value) && !symbol2.equals(value);
    }

    /**
     * not like
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec notlike(String fieldName, Object value) {
        if (verifyLikeValue(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.NOTLIKE, fieldName), value);
        } else {
            throw new DaoQueryException("not like查询的值不符合规则。字段：" + fieldName + "，值为空或仅有%符号");
        }
        return this;
    }

    /**
     * 大于
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec gt(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.GT, fieldName), value);
        } else {
            throw new DaoQueryException("gt查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 小于
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec lt(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.LT, fieldName), value);
        } else {
            throw new DaoQueryException("lt查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 大于等于
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec gte(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.GTE, fieldName), value);
        } else {
            throw new DaoQueryException("gte查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 小于等于
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec lte(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.LTE, fieldName), value);
        } else {
            throw new DaoQueryException("lte查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 不等于<>
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec not(String fieldName, Object value) {
        if (isNotBlank(value)) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.NOT, fieldName), value);
        } else {
            throw new DaoQueryException("<>查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * is null
     *
     * @param fieldName 字段名
     * @return {@link Spec}
     */
    public Spec isNull(String fieldName) {
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.ISNULL, fieldName), null);
        return this;
    }

    /**
     * is not null
     *
     * @param fieldName 字段名
     * @return {@link Spec}
     */
    public Spec isNotNull(String fieldName) {
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.ISNOTNULL, fieldName), null);
        return this;
    }

    /**
     * = ''
     *
     * @param fieldName 字段名
     * @return {@link Spec}
     */
    public Spec isEmpty(String fieldName) {
        specMap.put(SpecSupport.Op.join(Op.ISEMPTY, fieldName), null);
        return this;
    }

    /**
     * <> ''
     *
     * @param fieldName 字段名
     * @return {@link Spec}
     */
    public Spec isNotEmpty(String fieldName) {
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.ISNOTEMPTY, fieldName), null);
        return this;
    }

    /**
     * in
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec in(String fieldName, Iterable<?> value) {
        if (value != null && value.iterator().hasNext()) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.IN, fieldName), value);
        } else {
            throw new DaoQueryException("in查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * not in
     *
     * @param fieldName 字段名
     * @param value     价值
     * @return {@link Spec}
     */
    public Spec notin(String fieldName, Iterable<?> value) {
        if (value != null && value.iterator().hasNext()) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.NOTIN, fieldName), value);
        } else {
            throw new DaoQueryException("not in查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * or
     *
     * @param rule 最少为两个入参
     * @return {@link Spec}
     */
    public Spec or(Spec... rule) {
        Validate.isTrue(rule != null && rule.length >= 2);
        // 此处的字段名spec是无意义的,因为解析的时候用不到它，可以为任何非空值
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.OR, "spec"), rule);
        return this;
    }

    /**
     * and
     *
     * @param rule 规则
     * @return {@link Spec}
     */
    public Spec and(Spec... rule) {
        Validate.isTrue(rule != null && rule.length != 0);
        // 此处的字段名spec是无意义的,因为解析的时候用不到它，可以为任何非空值
        specMap.put(SpecSupport.Op.join(SpecSupport.Op.AND, "spec"), rule);
        return this;
    }

    /**
     * between
     *
     * @param fieldName 字段名
     * @param valuePrev 价值:
     * @param valueNext 下一个值
     * @return {@link Spec}
     */
    public Spec between(String fieldName, Object valuePrev, Object valueNext) {
        if (valuePrev != null && valueNext != null && StringUtils.isNotBlank(Objects.toString(valuePrev)) && StringUtils.isNotBlank(
                Objects.toString(valueNext))) {
            specMap.put(SpecSupport.Op.join(SpecSupport.Op.BETWEEN, fieldName), new Object[]{valuePrev, valueNext});
        } else {
            throw new DaoQueryException("between查询的值不符合规则。字段：" + fieldName + "，值为空");
        }
        return this;
    }

    /**
     * 返回排序对象
     *
     * @return {@link Sort}
     */
    public Sort sort() {
        return sort;
    }

    /**
     * 集团
     *
     * @param groupBy 集团
     */
    public void groupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * 获得集团通过
     *
     * @return {@link String}
     */
    public String getGroupBy() {
        return groupBy;
    }

    /**
     * 获得有
     *
     * @return {@link String}
     */
    public String getHaving() {
        return having;
    }

    /**
     * 查询条数限制
     *
     * @param size 大小
     * @return {@link Spec}
     */
    public Spec limit(int size) {
        limit = size;
        return this;
    }

    /**
     * 从第几条开始查询
     *
     * @param end 结束
     * @return {@link Spec}
     */
    public Spec limitBegin(int end) {
        limitBegin = end;
        return this;
    }

    /**
     * 获得限制开始
     *
     * @return int
     */
    public int getLimitBegin() {
        return limitBegin;
    }

    /**
     * 获得限制
     *
     * @return int
     */
    public int getLimit() {
        return limit;
    }

    /**
     * 大小
     *
     * @return int
     */
    public int size() {
        return specMap.size();
    }

    /**
     * 是否空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return specMap.isEmpty();
    }

    /**
     * 获得过滤条件的sql
     *
     * @return {@link String}
     */
    public String getFilterSql() {
        conditions.clear();
        String result = buildQuerySpecification(this, null);
        if (StringUtils.isBlank(result)) {
            result = "1=1";
        }
        return result;
    }

    /**
     * 获得过滤条件的sql
     *
     * @param alias 表别名
     * @return {@link String}
     */
    public String getFilterSql(String alias) {
        conditions.clear();
        String result = buildQuerySpecification(this, alias);
        if (StringUtils.isBlank(result)) {
            result = "1=1";
        }
        return result;
    }

    /**
     * 获得过滤条件的params(必须先执行getFilterSql方法，namedParam才会有值)
     *
     * @return {@link Object[]}
     */
    public Object[] getFilterParams() {
        return getConditions().stream().flatMap(e -> Arrays.stream(e.getValues())).toArray();
    }

    /**
     * 获得查询条件
     *
     * @return {@link List}<{@link QueryCondition}>
     */
    public List<QueryCondition> getConditions() {
        return conditions;
    }

    /**
     * 构建数据规格说明
     *
     * @param spec  数据规格
     * @param alias 表别名
     * @return {@link String}
     */
    private String buildQuerySpecification(Spec spec, String alias) {
        if (getSpecMap().isEmpty()) {
            return "";
        }
        StringBuilder specSql = new StringBuilder();
        Iterator<SpecSupport> filterIterator = SpecSupport.parse(spec.getSpecMap()).values().iterator();
        while (filterIterator.hasNext()) {
            SpecSupport filter = filterIterator.next();
            switch (filter.operator) {
                case EQ:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append("= ?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case LIKE:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" like ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case NOTLIKE:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" not like ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case GT:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" > ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case LT:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" < ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case GTE:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" >= ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case LTE:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" <= ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case BETWEEN:
                    Object[] arr = (Object[]) filter.value;
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" between ").append("?")
                            .append(" and ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{arr[0], arr[1]}, 2));
                    break;
                case ISNOTNULL:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" is not null");
                    break;
                case ISNULL:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" is null");
                    break;
                case ISEMPTY:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" = ''");
                    break;
                case ISNOTEMPTY:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" <> ''");
                    break;
                case NOT:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" <> ").append("?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{filter.value}, 1));
                    break;
                case IN:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" in(")
                            .append(getInPattern(filter.value)).append(")");
                    conditions.add(new QueryCondition(filter.fieldName, expendInValue(filter.value), 1));
                    break;
                case NOTIN:
                    specSql.append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(" not in(")
                            .append(getInPattern(filter.value)).append(")");
                    conditions.add(new QueryCondition(filter.fieldName, expendInValue(filter.value), 1));
                    break;
                case JSON_CONTAINS:
                    Object[]  arr2 = (Object[]) filter.value;
                    boolean noPath = StringUtils.isBlank((String) arr2[1]);
                    specSql.append(" json_contains(").append(getTableAliasPrefix(alias)).append(Sqls.getSecurityFieldName(filter.fieldName)).append(", ?");
                    conditions.add(new QueryCondition(filter.fieldName, new Object[]{getJsonSearchVal(arr2[0])}, 1));
                    if (noPath) {
                        specSql.append(")");
                    } else {
                        specSql.append(", ?)");
                        conditions.add(new QueryCondition(filter.fieldName, new Object[]{arr2[1]}, 1));
                    }
                    break;
                case OR:
                case AND:
                    andHander(alias, specSql, filter);
                    break;
                default:
                    throw new RuntimeException("非法操作符");
            }
            if (filterIterator.hasNext()) {
                specSql.append(" and ");
            }
        }
        return specSql.toString();
    }

    /**
     * 消耗价值
     *
     * @param obj obj
     * @return {@link Object[]}
     */
    private Object[] expendInValue(Object obj) {
        List<Object> values = new ArrayList<>();
        if (obj instanceof @SuppressWarnings("rawtypes")Iterable coll) {
            for (Object val : coll) {
                values.add(val);
            }
        } else if (obj instanceof String) {
            values.add(obj);
        } else if (obj instanceof Object[] objects) {
            values.addAll(Arrays.asList(objects));
        }
        return values.toArray();
    }

    /**
     * 获得在模式
     *
     * @param obj obj
     * @return {@link String}
     */
    static String getInPattern(Object obj) {
        StringBuilder sb = new StringBuilder();
        if (obj instanceof @SuppressWarnings("rawtypes")Iterable coll) {
            for (Object ignored : coll) {
                sb.append("?").append(",");
            }
        } else if (obj instanceof String) {
            sb.append("?").append(",");
        } else if (obj instanceof Object[] objects) {
            for (Object ignored : objects) {
                sb.append("?").append(",");
            }
        } else {
            throw new RuntimeException("in的值格式不正确。可以为String，Object[]，Iterable");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 获得json搜索值
     *
     * @param o o
     * @return {@link Object}
     */
    private Object getJsonSearchVal(Object o) {
        if (o != null) {
            if (o instanceof String) {
                return "\"" + o + "\"";
            } else {
                return String.valueOf(o);
            }
        }
        return null;
    }

    /**
     * 和夹头
     *
     * @param alias   别名
     * @param specSql 规范sql
     * @param filter  过滤器
     */
    private void andHander(String alias, StringBuilder specSql, SpecSupport filter) {
        Spec[] specs = (Spec[]) filter.value;
        StringBuilder innerSpecSql = new StringBuilder("(");
        for (Spec rule : specs) {
            // innerSpecSql 为空，开始进行or-and操作，前面没有or-and符号
            if (innerSpecSql.length() == 1) {
                // 如果当前Spec中只有一个条件，则不用加括号
                if (rule.size() == 1) {
                    innerSpecSql.append(buildQuerySpecification(rule, alias));
                } else {
                    innerSpecSql.append("(").append(buildQuerySpecification(rule, alias)).append(")");
                }
            } else { // 之后就是中间环节，要加or-and符号
                // or-and
                innerSpecSql.append(" ").append(filter.operator.name().toLowerCase());
                if (rule.size() == 1) {
                    innerSpecSql.append(" ").append(buildQuerySpecification(rule, alias));
                } else {
                    innerSpecSql.append(" (").append(buildQuerySpecification(rule, alias)).append(")");
                }
            }
        }
        innerSpecSql.append(")");
        specSql.append(innerSpecSql);
    }

    /**
     * 获得别名前缀
     *
     * @param alias 别名
     * @return {@link String}
     */
    private String getTableAliasPrefix(String alias) {
        if (StringUtils.isBlank(alias)) {
            return "";
        }
        return alias + ".";
    }

    /**
     * 克隆
     *
     * @return {@link Spec}
     */
    @Override
    public Spec clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException ignored) {
        }
        Spec spec = new Spec();
        spec.specMap = Maps.newLinkedHashMap(this.specMap);
        spec.sort = this.sort.clone(spec);
        spec.groupBy = this.groupBy;
        spec.having = this.having;
        spec.limit = this.limit;
        spec.limitBegin = this.limitBegin;
        return spec;
    }

    /**
     * 字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return Texts.format("Spec [specMap:({})  sort:({})  groupby:({})  having:({}) limit:({},{})]", specMap, String.valueOf(sort),
                groupBy, having, limitBegin + "", limit + "");
    }


}
