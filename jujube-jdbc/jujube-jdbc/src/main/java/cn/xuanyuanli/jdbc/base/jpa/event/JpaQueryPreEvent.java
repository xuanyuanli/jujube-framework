package cn.xuanyuanli.jdbc.base.jpa.event;

import java.lang.reflect.Method;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

/**
 * jpa 查询前事件
 *
 * @author xuanyuanli
 */
public class JpaQueryPreEvent extends ApplicationEvent {

    /**
     * jpa查询前事件
     *
     * @param method 方法
     * @param args   参数
     */
    public JpaQueryPreEvent(Method method, Object[] args) {
        super(new JpaQueryPreEventSource(method, args));
    }

    @Data
    @AllArgsConstructor
    public static class JpaQueryPreEventSource {

        private Method method;
        private Object[] args;
    }
}
