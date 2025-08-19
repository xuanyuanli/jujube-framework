package cn.xuanyuanli.core.lang;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

public class BaseEntityTest {

    @Test
    public void toRecordEmpty() {
        FromEntity entity = new FromEntity();
        Assertions.assertThat(entity.toRecord()).isEqualTo(new Record());
    }

    @Test
    public void toRecord() {
        FromEntity entity = new FromEntity();
        entity.setName("a");
        entity.setAge(10);
        Assertions.assertThat(entity.toRecord()).isEqualTo(new Record().set("name", "a").set("age", 10));
    }


    @Test
    public void toMapEmpty() {
        FromEntity entity = new FromEntity();
        HashMap<Object, Object> expected = new HashMap<>();
        expected.put("name", null);
        expected.put("age", null);
        expected.put("bo", null);
        expected.put("list", null);
        Assertions.assertThat(entity.toMap()).isEqualTo(expected);
    }

    @Test
    public void toMap() {
        FromEntity entity = new FromEntity();
        entity.setName("a");
        entity.setAge(10);
        HashMap<Object, Object> expected = new HashMap<>();
        expected.put("name", "a");
        expected.put("age", 10);
        expected.put("bo", null);
        expected.put("list", null);
        Assertions.assertThat(entity.toMap()).isEqualTo(expected);
    }

    @Test
    public void toMapFilterNull() {
        FromEntity entity = new FromEntity();
        entity.setName("a");
        entity.setAge(10);
        HashMap<Object, Object> expected = new HashMap<>();
        expected.put("name", "a");
        expected.put("age", 10);
        Assertions.assertThat(entity.toMapFilterNull()).isEqualTo(expected);
    }

    @Test
    public void toBO() {
        DestEntity destEntity = new DestEntity();
        destEntity.setAge(12);
        destEntity.setName("abc");
        InnerBO bo = new InnerBO();
        bo.setName("efg");
        destEntity.setBo(bo);
        destEntity.setList(Lists.newArrayList("7"));
        FromBO fromBO = destEntity.toBO(FromBO.class);
        Assertions.assertThat(fromBO.getAge()).isEqualTo(destEntity.getAge());
        Assertions.assertThat(fromBO.getName()).isEqualTo(destEntity.getName());
        Assertions.assertThat(fromBO.getBo()).isEqualTo(destEntity.getBo());
        Assertions.assertThat(fromBO.getList()).isEqualTo(destEntity.getList());
    }

    @Test
    public void toJson() {
        DestEntity destEntity = new DestEntity();
        destEntity.setAge(12);
        destEntity.setName("abc");
        Assertions.assertThat(destEntity.toJson()).isEqualTo("{\"name\":\"abc\",\"age\":12,\"list\":null,\"bo\":null,\"type\":null,\"ext\":false}");
    }

    @Test
    public void cloneSelf() {
        DestEntity destEntity = new DestEntity();
        destEntity.setAge(12);
        destEntity.setName("abc");
        InnerBO bo = new InnerBO();
        bo.setName("efg");
        destEntity.setBo(bo);
        destEntity.setExt(true);
        DestEntity target = destEntity.cloneSelf();
        Assertions.assertThat(target.getAge()).isEqualTo(destEntity.getAge());
        Assertions.assertThat(target.getName()).isEqualTo(destEntity.getName());
        Assertions.assertThat(target.getBo()).isEqualTo(destEntity.getBo());
        Assertions.assertThat(target.isExt()).isTrue();
    }

    @Test
    public void cloneSelfArr() {
        DestEntity destEntity = new DestEntity();
        destEntity.setList(Lists.newArrayList("7", "8"));
        DestEntity target = destEntity.cloneSelf();
        target.getList().remove("8");
        Assertions.assertThat(target.getList()).containsExactly("7");
        Assertions.assertThat(destEntity.getList()).containsExactly("7", "8");
    }

    @Data
    public static class InnerBO implements BaseEntity {

        private String name;
    }

    @Data
    public static class FromEntity implements BaseEntity {

        private String name;
        private Integer age;
        private List<String> list;
        private InnerBO bo;
    }

    @Data
    public static class FromBO implements BaseEntity {

        private String name;
        private Integer age;
        private List<String> list;
        private InnerBO bo;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @ToString(callSuper = true)
    public static class DestEntity extends FromEntity {

        private Integer type;
        private boolean ext;
    }
}
