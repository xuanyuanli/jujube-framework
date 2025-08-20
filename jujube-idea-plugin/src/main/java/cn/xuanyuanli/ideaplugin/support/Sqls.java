package cn.xuanyuanli.ideaplugin.support;

import com.google.common.collect.Lists;
import com.intellij.database.model.DasTable;
import com.intellij.database.model.DataType;
import com.intellij.database.psi.DbDataSource;
import com.intellij.database.psi.DbPsiFacade;
import com.intellij.database.util.DasUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypes;
import com.intellij.psi.impl.JavaPsiFacadeEx;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import org.apache.commons.lang3.StringUtils;
import cn.xuanyuanli.util.CamelCase;

/**
 * @author John Li
 */
public class Sqls {

    private static final List<String> INT_STRS = Lists.newArrayList("type", "checked", "is", "need", "has", "enable", "status", "state", "ratio");
    private static final List<String> LONG_STRS = Lists.newArrayList("id", "time", "count");
    private static final List<String> STRING_STRS = Lists.newArrayList("lotid", "jump");
    private static final List<String> DOUBLE_STRS = Lists.newArrayList("price", "account", "amount", "total", "charge", "commission", "tax", "exchange");


    /**
     * 获得sql select部分的列
     *
     * @param sql sql
     * @return {@link List}<{@link Column}>
     * @throws JSQLParserException jsqlparser例外
     */
    public static List<Column> getColumns(String sql, Project project) throws JSQLParserException {
        Select selectBody = (Select) CCJSqlParserUtil.parse(sql);
        List<Column> list = new ArrayList();
        if (selectBody instanceof PlainSelect plainSelect) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                Expression expression = item.getExpression();
                PsiType type = null;
                if (expression instanceof net.sf.jsqlparser.schema.Column column) {
                    String tableName = getTableName(plainSelect, column.getTable());
                    type = dataTypeToPsiType(getColumnType(tableName, column.getColumnName(), project), project);
                } else if (expression instanceof ParenthesedSelect subSelect) {
                    // 递归解析子查询
                    List<Column> columns = getColumns(subSelect.toString(), project);
                    type = columns.isEmpty() ? PsiTypes.voidType() : columns.getFirst().getPsiType();
                } else if (expression instanceof Function function) {
                    type = mySqlFunctionToPsiType(function.getName(), project);
                }
                Alias alias = item.getAlias();
                String columnName = "";
                // 这里可能存在子查询递归item是函数的情况，这种情况不处理
                if (alias != null) {
                    columnName = alias.getName();
                } else if (item.getExpression() instanceof net.sf.jsqlparser.schema.Column column) {
                    columnName = column.getColumnName();
                }
                Column col = new Column();
                columnName = columnName.replace("`", "");
                col.setField(CamelCase.toCamelCase(columnName));
                col.setPsiType(type != null ? type : getTypeByName(columnName));
                list.add(col);
            }
        }
        return list;
    }

    private static String getTableName(PlainSelect plainSelect, Table ptable) {
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof Table table && (ptable == null || table.getAlias().getName().equals(ptable.getName()))) {
            return table.getName();
        }
        // 这里可能存在rightItem是subselect的情况，不过不做处理
        return plainSelect.getJoins().stream().filter(e -> e.getRightItem() instanceof Table table && table.getAlias().getName().equals(ptable.getName()))
                .findFirst().map(e -> ((Table) e.getRightItem()).getName()).orElse(null);
    }


    public static PsiType mySqlFunctionToPsiType(String functionName, Project project) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        PsiElementFactory elementFactory = javaPsiFacade.getElementFactory();
        String function = functionName.toLowerCase();
        return switch (function) {
            case "count" -> PsiTypes.longType();
            case "sum", "avg" -> PsiTypes.doubleType();
            default -> elementFactory.createTypeFromText("java.lang.String", null);
        };
    }

    public static DataType getColumnType(String tableName, String columnName, Project project) {
        if (StringUtils.isNotBlank(tableName) && StringUtils.isNotBlank(columnName)) {
            DbPsiFacade psiFacade = DbPsiFacade.getInstance(project);
            Collection<DbDataSource> dataSources = psiFacade.getDataSources();
            String ctableName = tableName.replace("`", "").toLowerCase();
            String ccolumnName = columnName.replace("`", "").toLowerCase();
            for (DbDataSource dataSource : dataSources) {
                DasTable first = DasUtil.getTables(dataSource).filter(dasTable -> dasTable.getName().equals(ctableName)).first();
                return DasUtil.getColumns(first).filter(dasColumn -> dasColumn.getName().equals(ccolumnName)).first().getDasType().toDataType();
            }
        }
        return null;
    }

    public static PsiType dataTypeToPsiType(DataType dataType, Project project) {
        JavaPsiFacadeEx javaPsiFacade = JavaPsiFacadeEx.getInstanceEx(project);
        PsiElementFactory elementFactory = javaPsiFacade.getElementFactory();

        if (dataType != null) {
            String sqlType = dataType.typeName.toLowerCase();

            return switch (sqlType) {
                case "integer", "int", "smallint", "tinyint" -> PsiTypes.intType();
                case "bigint" -> PsiTypes.longType();
                case "double", "double precision", "decimal", "float" -> PsiTypes.doubleType();
                default -> elementFactory.createTypeFromText("java.lang.String", null);
            };
        }
        return null;
    }

    /**
     * 得到类型(String类型这里用Void表示)
     */
    private static PsiType getTypeByName(String field) {
        field = field.toLowerCase();
        String finalField = field;
        if (STRING_STRS.stream().anyMatch(finalField::contains)) {
            return PsiTypes.voidType();
        } else if (LONG_STRS.stream().anyMatch(finalField::contains)) {
            return PsiTypes.longType();
        } else if (INT_STRS.stream().anyMatch(finalField::contains)) {
            return PsiTypes.intType();
        } else if (DOUBLE_STRS.stream().anyMatch(finalField::contains)) {
            return PsiTypes.doubleType();
        }
        return PsiTypes.nullType();
    }
}
