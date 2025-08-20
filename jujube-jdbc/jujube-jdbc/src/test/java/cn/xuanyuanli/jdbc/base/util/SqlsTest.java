package cn.xuanyuanli.jdbc.base.util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.util.Collections3;
import org.junit.jupiter.api.Test;

public class SqlsTest {

    @Test
    public void getSecurityFieldName() {
        Assertions.assertThat(Sqls.getSecurityFieldName("name")).isEqualTo("`name`");
        Assertions.assertThat(Sqls.getSecurityFieldName("`name`")).isEqualTo("`name`");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name")).isEqualTo("u.`name`");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.`name`")).isEqualTo("u.`name`");
        Assertions.assertThat(Sqls.getSecurityFieldName("*")).isEqualTo("*");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.*")).isEqualTo("u.*");
        Assertions.assertThat(Sqls.getSecurityFieldName("id user_id")).isEqualTo("`id` user_id");
        Assertions.assertThat(Sqls.getSecurityFieldName("`id` user_id")).isEqualTo("`id` user_id");
        Assertions.assertThat(Sqls.getSecurityFieldName("id as user_id")).isEqualTo("`id` user_id");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name userName")).isEqualTo("u.`name` userName");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name AS userName")).isEqualTo("u.`name` userName");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name As userName")).isEqualTo("u.`name` userName");
        Assertions.assertThat(Sqls.getSecurityFieldName("u.name aS userName")).isEqualTo("u.`name` userName");
    }

    @Test
    public void getCountSql() {
        // 普通
        String sql = "select * from user u ;\n";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM user u");

        // order by
        sql = "select * from user u order by u.age;";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM user u");

        // 子查询+order by 1
        sql = "select * from (select * from a where a.id = 5) order by age\n;";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM a WHERE a.id = 5)");

        // 子查询+order by 2
        sql = "select a.*,(select * from b where b.id = a.id limit 1) 't' from a order by a.age";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM a");

        // 子查询+order by 3
        sql = "select a.*,(select * from b where b.id = a.id group by b.age limit 1) 't' from (select * from u group by u.id) a order by a.age";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM u GROUP BY u.id) a");
    }

    @Test
    public void getCountSql2() {
        // 普通
        String sql = "select * from (select * from user u) t5";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM user u) t5");

        // order by
        sql = "select * from (select * from user u order by u.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM user u ORDER BY u.age) t5");

        // 子查询+order by 1
        sql = "select * from (select * from (select * from a where a.id = 5) order by age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM (SELECT * FROM a WHERE a.id = 5) ORDER BY age) t5");

        // 子查询+order by 2
        sql = "select * from (select a.*,(select * from b where b.id = a.id limit 1) 't' from a order by a.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT a.*, (SELECT * FROM b WHERE b.id = a.id LIMIT 1) 't' FROM a ORDER BY a.age) t5");

        // 子查询+order by 3
        sql = "select * from (select a.*,(select * from b where b.id = a.id group by b.age limit 1) 't' from (select * from u group by u.id) a order by a.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo(
                "SELECT COUNT(*) FROM (SELECT a.*, (SELECT * FROM b WHERE b.id = a.id GROUP BY b.age LIMIT 1) 't' FROM (SELECT * FROM u GROUP BY u.id) a ORDER BY a.age) t5");

        // 子查询+order by 4
        sql = "select * from (select a.*,(select * from b where b.id = a.id limit 1) 't' from (select * from user) t2 group by a.age) t5";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT a.*, (SELECT * FROM b WHERE b.id = a.id LIMIT 1) 't' FROM (SELECT * FROM user) t2 GROUP BY a.age) t5");
    }

    @Test
    public void getCountSqlOfGroupBy() {
        String sql = "select * from user group by id order by age";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT 1 FROM user GROUP BY id ) getcountsql_t_t");

        sql = "select * from (select *,(select age from stu where name = '123') u from (select * from stu s) t1 group by id order by age) t2";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo(
                        "SELECT COUNT(*) FROM (SELECT *, (SELECT age FROM stu WHERE name = '123') u FROM (SELECT * FROM stu s) t1 GROUP BY id ORDER BY age) t2");
    }

    @Test
    public void getCountSqlOfUnion() {
        String sql = "select * from x group by id order by age union select * from stu s where s.type >5";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM x GROUP BY id ORDER BY age UNION SELECT * FROM stu s WHERE s.type > 5) getcountsql_t_t");
    }

    @Test
    public void getCountSqlOfUnion2() {
        String sql = "select * from (select * from x group by id order by age union select * from stu s where s.type >5) t";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM x GROUP BY id ORDER BY age UNION SELECT * FROM stu s WHERE s.type > 5) t");
    }

    @Test
    public void getCountSqlOfUnion3() {
        String sql = "select *,(select count(*) from user) num from (select * from x group by id order by age union select * from stu s where s.type >5) getcountsql_t_t";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM (SELECT * FROM x GROUP BY id ORDER BY age UNION SELECT * FROM stu s WHERE s.type > 5) getcountsql_t_t");
    }

    @Test
    public void getCount4() {
        // 普通
        String sql = "select count(*) from user u group by id";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM (SELECT 1 FROM user u GROUP BY id ) getcountsql_t_t");
    }

    @Test
    public void getCount5() {
        // 普通
        String sql = "select u.id from user u where (select count(*) from bill b where b.uid=u.id) > 0 group by id";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo(
                        "SELECT COUNT(*) FROM (SELECT 1 FROM user u WHERE (SELECT count(*) FROM bill b WHERE b.uid = u.id) > 0 GROUP BY id ) getcountsql_t_t");
    }

    @Test
    public void getCountOfJoin() {
        String sql = "select * from a left join b on b.id = a.id where a.age = 4 and b.id > 6";
        String countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql).isEqualTo("SELECT COUNT(*) FROM a LEFT JOIN b ON b.id = a.id  WHERE a.age = 4 AND b.id > 6");

        sql = "select * from a left join b on b.id = a.id left join c on c.id = b.bid where a.age = 4 and b.id > 6";
        countSql = Sqls.getCountSql(sql);
        Assertions.assertThat(countSql)
                .isEqualTo("SELECT COUNT(*) FROM a LEFT JOIN b ON b.id = a.id LEFT JOIN c ON c.id = b.bid  WHERE a.age = 4 AND b.id > 6");
    }

    @Test
    void getCountSqlWithIn() {
        Assertions.assertThat(Sqls.getCountSqlWithIn("select * from a where a.id in (select id from a where a.type = 1) ; "))
                .isEqualTo("SELECT COUNT(*) FROM a WHERE a.type = 1");
        Assertions.assertThat(Sqls.getCountSqlWithIn("select * from a where a.id = 1")).isEqualTo("SELECT COUNT(*) FROM a WHERE a.id = 1");
    }

    @Test
    public void singleQuotes() {
        String sql = Sqls.inJoin(new ArrayList<>(Arrays.asList("name", "17世纪 铜鎏金自在观音'd", "\\d'")));
        Assertions.assertThat(sql).isEqualTo("'name','17世纪 铜鎏金自在观音\\'d','\\\\d\\''");
    }

    @Test
    public void getBatchInsertSql() {
        List<Map<String, Object>> mapList = new ArrayList<>();
        mapList.add(Collections3.newHashMap("id", "1", "age", 12, "name_cn", null));
        String sql = Sqls.getBatchInsertSql("user", new ArrayList<>(Arrays.asList("id", "age", "name_cn")), mapList);
        Assertions.assertThat(sql).isEqualTo("insert into user (`id` ,`age` ,`name_cn`) values('1' ,12 ,NULL)");
    }

    @Test
    public void getBatchInsertSql2() {
        String sql = Sqls.getBatchInsertSql("user", new ArrayList<>(List.of(new BatchEntity(1L, 12, "jack", null))));
        Assertions.assertThat(sql).isEqualTo("insert into user (`age` ,`desc` ,`id` ,`name_cn`) values(12 ,NULL ,1 ,'jack')");
    }

    @Test
    void realSql(){
        String sql = Sqls.realSql("select * from a where t = ? and b like ?", new ArrayList<>(Arrays.asList(1, "%2")));
        Assertions.assertThat(sql).isEqualTo("select * from a where t = 1 and b like '%2'");

        sql = Sqls.realSql("insert into a values(?,?,?)", new ArrayList<>(Arrays.asList(1, 2,3)));
        Assertions.assertThat(sql).isEqualTo("insert into a values(1,2,3)");

        sql = Sqls.realSql("update info set a=?,b=? where id=?", new ArrayList<>(Arrays.asList(1, 2,3,4)));
        Assertions.assertThat(sql).isEqualTo("update info set a=1,b=2 where id=3");
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchEntity {

        private Long id;
        private Integer age;
        private String name_cn;
        private String desc;
    }
}
