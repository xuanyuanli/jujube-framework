package cn.xuanyuanli.jdbc.base.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.assertj.core.api.Assertions;
import cn.xuanyuanli.jdbc.base.annotation.Column;
import cn.xuanyuanli.jdbc.base.annotation.VisualColumn;
import cn.xuanyuanli.jdbc.base.jpa.entity.RecordEntity;
import cn.xuanyuanli.jdbc.base.jpa.strategy.query.impl.JavaEntityClass;
import cn.xuanyuanli.jdbc.base.util.JdbcPojos.FieldColumn;
import cn.xuanyuanli.core.lang.BaseEntity;
import org.junit.jupiter.api.Test;

class JdbcPojosTest {

    @Data
    @Accessors(chain = true)
    public static class JdbcPojosTestUser implements BaseEntity {

        private Long id;
        private String name;
        @Column("t_title")
        private String title;
        @VisualColumn
        private String vtitle;
    }

    @Test
    void mapping() {
        RecordEntity record = new RecordEntity();
        record.set("id", 10L).set("name", "li").set("t_title", "art");
        JdbcPojosTestUser user = JdbcPojos.mapping(record, JdbcPojosTestUser.class);
        Assertions.assertThat(user.getId()).isEqualTo(10L);
        Assertions.assertThat(user.getName()).isEqualTo("li");
        Assertions.assertThat(user.getTitle()).isEqualTo("art");
    }

    @Test
    void mappingArray() {
        RecordEntity record = new RecordEntity();
        record.set("id", 10L).set("name", "li").set("t_title", "art");
        List<JdbcPojosTestUser> users = JdbcPojos.mappingArray(new ArrayList<>(List.of(record)), JdbcPojosTestUser.class);
        Assertions.assertThat(users).hasSize(1);
        JdbcPojosTestUser user = users.get(0);
        Assertions.assertThat(user.getId()).isEqualTo(10L);
        Assertions.assertThat(user.getName()).isEqualTo("li");
        Assertions.assertThat(user.getTitle()).isEqualTo("art");
    }

    @Test
    void getFieldColumns() {
        List<FieldColumn> fieldColumns = JdbcPojos.getFieldColumns(new JavaEntityClass(JdbcPojosTestUser.class));
        Assertions.assertThat(fieldColumns.get(0).getVisual()).isFalse();
        Assertions.assertThat(fieldColumns.get(1).getVisual()).isFalse();
        Assertions.assertThat(fieldColumns.get(2).getVisual()).isFalse();
        Assertions.assertThat(fieldColumns.get(3).getVisual()).isTrue();
    }
}
