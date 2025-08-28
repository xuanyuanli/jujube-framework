package cn.xuanyuanli.boot.taskhandler.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

class TaskMappingContextTest {

    @AfterEach
    void tearDown() throws Exception {
        Field f = TaskMappingContext.class.getDeclaredField("HANDLERS");
        f.setAccessible(true);
        ((java.util.Map<?, ?>) f.get(null)).clear();
    }

    static class BeanA {
        static String[] last;
        @TaskMapping("A")
        public void a(String[] x) { last = x; }
    }

    @Test
    void run_missingHandler_shouldNotThrow() {
        Assertions.assertThatCode(() -> TaskMappingContext.run(new TaskNameResolver("none") {
            @Override public String getTaskName() { return "none"; }
            @Override public String[] getTaskParam() { return new String[]{}; }
        })).doesNotThrowAnyException();
    }

    @Test
    void run_existingHandler_shouldExecute() {
        // 通过后置处理器注册
        new TaskMappingPostProcessor().postProcessAfterInitialization(new BeanA(), "beanA");
        TaskMappingContext.run(new TaskNameResolver("A#1$2") {
            @Override public String getTaskName() { return "A"; }
            @Override public String[] getTaskParam() { return new String[]{"1","2"}; }
        });
        Assertions.assertThat(BeanA.last).containsExactly("1","2");
    }
}

