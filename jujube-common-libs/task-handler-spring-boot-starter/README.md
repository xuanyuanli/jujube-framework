## Task Handler Spring Boot Starter

基于 Spring 的轻量任务路由器：使用 `@TaskMapping` 为方法绑定任务名，通过 `TaskMappingContext.run(...)` 在运行时按“任务名 + 参数”分发执行。

自动装配，无需手动注入或额外配置，适合快速在项目内实现简单的“任务调度/指令执行”。

### 特性
- 简单：一个注解 + 一个调用入口即可完成分发
- 自动：开箱即用的 Spring Boot AutoConfiguration
- 可扩展：支持自定义任务名解析器 `TaskNameResolver`
- 安全：重复任务名与空任务名在注册阶段即抛出异常
- 线程友好：任务处理映射使用并发容器

---

### 安装依赖（Maven）
```xml
<dependency>
  <groupId>cn.xuanyuanli.boot</groupId>
  <artifactId>task-handler-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### 快速开始
1) 在任意 Spring 管理的 Bean 上标注 `@TaskMapping`（`value` 为任务名）
```java
import cn.xuanyuanli.boot.taskhandler.core.TaskMapping;
import org.springframework.stereotype.Component;

@Component
public class DemoTasks {
  @TaskMapping("echo")
  public void echo(String[] args) {
    // args 由调用侧解析传入
  }

  @TaskMapping("refreshCache")
  public void refreshCache() {
    // 无参任务
  }
}
```

2) 在调用侧执行任务
```java
import cn.xuanyuanli.boot.taskhandler.core.TaskMappingContext;
import cn.xuanyuanli.boot.taskhandler.core.SplitTaskNameResolver;

// 任务体：任务名与参数的拼接串
TaskMappingContext.run(new SplitTaskNameResolver("echo#hello$world", "#", "$"));
TaskMappingContext.run(new SplitTaskNameResolver("refreshCache", "#", "$"));
```

说明：`SplitTaskNameResolver(body, taskNameSeparator, taskParamSeparator)`
- `body`：任务体字符串（如 `echo#hello$world`）
- `taskNameSeparator`：任务名与参数的分隔符（如 `#`）
- `taskParamSeparator`：参数之间的分隔符（如 `$`）
- 若 `taskParamSeparator` 为空字符串，则不进行字符级拆分，整个参数片段作为一个元素返回

---

### 工作机制
1) `@TaskMapping` 注解的方法，在 Spring 容器初始化完成后由 `TaskMappingPostProcessor` 扫描并注册到全局映射中。
2) 运行时通过 `TaskMappingContext.run(TaskNameResolver)` 解析任务体，定位对应处理器并反射调用。
3) 若未找到任务处理器，默认记录 `warn` 日志，不抛出异常。

---

### 约束与最佳实践
- 方法签名仅支持：
  - 无参 `()`
  - 单参数 `(String[] args)`
- 不建议与 `@Async/@Scheduled` 混用在同一方法；`@TaskMapping` 的执行基于反射，不会触发异步代理行为。
- 任务名必须唯一且非空；重复或空白的任务名会在容器启动阶段抛出异常。

---

### 扩展：自定义 TaskNameResolver
```java
import cn.xuanyuanli.boot.taskhandler.core.TaskNameResolver;

public class JsonTaskNameResolver extends TaskNameResolver {
  private String name;
  private String[] params;

  public JsonTaskNameResolver(String body) { super(body); /* 解析 JSON，填充 name/params */ }
  @Override public String getTaskName() { return name; }
  @Override public String[] getTaskParam() { return params == null ? new String[]{} : params; }
}
```

---

### 典型用例
- 从消息队列中消费一条“任务指令字符串”，经解析后在本地执行具体方法
- 简化内部调度：任务名 + 参数的轻量分发，不引入完整任务编排系统

---

### 自动装配
本 Starter 通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 自动装配 `TaskHandlerConfiguration`，无需手动引入配置类。

---

### 疑难解答
- 日志提示“没有找到对应的task”：
  - 检查任务名是否与 `@TaskMapping` 的 `value` 完全一致
  - 检查分隔符与参数解析是否正确
  - 确认被注解的方法所在 Bean 已被 Spring 管理





