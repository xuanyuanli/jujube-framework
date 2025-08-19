package cn.xuanyuanli.jdbc.base.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.core.util.Beans;
import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Texts;

/**
 * Sql解析工具
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2022/07/16
 */
@SuppressWarnings("unused")
public class Sqls {

    /**
     * 中
     */
    private Sqls() {
    }

    /**
     * 解析一条sql，返回获取表数据总数的sql(仅适用于单独in条件下使用)
     *
     * @param sql sql
     * @return {@link String}
     */
    @SneakyThrows
    public static String getCountSqlWithIn(String sql) {
        sql = wipeoffEndSemicolon(sql);
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        if (select instanceof PlainSelect plainSelect) {
            Expression where = plainSelect.getWhere();
            if (where instanceof InExpression) {
                Expression rightExpression = ((InExpression) where).getRightExpression();
                return getCountSql(((ParenthesedSelect) rightExpression).getSelect().toString());
            }
        }
        return getCountSql(sql);
    }

    /**
     * 解析一条sql，返回获取表数据总数的sql
     *
     * @param sql sql
     * @return {@link String}
     */
    public static String getCountSql(String sql) {
        try {
            sql = wipeoffEndSemicolon(sql);
            Select select = (Select) CCJSqlParserUtil.parse(sql);
            if (select instanceof PlainSelect plainSelect) {
                if (plainSelect.getGroupBy() != null) {
                    Expression selectBodyHaving = plainSelect.getHaving();
                    String having = selectBodyHaving != null ? selectBodyHaving.toString() : "";
                    String formAndWhere = getFormAndWhere(plainSelect);
                    return "SELECT COUNT(*) FROM (SELECT 1 FROM " + formAndWhere + " " + plainSelect.getGroupBy() + " " + having + ") getcountsql_t_t";
                } else {
                    return "SELECT COUNT(*) FROM " + getFormAndWhere(plainSelect);
                }
            } else if (select instanceof SetOperationList) {
                return "SELECT COUNT(*) FROM (" + select + ") getcountsql_t_t";
            }
            return "";
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得形式和在哪里
     *
     * @param plainSelect 简单选择
     * @return {@link String}
     */
    private static String getFormAndWhere(PlainSelect plainSelect) {
        String formAndWhere = plainSelect.getFromItem().toString();
        formAndWhere += getJoin(plainSelect.getJoins());
        if (plainSelect.getWhere() != null) {
            formAndWhere += " WHERE " + plainSelect.getWhere();
        }
        return formAndWhere;
    }

    /**
     * 获得加入
     *
     * @param joins 连接
     * @return {@link String}
     */
    private static String getJoin(List<Join> joins) {
        StringBuilder joinStr = new StringBuilder();
        if (joins != null) {
            for (Join join : joins) {
                joinStr.append(join).append(" ");
            }
            joinStr.insert(0, " ");
        }
        return joinStr.toString();
    }

    /**
     * 用子查询把对原始sql进行包裹
     *
     * @param sql sql
     * @return {@link String}
     */
    public static String wrapSql(String sql) {
        return "select * from (" + sql + ") t_" + Thread.currentThread().threadId() + "_" + System.currentTimeMillis();
    }

    /**
     * 填充命名参数，获得真实sql
     *
     * @param sql        sql
     * @param namedParam 命名参数
     * @return {@link String}
     */
    public static String realSql(String sql, List<Object> namedParam) {
        if (namedParam == null || namedParam.isEmpty()) {
            return sql;
        }
        sql = sql.replace("?", "{}");
        return Texts.format(sql, namedParam.stream().map(Sqls::singleQuotes).toArray());
    }

    /**
     * 如果是String类型，则用单引号包裹
     *
     * @param obj obj
     * @return {@link Object}
     */
    private static Object singleQuotes(Object obj) {
        if (obj instanceof String sobj) {
            // 如果包含反斜杠，则进行转义
            if (sobj.contains("\\")) {
                sobj = StringUtils.replace(sobj, "\\", "\\\\");
            }
            // 如果包含单引号，则进行转义
            if (sobj.contains("'")) {
                sobj = StringUtils.replace(sobj, "'", "\\'");
            }
            return ("'" + sobj + "'");
        }
        return obj;
    }

    /**
     * 获得安全的字段值
     *
     * @param fieldName 字段名字
     * @return {@link String}
     */
    public static String getSecurityFieldName(String fieldName) {
        int index = fieldName.indexOf(".");
        if (index != -1) {
            index++;
            String field = fieldName.substring(index);
            return fieldName.substring(0, index) + ("*".equals(field) ? field : getSecurityAliasFieldName(field));
        } else {
            return "*".equals(fieldName) ? fieldName : getSecurityAliasFieldName(fieldName);
        }
    }

    /**
     * 获得安全别名字段名字
     *
     * @param fieldName 字段名字
     * @return {@link String}
     */
    private static String getSecurityAliasFieldName(String fieldName) {
        String curField = fieldName.trim().toLowerCase();
        if (curField.contains(" as ")) {
            String separator = fieldName.contains(" as ") ? " as " : (fieldName.contains("AS") ? " AS " : (fieldName.contains("aS") ? " aS " : "As"));
            List<String> list = Arrays.stream(StringUtils.splitByWholeSeparator(fieldName, separator)).filter(StringUtils::isNotBlank).map(String::trim)
                    .toList();
            String field2 = list.get(0);
            return (field2.startsWith("`") ? field2 : "`" + field2 + "`") + " " + list.get(1);
        } else if (curField.contains(" ")) {
            List<String> list = Arrays.stream(StringUtils.splitByWholeSeparator(fieldName, " ")).filter(StringUtils::isNotBlank).toList();
            String field2 = list.get(0);
            return (field2.startsWith("`") ? field2 : "`" + field2 + "`") + " " + list.get(1);
        } else {
            return fieldName.startsWith("`") ? fieldName : "`" + fieldName + "`";
        }
    }

    /**
     * 获得in括号中的条件
     *
     * @param obj obj
     * @return {@link String}
     */
    public static String inJoin(Object obj) {
        StringBuilder sb = new StringBuilder();
        switch (obj) {
            case @SuppressWarnings("rawtypes")Iterable coll -> {
                for (Object object : coll) {
                    sb.append(singleQuotes(object)).append(",");
                }
            }
            case String s -> sb.append(obj).append(",");
            case Object[] objects -> {
                for (Object object : objects) {
                    sb.append(singleQuotes(object)).append(",");
                }
            }
            case null, default -> throw new RuntimeException("in的值格式不正确。可以为String，Object[]，Iterable");
        }
        return sb.substring(0, sb.length() - 1);
    }

    /**
     * 喜欢包装
     *
     * @param name 名字
     * @return {@link String}
     */
    public static String likeWrap(String name) {
        return "%" + name + "%";
    }

    /**
     * 左就像包装
     *
     * @param name 名字
     * @return {@link String}
     */
    public static String leftLikeWrap(String name) {
        return "%" + name;
    }

    /**
     * 右就像包装
     *
     * @param name 名字
     * @return {@link String}
     */
    public static String rightLikeWrap(String name) {
        return name + "%";
    }

    /**
     * 获得批量插入sql
     *
     * @param tableName     表名
     * @param colNames      表字段列表
     * @param propertyNames entity字段列表
     * @param list          表数据
     * @return {@link String}
     */
    private static <T> String getBatchInsertSql(String tableName, List<String> colNames, List<String> propertyNames, List<T> list) {
        if (list.isEmpty()) {
            return "";
        }
        StringBuilder saveSqlPre = new StringBuilder("insert into ").append(tableName).append(" (");
        for (int j = 0; j < colNames.size(); j++) {
            saveSqlPre.append("`").append(colNames.get(j)).append("`");
            if (j < colNames.size() - 1) {
                saveSqlPre.append(" ,");
            }
        }
        saveSqlPre.append(") values");
        StringBuilder saveSql = new StringBuilder(saveSqlPre);
        for (T t : list) {
            saveSql.append("(");
            for (int j = 0; j < propertyNames.size(); j++) {
                Object obj = Beans.getProperty(t, propertyNames.get(j));
                if (obj == null) {
                    saveSql.append("NULL");
                } else {
                    if (obj instanceof String) {
                        saveSql.append("'").append(obj).append("'");
                    } else {
                        saveSql.append(obj);
                    }
                }
                if (j < propertyNames.size() - 1) {
                    saveSql.append(" ,");
                }
            }
            saveSql.append("),");
        }
        return saveSql.substring(0, saveSql.length() - 1);
    }

    /**
     * 获得批量插入sql
     *
     * @param tableName 表名
     * @param colNames  表字段列表
     * @param list      表数据
     * @param <T>       泛型
     * @return {@link String}
     */
    public static <T> String getBatchInsertSql(String tableName, List<String> colNames, List<T> list) {
        return getBatchInsertSql(tableName, colNames, colNames, list);
    }

    /**
     * 获得批量插入sql
     *
     * @param tableName 表名
     * @param entities  实体类集合
     * @param <T>       泛型
     * @return {@link String}
     */
    public static <T> String getBatchInsertSql(String tableName, List<T> entities) {
        if (entities.isEmpty()) {
            return "";
        }
        List<String> fieldNames = Beans.getAllDeclaredFieldNames(entities.get(0).getClass());
        List<String> columnNames = fieldNames.stream().map(CamelCase::toUnderlineName).collect(Collectors.toList());
        return getBatchInsertSql(tableName, columnNames, fieldNames, entities);
    }

    /**
     * 去除结尾分号
     *
     * @param sql sql
     * @return {@link String}
     */
    public static String wipeoffEndSemicolon(String sql) {
        sql = sql.trim();
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }
        return sql;
    }
}
