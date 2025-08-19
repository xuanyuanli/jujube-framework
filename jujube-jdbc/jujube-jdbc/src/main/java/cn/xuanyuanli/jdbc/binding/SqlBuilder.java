package cn.xuanyuanli.jdbc.binding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import cn.xuanyuanli.jdbc.binding.fmtmethod.JoinMethod;
import cn.xuanyuanli.jdbc.binding.fmtmethod.NotBlankMethod;
import cn.xuanyuanli.jdbc.binding.fmtmethod.NotNullMethod;
import cn.xuanyuanli.jdbc.binding.fmtmethod.TypeOfMethod;
import cn.xuanyuanli.core.util.Ftls;
import cn.xuanyuanli.core.util.Texts;

/**
 * Sql构建器
 *
 * @author John Li
 */
public class SqlBuilder {

    /**
     * union查询的段落分割标志
     */
    public static final String JUJUBE_UNION = "#jujube-union";

    private final String unionBefore;
    private final String[] unionAfterArr;

    /**
     * sql builder
     *
     * @param originSql 起源sql
     */
    public SqlBuilder(List<String> originSql) {
        // 过滤注释并去掉换行符
        String sql = originSql.stream().map(e -> e.trim().startsWith("#") && !e.trim().startsWith("#jujube-union") ? "" : e)
                .filter(StringUtils::isNotBlank).collect(Collectors.joining(" ")).trim();
        String[] arr = StringUtils.splitByWholeSeparator(sql, JUJUBE_UNION);
        arr = wipeoffSimicolon(arr);
        unionBefore = arr[0];
        if (arr.length == 1) {
            unionAfterArr = null;
        } else {
            unionAfterArr = Arrays.copyOfRange(arr, 1, arr.length);
        }
    }

    /**
     * 去除simicolon
     */
    private String[] wipeoffSimicolon(String[] arr) {
        String[] narr = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            String sql = Sqls.wipeoffEndSemicolon(arr[i]);
            narr[i] = sql;
        }
        return narr;
    }

    /**
     * 构建最终的查询sql
     *
     * @param queryMap  入参
     * @param sourceSql 源sql
     * @return {@link SqlAndParams}
     */
    private static SqlAndParams builderSqlResult(String sourceSql, Map<String, Object> queryMap) {
        HashMap<String, Object> curMap = new HashMap<>(16);
        if (queryMap != null) {
            curMap.putAll(queryMap);
        }
        fullFreemarkerRoot(curMap);
        return getSqlAndParams(sourceSql, curMap);
    }

    /**
     * 获得摘除${..}之后的sql和param
     */
    private static SqlAndParams getSqlAndParams(String sourceSql, Map<String, Object> queryMap) {
        sourceSql = getWrapSql(sourceSql, queryMap);
        StringBuilder rsql = new StringBuilder();
        List<String> params = new ArrayList<>();
        List<String> paramsType = new ArrayList<>();
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery("('?)(\\%?)\\(=\\{(.*?)\\(\\|=\\|\\)(string|number|bool|origin|join)\\}=\\)(\\%?)('?)",
                sourceSql);
        int start = 0;
        for (Texts.RegexQueryInfo queryInfo : regexQueryInfos) {
            String type = queryInfo.getGroups().get(3);
            queryInfo.getGroups().remove(3);
            String val = String.join("", queryInfo.getGroups());
            // join的处理
            if ("join".equals(type)) {
                rsql.append(sourceSql, start, queryInfo.getStart());
                boolean isStr = val.endsWith("'");
                List<String> inVals = isStr ? Arrays.asList(val.split("',")) : Arrays.asList(val.split(","));
                for (int i = 0; i < inVals.size(); i++) {
                    rsql.append("?");
                    String cur = inVals.get(i);
                    boolean startSingle = cur.startsWith("'");
                    boolean endSingle = cur.endsWith("'");
                    if (isStr && (startSingle || endSingle)) {
                        paramsType.add("string");
                        // 如果为string类型，则去掉单引号
                        params.add(cur.substring(startSingle ? 1 : 0, cur.length() - (endSingle ? 1 : 0)));
                    } else {
                        paramsType.add("origin");
                        params.add(cur);
                    }
                    if (i != inVals.size() - 1) {
                        rsql.append(",");
                    }
                }
            } else if (Texts.find(sourceSql.substring(start, queryInfo.getStart() + 1), "(\\s+)like(\\s*?)'$", true)) {
                // like的处理，去掉单引号
                params.add(val.substring(1, val.length() - 1));
                paramsType.add(type);
                rsql.append(sourceSql, start, queryInfo.getStart()).append("?");
            } else {
                params.add(val);
                paramsType.add(type);
                rsql.append(sourceSql, start, queryInfo.getStart()).append("?");
            }
            start = queryInfo.getEnd();
        }
        rsql.append(sourceSql.substring(start));
        return new SqlAndParams(Sqls.wipeoffEndSemicolon(rsql.toString()), getActualType(params, paramsType));
    }

    /**
     * 获得真实的类型
     */
    private static List<Object> getActualType(List<String> params, List<String> paramsType) {
        List<Object> list = new ArrayList<>(params.size());
        for (int i = 0; i < params.size(); i++) {
            String cur = params.get(i);
            String type = paramsType.get(i);
            if ("string".equals(type)) {
                list.add(cur);
            } else if ("number".equals(type)) {
                if (cur.contains(".")) {
                    list.add(NumberUtils.toDouble(cur));
                } else {
                    list.add(NumberUtils.toLong(cur));
                }
            } else if ("bool".equals(type)) {
                list.add(Boolean.valueOf(cur));
            } else {
                if ("true".equalsIgnoreCase(cur) || "false".equalsIgnoreCase(cur)) {
                    list.add(Boolean.valueOf(cur));
                } else if (cur.contains(".")) {
                    double dou = NumberUtils.toDouble(cur, Double.MIN_VALUE);
                    if (dou == Double.MIN_VALUE) {
                        list.add(cur);
                    } else {
                        list.add(dou);
                    }
                } else {
                    long lon = NumberUtils.toLong(cur, Long.MIN_VALUE);
                    if (lon == Long.MIN_VALUE) {
                        list.add(cur);
                    } else {
                        list.add(lon);
                    }
                }
            }
        }
        return list;
    }

    /**
     * 执行Freemarker语句，用(={...}=)来包裹$取值。中间用(|=|)来分隔值与值类型
     */
    private static String getWrapSql(String sourceSql, Map<String, Object> queryMap) {
        StringBuilder rsql = new StringBuilder();
        List<Texts.RegexQueryInfo> regexQueryInfos = Texts.regQuery("\\$\\{(.*?)\\}", sourceSql);
        int start = 0;
        for (Texts.RegexQueryInfo queryInfo : regexQueryInfos) {

            String type;
            if (isJoinFunc(queryInfo.getGroup())) {
                type = "join";
            } else {
                String temp = "<#if {}?is_string>string<#elseif {}?is_number>number<#elseif {}?is_boolean>bool<#else>origin</#if>";
                String val = queryInfo.getGroups().get(0);
                if (!val.trim().startsWith("(")) {
                    val = "(" + val + ")";
                }
                type = Texts.format(temp, val, val, val);
            }
            rsql.append(sourceSql, start, queryInfo.getStart()).append("(={").append(queryInfo.getGroup())
                    .append("(|=|)").append(type).append("}=)");
            start = queryInfo.getEnd();
        }
        rsql.append(sourceSql.substring(start));
        String ftlSource = rsql.toString();
        if (ftlSource.contains("@{")) {
            ftlSource = StringUtils.replace(ftlSource, "@{", "${");
            ftlSource = Ftls.processStringTemplateToString(ftlSource, queryMap);
        }
        return Ftls.processStringTemplateToString(ftlSource, queryMap);
    }

    static boolean isJoinFunc(String group) {
        return Texts.find(group, "\\$\\{(\\s*?)join\\((.+?)\\)(\\s*?)\\}") || Texts.find(group, "\\$\\{(.+?)\\?join\\((.+?)\\)(\\s*?)\\}");
    }

    /**
     * 填充Freemarker Root
     */
    private static void fullFreemarkerRoot(Map<String, Object> queryMap) {
        if (queryMap == null) {
            queryMap = new HashMap<>(4);
        }
        queryMap.put("join", new JoinMethod());
        queryMap.put("notBlank", new NotBlankMethod());
        queryMap.put("notNull", new NotNullMethod());
        queryMap.put("typeOf", new TypeOfMethod());
    }

    /**
     * 构建最终的查询sql
     *
     * @param queryMap 入参
     * @return SqlResult包含了查询sql和查询参数集合
     */
    public SqlResult builder(Map<String, Object> queryMap) {
        SqlResult result = new SqlResult();
        SqlAndParams sqlAndParams = builderSqlResult(unionBefore, queryMap);
        result.setSql(sqlAndParams.getSql());
        result.setFilterParams(sqlAndParams.getParams().toArray());
        if (unionAfterArr != null) {
            result.setUnion(true);
            List<UnionSqlInfo> unionSqlInfos = new ArrayList<>();
            for (String unionSql : unionAfterArr) {
                SqlAndParams sqlResult = builderSqlResult(unionSql.trim(), queryMap);
                UnionSqlInfo unionSqlInfo = new UnionSqlInfo().setSql(sqlResult.getSql()).setFilterParams(sqlResult.getParams().toArray());
                unionSqlInfos.add(unionSqlInfo);
            }
            result.setUnionAfterSqlInfo(unionSqlInfos);
        }
        return result;
    }

    /**
     * 包含了查询sql和查询参数集合
     */
    @Data
    @Accessors(chain = true)
    public static class SqlResult {

        /**
         * 联合查询的SQL及参数信息集合
         */
        List<UnionSqlInfo> unionAfterSqlInfo;
        private boolean union;
        private String sql;
        private Object[] filterParams;
    }

    /**
     * 联合查询的SQL及参数信息
     */
    @Data
    @Accessors(chain = true)
    public static class UnionSqlInfo {

        private String sql;
        private Object[] filterParams;
        private long sqlCount;
    }

    /**
     * 占位符sql和对应的参数列表
     */
    @Data
    @AllArgsConstructor
    static class SqlAndParams {

        private String sql;
        private List<Object> params;
    }
}
