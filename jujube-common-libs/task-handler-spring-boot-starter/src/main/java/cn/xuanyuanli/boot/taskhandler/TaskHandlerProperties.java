package cn.xuanyuanli.boot.taskhandler;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author John Li
 */
@ConfigurationProperties(prefix = "taskhandler")
@Data
public class TaskHandlerProperties {
    // 目前暂无可配置属性；预留扩展
}
