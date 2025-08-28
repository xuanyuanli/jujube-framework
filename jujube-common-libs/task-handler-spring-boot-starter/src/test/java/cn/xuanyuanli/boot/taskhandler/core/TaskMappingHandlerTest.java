package cn.xuanyuanli.boot.taskhandler.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class TaskMappingHandlerTest {

    static class M {
        static String[] last;
        public void a() { last = new String[]{}; }
        public void b(String[] args) { last = args; }
        public void c(String[] a, String[] b) { last = new String[]{"bad"}; }
    }

    @Test
    void execute_noArg_and_oneArg_supported() throws Exception {
        M m = new M();

        TaskMappingHandler h0 = new TaskMappingHandler();
        Method m0 = M.class.getMethod("a");
        h0.setHandlerInstance(m);
        h0.setHandlerMethod(m0);
        h0.execute(new String[]{"x"});
        Assertions.assertThat(M.last).isEmpty();

        TaskMappingHandler h1 = new TaskMappingHandler();
        Method m1 = M.class.getMethod("b", String[].class);
        h1.setHandlerInstance(m);
        h1.setHandlerMethod(m1);
        h1.execute(new String[]{"p","q"});
        Assertions.assertThat(M.last).containsExactly("p","q");
    }

    @Test
    void execute_unsupportedParamCount_shouldNotThrow() throws Exception {
        M m = new M();
        TaskMappingHandler hx = new TaskMappingHandler();
        Method mx = M.class.getMethod("c", String[].class, String[].class);
        hx.setHandlerInstance(m);
        hx.setHandlerMethod(mx);
        // 仅记录 warn，不抛异常
        Assertions.assertThatCode(() -> hx.execute(new String[]{"1","2"})).doesNotThrowAnyException();
    }
}

