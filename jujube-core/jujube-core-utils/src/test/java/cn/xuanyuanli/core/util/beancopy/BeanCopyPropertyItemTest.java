package cn.xuanyuanli.core.util.beancopy;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BeanCopyPropertyItem 属性项测试")
class BeanCopyPropertyItemTest {

    @Setter
    @Getter
    static class TestBean {
        private String property;
    }

    @Nested
    @DisplayName("属性设置和获取测试")
    class PropertySetAndGetTests {

        @Test
        @DisplayName("testBeanCopyPropertyItem_应该正确设置和获取所有属性_当创建新实例时")
        void testBeanCopyPropertyItem_shouldCorrectlySetAndGetAllProperties_whenCreatingNewInstance() throws IntrospectionException {
            // Arrange
            BeanCopyPropertyItem item = new BeanCopyPropertyItem();
            String sourcePropertyName = "property";
            PropertyDescriptor sourceProperty = new PropertyDescriptor("property", TestBean.class, "getProperty", "setProperty");
            PropertyDescriptor targetProperty = new PropertyDescriptor("property", TestBean.class, "getProperty", "setProperty");

            // Act
            item.setSourceIsMap(true);
            item.setSourcePropertyName(sourcePropertyName);
            item.setSourceProperty(sourceProperty);
            item.setTargetProperty(targetProperty);

            // Assert
            assertThat(item.isSourceIsMap()).isTrue();
            assertThat(item.getSourcePropertyName()).isEqualTo(sourcePropertyName);
            assertThat(item.getSourceProperty()).isEqualTo(sourceProperty);
            assertThat(item.getTargetProperty()).isEqualTo(targetProperty);
        }
    }
}
