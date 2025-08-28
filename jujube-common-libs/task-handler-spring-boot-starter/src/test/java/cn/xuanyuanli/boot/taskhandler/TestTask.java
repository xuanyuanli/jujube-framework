package cn.xuanyuanli.boot.taskhandler;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import cn.xuanyuanli.boot.taskhandler.core.TaskMapping;

@Component
public class TestTask {
    static String[] param;

    @TaskMapping("test")
    public void exec(String params[]) {
        TestTask.param = params;
    }

    @TaskMapping("test2")
    public void exec2() {
        TestTask.param = new String[] {};
    }

    @TaskMapping("test3")
    public void exec2(String params[]) {
        TestTask.param = params;
    }

    @Scheduled(cron = "0 0 0 0 1 ?")
    @Async
    public void  ext(){

    }
}
