package cn.xuanyuanli.jdbc.pagination;

import cn.xuanyuanli.core.util.Pojos;
import cn.xuanyuanli.core.lang.BaseEntity;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PageableTest {

    @Test
    void Pageable3(){
        Pageable<?> pageable = new Pageable<>(1, 10,5);
        assertThat(pageable.getIndex()).isEqualTo(1);
        assertThat(pageable.getSize()).isEqualTo(10);
        assertThat(pageable.getStart()).isEqualTo(5);
    }

    @Test
    void Pageable2(){
        Pageable<?> pageable = new Pageable<>(1, 10);
        assertThat(pageable.getIndex()).isEqualTo(1);
        assertThat(pageable.getSize()).isEqualTo(10);
    }

    @Test
    void Pageable1(){
        Pageable<?> pageable = new Pageable<>(1);
        assertThat(pageable.getIndex()).isEqualTo(1);
        assertThat(pageable.getSize()).isEqualTo(10);
    }

    @Test
    void Pageable(){
        Pageable<?> pageable = new Pageable<>();
        assertThat(pageable.getIndex()).isEqualTo(1);
        assertThat(pageable.getSize()).isEqualTo(10);
    }

    @Test
    void getData(){
        Pageable<String> pageable = new Pageable<>();
        pageable.setData(Lists.newArrayList("1"));
        assertThat(pageable.getData()).containsOnly("1");
    }

    @Test
    void getTotalElements(){
        Pageable<?> pageable = new Pageable<>();
        pageable.setTotalElements(20);
        assertThat(pageable.getTotalElements()).isEqualTo(20);
    }

    @Test
    void getSize(){
        Pageable<?> pageable = new Pageable<>();
        pageable.setSize(20);
        assertThat(pageable.getSize()).isEqualTo(20);
    }

    @Test
    void getIndex(){
        Pageable<?> pageable = new Pageable<>();
        pageable.setIndex(20);
        assertThat(pageable.getIndex()).isEqualTo(20);
    }

    @Test
    void toGenericType(){
        Pageable<FromBO> pageable = new Pageable<>();
        pageable.setTotalElements(1);
        pageable.setData(Lists.newArrayList(new FromBO().setAge(1).setName("a").setList(Lists.newArrayList("0"))));
        Pageable<DestBO> boPageable = pageable.toGenericType(DestBO.class);
        assertThat(boPageable.getIndex()).isEqualTo(1);
        assertThat(boPageable.getSize()).isEqualTo(10);
        assertThat(boPageable.getTotalElements()).isEqualTo(1);
        assertThat(boPageable.getStart()).isEqualTo(0);
        assertThat(boPageable.getData()).containsOnly(new DestBO().setAge(1).setName("a").setList(Lists.newArrayList("0")));
    }

    @Test
    void toGenericTypeDataIsNull(){
        Pageable<FromBO> pageable = new Pageable<>();
        pageable.setTotalElements(1);
        pageable.setData(null);
        Pageable<DestBO> boPageable = pageable.toGenericType(DestBO.class);
        assertThat(boPageable.getIndex()).isEqualTo(1);
        assertThat(boPageable.getSize()).isEqualTo(10);
        assertThat(boPageable.getTotalElements()).isEqualTo(1);
        assertThat(boPageable.getStart()).isEqualTo(0);
        assertThat(boPageable.getData()).isNull();
    }

    @Test
    void toGenericFunc(){
        Pageable<FromBO> pageable = new Pageable<>();
        pageable.setTotalElements(1);
        pageable.setData(Lists.newArrayList(new FromBO().setAge(1).setName("a").setList(Lists.newArrayList("0"))));
        Pageable<DestBO> boPageable = pageable.toGenericType(f-> Pojos.mapping(f,DestBO.class));
        assertThat(boPageable.getIndex()).isEqualTo(1);
        assertThat(boPageable.getSize()).isEqualTo(10);
        assertThat(boPageable.getTotalElements()).isEqualTo(1);
        assertThat(boPageable.getStart()).isEqualTo(0);
        assertThat(boPageable.getData()).containsOnly(new DestBO().setAge(1).setName("a").setList(Lists.newArrayList("0")));
    }

    @Test
    void iterator(){
        Pageable<FromBO> pageable = new Pageable<>();
        ArrayList<FromBO> data = Lists.newArrayList(new FromBO().setAge(1).setName("a").setList(Lists.newArrayList("0")));
        pageable.setData(data);
        assertThat(pageable.iterator().next()).isEqualTo(data.iterator().next());
    }

    @Test
    public void hasPreviousPage() {
        Pageable<?> pageable = new Pageable<>(1, 10);
        pageable.setTotalElements(100);
        assertThat(pageable.hasPreviousPage()).isFalse();

        pageable.setIndex(2);
        assertThat(pageable.hasPreviousPage()).isTrue();
    }

    @Test
    public void isFirstPage() {
        Pageable<?> pageable = new Pageable<>(1, 10);
        assertThat(pageable.isFirstPage()).isTrue();

        pageable.setIndex(2);
        assertThat(pageable.isFirstPage()).isFalse();
    }

    @Test
    public void hasNextPage() {
        Pageable<?> pageable = new Pageable<>(1, 10);
        pageable.setTotalElements(100);
        assertThat(pageable.hasNextPage()).isTrue();

        pageable.setIndex(10);
        assertThat(pageable.hasNextPage()).isFalse();
    }

    @Test
    public void isLastPage() {
        Pageable<?> pageable = new Pageable<>(1, 10);
        pageable.setTotalElements(100);
        assertThat(pageable.isLastPage()).isFalse();

        pageable.setIndex(10);
        assertThat(pageable.isLastPage()).isTrue();
    }

    @Test
    public void getStart(){
        Pageable<?> pageable = new Pageable<>(1, 10);
        assertThat(pageable.getStart()).isEqualTo(0);

        pageable.setIndex(2);
        assertThat(pageable.getStart()).isEqualTo(10);

        pageable.setIndex(20);
        assertThat(pageable.getStart()).isEqualTo(190);
    }

    @Test
    public void getTotalPages(){
        Pageable<?> pageable = new Pageable<>(1, 10);
        pageable.setTotalElements(20);
        assertThat(pageable.getTotalPages()).isEqualTo(2);

        pageable.setTotalElements(201);
        assertThat(pageable.getTotalPages()).isEqualTo(21);
    }

    @Data
    @Accessors(chain = true)
    public static class FromBO implements BaseEntity {

        private String name;
        private Integer age;
        private List<String> list;
    }

    @Data
    @Accessors(chain = true)
    public static class DestBO implements BaseEntity {

        private String name;
        private Integer age;
        private List<String> list;
    }
}
