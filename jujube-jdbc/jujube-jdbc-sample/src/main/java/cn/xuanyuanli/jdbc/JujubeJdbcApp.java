package cn.xuanyuanli.jdbc;

import cn.xuanyuanli.jdbc.base.h2.H2JdbcTemplateAopSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author xuanyuanli
 */
@SpringBootApplication(scanBasePackages = "cn.xuanyuanli.jdbc")
@EnableAspectJAutoProxy(exposeProxy = true)
public class JujubeJdbcApp {
    public static void main(String[] args) {
        SpringApplication.run(JujubeJdbcApp.class, args);
    }

    @Bean
    public H2JdbcTemplateAopSupport h2JdbcTemplateAopSupport() {
        return new H2JdbcTemplateAopSupport();
    }

}
