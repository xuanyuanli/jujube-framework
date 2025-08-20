package cn.xuanyuanli.jdbc.binding;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

public class SqlBuilderTest {

    @Test
    public void builderIfNotBlankNotNullLikeEqIn() {
        List<String> originSql = new ArrayList<>(Arrays.asList("<#if notBlank(name)>", "  and (u.name like '%${name}%' or u.name = ${name})" + "</#if>",
                "<#if notNull(ids)>", "  and u.id in (${join(ids,',')})", "</#if>"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("ids", new ArrayList<>(Arrays.asList("1", "2", "3")));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and u.id in (?,?,?)");
        assertThat(result.getFilterParams()).containsExactly("1", "2", "3");

        map = new HashMap<>();
        map.put("ids", new ArrayList<>(Arrays.asList(1, 2, 3)));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and u.id in (?,?,?)");
        assertThat(result.getFilterParams()).containsExactly(1L, 2L, 3L);

        originSql = new ArrayList<>(List.of("<#if isFilter>abc</#if>"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("isFilter", true);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("abc");
    }

    @Test
    public void builderIfNotBlankNotNullLikeEqInIfNullAndGt() {
        List<String> originSql = new ArrayList<>(Arrays.asList("<#if notBlank(name)>", "  and (u.name like '%${name}%' or u.name = ${name})", "</#if>",
                "<#if notNull(ids)>", "  and u.id in (${join(ids,',')})", "</#if>", "<#if age?? && age gt 0>", "  and u.age > ${age}", "</#if>"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "abc'");
        map.put("age", 10);
        map.put("ids", new ArrayList<>(Arrays.asList(1, 2, 3)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and (u.name like ? or u.name = ?)     and u.id in (?,?,?)     and u.age > ?");
        assertThat(result.getFilterParams()).containsExactly("%abc'%", "abc'", 1L, 2L, 3L, 10L);

        map = new HashMap<>();
        map.put("age", 10);
        map.put("ids", new ArrayList<>(Arrays.asList(1, 2, 3)));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and u.id in (?,?,?)     and u.age > ?");
        assertThat(result.getFilterParams()).containsExactly(1L, 2L, 3L, 10L);

        map = new HashMap<>();
        map.put("name", "abc'");
        map.put("ids", new ArrayList<>(Arrays.asList(1, 2, 3)));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and (u.name like ? or u.name = ?)     and u.id in (?,?,?)");
        assertThat(result.getFilterParams()).containsExactly("%abc'%", "abc'", 1L, 2L, 3L);

        map = new HashMap<>();
        map.put("name", "abc'");
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and (u.name like ? or u.name = ?)");
        assertThat(result.getFilterParams()).hasSize(2).containsExactly("%abc'%", "abc'");

        map = new HashMap<>();
        map.put("ids", new ArrayList<>(Arrays.asList(1, 2, 3)));
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and u.id in (?,?,?)");
        assertThat(result.getFilterParams()).containsExactly(1L, 2L, 3L);
    }

    @Test
    public void builderComment() {
        List<String> originSql = new ArrayList<>(Arrays.asList("#注释", "   #注释   ", "select * from user;"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user");
    }

    @Test
    public void builderListLike() {
        List<String> originSql = Arrays.asList(("""
                <#if category??>
                and (
                    <#list category as c>
                    p.category like '%${c}%'
                    <#if c_has_next> or </#if>
                    </#list>
                    )
                </#if>""").split("\n"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("category", new ArrayList<>(Arrays.asList("china", "类")));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("and (          p.category like ?      or           p.category like ?               )");
        assertThat(result.getFilterParams()).containsExactly("%china%", "%类%");
    }

    @Test
    public void builderLikeInLonger() {
        List<String> originSql = new ArrayList<>(Arrays.asList("name like                                                      '%${name}%'",
                "type in                                                      (${join(type,',')})"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "类");
        map.put("type", new ArrayList<>(Arrays.asList(5, 8)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo(
                "name like                                                      ? type in                                                      (?,?)");
        assertThat(result.getFilterParams()).containsExactly("%类%", 5L, 8L);
    }

    @Test
    public void builderLike() {
        List<String> originSql = new ArrayList<>(List.of("name like'%${name}%'"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "类");
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo(
                "name like?");
        assertThat(result.getFilterParams()).containsExactly("%类%");
    }

    @Test
    public void builderSimple() {
        List<String> originSql = new ArrayList<>(List.of("age > ${age}"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("age", 20);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("age > ?");
        assertThat(result.getFilterParams()).containsExactly(20L);
    }

    @Test
    public void builderAssign() {
        List<String> list = new ArrayList<>(Arrays.asList("<#assign sub='and time > ${begin}'>type = 1 @{sub}",
                "<#assign sub>and time > ${begin}</#assign>type = 1 @{sub}",
                "<#assign sub><#if begin?? && begin gt 0>and time > ${begin}</#if></#assign>type = 1 @{sub}"));
        for (String s : list) {
            List<String> originSql = new ArrayList<>(Collections.singletonList(s));
            SqlBuilder sqlBuilder = new SqlBuilder(originSql);
            Map<String, Object> map = new HashMap<>();
            map.put("begin", 2022);
            SqlBuilder.SqlResult result = sqlBuilder.builder(map);
            assertThat(result.getSql()).isEqualTo("type = 1 and time > ?");
            assertThat(result.getFilterParams()).containsExactly(2022L);
        }

    }

    @Test
    public void builderCalc() {
        List<String> list = new ArrayList<>(Arrays.asList("time > ${now + 10*24}", "time > ${(now + 10*24)}", "time > ${ (now + 10*24 )}", "time > ${ ( now + 10*24 ) }"));
        for (String s : list) {
            List<String> originSql = new ArrayList<>(Collections.singletonList(s));
            SqlBuilder sqlBuilder = new SqlBuilder(originSql);
            Map<String, Object> map = new HashMap<>();
            map.put("now", 100);
            SqlBuilder.SqlResult result = sqlBuilder.builder(map);
            assertThat(result.getSql()).isEqualTo("time > ?");
            assertThat(result.getFilterParams()).containsExactly(340L);
        }
    }

    @Test
    void builderIn() {
        String str = " SELECT ap.id productId,ap.`matches_id` matchId,am.user_id auctionId,ap.`lot_id` lot,ap.`image_id`,ap.`name` productName,        ap.lowest_estimate_price as lowest,ap.highest_estimate_price as highest,ap.`unit`,ap.`start_price` startPrice,am.nature,        ap.`is_aborted`,ap.`is_allow_exit`,ap.`is_embargo`,am.begin_time beginTime,am.status matchStatus,au.name_cn auctionName,        ap.img_width imgWidth,ap.img_height imgHeight,ai.original_file_path AS imageUrl FROM product ap LEFT JOIN `match` am ON am.id = ap.matches_id LEFT JOIN user_auctioneer au on au.user_id = am.user_id LEFT JOIN product_image ai ON ai.id= ap.image_id WHERE ap.`status` = 1  AND ap.`id` NOT IN(${join(priorityProIds,',')}) and ap.id > 0;    ";
        SqlBuilder sqlBuilder = new SqlBuilder(new ArrayList<>(List.of(str)));
        Map<String, Object> map = new HashMap<>();
        map.put("priorityProIds", new ArrayList<>(Arrays.asList(5, 8)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo(
                "SELECT ap.id productId,ap.`matches_id` matchId,am.user_id auctionId,ap.`lot_id` lot,ap.`image_id`,ap.`name` productName,        ap.lowest_estimate_price as lowest,ap.highest_estimate_price as highest,ap.`unit`,ap.`start_price` startPrice,am.nature,        ap.`is_aborted`,ap.`is_allow_exit`,ap.`is_embargo`,am.begin_time beginTime,am.status matchStatus,au.name_cn auctionName,        ap.img_width imgWidth,ap.img_height imgHeight,ai.original_file_path AS imageUrl FROM product ap LEFT JOIN `match` am ON am.id = ap.matches_id LEFT JOIN user_auctioneer au on au.user_id = am.user_id LEFT JOIN product_image ai ON ai.id= ap.image_id WHERE ap.`status` = 1  AND ap.`id` NOT IN(?,?) and ap.id > 0");
        assertThat(result.getFilterParams()).containsExactly(5L, 8L);
    }

    @Test
    public void builderInBlank() {
        List<String> originSql = new ArrayList<>(List.of(
                "type in                                                      (  ${join(type,',')})"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();

        map.put("type", new ArrayList<>(Arrays.asList(5, 8)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("type in                                                      (  ?,?)");
        assertThat(result.getFilterParams()).containsExactly(5L, 8L);
    }

    @Test
    public void builderInByOriginJoin() {
        List<String> originSql = new ArrayList<>(List.of(
                "type in (${type?join(',')})"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();

        map.put("type", new ArrayList<>(Arrays.asList(5, 8, 4)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("type in (?,?,?)");
        assertThat(result.getFilterParams()).containsExactly(5L, 8L, 4L);
    }

    @Test
    public void builderInAndOrderFieldByOriginJoin() {
        List<String> originSql = new ArrayList<>(List.of(
                "type in (${type?join(',')}) order by field(type,${type?join(',')})"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();

        map.put("type", new ArrayList<>(Arrays.asList(5, 8, 4)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("type in (?,?,?) order by field(type,?,?,?)");
        assertThat(result.getFilterParams()).containsExactly(5L, 8L, 4L, 5L, 8L, 4L);
    }

    @Test
    public void builderInAndOrderField() {
        List<String> originSql = new ArrayList<>(List.of(
                "type in (${join(type,',')}) order by field(type,${join(type,',')})"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();

        map.put("type", new ArrayList<>(Arrays.asList(5, 8, 4)));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("type in (?,?,?) order by field(type,?,?,?)");
        assertThat(result.getFilterParams()).containsExactly(5L, 8L, 4L, 5L, 8L, 4L);
    }

    @Test
    public void builderInSpecialChar() {
        List<String> originSql = new ArrayList<>(List.of(
                "name in (${join(names,',')})"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("names", new ArrayList<>(Arrays.asList("Karl's Collections Inc", "MY Antique Collection, Inc", "红钻")));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("name in (?,?,?)");
        assertThat(result.getFilterParams()).containsExactly("Karl's Collections Inc", "MY Antique Collection, Inc", "红钻");
    }

    @Test
    public void builderOrigin() {
        List<String> originSql = new ArrayList<>(List.of("select * from user"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user");
    }

    @Test
    public void builderOriginRootBean() {
        List<String> originSql = new ArrayList<>(List.of("select * from user where id = ${cmd.id}"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("cmd", new User(12L));
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user where id = ?");
        assertThat(result.getFilterParams()).hasSize(1).containsExactly(12L);
    }

    @Test
    public void builderOriginRootMap() {
        List<String> originSql = new ArrayList<>(List.of("select * from user where id = ${cmd.id}"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        Map<Object, Object> root = new HashMap<>();
        root.put("id", 12L);
        map.put("cmd", root);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user where id = ?");
        assertThat(result.getFilterParams()).hasSize(1).containsExactly(12L);
    }

    @Test
    public void builderLimit() {
        List<String> originSql = new ArrayList<>(List.of("select * from user limit 10"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit 10");

        originSql = new ArrayList<>(List.of("select * from user limit ${t}"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("t", 10);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ?");

        originSql = new ArrayList<>(List.of("select * from user limit ${b},${e}"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        map.put("e", 5);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ?,?");

        originSql = new ArrayList<>(List.of("select * from user limit ${b},5"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ?,5");

        originSql = new ArrayList<>(List.of("select * from user limit ${b} , ${e}"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        map.put("e", 5);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ? , ?");

        originSql = new ArrayList<>(List.of("select * from user limit ${b} , 5"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("b", 10);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from user limit ? , 5");

        originSql = new ArrayList<>(List.of("select * from (select * from user limit 1) t"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select * from (select * from user limit 1) t");
    }

    @Test
    public void builderComplexLimit() {
        List<String> originSql = new ArrayList<>(List.of("select u.* from user u,(select id form user limit ${a}) t where t.id = u.id"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("a", 10);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select u.* from user u,(select id form user limit ?) t where t.id = u.id");

        originSql = new ArrayList<>(List.of("select u.* from user u,(select id form user limit ${a},${e}) t where t.id = u.id"));
        sqlBuilder = new SqlBuilder(originSql);
        map = new HashMap<>();
        map.put("a", 10);
        map.put("e", 5);
        result = sqlBuilder.builder(map);
        assertThat(result.getSql()).isEqualTo("select u.* from user u,(select id form user limit ?,?) t where t.id = u.id");
    }

    @Test
    public void builderUnion() {
        List<String> originSql = new ArrayList<>(List.of("  select * from user where id > ${id} ;", "#jujube-union ", "select * from user where id > ${id} and age = ${age} ;   \n",
                        "#jujube-union", "select * from product where name like '%${productName}%' and status = ${status};"));
        SqlBuilder sqlBuilder = new SqlBuilder(originSql);
        Map<String, Object> map = new HashMap<>();
        map.put("id", 10);
        map.put("age", 5);
        map.put("productName", "大明");
        map.put("status", 3);
        SqlBuilder.SqlResult result = sqlBuilder.builder(map);
        assertThat(result.isUnion()).isTrue();
        assertThat(result.getSql()).isEqualTo("select * from user where id > ?");
        assertThat(result.getFilterParams()).containsExactly(10L);

        assertThat(result.getUnionAfterSqlInfo().get(0).getSql()).isEqualTo("select * from user where id > ? and age = ?");
        assertThat(result.getUnionAfterSqlInfo().get(0).getFilterParams()).containsExactly(10L, 5L);

        assertThat(result.getUnionAfterSqlInfo().get(1).getSql()).isEqualTo("select * from product where name like ? and status = ?");
        assertThat(result.getUnionAfterSqlInfo().get(1).getFilterParams()).containsExactly("%大明%", 3L);
    }

    @Test
    void isJoinFunc() {
        assertThat(SqlBuilder.isJoinFunc("${join(type,',')}")).isTrue();
        assertThat(SqlBuilder.isJoinFunc("${ join( type, ',' ) }")).isTrue();
        assertThat(SqlBuilder.isJoinFunc("${type?join(',')}")).isTrue();
        assertThat(SqlBuilder.isJoinFunc("${ type?join( ',' ) }")).isTrue();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {

        private Long id;
    }

}
