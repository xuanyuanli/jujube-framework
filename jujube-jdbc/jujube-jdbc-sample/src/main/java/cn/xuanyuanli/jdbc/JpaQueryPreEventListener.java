package cn.xuanyuanli.jdbc;

import cn.xuanyuanli.jdbc.base.jpa.event.JpaQueryPreEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author xuanyuanli
 */
@Component
public class JpaQueryPreEventListener implements ApplicationListener<JpaQueryPreEvent> {
    @Override
    public void onApplicationEvent(JpaQueryPreEvent event) {
        JpaQueryPreEvent.JpaQueryPreEventSource source = (JpaQueryPreEvent.JpaQueryPreEventSource) event.getSource();
        System.out.println(source.getMethod().getDeclaringClass().getName() + "#" + source.getMethod().getName());
    }
}
