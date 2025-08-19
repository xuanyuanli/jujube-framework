package cn.xuanyuanli.jdbc.boot;

import lombok.extern.slf4j.Slf4j;
import cn.xuanyuanli.jdbc.spring.JujubeJdbcConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * 这里不能用@EnableConfigurationProperties的方式来获得属性，因为JujubeJdbcConfiguration本身是一个BeanDefinitionRegistryPostProcessor。<br>
 * 而@EnableConfigurationProperties的逻辑是：把Properties类动态注入到容器，然后用BindBeanPostProcessor来绑定属性。这里有一个矛盾就是JujubeJdbcConfiguration会先执行，所以获得的Properties类中的属性为空，因为他们还没有被绑定
 *
 * @author John Li
 */
@AutoConfiguration
@Slf4j
public class JujubeJdbcAutoConfiguration {

    /**
     * 创建JujubeJdbcConfiguration
     *
     * @param environment 环境
     * @return {@link JujubeJdbcConfiguration}
     */
    @Bean
    @ConditionalOnMissingBean
    public JujubeJdbcConfiguration jujubeJdbcFactoryBean(Environment environment) {
        Binder binder = Binder.get(environment);
        JujubeJdbcProperties foo = binder.bind("jujube.jdbc", Bindable.of(JujubeJdbcProperties.class)).get();

        JujubeJdbcConfiguration jujubeJdbcFactoryBean = new JujubeJdbcConfiguration();
        jujubeJdbcFactoryBean.setBasePackage(foo.getBasePackage());
        jujubeJdbcFactoryBean.setAutoRefreshSql(foo.isAutoRefreshSql());
        jujubeJdbcFactoryBean.setRefreshSqlPeriod(foo.getRefreshSqlPeriod());
        return jujubeJdbcFactoryBean;
    }


}
