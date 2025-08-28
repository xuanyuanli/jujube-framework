package cn.xuanyuanli.boot.taskhandler;

import cn.xuanyuanli.boot.taskhandler.core.TaskMappingPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author John Li
 */
@AutoConfiguration
@EnableConfigurationProperties(TaskHandlerProperties.class)
@Slf4j
public class TaskHandlerConfiguration {

    

    /**
     * 任务映射后置处理程序
     *
     * @return {@link TaskMappingPostProcessor}
     */
    @Bean
    public static TaskMappingPostProcessor taskMappingPostProcessor() {
        return new TaskMappingPostProcessor();
    }
}
