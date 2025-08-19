package cn.xuanyuanli.core.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

class ApplicationContextHolderTest {

    private ApplicationContext applicationContext;
    private Environment environment;

    @BeforeEach
    void setUp() {
        applicationContext = Mockito.mock(ApplicationContext.class);
        environment = Mockito.mock(Environment.class);
        when(applicationContext.getEnvironment()).thenReturn(environment);
    }

    @Test
    void setApplicationContext() {
        ApplicationContextHolder holder = new ApplicationContextHolder();
        holder.setApplicationContext(applicationContext);
        assertEquals(applicationContext, ApplicationContextHolder.getApplicationContext());
    }

    @Test
    void testManualSetApplicationContext() {
        ApplicationContextHolder.manualSetApplicationContext(applicationContext);
        assertEquals(applicationContext, ApplicationContextHolder.getApplicationContext());
    }

    @Test
    void testGetEnvironment() {
        ApplicationContextHolder.manualSetApplicationContext(applicationContext);
        assertEquals(environment, ApplicationContextHolder.getEnvironment());
    }

    @Test
    void testGetProperty() {
        ApplicationContextHolder.manualSetApplicationContext(applicationContext);
        when(environment.getProperty("test.property2")).thenReturn("testValue");
        assertEquals("testValue", ApplicationContextHolder.getProperty("test.property2"));
    }

    @Test
    void testGetPropertyWithDefaultValue() {
        ApplicationContextHolder.manualSetApplicationContext(applicationContext);
        when(environment.getProperty("test.property")).thenReturn(null);
        assertEquals("defaultValue", ApplicationContextHolder.getProperty("test.property", "defaultValue"));
    }

    @Test
    void testGetPropertyWhenEnvironmentIsNull() {
        ApplicationContextHolder.manualSetApplicationContext(null);
        assertNull(ApplicationContextHolder.getProperty("test.property"));
    }

    @Test
    void testGetPropertyWithDefaultValueWhenEnvironmentIsNull() {
        ApplicationContextHolder.manualSetApplicationContext(null);
        assertEquals("defaultValue", ApplicationContextHolder.getProperty("test.property", "defaultValue"));
    }
}
