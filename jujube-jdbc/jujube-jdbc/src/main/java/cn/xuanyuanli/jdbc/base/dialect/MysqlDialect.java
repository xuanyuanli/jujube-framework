package cn.xuanyuanli.jdbc.base.dialect;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import cn.xuanyuanli.core.lang.Record;

import java.util.List;
import java.util.Map.Entry;

/**
 * MysqlDialect.
 *
 * @author John Li
 */
public class MysqlDialect implements Dialect {

    private static final String SQL_CONTAIN_SYMBOL = "`";
    /**
     * 点
     */
    public static final String DOT = ".";

    @Override
    public String getSecurityTableName(String tableName) {
        String result = tableName.trim();
        if (!result.contains(DOT)) {
            result = SQL_CONTAIN_SYMBOL + result + SQL_CONTAIN_SYMBOL;
        }
        return result;
    }

    @Override
    public String getSecurityFields(String fields, String tableName) {
        Validate.notBlank(fields);
        String securityTableName = null;
        if (StringUtils.isNotBlank(tableName)) {
            securityTableName = tableName.startsWith(SQL_CONTAIN_SYMBOL) ? tableName : getSecurityTableName(tableName);
        }
        fields = fields.trim();
        if (fields.contains(",")) {
            String[] arr = StringUtils.splitByWholeSeparator(fields, ",");
            StringBuilder fieldsBuilder = new StringBuilder();
            for (String ele : arr) {
                ele = ele.trim();
                if (StringUtils.isNotBlank(ele)) {
                    // 子查询处理
                    if (ele.startsWith("(")) {
                        fieldsBuilder.append(ele);
                    } else if (!ele.contains(".")) {
                        if (securityTableName != null) {
                            fieldsBuilder.append(securityTableName).append(".").append(Sqls.getSecurityFieldName(ele));
                        } else {
                            fieldsBuilder.append(Sqls.getSecurityFieldName(ele));
                        }
                    }
                    fieldsBuilder.append(",");
                }
            }
            String result = fieldsBuilder.toString();
            if (result.endsWith(",")) {
                return result.substring(0, fieldsBuilder.length() - 1);
            } else {
                return result;
            }
        } else {
            if (!fields.contains(".") && !fields.startsWith("(")) {
                return securityTableName + "." + Sqls.getSecurityFieldName(fields);
            } else {
                return fields;
            }
        }
    }

    @Override
    public String forDbSimpleQuery(String fields, String tableName, String filters) {
        return "select " + fields + " from " + getSecurityTableName(tableName) + " where " + filters;
    }

    @Override
    public String forDbSimpleQuery(String fields, String tableName) {
        return "select " + fields + " from " + getSecurityTableName(tableName);
    }

    @Override
    public String forDbFindById(String tableName, String primaryKey, String columns) {
        StringBuilder sql = new StringBuilder("select ");
        String symbol = "*";
        if (symbol.equals(columns.trim())) {
            sql.append(columns);
        } else {
            String[] columnsArray = StringUtils.splitByWholeSeparator(columns, ",");
            for (int i = 0; i < columnsArray.length; i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append(SQL_CONTAIN_SYMBOL).append(columnsArray[i].trim()).append(SQL_CONTAIN_SYMBOL);
            }
        }
        sql.append(" from ");
        sql.append(getSecurityTableName(tableName));
        sql.append(" where `").append(primaryKey).append("` = ?");
        return sql.toString();
    }

    @Override
    public String forDbDeleteById(String tableName, String primaryKey) {
        return "delete from " + getSecurityTableName(tableName) + " where `" + primaryKey + "` = ?";
    }

    @Override
    public String forDbSave(String tableName, Record record, List<Object> paras) {
        if (record.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("insert into ");
        sql.append(getSecurityTableName(tableName)).append("(");
        StringBuilder temp = new StringBuilder();
        temp.append(") values(");

        for (Entry<String, Object> e : record.entrySet()) {
            if (!paras.isEmpty()) {
                sql.append(", ");
                temp.append(", ");
            }
            sql.append(SQL_CONTAIN_SYMBOL).append(e.getKey()).append(SQL_CONTAIN_SYMBOL);
            temp.append("?");
            paras.add(e.getValue());
        }
        sql.append(temp).append(")");
        return sql.toString();
    }

    @Override
    public String forDbUpdate(String tableName, String primaryKey, Object id, Record record, List<Object> paras) {
        if (record.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(getSecurityTableName(tableName)).append(" set ");
        for (Entry<String, Object> e : record.entrySet()) {
            String colName = e.getKey();
            if (!primaryKey.equalsIgnoreCase(colName)) {
                if (!paras.isEmpty()) {
                    sql.append(", ");
                }
                sql.append(SQL_CONTAIN_SYMBOL).append(colName).append("` = ? ");
                paras.add(e.getValue());
            }
        }
        sql.append(" where `").append(primaryKey).append("` = ?");
        paras.add(id);
        return sql.toString();
    }

    @Override
    public String forDbDelete(String tableName, String filters) {
        return "delete from " + getSecurityTableName(tableName) + " where " + filters;
    }

    @Override
    public String forDbPaginationQuery(String origSql, long start, int size) {
        StringBuilder pageSql = new StringBuilder();
        String lowerSql = origSql.toLowerCase().trim();
        int index = lowerSql.lastIndexOf(")");
        if (index > 0) {
            lowerSql = lowerSql.substring(index);
            int indexLimit = lowerSql.lastIndexOf(" limit ");
            if (indexLimit > 0) {
                origSql = origSql.substring(0, index + indexLimit);
            }
        } else {
            int indexLimit = lowerSql.lastIndexOf(" limit ");
            if (indexLimit > 0) {
                origSql = origSql.substring(0, indexLimit);
            }
        }
        pageSql.append(origSql);
        pageSql.append(" limit ").append(start).append(",").append(size);
        return pageSql.toString();
    }

}
