# Playwright Stealth Pool

一个提供Playwright连接池管理和反检测功能的Java库。该项目旨在简化Playwright的使用，提供高效的资源管理，并集成反自动化检测功能。

## 🚀 核心特性

- **🔄 连接池管理**: 支持Playwright实例和Browser实例的连接池管理
- **🥷 反检测功能**: 内置JavaScript脚本绕过常见的自动化检测机制  
- **⚙️ 灵活配置**: 丰富的配置选项，支持各种使用场景
- **🛡️ 健壮性**: 完善的错误处理和资源清理机制
- **📊 监控支持**: 提供连接池状态监控和统计信息

## 📦 项目结构

```
cn.xuanyuanli.playwright.stealth/
├── config/                  # 配置管理
│   └── PlaywrightConfig.java
├── manager/                 # 管理器
│   ├── PlaywrightManager.java
│   └── PlaywrightBrowserManager.java  
├── pool/                    # 连接池工厂
│   ├── PlaywrightFactory.java
│   └── PlaywrightBrowserFactory.java
└── stealth/                 # 反检测功能
    └── StealthScriptProvider.java
```

## 🏗️ 架构设计

### 管理器对比

| 特性 | PlaywrightManager | PlaywrightBrowserManager |
|------|------------------|-------------------------|
| 管理对象 | Playwright实例 | Browser实例 |
| 适用场景 | 短时间、一次性操作 | 频繁操作、保持会话 |
| 资源开销 | 较低 | 较高 |
| 启动速度 | 较慢（每次创建Browser） | 较快（复用Browser） |
| 推荐用途 | 批量处理、脚本任务 | Web服务、长时间运行 |

## 📖 使用指南

### 基本用法

```java
// 1. 创建配置
PlaywrightConfig config = new PlaywrightConfig()
    .setHeadless(true)
    .setStealthMode(StealthMode.FULL)
    .setDisableImageRender(true);

// 2. 创建管理器
PlaywrightManager manager = new PlaywrightManager(8);

// 3. 执行页面操作
manager.execute(config, page -> {
    page.navigate("https://example.com");
    System.out.println(page.title());
});

// 4. 记得关闭
manager.close();
```

### 使用Browser管理器

```java
// 创建Browser连接池管理器
PlaywrightBrowserManager browserManager = new PlaywrightBrowserManager(config, 5);

// 并发执行任务
IntStream.range(0, 20).parallel().forEach(i -> {
    browserManager.execute(page -> {
        page.navigate("https://httpbin.org/get");
        // 处理页面...
    });
});

browserManager.close();
```

### 高级配置

```java
PlaywrightConfig config = new PlaywrightConfig()
    // 浏览器设置
    .setHeadless(false)                    // 显示浏览器界面
    .setStartMaximized(true)               // 窗口最大化
    .setSlowMo(100.0)                      // 操作延迟100ms
    
    // 性能优化
    .setDisableGpu(true)                   // 禁用GPU加速
    .setDisableImageRender(true)           // 禁用图片渲染
    
    // 反检测配置
    .setStealthMode(StealthMode.FULL)      // 完整反检测模式
    .setDisableAutomationControlled(true)  // 隐藏自动化标识
    
    // 网络配置
    .setProxy(new Proxy("http://proxy:8080"));
```

### 自定义浏览器上下文

```java
manager.execute(config, context -> {
    // 设置地理位置
    context.setGeolocation(new Geolocation(39.9042, 116.4074));
    
    // 授予权限
    context.grantPermissions(Arrays.asList("geolocation", "notifications"));
    
    // 设置额外头信息
    context.setExtraHTTPHeaders(Map.of("Custom-Header", "value"));
    
}, page -> {
    // 页面操作...
});
```

## 🥷 反检测功能

该库内置了多种反检测机制：

### JavaScript指纹修复
- ✅ 隐藏 `navigator.webdriver` 属性
- ✅ 模拟真实的 `navigator.plugins` 和 `mimeTypes`
- ✅ 修复 WebGL 指纹信息
- ✅ 模拟硬件信息（CPU核心数、内存等）
- ✅ 修复 AudioContext 指纹

### 浏览器启动参数
- ✅ `--disable-blink-features=AutomationControlled`
- ✅ `--disable-gpu`（可配置）
- ✅ 自定义 User-Agent

### 使用示例

```java
// 禁用反检测（性能最佳）
PlaywrightConfig disabledConfig = new PlaywrightConfig()
    .setStealthMode(StealthMode.DISABLED)
    .setDisableAutomationControlled(false);

// 轻量级反检测（平衡性能和隐蔽性）
PlaywrightConfig lightConfig = new PlaywrightConfig()
    .setStealthMode(StealthMode.LIGHT)      // 基础反检测
    .setDisableAutomationControlled(true);   // 配合启动参数

// 完整反检测（最强隐蔽性）  
PlaywrightConfig fullConfig = new PlaywrightConfig()
    .setStealthMode(StealthMode.FULL)       // 全面反检测
    .setDisableAutomationControlled(true);
```

### StealthMode 详细说明

| 模式 | 性能开销 | 检测覆盖 | 主要功能 | 适用场景 |
|------|----------|----------|----------|----------|
| **DISABLED** | 无 | 无 | 不注入任何脚本 | 内部测试、性能敏感 |
| **LIGHT** | 极低 | 基础检测 | `navigator.webdriver`<br/>`navigator.languages`<br/>`navigator.platform` | 一般网站、批量任务 |
| **FULL** | 中等 | 全面检测 | 所有LIGHT功能 +<br/>插件模拟、WebGL修复<br/>AudioContext修复等 | 强检测网站、生产环境 |
```

## 📊 监控和调试

### 连接池状态监控

```java
// 获取连接池状态
System.out.println(manager.getPoolStatus());
// 输出: Pool Status - Active: 2, Idle: 3, Total: 5/8

// 获取详细统计（仅BrowserManager支持）
System.out.println(browserManager.getPoolStatistics());
// 输出: Browser Pool Statistics - Created: 10, Borrowed: 25, Returned: 23...
```

### 预热和清理

```java
// 预热连接池
browserManager.warmUpPool(3);

// 清理空闲连接
browserManager.evictIdleBrowsers();
```

## ⚠️ 注意事项

### 反检测脚本使用声明

本库提供的反检测功能仅供以下合法用途：
- ✅ 自动化测试和质量保证
- ✅ 网站性能监控
- ✅ 合规的数据采集
- ✅ 学习和研究目的

**请务必**：
- 🔴 遵守目标网站的服务条款和robots.txt
- 🔴 遵守相关法律法规
- 🔴 不用于恶意目的或侵犯他人权益
- 🔴 控制请求频率，避免对目标服务器造成负担

### 性能建议

1. **选择合适的管理器**：
   - 短时间任务使用 `PlaywrightManager`
   - 长时间运行使用 `PlaywrightBrowserManager`

2. **连接池大小设置**：
   - 考虑CPU核心数和内存限制
   - 建议值：核心数 × 2 到核心数 × 4

3. **配置优化**：
   - 启用 `disableImageRender` 提高页面加载速度
   - 启用 `disableGpu` 在服务器环境中提高稳定性

### 常见问题

**Q: 为什么Browser创建很慢？**
A: 首次创建需要初始化浏览器引擎。使用 `warmUpPool()` 预热连接池。

**Q: 反检测脚本不起作用？**  
A: 确保 `stealthMode` 不是 `DISABLED`，根据网站检测强度选择 `LIGHT` 或 `FULL` 模式。

**Q: 内存使用过高？**
A: 适当减小连接池大小，及时调用 `close()` 释放资源。

## 🔗 相关链接

- [Playwright官方文档](https://playwright.dev/)
- [Apache Commons Pool2](https://commons.apache.org/proper/commons-pool/)

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request。在贡献代码前，请确保：

1. 遵循现有代码风格
2. 添加适当的测试
3. 更新相关文档
4. 确保所有测试通过

## 📄 许可证

请查看项目根目录的 LICENSE 文件了解许可证详情。
