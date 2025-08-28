package cn.xuanyuanli.boot.taskhandler.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

class TaskMappingPostProcessorTest {

    @AfterEach
    void tearDown() throws Exception {
        // 清理全局注册，避免测试之间相互影响
        Field f = TaskMappingContext.class.getDeclaredField("HANDLERS");
        f.setAccessible(true);
        ((java.util.Map<?, ?>) f.get(null)).clear();
    }

    @Component
    static class GoodBean {
        static String[] last;

        @TaskMapping("good")
        public void run(String[] args) { last = args; }

        @TaskMapping("goodNoArg")
        public void run2() { last = new String[]{}; }
    }

    @Component
    static class DuplicateBean {
        @TaskMapping("dup")
        public void a() {}
        @TaskMapping("dup")
        public void b() {}
    }

    @Component
    static class EmptyNameBean {
        @TaskMapping("")
        public void a() {}
    }

    @Test
    void registerHandlers_success() {
        TaskMappingPostProcessor p = new TaskMappingPostProcessor();
        GoodBean bean = new GoodBean();
        Object processed = p.postProcessAfterInitialization(bean, "goodBean");
        Assertions.assertThat(processed).isSameAs(bean);

        // 注册后可执行
        TaskMappingContext.run(new TaskNameResolver("good#1$2") {
            @Override public String getTaskName() { return "good"; }
            @Override public String[] getTaskParam() { return new String[]{"1","2"}; }
        });
        Assertions.assertThat(GoodBean.last).containsExactly("1","2");

        TaskMappingContext.run(new TaskNameResolver("goodNoArg") {
            @Override public String getTaskName() { return "goodNoArg"; }
            @Override public String[] getTaskParam() { return new String[]{}; }
        });
        Assertions.assertThat(GoodBean.last).isEmpty();
    }

    @Test
    void registerHandlers_duplicateName_shouldThrow() {
        TaskMappingPostProcessor p = new TaskMappingPostProcessor();
        Assertions.assertThatThrownBy(() -> p.postProcessAfterInitialization(new DuplicateBean(), "dupBean"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("已存在相同的taskName");
    }

    @Test
    void registerHandlers_emptyName_shouldThrow() {
        TaskMappingPostProcessor p = new TaskMappingPostProcessor();
        Assertions.assertThatThrownBy(() -> p.postProcessAfterInitialization(new EmptyNameBean(), "emptyBean"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("taskName不能为空");
    }
}

