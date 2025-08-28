package cn.xuanyuanli.boot.taskhandler.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Arrays;

/**
 * @author John Li
 */
@Slf4j
public class TaskMappingPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();
        if (AopUtils.isAopProxy(bean)) {
            aClass = AopUtils.getTargetClass(bean);
        }
        Arrays.stream(aClass.getMethods()).filter(m -> m.isAnnotationPresent(TaskMapping.class)).forEach(m -> {
            TaskMapping taskMapping = m.getAnnotation(TaskMapping.class);
            String taskName = taskMapping.value();
            if (taskName == null || taskName.trim().isEmpty()) {
                throw new RuntimeException("taskName不能为空，请检查方法：" + m);
            }
            if (TaskMappingContext.HANDLERS.containsKey(taskName)) {
                throw new RuntimeException("已存在相同的taskName：" + taskName);
            }
            TaskMappingHandler handler = new TaskMappingHandler();
            handler.setHandlerMethod(m);
            handler.setHandlerInstance(bean);
            TaskMappingContext.HANDLERS.put(taskName, handler);
        });
        return bean;
    }
}
