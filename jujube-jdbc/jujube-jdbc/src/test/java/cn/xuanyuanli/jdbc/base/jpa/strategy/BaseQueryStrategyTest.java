package cn.xuanyuanli.jdbc.base.jpa.strategy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.jdbc.base.annotation.SelectField;
import cn.xuanyuanli.jdbc.base.jpa.handler.HandlerTest;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.DbAndEntityFiled;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaDaoMethod;
import cn.xuanyuanli.core.util.Beans;
import org.junit.jupiter.api.Test;

class BaseQueryStrategyTest {

    @Test
    void getDbColumnNames() {
        List<DbAndEntityFiled> fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "RecommendStatusAndId");
        Assertions.assertThat(fieldList.stream().map(DbAndEntityFiled::getDbField).collect(Collectors.toList())).hasSize(2)
                .containsExactly("recommend_status", "id");

        fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "Title");
        Assertions.assertThat(fieldList.stream().map(DbAndEntityFiled::getDbField).collect(Collectors.toList())).hasSize(1).containsExactly("title");

        fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "AndroidVersion");
        Assertions.assertThat(fieldList.stream().map(DbAndEntityFiled::getDbField).collect(Collectors.toList())).hasSize(1).containsExactly("android_version");

        fieldList = BaseQueryStrategy.getDbColumnNames(HandlerTest.demoMethod(), "AndroidVersionAndAliasName");
        Assertions.assertThat(fieldList.stream().map(DbAndEntityFiled::getDbField).collect(Collectors.toList())).hasSize(2)
                .containsExactly("android_version", "mname");
    }

    @SuppressWarnings("unused")
    @SelectField({"id", "name", "id userId", "name userName", "(select d.`name` from `department` d where ${department_id}=d.id) departName"})
    void findByDepartId() {
    }

    @Test
    void getSelectFieldsByMethod() {
        Method method = Beans.getDeclaredMethod(BaseQueryStrategyTest.class, "findByDepartId");
        List<String> list = BaseQueryStrategy.getSelectFieldsByMethod(new JavaDaoMethod(method), "user");
        Assertions.assertThat(list).containsSequence("id",
                "name",
                "id userId",
                "name userName",
                "(select d.`name` from `department` d where `user`.`department_id`=d.id) departName");
    }
}
