package cn.xuanyuanli.jdbc.base.jpa.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Data;
import cn.xuanyuanli.jdbc.base.BaseDao;
import cn.xuanyuanli.jdbc.base.annotation.Column;
import cn.xuanyuanli.jdbc.base.jpa.strategy.BaseQueryStrategy;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DaoMethod;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.jdbc.base.spec.Spec;
import cn.xuanyuanli.core.lang.BaseEntity;
import cn.xuanyuanli.core.util.Beans;
import org.junit.jupiter.api.Test;

public class HandlerTest {

    @SuppressWarnings("unused")
    public interface HandlerEntityDao extends BaseDao<HandlerEntity, Long> {

        long findIdByRecommendStatus(int status);

        long demo();
    }

    @Data
    public static class HandlerEntity implements BaseEntity {

        private Long id;
        private Long beginTime;
        private Long parentId;
        private Long recommendStatus;
        private String name;
        private String source;
        private String title;
        private String mobile;
        private String subTitle;
        private Integer userType;
        private Integer authStatus;
        private Integer status;
        private Integer age;
        private Integer type;
        private Integer androidVersion;

        @Column("mname")
        private String aliasName;
    }

    private static DaoMethod getMethod(String methodName, Class<?>... clazz) {
        return new JavaDaoMethod(Beans.getMethod(HandlerEntityDao.class, methodName, clazz));
    }

    public static DaoMethod demoMethod() {
        return getMethod("demo");
    }

    @Test
    public void eqHandler() {
        EqHandler eqHandler = new EqHandler();
        Spec spec = new Spec();
        String methodName = "UserType";
        List<Object> args = Lists.newArrayList(1);
        eqHandler.handler(demoMethod(), spec, methodName, args, null);
        assertThat(spec.getFilterSql()).isEqualTo("`user_type`= ?");
        assertThat(spec.getFilterParams()).contains(1).hasSize(1);
        assertThat(args).isEmpty();
    }

    @Test
    public void likeHandler() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "NameLikeAndSourceNotLike";
        List<Object> args = Lists.newArrayList("12", "微软");
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`name` like ? and `source` not like ?)");
        assertThat(args).isEmpty();
    }

    @Test
    public void complexSpec() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "AgeAndNameLikeAndTypeBetweenAndSourceInAndTitleIsNullAndSubTitleIsNotNullAndMobileNotOrderByIdDescLimit10";
        List<Object> args = Lists.newArrayList(12, "微软", 3, 6, Lists.newArrayList("a\\", "b'"), "15911105446");
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo(
                "(`age`= ? and `name` like ? and `type` between ? and ? and `source` in(?,?) and `title` is null and `sub_title` is not null and `mobile` <> ?)");
        assertThat(spec.sort().buildSqlSort()).isEqualTo(" order by id desc");
        assertThat(spec.getLimit()).isEqualTo(10);
        assertThat(args).isEmpty();

        assertThat(spec.getFilterParams()).hasSize(7).containsExactly(12, "微软", 3, 6, "a\\", "b'", "15911105446");
    }

    @Test
    public void complexSpec2() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "SourceNotInAndTitleIn";
        List<Object> args = Lists.newArrayList(Lists.newArrayList("1", "2"), Lists.newArrayList("a", "b"));
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`source` not in(?,?) and `title` in(?,?))");
        assertThat(args).isEmpty();

        assertThat(spec.getFilterParams()).containsExactly("1", "2", "a", "b");
    }

    @Test
    public void complexSpec3() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "BeginTimeIsNotNullAndParentIdAndStatusNotAndAuthStatusInOrderByBeginTime";
        List<Object> args = Lists.newArrayList(0, 1, Lists.newArrayList("a", "b"));
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`begin_time` is not null and `parent_id`= ? and `status` <> ? and `auth_status` in(?,?))");
        assertThat(args).isEmpty();

        assertThat(spec.getConditions()).hasSize(3);
        assertThat(spec.getFilterParams()).containsExactly(0, 1, "a", "b");
    }

    @Test
    public void groupBy() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.handler(demoMethod(), spec, "GroupByIdAndAgeLimit1", null);
        assertThat(spec.getGroupBy()).isEqualTo("id,age");
        assertThat(spec.getLimit()).isEqualTo(1);
    }

    @Test
    public void orderBy() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.handler(demoMethod(), spec, "OrderByIdAndAgeDescAndTitleAsc", null);
        assertThat(spec.sort().buildSqlSort()).isEqualTo(" order by id,age desc,title");
    }

    @Test
    public void simpleSpec() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "IdAndTitleIsNotEmpty";
        List<Object> args = Lists.newArrayList(12L);
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`id`= ? and `title` <> '')");
        Object[] filterParams = spec.getFilterParams();
        assertThat(filterParams).hasSize(1).containsExactly(12L);
    }

    @Test
    public void findAndByAnd() {
        DaoMethod method = getMethod("findIdByRecommendStatus", int.class);
        String queryField = BaseQueryStrategy.getDbColumnName(method, "RecommendStatus");
        assertThat(queryField).isEqualTo("recommend_status");

        queryField = BaseQueryStrategy.getDbColumnName(method, "aliasName");
        assertThat(queryField).isEqualTo("mname");
    }

    @Test
    public void handlerChain() {
        Spec spec = new Spec();
        DefaultHandlerChain selfChain = new DefaultHandlerChain();
        selfChain.addHandlers(HandlerContext.PREPOSITION_HANDLER);
        selfChain.addHandlers(HandlerContext.COMPLEX_HANDLER);
        selfChain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        String tmname = "AgeGtAndTypeLteAndSourceGteAndIdLt";
        List<Object> args = Lists.newArrayList(3, 6, 1, 2);
        selfChain.handler(demoMethod(), spec, tmname, args);
        assertThat(spec.getFilterSql()).isEqualTo("(`age` > ? and `type` <= ? and `source` >= ? and `id` < ?)");
        assertThat(spec.getFilterParams()).containsExactly(3, 6, 1, 2).hasSize(4);
    }

    @Test
    public void jsonContainsHandler() {
        DefaultHandlerChain chain = new DefaultHandlerChain();
        chain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        Spec spec = new Spec();
        String methodName = "NameJsonContains";
        List<Object> args = Lists.newArrayList("li");
        chain.handler(demoMethod(), spec, methodName, args);
        assertThat(spec.getFilterSql()).isEqualTo(" json_contains(`name`, ?)");
        assertThat(spec.getFilterParams()).containsExactly("\"li\"");
    }

    @Test
    public void jsonContains$Handler() {
        DefaultHandlerChain chain = new DefaultHandlerChain();
        chain.addHandlers(HandlerContext.SIMPLE_HANDLER);
        Spec spec = new Spec();
        String methodName = "NameJsonContains$";
        List<Object> args = Lists.newArrayList(0,"$[0]");
        chain.handler(demoMethod(), spec, methodName, args);
        assertThat(spec.getFilterSql()).isEqualTo(" json_contains(`name`, ?, ?)");
        assertThat(spec.getFilterParams()).containsExactly("0","$[0]");
    }
}
