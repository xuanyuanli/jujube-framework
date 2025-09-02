package cn.xuanyuanli.core.util.beancopy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import cn.xuanyuanli.core.util.Beans;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JavassistBeanCopyFactory 工厂类测试")
class JavassistBeanCopyFactoryTest {

    @Nested
    @DisplayName("方法体生成测试")
    class MethodBodyGenerationTests {

        @Test
        @DisplayName("createBeanCopierBase_应该生成正确的方法体_当使用基本类型转换时")
        void createBeanCopierBase_shouldGenerateCorrectMethodBody_whenUsingBasicTypeConversion() {
            // Arrange
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

            // Act
            String methodBody = new JavassistBeanCopierFactory().getCopyBeanMethodBody(sourceClass, targetClass, items, true);
            
            // Assert
            assertThat(methodBody).isEqualTo("""
                    {
                    cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean source = (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean) $1;
                    cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean target = $3 ? new cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean() : (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean) $2;
                    target.setName((java.lang.String)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.getName(),java.lang.String.class));
                    target.setAge((java.lang.Integer)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.getAge(),java.lang.Integer.class));
                    return target;
                    }
                    """);
        }

        @Test
        @DisplayName("createBeanCopierSameType_应该生成正确的方法体_当源为Map类型时")
        void createBeanCopierSameType_shouldGenerateCorrectMethodBody_whenSourceIsMapType() {
            // Arrange
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

            // Act
            String methodBody = new JavassistBeanCopierFactory().getCopyBeanMethodBody(sourceClass, targetClass, items, true);
            
            // Assert
            assertThat(methodBody).isEqualTo("""
                    {
                    java.util.Map source = (java.util.Map) $1;
                    cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean target = $3 ? new cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean() : (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBean) $2;
                    target.setName((java.lang.String)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.get("name"),java.lang.String.class));
                    target.setAge((java.lang.Integer)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.get("age"),java.lang.Integer.class));
                    return target;
                    }
                    """);
        }

        @Test
        @DisplayName("createBeanCopierPrimitive_应该生成正确的方法体_当目标为原始类型时")
        void createBeanCopierPrimitive_shouldGenerateCorrectMethodBody_whenTargetHasPrimitiveType() {
            // Arrange
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

            // Act
            String methodBody = new JavassistBeanCopierFactory().getCopyBeanMethodBody(sourceClass, targetClass, items, true);
            
            // Assert
            assertThat(methodBody).isEqualTo("""
                    {
                    cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean source = (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$SourceBean) $1;
                    cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBeanPrimitive target = $3 ? new cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBeanPrimitive() : (cn.xuanyuanli.core.util.beancopy.JavassistBeanCopyFactoryTest$TargetBeanPrimitive) $2;
                    target.setName((java.lang.String)cn.xuanyuanli.core.util.Beans.getExpectTypeValue(source.getName(),java.lang.String.class));
                    target.setAge(cn.xuanyuanli.core.util.beancopy.BeanCopier.getInt(source.getAge()));
                    return target;
                    }
                    """);
        }
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
