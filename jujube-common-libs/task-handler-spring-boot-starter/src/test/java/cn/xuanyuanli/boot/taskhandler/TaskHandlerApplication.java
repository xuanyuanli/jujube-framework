package cn.xuanyuanli.boot.taskhandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "cn.xuanyuanli.boot.taskhandler")
@EnableAsync
public class TaskHandlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskHandlerApplication.class, args);
    }
}
