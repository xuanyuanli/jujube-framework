package cn.xuanyuanli.boot.taskhandler.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

/**
 * @author John Li
 */
@Slf4j
public class TaskMappingContext {
    static Map<String, TaskMappingHandler> HANDLERS = new ConcurrentHashMap<>(16);

    public static void run(TaskNameResolver nameResolver) {
        TaskMappingHandler taskMappingHandler = HANDLERS.get(nameResolver.getTaskName());
        if (taskMappingHandler != null) {
            taskMappingHandler.execute(nameResolver.getTaskParam());
        } else {
            log.warn("没有找到对应的task--taskname:{},taskParam:{}", nameResolver.getTaskName(), nameResolver.getTaskParam());
        }
    }
}
