package cn.xuanyuanli.jdbc.base.dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import cn.xuanyuanli.core.lang.Record;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MysqlDialectTest")
class MysqlDialectTest {

    private static final MysqlDialect dialect = new MysqlDialect();

    @Nested
    @DisplayName("getSecurityTableName方法测试")
    class GetSecurityTableNameTest {
        @ParameterizedTest
        @ValueSource(strings = {"user", "USER", "  user  ", "  USER  "})
        @DisplayName("正常表名，应添加反引号")
        void testNormalTableName(String tableName) {
            assertThat(dialect.getSecurityTableName(tableName)).isEqualTo("`" + tableName.trim() + "`");
        }

        @ParameterizedTest
        @ValueSource(strings = {"db.user", "`db`.user", "db.`user`", "`db`.`user`"})
        @DisplayName("已包含点的表名，不应添加反引号")
        void testTableNameWithDot(String tableName) {
            assertThat(dialect.getSecurityTableName(tableName)).isEqualTo(tableName.trim());
        }
    }

    @Nested
    @DisplayName("getSecurityFields方法测试")
    class GetSecurityFieldsTest {

        static Stream<Arguments> variousFieldsAndTableNamesProvider() {
            return Stream.of(
                    Arguments.of("*", "user", "`user`.*"),
                    Arguments.of("age", "user", "`user`.`age`"),
                    Arguments.of(" age ", "user", "`user`.`age`"),
                    Arguments.of("name", "user", "`user`.`name`"),
                    Arguments.of("age, name", "user", "`user`.`age`,`user`.`name`"),
                    Arguments.of("age,name", "user", "`user`.`age`,`user`.`name`"),
                    Arguments.of("age,,name", "user", "`user`.`age`,`user`.`name`"),
                    Arguments.of("age, name", " ", "`age`,`name`"), //tableName为空格
                    Arguments.of("age, name", "`user`", "`user`.`age`,`user`.`name`"), //tableName 已经包含反引号
                    Arguments.of("age ,name , (select type from user_type t where t.user_id = ${id}) type", "user", "`user`.`age`,`user`.`name`,(select type from user_type t where t.user_id = ${id}) type"),
                    Arguments.of("age ,name , (select type from user_type t where t.user_id = ${id}) type,(select status from user_type t where t.user_id = ${id}) status", "user", "`user`.`age`,`user`.`name`,(select type from user_type t where t.user_id = ${id}) type,(select status from user_type t where t.user_id = ${id}) status"),
                    Arguments.of("user.age", "user", "user.age"), //字段已包含表名
                    Arguments.of("user.age", "other_table", "user.age"), //字段已包含表名，tableName不匹配
                    Arguments.of("(select max(age) from user) max_age", "user", "(select max(age) from user) max_age"), //字段是子查询
                    Arguments.of("MAX(age)", "user", "`user`.`MAX(age)`"), // 字段是函数
                    Arguments.of("count(*)", "user", "`user`.`count(*)`"), // 字段是聚合函数
                    Arguments.of("CASE WHEN age > 18 THEN 'adult' ELSE 'minor' END as age_group", "user", "`user`.`CASE WHEN age > 18 THEN 'adult' ELSE 'minor' END` age_group") // 字段是case when
            );
        }

        @ParameterizedTest
        @MethodSource("variousFieldsAndTableNamesProvider")
        @DisplayName("各种字段和表名组合")
        void testVariousFieldsAndTableNames(String fields, String tableName, String expectedFields) {
            assertThat(dialect.getSecurityFields(fields, tableName)).isEqualTo(expectedFields);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ","\n"})
        @DisplayName("当fields为空时, 应抛出IllegalArgumentException")
        void testBlankFieldsThrowsIllegalArgumentException(String blankFields) {
            assertThrows(IllegalArgumentException.class, () -> dialect.getSecurityFields(blankFields, "user"));
        }

        @ParameterizedTest
        @NullSource
        @DisplayName("当fields为Null时, 应抛出NullPointerException")
        void testBlankFieldsThrowsNullPointerException(String blankFields) {
            assertThrows(NullPointerException.class, () -> dialect.getSecurityFields(blankFields, "user"));
        }
    }


    @Nested
    @DisplayName("forDbSimpleQuery方法测试")
    class ForDbSimpleQueryTest {
        @Test
        @DisplayName("无filters参数，应生成不带where条件的查询SQL")
        void testWithoutFilters() {
            assertThat(dialect.forDbSimpleQuery("*", "user")).isEqualTo("select * from `user`");
        }

        @Test
        @DisplayName("有filters参数，应生成带where条件的查询SQL")
        void testWithFilters() {
            assertThat(dialect.forDbSimpleQuery("*", "user", "age > 18")).isEqualTo("select * from `user` where age > 18");
        }
    }

    @Nested
    @DisplayName("forDbFindById方法测试")
    class ForDbFindByIdTest {

        static Stream<Arguments> differentPrimaryKeyAndColumnsProvider() {
            return Stream.of(
                    Arguments.of("id", "age,name", "select `age`, `name` from `user` where `id` = ?"),
                    Arguments.of("userId", "*", "select * from `user` where `userId` = ?"),
                    Arguments.of("order_id", "order_no,order_date", "select `order_no`, `order_date` from `user` where `order_id` = ?"),
                    Arguments.of("productID", "productName , productPrice", "select `productName`, `productPrice` from `user` where `productID` = ?")
            );
        }

        @ParameterizedTest
        @MethodSource("differentPrimaryKeyAndColumnsProvider")
        @DisplayName("不同主键和列名")
        void testDifferentPrimaryKeyAndColumns(String primaryKey, String columns, String expectedSql) {
            assertThat(dialect.forDbFindById("user", primaryKey, columns)).isEqualTo(expectedSql);
        }
    }

    @Nested
    @DisplayName("forDbDeleteById方法测试")
    class ForDbDeleteByIdTest {
        @ParameterizedTest
        @ValueSource(strings = {"id", "userId", "orderId", "productId"})
        @DisplayName("不同主键")
        void testDifferentPrimaryKeys(String primaryKey) {
            assertThat(dialect.forDbDeleteById("user", primaryKey)).isEqualTo("delete from `user` where `" + primaryKey + "` = ?");
        }
    }

    @Nested
    @DisplayName("forDbSave方法测试")
    class ForDbSaveTest {
        @Test
        @DisplayName("空Record，应返回空字符串")
        void testEmptyRecord() {
            Record record = new Record();
            List<Object> params = new ArrayList<>();
            assertThat(dialect.forDbSave("user", record, params)).isEmpty();
            assertThat(params).isEmpty();
        }

        @Test
        @DisplayName("非空Record，应生成insert SQL并填充参数")
        void testNonEmptyRecord() {
            Record record = new Record().set("name", "john").set("age", 30);
            List<Object> params = new ArrayList<>();
            assertThat(dialect.forDbSave("user", record, params)).isEqualTo("insert into `user`(`name`, `age`) values(?, ?)");
            assertThat(params).containsExactly( "john",30);
        }

        @Test
        @DisplayName("包含特殊字符的字段名")
        void testSpecialCharFieldName() {
            Record record = new Record().set("user.name", "john").set("order-id", 1001);
            List<Object> params = new ArrayList<>();
            assertThat(dialect.forDbSave("user", record, params)).isEqualTo("insert into `user`(`user.name`, `order-id`) values(?, ?)");
            assertThat(params).containsExactly( "john",1001);
        }
    }

    @Nested
    @DisplayName("forDbUpdate方法测试")
    class ForDbUpdateTest {
        @Test
        @DisplayName("空Record，应返回空字符串")
        void testEmptyRecord() {
            Record record = new Record();
            List<Object> params = new ArrayList<>();
            assertThat(dialect.forDbUpdate("user", "id", 1, record, params)).isEmpty();
            assertThat(params).isEmpty();
        }

        @Test
        @DisplayName("非空Record，应生成update SQL并填充参数，排除主键字段")
        void testNonEmptyRecord() {
            Record record = new Record().set("name", "john").set("age", 30).set("id", 1); // 包含主键字段，应被排除
            List<Object> params = new ArrayList<>();
            assertThat(dialect.forDbUpdate("user", "id", 1, record, params)).isEqualTo("update `user` set `name` = ? , `age` = ?  where `id` = ?");
            assertThat(params).containsExactly( "john", 30,1);
        }

        @Test
        @DisplayName("主键字段名大小写不敏感")
        void testPrimaryKeyCaseInsensitive() {
            Record record = new Record().set("name", "john").set("Age", 30);
            List<Object> params = new ArrayList<>();
            assertThat(dialect.forDbUpdate("user", "ID", 1, record, params)).isEqualTo("update `user` set `name` = ? , `Age` = ?  where `ID` = ?");
            assertThat(params).containsExactly( "john",30, 1);
        }
    }

    @Nested
    @DisplayName("forDbDelete方法测试")
    class ForDbDeleteTest {
        @Test
        @DisplayName("带filters参数，应生成带where条件的delete SQL")
        void testWithFilters() {
            assertThat(dialect.forDbDelete("user", "age < 18")).isEqualTo("delete from `user` where age < 18");
        }

        @Test
        @DisplayName("filters参数包含特殊字符")
        void testFiltersWithSpecialChars() {
            assertThat(dialect.forDbDelete("user", "name = 'john' and status != 'deleted'")).isEqualTo("delete from `user` where name = 'john' and status != 'deleted'");
        }
    }

    @Nested
    @DisplayName("forDbPaginationQuery方法测试")
    class ForDbPaginationQueryTest {

        static Stream<Arguments> variousSqlAndPaginationParamsProvider() {
            return Stream.of(
                    Arguments.of("select * from user", 0L, 10, "select * from user limit 0,10"),
                    Arguments.of("SELECT * FROM USER", 10L, 20, "SELECT * FROM USER limit 10,20"),
                    Arguments.of("select * from user where age > 18", 5L, 15, "select * from user where age > 18 limit 5,15"),
                    Arguments.of("select * from user order by age desc", 20L, 10, "select * from user order by age desc limit 20,10"),
                    Arguments.of("select count(*) from user", 0L, 10, "select count(*) from user limit 0,10"), // count 查询
                    Arguments.of("SELECT * FROM (SELECT id, name FROM user WHERE status = 'active') AS active_users", 0L, 5, "SELECT * FROM (SELECT id, name FROM user WHERE status = 'active') AS active_users limit 0,5"), // 子查询
                    Arguments.of("select * from user limit 10", 0L, 10, "select * from user limit 0,10"), // SQL 已经包含 limit
                    Arguments.of("select * from user LIMIT 20, 10", 0L, 10, "select * from user limit 0,10"), // SQL 已经包含大写 LIMIT
                    Arguments.of("select * from user  limit 30 , 5", 0L, 10, "select * from user  limit 0,10"), // SQL 包含空格和 limit
                    Arguments.of("select * from user --limit 40, 6", 0L, 10, "select * from user --limit 40, 6 limit 0,10") // SQL 包含注释和 limit, 注释后的limit不应该被移除
            );
        }

        @ParameterizedTest
        @MethodSource("variousSqlAndPaginationParamsProvider")
        @DisplayName("各种SQL和分页参数")
        void testVariousSqlAndPaginationParams(String origSql, long start, int size, String expectedSql) {
            assertThat(dialect.forDbPaginationQuery(origSql, start, size)).isEqualTo(expectedSql);
        }

    }
}
