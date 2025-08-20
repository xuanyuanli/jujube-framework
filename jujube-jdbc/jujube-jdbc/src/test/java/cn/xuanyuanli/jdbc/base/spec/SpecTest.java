package cn.xuanyuanli.jdbc.base.spec;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import cn.xuanyuanli.jdbc.exception.DaoQueryException;
import cn.xuanyuanli.jdbc.base.util.Sqls;
import org.junit.jupiter.api.Test;

public class SpecTest {

    @Test
    public void testEq() {
        Spec spec = new Spec().eq("name", "abc");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name`= ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("abc");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testEqEmpty() {
        org.junit.jupiter.api.Assertions.assertThrows(DaoQueryException.class, () -> new Spec().eq("name", ""));
    }

    @Test
    public void testEqNull() {
        org.junit.jupiter.api.Assertions.assertThrows(DaoQueryException.class, () -> new Spec().eq("name", null));
    }

    @Test
    public void testLike() {
        Spec spec = new Spec().like("name", Sqls.leftLikeWrap("abc"));
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` like ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("%abc");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testLikeEx() {
        org.junit.jupiter.api.Assertions.assertThrows(DaoQueryException.class, () -> new Spec().like("name", ""));
    }

    @Test
    public void testNotlike() {
        Spec spec = new Spec().notlike("name", "abc");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` not like ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("abc");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testGt() {
        Spec spec = new Spec().gt("age", "12");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`age` > ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("12");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testLt() {
        Spec spec = new Spec().lt("age", "12");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`age` < ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("12");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testGte() {
        Spec spec = new Spec().gte("age", "12");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`age` >= ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("12");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testLte() {
        Spec spec = new Spec().lte("age", "12");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`age` <= ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("12");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testNot() {
        Spec spec = new Spec().not("age", "12");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`age` <> ?");
        assertThat(spec.getFilterParams()).hasSize(1).contains("12");
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testIsNull() {
        Spec spec = new Spec().isNull("name");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` is null");
        assertThat(spec.getFilterParams()).isEmpty();
        assertThat(spec.getConditions()).isEmpty();
    }

    @Test
    public void testIsNotNull() {
        Spec spec = new Spec().isNotNull("name");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` is not null");
        assertThat(spec.getFilterParams()).isEmpty();
        assertThat(spec.getConditions()).isEmpty();
    }

    @Test
    public void testIsEmpty() {
        Spec spec = new Spec().isEmpty("name");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` = ''");
        assertThat(spec.getFilterParams()).isEmpty();
        assertThat(spec.getConditions()).isEmpty();
    }

    @Test
    public void testIsNotEmpty() {
        Spec spec = new Spec().isNotEmpty("name");
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` <> ''");
        assertThat(spec.getFilterParams()).isEmpty();
        assertThat(spec.getConditions()).isEmpty();
    }

    @Test
    public void testIn() {
        List<Long> list = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        Spec spec = new Spec().in("name", list);
        System.out.println(spec.getFilterSql());
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` in(?,?,?)");
        assertThat(spec.getFilterParams()).containsExactly(1L, 2L, 3L);
        assertThat(spec.getConditions()).hasSize(1);
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testNotin() {
        List<Long> list = new ArrayList<>(Arrays.asList(1L, 2L, 3L));
        Spec spec = new Spec().notin("name", list);
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`name` not in(?,?,?)");
        assertThat(spec.getFilterParams()).containsExactly(1L, 2L, 3L);
        assertThat(spec.getConditions()).hasSize(1);
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(1);
    }

    @Test
    public void testOr() {
        Spec rule = new Spec().eq("age", 12);
        Spec rule2 = new Spec().like("age", 14);
        Spec spec = new Spec().or(rule, rule2);
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("(`age`= ? or `age` like ?)");
        assertThat(spec.getFilterParams()).contains(12).contains(14);
        assertThat(spec.getConditions()).hasSize(2);
    }

    @Test
    public void testAnd() {
        Spec rule = new Spec().eq("age", 12);
        Spec rule2 = new Spec().like("age", 14);
        Spec spec = new Spec().and(rule, rule2);
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("(`age`= ? and `age` like ?)");
        assertThat(spec.getFilterParams()).contains(12).contains(14);
        assertThat(spec.getConditions()).hasSize(2);
    }

    @Test
    public void testBetween() {
        Spec spec = new Spec().between("age", 18, 30);
        assertThat(spec.getSpecMap()).hasSize(1);
        assertThat(spec.getFilterSql()).isEqualTo("`age` between ? and ?");
        System.out.println(spec.getFilterSql());
        assertThat(spec.getFilterParams()).contains(18).contains(30);
        assertThat(spec.getConditions()).hasSize(1);
        assertThat(spec.getConditions().get(0).getParamNum()).isEqualTo(2);
    }

    @Test
    public void testSort() {
        Spec spec = new Spec().sort().desc("age").end();
        assertThat(spec.sort().buildSqlSort()).isEqualTo(" order by age desc");
        assertThat(spec.getFilterParams()).isEmpty();
    }

    @Test
    public void testClone() {
        Spec spec = new Spec().eq("name", "abc").limit(10).sort().asc("age").desc("number").end();
        Spec spec2 = spec.clone();
        assertThat(spec == spec2).isFalse();
        assertThat(spec.getClass()).isEqualTo(spec2.getClass());
        assertThat(spec).isEqualTo(spec2);
    }

    @Test
    public void testNewS() {
        Spec spec = new Spec();
        assertThat(spec).isNotNull();
    }

    @Test
    public void jsonContainsNoPath() {
        Spec spec = new Spec();
        spec.jsonContains("name", "li", null);
        assertThat(spec.getFilterSql()).isEqualTo(" json_contains(`name`, ?)");
        assertThat(spec.getFilterParams()).containsExactly("\"li\"");

        spec = new Spec();
        spec.jsonContains("age", 40, null);
        assertThat(spec.getFilterSql()).isEqualTo(" json_contains(`age`, ?)");
        assertThat(spec.getFilterParams()).containsExactly("40");
    }

    @Test
    public void jsonContainsHasPath() {
        Spec spec = new Spec();
        spec.jsonContains("name", "li", "$.a");
        assertThat(spec.getFilterSql()).isEqualTo(" json_contains(`name`, ?, ?)");
        assertThat(spec.getFilterParams()).containsExactly("\"li\"", "$.a");

        spec = new Spec();
        spec.jsonContains("age", 50L, "$.a");
        assertThat(spec.getFilterSql()).isEqualTo(" json_contains(`age`, ?, ?)");
        assertThat(spec.getFilterParams()).containsExactly("50", "$.a");
    }
}
