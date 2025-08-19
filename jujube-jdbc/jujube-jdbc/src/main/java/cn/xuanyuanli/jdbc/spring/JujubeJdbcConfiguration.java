package cn.xuanyuanli.jdbc.spring;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import cn.xuanyuanli.jdbc.binding.DaoSqlRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

/**
 * JujubeJdbc配置类，也是一个Bean注册器后置处理器。会把{@link #basePackage}下所有的Dao注册到Spring容器中，并建立Dao与Sql文件间的对应信息
 *
 * @author John Li
 */
@Setter
public class JujubeJdbcConfiguration implements BeanDefinitionRegistryPostProcessor {

    static final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
            new BasicThreadFactory.Builder().namingPattern("DaoSqlRegistry-scheduled-pool-%d").build());

    /**
     * Dao接口所在的package
     */
    private String basePackage;


    /**
     * 是否自动刷新Dao Sql的缓存
     */
    private boolean autoRefreshSql;

    /** 刷新周期，单位为秒。默认值：5 */
    private Integer refreshSqlPeriod;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        // 注册dao与dao sql的对应信息
        DaoSqlRegistry.setBasePackage(basePackage);
        DaoSqlRegistry.init();
        if (autoRefreshSql) {
            executorService.scheduleAtFixedRate(DaoSqlRegistry::init, getRefreshSqlPeriod(), getRefreshSqlPeriod(), TimeUnit.SECONDS);
        }

        // 代理BaseDao的所有子接口
        ClassPathDaoScanner scanner = new ClassPathDaoScanner(registry);
        scanner.registerFilters();
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));

        BeanDefinitionBuilder holderBuilder = BeanDefinitionBuilder.genericBeanDefinition(SpringContextHolder.class);
        registry.registerBeanDefinition("springContextHolder", holderBuilder.getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    public Integer getRefreshSqlPeriod() {
        return (refreshSqlPeriod == null || refreshSqlPeriod <= 0) ? 5 : refreshSqlPeriod;
    }
}
