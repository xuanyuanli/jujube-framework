package cn.xuanyuanli.boot.taskhandler.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SplitTaskNameResolverTest {

    @Test
    void getTaskName() {
        SplitTaskNameResolver nameResolver = new SplitTaskNameResolver("task#TASKNAME#", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(nameResolver.getTaskName()).isEqualTo("task");
        Assertions.assertThat(nameResolver.getTaskParam()).isEmpty();

        nameResolver = new SplitTaskNameResolver("task#TASKNAME#", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(nameResolver.getTaskName()).isEqualTo("task");
        Assertions.assertThat(nameResolver.getTaskParam()).isEmpty();

        nameResolver = new SplitTaskNameResolver("task#TASKNAME#12", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(nameResolver.getTaskName()).isEqualTo("task");
        Assertions.assertThat(nameResolver.getTaskParam()).containsExactly("12");

        nameResolver = new SplitTaskNameResolver("task#TASKNAME#ab$TASKPARAM$12", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(nameResolver.getTaskName()).isEqualTo("task");
        Assertions.assertThat(nameResolver.getTaskParam()).containsExactly("ab", "12");
    }
}
