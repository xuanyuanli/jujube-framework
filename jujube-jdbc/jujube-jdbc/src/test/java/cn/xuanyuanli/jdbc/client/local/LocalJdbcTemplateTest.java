package cn.xuanyuanli.jdbc.client.local;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import cn.xuanyuanli.jdbc.client.local.LocalJdbcTemplate.Column;
import cn.xuanyuanli.core.lang.Record;
import cn.xuanyuanli.core.util.Collections3;
import cn.xuanyuanli.core.util.Texts;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;

@Disabled
class LocalJdbcTemplateTest {

    @Test
    void getTableStructure() throws SQLException {
        Connection connection = LocalJdbcTemplate.getConnection();
        String tableName = "user";
        List<Column> columnList = LocalJdbcTemplate.getTableStructure(connection, tableName, "main", null);
        List<String> colNames = Collections3.extractToListString(columnList, "colName");
        List<Map<String, Object>> mapList = LocalJdbcTemplate.getJdbcTemplate()
                .queryForList(Texts.format("select * from " + tableName + " limit {},{}", 0, 5));
        System.out.println(Sqls.getBatchInsertSql(tableName, colNames, mapList));
    }

    @Test
    void queryUserIn() throws SQLException {
        Connection connection = LocalJdbcTemplate.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select * from `user` u where u.id in (?,?,?)  ");
        preparedStatement.setObject(1, "1");
        preparedStatement.setObject(2, 4);
        preparedStatement.setObject(3, 3L);
        System.out.println(preparedStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Record> records = listRecordResultSetExtractor.extractData(resultSet);
        for (Record record : records) {
            System.out.println(record);
        }
    }

    @Test
    void queryUserEq() throws SQLException {
        Connection connection = LocalJdbcTemplate.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select * from `user` u where u.id =? and u.is_test=1  ");
        preparedStatement.setObject(1, 1);
        System.out.println(preparedStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Record> records = listRecordResultSetExtractor.extractData(resultSet);
        for (Record record : records) {
            System.out.println(record);
        }
    }

    @Test
    void queryUserLike() throws SQLException {
        Connection connection = LocalJdbcTemplate.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("select * from `user` u where u.user_name like?  ");
        preparedStatement.setObject(1, "%客户%");
        System.out.println(preparedStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Record> records = listRecordResultSetExtractor.extractData(resultSet);
        for (Record record : records) {
            System.out.println(record);
        }
    }

    @Test
    void queryUserJsonContains() throws SQLException {
        Connection connection = LocalJdbcTemplate.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM spider_auctioneer_alias a WHERE JSON_CONTAINS(a.`alias`,?) ");
        preparedStatement.setObject(1, "\"AKA AUCTION\"");
        System.out.println(preparedStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Record> records = listRecordResultSetExtractor.extractData(resultSet);
        for (Record record : records) {
            System.out.println(record);
        }
    }

    @Test
    void queryUserJsonContains2() throws SQLException {
        Connection connection = LocalJdbcTemplate.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT JSON_CONTAINS('{\"a\": 1, \"b\": 2, \"c\": {\"d\": 4}}',?,?)");
        preparedStatement.setObject(1, "4");
        preparedStatement.setObject(2, "$.c.d");
        System.out.println(preparedStatement);
        ResultSet resultSet = preparedStatement.executeQuery();
        List<Record> records = listRecordResultSetExtractor.extractData(resultSet);
        for (Record record : records) {
            System.out.println(record);
        }
    }

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
        return list;
    };
}
