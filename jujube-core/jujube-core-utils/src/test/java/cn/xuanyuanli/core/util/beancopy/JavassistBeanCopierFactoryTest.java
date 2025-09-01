package cn.xuanyuanli.core.util.beancopy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.xuanyuanli.core.util.Beans;
import lombok.Data;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("JavassistBeanCopierFactory 测试")
public class JavassistBeanCopierFactoryTest {

    private final JavassistBeanCopierFactory factory = new JavassistBeanCopierFactory();

    @Nested
    @DisplayName("Bean拷贝器创建测试")
    class CreateBeanCopierTests {

        @Test
        @DisplayName("createBeanCopier_应该创建有效的Bean拷贝器_当提供有效参数时")
        void createBeanCopier_shouldCreateValidBeanCopier_whenValidParametersProvided() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);

            // Act
            BeanCopier copier = factory.createBeanCopier(SourceBean.class, TargetBean.class, items, true);

            // Assert
            assertThat(copier).isNotNull();
        }

        @Test
        @DisplayName("createBeanCopier_应该返回能执行拷贝的Bean拷贝器_当覆盖模式为true时")
        void createBeanCopier_shouldReturnFunctionalCopier_whenCoverModeIsTrue() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);
            BeanCopier copier = factory.createBeanCopier(SourceBean.class, TargetBean.class, items, true);
            SourceBean source = new SourceBean().setId(100L).setName("test").setAge(25);

            // Act
            TargetBean result = (TargetBean) copier.copyBean(source, TargetBean.class, true);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getName()).isEqualTo("test");
            assertThat(result.getAge()).isEqualTo(25);
        }

        @Test
        @DisplayName("createBeanCopier_应该返回能处理非覆盖模式的Bean拷贝器_当覆盖模式为false时")
        void createBeanCopier_shouldHandleNonCoverMode_whenCoverModeIsFalse() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);
            BeanCopier copier = factory.createBeanCopier(SourceBean.class, TargetBean.class, items, false);
            SourceBean source = new SourceBean().setId(100L).setName("test").setAge(25);
            TargetBean existing = new TargetBean().setId(200L).setName("existing");

            // Act
            TargetBean result = (TargetBean) copier.copyBean(source, existing, false);

            // Assert
            assertThat(result).isSameAs(existing);
            assertThat(result.getId()).isEqualTo(200L); // 保持原有值
            assertThat(result.getName()).isEqualTo("existing"); // 保持原有值
            assertThat(result.getAge()).isEqualTo(25); // 新值，因为原来为null
        }

        @Test
        @DisplayName("createBeanCopier_应该支持Map到Bean的拷贝_当源对象为Map时")
        void createBeanCopier_shouldSupportMapToBean_whenSourceIsMap() {
            // Arrange
            List<BeanCopyPropertyItem> items = createMapPropertyItems(TargetBean.class);
            BeanCopier copier = factory.createBeanCopier(Map.class, TargetBean.class, items, true);
            Map<String, Object> source = new HashMap<>();
            source.put("id", 100L);
            source.put("name", "mapTest");
            source.put("age", 30);

            // Act
            TargetBean result = (TargetBean) copier.copyBean(source, TargetBean.class, true);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getName()).isEqualTo("mapTest");
            assertThat(result.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("createBeanCopier_应该处理基本类型转换_当涉及基本类型时")
        void createBeanCopier_shouldHandlePrimitiveTypes_whenPrimitiveTypesInvolved() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, PrimitiveTargetBean.class);
            BeanCopier copier = factory.createBeanCopier(SourceBean.class, PrimitiveTargetBean.class, items, true);
            SourceBean source = new SourceBean().setId(100L).setName("primitiveTest").setAge(25);

            // Act
            PrimitiveTargetBean result = (PrimitiveTargetBean) copier.copyBean(source, PrimitiveTargetBean.class, true);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getName()).isEqualTo("primitiveTest");
            assertThat(result.getAge()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("方法体生成测试")
    class MethodBodyGenerationTests {

        @Test
        @DisplayName("getCopyBeanMethodBody_应该生成有效的方法体_当提供有效参数时")
        void getCopyBeanMethodBody_shouldGenerateValidMethodBody_whenValidParametersProvided() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);

            // Act
            String methodBody = factory.getCopyBeanMethodBody(SourceBean.class, TargetBean.class, items, true);

            // Assert
            assertThat(methodBody).isNotNull();
            assertThat(methodBody).contains("SourceBean source");
            assertThat(methodBody).contains("TargetBean target");
            assertThat(methodBody).contains("return target");
        }

        @Test
        @DisplayName("getCopyBeanMethodBody_应该包含覆盖逻辑_当覆盖模式为true时")
        void getCopyBeanMethodBody_shouldIncludeCoverLogic_whenCoverModeIsTrue() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);

            // Act
            String methodBody = factory.getCopyBeanMethodBody(SourceBean.class, TargetBean.class, items, true);

            // Assert
            assertThat(methodBody).isNotNull();
            assertThat(methodBody).doesNotContain("if(target.");
        }

        @Test
        @DisplayName("getCopyBeanMethodBody_应该包含null检查_当覆盖模式为false时")
        void getCopyBeanMethodBody_shouldIncludeNullCheck_whenCoverModeIsFalse() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);

            // Act
            String methodBody = factory.getCopyBeanMethodBody(SourceBean.class, TargetBean.class, items, false);

            // Assert
            assertThat(methodBody).isNotNull();
            assertThat(methodBody).contains("if(target.");
            assertThat(methodBody).contains("==null)");
        }

        @Test
        @DisplayName("getCopyBeanMethodBody_应该处理Map源对象_当源对象为Map时")
        void getCopyBeanMethodBody_shouldHandleMapSource_whenSourceIsMap() {
            // Arrange
            List<BeanCopyPropertyItem> items = createMapPropertyItems(TargetBean.class);

            // Act
            String methodBody = factory.getCopyBeanMethodBody(Map.class, TargetBean.class, items, true);

            // Assert
            assertThat(methodBody).isNotNull();
            assertThat(methodBody).contains("source.get(");
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("createBeanCopier_应该不抛出异常_当处理正常情况时")
        void createBeanCopier_shouldNotThrowException_whenHandlingNormalCases() {
            // Arrange
            List<BeanCopyPropertyItem> items = createPropertyItems(SourceBean.class, TargetBean.class);

            // Act & Assert
            assertThatNoException().isThrownBy(() -> 
                factory.createBeanCopier(SourceBean.class, TargetBean.class, items, true));
        }
    }

    // 辅助方法
    private List<BeanCopyPropertyItem> createPropertyItems(Class<?> sourceClass, Class<?> targetClass) {
        List<BeanCopyPropertyItem> items = new ArrayList<>();
        
        PropertyDescriptor idSource = Beans.getPropertyDescriptor(sourceClass, "id");
        PropertyDescriptor idTarget = Beans.getPropertyDescriptor(targetClass, "id");
        if (idSource != null && idTarget != null) {
            BeanCopyPropertyItem idItem = new BeanCopyPropertyItem();
            idItem.setSourceProperty(idSource);
            idItem.setTargetProperty(idTarget);
            items.add(idItem);
        }

        PropertyDescriptor nameSource = Beans.getPropertyDescriptor(sourceClass, "name");
        PropertyDescriptor nameTarget = Beans.getPropertyDescriptor(targetClass, "name");
        if (nameSource != null && nameTarget != null) {
            BeanCopyPropertyItem nameItem = new BeanCopyPropertyItem();
            nameItem.setSourceProperty(nameSource);
            nameItem.setTargetProperty(nameTarget);
            items.add(nameItem);
        }

        PropertyDescriptor ageSource = Beans.getPropertyDescriptor(sourceClass, "age");
        PropertyDescriptor ageTarget = Beans.getPropertyDescriptor(targetClass, "age");
        if (ageSource != null && ageTarget != null) {
            BeanCopyPropertyItem ageItem = new BeanCopyPropertyItem();
            ageItem.setSourceProperty(ageSource);
            ageItem.setTargetProperty(ageTarget);
            items.add(ageItem);
        }

        return items;
    }

    private List<BeanCopyPropertyItem> createMapPropertyItems(Class<?> targetClass) {
        List<BeanCopyPropertyItem> items = new ArrayList<>();

        PropertyDescriptor idTarget = Beans.getPropertyDescriptor(targetClass, "id");
        if (idTarget != null) {
            BeanCopyPropertyItem idItem = new BeanCopyPropertyItem();
            idItem.setSourceIsMap(true);
            idItem.setSourcePropertyName("id");
            idItem.setTargetProperty(idTarget);
            items.add(idItem);
        }

        PropertyDescriptor nameTarget = Beans.getPropertyDescriptor(targetClass, "name");
        if (nameTarget != null) {
            BeanCopyPropertyItem nameItem = new BeanCopyPropertyItem();
            nameItem.setSourceIsMap(true);
            nameItem.setSourcePropertyName("name");
            nameItem.setTargetProperty(nameTarget);
            items.add(nameItem);
        }

        PropertyDescriptor ageTarget = Beans.getPropertyDescriptor(targetClass, "age");
        if (ageTarget != null) {
            BeanCopyPropertyItem ageItem = new BeanCopyPropertyItem();
            ageItem.setSourceIsMap(true);
            ageItem.setSourcePropertyName("age");
            ageItem.setTargetProperty(ageTarget);
            items.add(ageItem);
        }

        return items;
    }

    // 测试数据类
    @Data
    @Accessors(chain = true)
    public static class SourceBean {
        private Long id;
        private String name;
        private Integer age;
    }

    @Data
    @Accessors(chain = true)
    public static class TargetBean {
        private Long id;
        private String name;
        private Integer age;
    }

    @Data
    @Accessors(chain = true)
    public static class PrimitiveTargetBean {
        private long id;
        private String name;
        private int age;
    }
}