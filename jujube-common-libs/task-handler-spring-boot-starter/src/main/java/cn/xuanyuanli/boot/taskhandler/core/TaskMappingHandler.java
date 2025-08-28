package cn.xuanyuanli.boot.taskhandler.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author John Li
 */
@Slf4j
@Data
public class TaskMappingHandler {
    private Method handlerMethod;
    private Object handlerInstance;

    public void execute(String[] taskParam) {
        try {
            if (handlerMethod.getParameterCount() == 0) {
                handlerMethod.invoke(handlerInstance);
            } else if (handlerMethod.getParameterCount() == 1) {
                handlerMethod.invoke(handlerInstance, (Object) taskParam);
            } else {
                log.warn("不支持的参数个数: method={}, paramCount={}", handlerMethod, handlerMethod.getParameterCount());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("handler execute", e);
        }
    }
}
