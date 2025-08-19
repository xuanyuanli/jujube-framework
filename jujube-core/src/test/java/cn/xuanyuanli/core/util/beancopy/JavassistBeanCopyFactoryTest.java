package cn.xuanyuanli.core.util.beancopy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import cn.xuanyuanli.core.util.Beans;
import org.junit.jupiter.api.Test;

class JavassistBeanCopyFactoryTest {

    @Test
    void createBeanCopierBase() {
        Class<?> sourceClass = SourceBean.class;
        Class<?> targetClass = TargetBean.class;
        List<BeanCopyPropertyItem> items = new ArrayList<>();
        BeanCopyPropertyItem item1 = new BeanCopyPropertyItem();
        item1.setSourceProperty(Beans.getPropertyDescriptor(sourceClass, "name"));
        item1.setTargetProperty(Beans.getPropertyDescriptor(targetClass, "name"));
        items.add(item1);
        BeanCopyPropertyItem item2 = new BeanCopyPropertyItem();
        item2.setSourceProperty(Beans.getPropertyDescriptor(sourceClass, "age"));
        item2.setTargetProperty(Beans.getPropertyDescriptor(targetClass, "age"));
        items.add(item2);

        String methodBody = new JavassistBeanCopierFactory().getCopyBeanMethodBody(sourceClass, targetClass, items, true);
        org.assertj.core.api.Assertions.assertThat(methodBody).isEqualTo("{\n"
                + "cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean source = (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean) $1;\n"
                + "cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean target = $3 ? new cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean() : (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean) $2;\n"
                + "target.setName((java.lang.String)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.getName(),java.lang.String.class));\n"
                + "target.setAge((java.lang.Integer)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.getAge(),java.lang.Integer.class));\n"
                + "return target;\n"
                + "}\n");
    }

    @Test
    void createBeanCopierSameType() {
        Class<?> sourceClass = Map.class;
        Class<?> targetClass = TargetBean.class;
        List<BeanCopyPropertyItem> items = new ArrayList<>();
        BeanCopyPropertyItem item1 = new BeanCopyPropertyItem();
        item1.setSourceIsMap(true);
        item1.setSourcePropertyName("name");
        item1.setTargetProperty(Beans.getPropertyDescriptor(targetClass, "name"));
        items.add(item1);
        BeanCopyPropertyItem item2 = new BeanCopyPropertyItem();
        item2.setSourceIsMap(true);
        item2.setSourcePropertyName("age");
        item2.setTargetProperty(Beans.getPropertyDescriptor(targetClass, "age"));
        items.add(item2);

        String methodBody = new JavassistBeanCopierFactory().getCopyBeanMethodBody(sourceClass, targetClass, items, true);
        org.assertj.core.api.Assertions.assertThat(methodBody).isEqualTo("{\n"
                + "java.util.Map source = (java.util.Map) $1;\n"
                + "cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean target = $3 ? new cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean() : (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean) $2;\n"
                + "target.setName((java.lang.String)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.get(\"name\"),java.lang.String.class));\n"
                + "target.setAge((java.lang.Integer)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.get(\"age\"),java.lang.Integer.class));\n"
                + "return target;\n"
                + "}\n");
    }

    @Test
    void createBeanCopierPrimitive() {
        Class<?> sourceClass = SourceBean.class;
        Class<?> targetClass = TargetBeanPrimitive.class;
        List<BeanCopyPropertyItem> items = new ArrayList<>();
        BeanCopyPropertyItem item1 = new BeanCopyPropertyItem();
        item1.setSourceProperty(Beans.getPropertyDescriptor(sourceClass, "name"));
        item1.setTargetProperty(Beans.getPropertyDescriptor(targetClass, "name"));
        items.add(item1);
        BeanCopyPropertyItem item2 = new BeanCopyPropertyItem();
        item2.setSourceProperty(Beans.getPropertyDescriptor(sourceClass, "age"));
        item2.setTargetProperty(Beans.getPropertyDescriptor(targetClass, "age"));
        items.add(item2);

        String methodBody = new JavassistBeanCopierFactory().getCopyBeanMethodBody(sourceClass, targetClass, items, true);
        org.assertj.core.api.Assertions.assertThat(methodBody).isEqualTo("{\n"
                + "cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean source = (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean) $1;\n"
                + "cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBeanPrimitive target = $3 ? new cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBeanPrimitive() : (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBeanPrimitive) $2;\n"
                + "target.setName((java.lang.String)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.getName(),java.lang.String.class));\n"
                + "target.setAge(cn.xuanyuanli.core.util.beancopy.BeanCopier.getInt(source.getAge()));\n"
                + "return target;\n"
                + "}\n");
    }

    @Data
    static class SourceBean {

        private String name;
        private String age;
    }

    @Data
    static class TargetBean {

        private String name;
        private Integer age;
    }

    @Data
    static class TargetBeanPrimitive {

        private String name;
        private int age;
    }
}
