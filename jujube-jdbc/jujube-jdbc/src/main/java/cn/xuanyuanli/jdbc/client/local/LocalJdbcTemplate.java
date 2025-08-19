package cn.xuanyuanli.jdbc.client.local;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import cn.xuanyuanli.jdbc.base.BaseDaoSupport;
import cn.xuanyuanli.core.util.CamelCase;
import cn.xuanyuanli.core.util.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Jdbc相关工具
 * <pre>
 *     请在使用之前设置DataSource，对应的方法是：setDataSourceInfo()
 * </pre>
 *
 * @author John Li Email：jujubeframework@163.com
 * @date 2022/07/16
 */
public class LocalJdbcTemplate {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(LocalJdbcTemplate.class);
    /**
     * int限制
     */
    public static final int INT_LIMIT = 10;

    /**
     * jdbc模板
     */
    private LocalJdbcTemplate() {
    }

    /**
     * 数据源
     */
    private static DriverManagerDataSource dataSource;

    static {
        setDataSourceInfo();
    }

    /**
     * 手动设置DataSource
     */
    private static void setDataSourceInfo() {
        if (dataSource == null) {
            if (StringUtils.isNotBlank(LocalConfig.JDBC_URL) && StringUtils.isNotBlank(LocalConfig.JDBC_DRIVER_CLASS_NAME)
                    && StringUtils.isNotBlank(LocalConfig.JDBC_USERNAME)
                    && StringUtils.isNotBlank(LocalConfig.JDBC_PASSWORD)) {
                logger.info("init datasource：url:{},driverClass:{},username:{},pwd:{}", LocalConfig.JDBC_URL, LocalConfig.JDBC_DRIVER_CLASS_NAME,
                        LocalConfig.JDBC_USERNAME, LocalConfig.JDBC_PASSWORD);
                dataSource = new DriverManagerDataSource();
                dataSource.setUrl(LocalConfig.JDBC_URL);
                dataSource.setUsername(LocalConfig.JDBC_USERNAME);
                dataSource.setPassword(LocalConfig.JDBC_PASSWORD);
                dataSource.setDriverClassName(LocalConfig.JDBC_DRIVER_CLASS_NAME);
            } else {
                logger.info("无法初始化数据库，因为不存在LocalConfig.JDBC_URL值");
            }
        }
    }

    /**
     * jdbc模板线程本地
     */
    private final static ThreadLocal<JdbcTemplate> JDBC_TEMPLATE_THREAD_LOCAL = new ThreadLocal<>() {
        @Override
        protected JdbcTemplate initialValue() {
            return new JdbcTemplate(dataSource);
        }
    };

    /**
     * 获得jdbc模板
     *
     * @return {@link JdbcTemplate}
     */
    public static JdbcTemplate getJdbcTemplate() {
        return JDBC_TEMPLATE_THREAD_LOCAL.get();
    }

    /**
     * 处理过异常的queryForObject()方法
     *
     * @param jdbcTemplate jdbc模板
     * @param sql          sql
     * @param cl           cl
     * @param args         参数
     * @return {@link T}
     * @param <T> 泛型
     */
    public static <T> T queryForObject(JdbcTemplate jdbcTemplate, String sql, Class<T> cl, Object... args) {
        try {
            return jdbcTemplate.queryForObject(sql, cl, args);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * 可以返回id的更新方法,如果更新失败，会返回-1
     *
     * @param jdbcTemplate jdbc模板
     * @param sql          sql
     * @param args         参数
     * @return {@link PK}
     * @param <PK> 泛型
     */
    public static <PK> PK save(JdbcTemplate jdbcTemplate, final String sql, final Object... args) {
        PK result;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> BaseDaoSupport.getPreparedStatement(sql,conn,args), keyHolder);
            result = (PK)keyHolder.getKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 获得数据库连接
     *
     * @return {@link Connection}
     */
    public static Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    /**
     * 获得表注释
     *
     * @param conn      康涅狄格州
     * @param tableName 表名
     * @return {@link String}
     * @throws SQLException sqlexception异常
     */
    public static String getTableComment(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tableRet = metaData.getTables(null, "%", tableName, new String[]{"TABLE"});
        String tableComment = "";
        if (tableRet.first()) {
            tableComment = tableRet.getString("REMARKS");
        }
        return tableComment;
    }

    /**
     * 获得schema
     *
     * @param conn      康涅狄格州
     * @param tableName 表名
     * @return {@link String}
     * @throws SQLException sqlexception异常
     */
    @SuppressWarnings("unused")
    public static String getSchema(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet tableRet = metaData.getTables(null, "%", tableName, new String[]{"TABLE"});
        String schema = "";
        if (tableRet.first()) {
            schema = tableRet.getString("TABLE_CAT");
            if (StringUtils.isBlank(schema)) {
                schema = tableRet.getString("TABLE_SCHEM");
            }
        }
        return schema;
    }

    /**
     * 获得表结构
     *
     * @param conn      康涅狄格州
     * @param tableName 表名
     * @param schema    模式
     * @param imports   进口
     * @return {@link List}<{@link Column}>
     * @throws SQLException sqlexception异常
     * @author John Li Email：jujubeframework@163.com
     */
    public static List<Column> getTableStructure(Connection conn, String tableName, String schema, List<String> imports) throws SQLException {
        List<Column> columns = new ArrayList<>();

        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet colRet = metaData.getColumns(schema, schema, tableName, "%");

        // 找到主键
        String primaryKey = getPkname(conn, tableName, schema);

        // 生成字段
        while (colRet.next()) {
            String colName = colRet.getString("COLUMN_NAME");
            int dataType = colRet.getInt("DATA_TYPE");
            String comment = colRet.getString("REMARKS");
            String columnSize = colRet.getString("COLUMN_SIZE");

            int precision = NumberUtils.toInt(columnSize, 255);
            String field = CamelCase.toSpecilCamelCase(colName.toLowerCase());
            String type = typeMappingOfMySql(dataType, precision, imports);
            boolean isPk = colName.equals(primaryKey);
            columns.add(new Column(field, colName, type, comment, precision, isPk));
        }
        return columns;
    }

    /**
     * 获得主键名称(数据库字段名)
     *
     * @param conn      康涅狄格州
     * @param tableName 表名
     * @param schema    模式
     * @return {@link String}
     * @throws SQLException sqlexception异常
     */
    public static String getPkname(Connection conn, String tableName, String schema) throws SQLException {
        String primaryKey = "";
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet pkSet = metaData.getPrimaryKeys(schema, schema, tableName);
        if (pkSet.first()) {
            // COLUMN_NAME
            primaryKey = pkSet.getString(4);
        }
        return primaryKey;
    }

    /**
     * 类型映射（把数据库类型映射为Java类型）<br> 参考：<a href= "http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-type-conversions.html" >Java, JDBC and MySQL
     * Types</a>
     *
     * @param dataType  数据类型
     * @param precision 精度
     * @param imports   进口
     * @return {@link String}
     */
    public static String typeMappingOfMySql(int dataType, int precision, List<String> imports) {
        String javaType = "";
        if (imports == null) {
            imports = new ArrayList<>();
        }
        switch (dataType) {
            case Types.INTEGER:
            case Types.BIT:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BOOLEAN:
                if (precision > INT_LIMIT) {
                    javaType = "Long";
                } else {
                    javaType = "Integer";
                }
                break;
            case Types.BIGINT:
                javaType = "Long";
                break;
            case Types.FLOAT:
            case Types.REAL:
                javaType = "Float";
                break;
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.NUMERIC:
                javaType = "Double";
                break;
            case Types.BLOB:
            case Types.BINARY:
            case Types.LONGVARBINARY:
            case Types.VARBINARY:
                javaType = "Byte[]";
                break;
            case Types.CHAR:
            case Types.CLOB:
            case Types.VARCHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NCLOB:
            case Types.NVARCHAR:
                javaType = "String";
                break;
            case Types.TIME:
                imports.add("java.sql.Time");
                javaType = "Time";
                break;
            case Types.TIMESTAMP:
                imports.add("java.sql.Timestamp");
                javaType = "Timestamp";
                break;
            case Types.DATE:
                imports.add("java.sql.Date");
                javaType = "Date";
                break;
            // oracle 链路
            case Types.DATALINK:
            case Types.DISTINCT:
            case Types.JAVA_OBJECT:
            case Types.NULL:
            case Types.OTHER:
            case Types.REF:
            case Types.ROWID:
            case Types.SQLXML:
            case Types.STRUCT:
                break;
            default:
                break;
        }
        return javaType;
    }

    /**
     * 转换数据库字符集类型
     *
     * @param in   在
     * @param type 类型
     * @return {@link String}
     */
    public static String convertDatabaseCharsetType(String in, String type) {
        String dbUser;
        if (in != null) {
            dbUser = switch (type) {
                case "oracle", "db2" -> in.toUpperCase();
                case "postgresql" -> "public";
                case "mysql", "mssqlserver" -> null;
                case null, default -> in;
            };
        } else {
            dbUser = "public";
        }
        return dbUser;
    }

    /**
     * 获得数据库的所有表
     *
     * @param catalog 目录
     * @return {@link List}<{@link String}>
     */
    public static List<String> getTables(String catalog) {
        Connection conn = getConnection();
        List<String> list = new ArrayList<>();
        try {
            DatabaseMetaData dbMetData = conn.getMetaData();
            ResultSet rs = dbMetData.getTables(catalog, convertDatabaseCharsetType("root", "mysql"), null, new String[]{"TABLE"});

            while (rs.next()) {
                boolean bool = rs.getString(4) != null && ("TABLE".equalsIgnoreCase(rs.getString(4)) || "VIEW".equalsIgnoreCase(rs.getString(4)));
                if (bool) {
                    String tableName = rs.getString(3).toLowerCase();
                    list.add(tableName);
                }
            }
        } catch (SQLException e) {
            logger.error("getTables", e);
        }
        return list;
    }

    /**
     * 执行对应的sql脚本
     *
     * @param jdbcTemplate jdbc模板
     * @param resoucePath  资源路径
     */
    public static void runScript(JdbcTemplate jdbcTemplate, String resoucePath) {
        DataSource dataSource = jdbcTemplate.getDataSource();
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.setSeparator(";");
        populator.setSqlScriptEncoding("utf-8");
        Resource pathResource = Resources.getClassPathResources(resoucePath);
        if (pathResource != null && dataSource != null) {
            populator.addScript(pathResource);
            DatabasePopulatorUtils.execute(populator, dataSource);
        }
    }

    @Data
    public static class Column {

        private String field;
        private String colName;
        private String type;
        private String comment;
        private Integer precision;
        private Boolean primaryKey;

        /**
         * 列
         */
        public Column() {
        }

        /**
         * 列
         *
         * @param field      字段
         * @param originName 原产地名称
         * @param type       类型
         * @param comment    评论
         * @param precision  精度
         * @param primaryKey 主键
         */
        public Column(String field, String originName, String type, String comment, Integer precision, Boolean primaryKey) {
            super();
            this.field = field;
            this.colName = originName;
            this.type = type;
            this.comment = comment;
            this.precision = precision;
            this.primaryKey = primaryKey;
        }

    }
}
