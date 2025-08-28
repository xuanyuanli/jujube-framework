package cn.xuanyuanli.boot.taskhandler;

import cn.xuanyuanli.boot.taskhandler.core.SplitTaskNameResolver;
import cn.xuanyuanli.boot.taskhandler.core.TaskMappingContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TaskHandlerApplication.class)
public class TaskHandlerTest {

    @Test
    public void test() {
        TaskMappingContext.run(new SplitTaskNameResolver("test3#null", "#", "$"));
        Assertions.assertThat(TestTask.param.length).isEqualTo(1);

        TaskMappingContext.run(new SplitTaskNameResolver("test#1$2", "#", "$"));
        Assertions.assertThat(TestTask.param).contains("1", "2");

        TaskMappingContext.run(new SplitTaskNameResolver("test@1_2", "@", "_"));
        Assertions.assertThat(TestTask.param).contains("1", "2");

        TaskMappingContext.run(new SplitTaskNameResolver("test2", "#", ""));
        Assertions.assertThat(TestTask.param.length).isEqualTo(0);

    }
}
