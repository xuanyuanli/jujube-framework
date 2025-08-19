package cn.xuanyuanli.core.util.beancopy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

class BeanCopyPropertyItemTest {

    // Internal class to simulate a bean with getter and setter methods
    @Setter
    @Getter
    static class TestBean {

        private String property;

    }

    @Test
    void testBeanCopyPropertyItem() throws IntrospectionException {
        BeanCopyPropertyItem item = new BeanCopyPropertyItem();

        // Test sourceIsMap
        item.setSourceIsMap(true);
        assertTrue(item.isSourceIsMap());

        // Test sourcePropertyName
        String sourcePropertyName = "property";
        item.setSourcePropertyName(sourcePropertyName);
        assertEquals(sourcePropertyName, item.getSourcePropertyName());

        // Test sourceProperty
        PropertyDescriptor sourceProperty = new PropertyDescriptor("property", TestBean.class, "getProperty", "setProperty");
        item.setSourceProperty(sourceProperty);
        assertEquals(sourceProperty, item.getSourceProperty());

        // Test targetProperty
        PropertyDescriptor targetProperty = new PropertyDescriptor("property", TestBean.class, "getProperty", "setProperty");
        item.setTargetProperty(targetProperty);
        assertEquals(targetProperty, item.getTargetProperty());
    }
}
