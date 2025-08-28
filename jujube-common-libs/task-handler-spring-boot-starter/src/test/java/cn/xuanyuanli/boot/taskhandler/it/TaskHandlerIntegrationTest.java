package cn.xuanyuanli.boot.taskhandler.it;

import cn.xuanyuanli.boot.taskhandler.core.SplitTaskNameResolver;
import cn.xuanyuanli.boot.taskhandler.core.TaskMapping;
import cn.xuanyuanli.boot.taskhandler.core.TaskMappingContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@SpringBootTest(classes = TaskHandlerIntegrationTest.ItTestApplication.class)
class TaskHandlerIntegrationTest {

    @SpringBootApplication
    static class ItTestApplication { }

    @Component
    static class ItTasks {
        static String[] last;

        @TaskMapping("itEcho")
        public void echo(String[] args) { last = args; }

        @TaskMapping("noArg")
        public void noArg() { last = new String[]{}; }
    }

    @AfterEach
    void tearDown() throws Exception {
        // 清理全局注册，避免影响其他测试
        Field f = TaskMappingContext.class.getDeclaredField("HANDLERS");
        f.setAccessible(true);
        ((java.util.Map<?, ?>) f.get(null)).clear();
    }

    @Test
    void autoConfigured_postProcessor_shouldRegisterHandlers_and_Dispatch() {
        // 命中已注册的处理器
        TaskMappingContext.run(new SplitTaskNameResolver("itEcho#x$y", "#", "$"));
        Assertions.assertThat(ItTasks.last).containsExactly("x", "y");

        // 无参方法
        TaskMappingContext.run(new SplitTaskNameResolver("noArg", "#", "$"));
        Assertions.assertThat(ItTasks.last).isEmpty();
    }

    @Test
    void missing_handler_should_not_throw() {
        Assertions.assertThatCode(() ->
            TaskMappingContext.run(new SplitTaskNameResolver("missingTask", "#", "$"))
        ).doesNotThrowAnyException();
    }
}

