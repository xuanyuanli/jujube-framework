package cn.xuanyuanli.boot.taskhandler.core;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class SplitTaskNameResolverTest {

    @Test
    void getTaskName() {
        // 无参数
        SplitTaskNameResolver r = new SplitTaskNameResolver("task#TASKNAME#", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(r.getTaskName()).isEqualTo("task");
        Assertions.assertThat(r.getTaskParam()).isEmpty();

        // 单参数
        r = new SplitTaskNameResolver("task#TASKNAME#12", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(r.getTaskName()).isEqualTo("task");
        Assertions.assertThat(r.getTaskParam()).containsExactly("12");

        // 多参数
        r = new SplitTaskNameResolver("task#TASKNAME#ab$TASKPARAM$12", "#TASKNAME#", "$TASKPARAM$");
        Assertions.assertThat(r.getTaskName()).isEqualTo("task");
        Assertions.assertThat(r.getTaskParam()).containsExactly("ab", "12");

        // 特殊字符作为分隔符（需要被转义）
        r = new SplitTaskNameResolver("do.sum(1,2)@1+2", "@", "+");
        Assertions.assertThat(r.getTaskName()).isEqualTo("do.sum(1,2)");
        Assertions.assertThat(r.getTaskParam()).containsExactly("1", "2");

        // 空参数分隔符：返回整个参数片段
        r = new SplitTaskNameResolver("task#abc", "#", "");
        Assertions.assertThat(r.getTaskParam()).containsExactly("abc");
    }
}
