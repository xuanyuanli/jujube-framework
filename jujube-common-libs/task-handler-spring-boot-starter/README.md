## 使用
本项目是基于Spring的任务路由，可实现简单的任务分发。同时也是Spring Boot的插件，会自动装载配置。  

使用方式如：
### 1、先使用@TaskMapping注解相应方法，value即为task名称
```java
    @TaskMapping("test")
    public void exec(String params[]) {
        ...
    }
```
注意：用@TaskMapping注解的方法如果用了其他注解如@Async，则不会起作用，因为@TaskMapping的调用是通过反射的方式
### 2、在任务调用侧的用法
```java
        TaskMappingContext.run(new SplitTaskNameResolver("test#1$2", "#", "$"));
```
SplitTaskNameResolver是一个任务名处理者，第一个参数是任务body，第二个参数和第三个参数负责对任务body进行分解。  
第二个参数是任务名分解符号，第三个参数是任务参数分解符号。  
任务名会和@TaskMapping的任务名关联，任务参数就是@TaskMapping注解的方法的入参

### 3、maven配置
```
            <dependency>
                <groupId>cn.xuanyuanli.boot</groupId>
                <artifactId>task-handler-spring-boot-starter</artifactId>
                <version>1.0.0</version>
            </dependency>
```

## 扩展点
### 1、TaskNameResolver
项目中有默认实现SplitTaskNameResolver，还可以根据自己的需求定制TaskNameResolver




