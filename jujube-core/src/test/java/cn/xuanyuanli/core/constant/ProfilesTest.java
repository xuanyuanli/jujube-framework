package cn.xuanyuanli.core.constant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Assertions;
import cn.xuanyuanli.core.spring.ApplicationContextHolder;
import cn.xuanyuanli.core.util.Envs;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

public class ProfilesTest {

    @Test
    public void should_production_when_system_given_production() {
        Profiles.setSpringProfileToSystemProperty(Profiles.PRODUCTION);
        Assertions.assertThat(Profiles.getSpringProfileFromSystemProperty()).isEqualTo(Profiles.PRODUCTION);
    }

    @Test
    public void testGetSpringProfileFromSystemProperty() {
        // Mock Environment
        Environment environment = Mockito.mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        try (MockedStatic<ApplicationContextHolder> mockedContextHolder = Mockito.mockStatic(ApplicationContextHolder.class)) {
            // Mock ApplicationContextHolder
            mockedContextHolder.when(ApplicationContextHolder::getEnvironment).thenReturn(environment);

            String result = Profiles.getSpringProfileFromSystemProperty();

            assertEquals("test", result);
        }
    }

    @Test
    public void testBlankProfile() {
        try (MockedStatic<Envs> mockedEnvs = Mockito.mockStatic(Envs.class)) {
            // Mock Envs
            mockedEnvs.when(() -> Envs.getEnv(Profiles.SPRING_PROFILES_ACTIVE)).thenReturn("");

            String result = Profiles.getSpringProfileFromSystemProperty();

            assertEquals("", result);
        }
    }


    @Test
    void should_test_is_true_when_system_given_null() {
        Profiles.setSpringProfileToSystemProperty("");
        Assertions.assertThat(Profiles.isTestProfile()).isTrue();
    }

    @Test
    void should_prod_is_false_when_system_given_null() {
        Profiles.setSpringProfileToSystemProperty("");
        Assertions.assertThat(Profiles.isProdProfile()).isFalse();
    }
}
