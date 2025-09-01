# Jujube Core

[![Maven Central](https://img.shields.io/maven-central/v/cn.xuanyuanli/jujube-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-core)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21+-green.svg)](https://www.oracle.com/java/)

Jujube Core 是一个功能丰富的Java核心工具库，旨在为Java开发者提供一套完整、高效的工具集合。

## 🎯 设计理念

每个项目都会有自己的核心工具二方包，这个二方包一般满足以下特点：
- **封装常用操作**：对项目常用的各种工具操作进行封装  
- **持续完善**：随着项目经验的累加，不断完善自己的工具包  

特别是第二点，就像一个熟练的汽车工程师会去积累自己的工具和经验一样，程序员在开发过程中也是不断积累自己的工具包的。

与传统轻量级工具包不同，**Jujube Core** 集成了众多业界流行的三方包作为基础，更适合作为企业级项目的二方包使用。如果你需要一个轻量级的三方包，推荐使用 [Hutool](https://hutool.cn/) 等其他选择。

## 🏗️ 模块化架构

Jujube Core 3.x 版本采用模块化设计，您可以根据需要选择引入特定功能模块：

### 📦 模块列表

| 模块 | 功能描述 | 依赖说明                 |
|------|----------|----------------------|
| `jujube-core-base` | 基础核心模块 | 仅依赖Slf4j 和 JDK |
| `jujube-core-utils` | 通用工具模块 | 依赖 Apache Commons 等  |
| `jujube-core-json` | JSON处理模块 | 基于 Jackson           |
| `jujube-core-image` | 图像处理模块 | 图片处理、二维码生成           |
| `jujube-core-office` | 办公文档模块 | Excel、Word、CSV 处理    |
| `jujube-core-security` | 安全加密模块 | AES 等加密算法            |
| `jujube-core-web` | Web开发模块 | HTTP、网络、用户代理解析       |
| `jujube-core-spring` | Spring集成模块 | Spring 环境增强功能        |
| `jujube-core-all` | 完整功能聚合模块 | 包含所有子模块              |

## 🚀 快速开始

### Maven 依赖

#### 方式一：引入完整功能包（推荐）
```xml
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-core-all</artifactId>
    <version>3.1.1</version>
</dependency>
```

#### 方式二：按需引入模块
```xml
<!-- 基础核心模块（必选） -->
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-core-base</artifactId>
    <version>3.1.1</version>
</dependency>

<!-- 通用工具模块 -->
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-core-utils</artifactId>
    <version>3.1.1</version>
</dependency>

<!-- 其他模块按需引入... -->
```

最新版本请查看：[Maven Central](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-core)

## 📦 核心功能

### 🏗️ 基础模块 (`jujube-core-base`)
- **常量类**：`Charsets`、`SystemProperties` - 系统常量定义
- **异常类**：`RepeatException` - 自定义异常
- **环境工具**：`Envs` - 环境变量处理

### 🔧 通用工具模块 (`jujube-core-utils`)
- **日期时间**：`Dates` - 提供丰富的日期时间处理功能
- **Bean操作**：`Beans` - 反射、类型转换、Bean拷贝等
- **集合操作**：`Collections3` - 集合处理增强工具
- **文本处理**：`Texts` - 字符串处理和验证
- **数字计算**：`Numbers`, `Calcs` - 数学计算和数字处理
- **文件操作**：`Files` - 文件读写、压缩等操作
- **随机数据**：`Randoms`, `DataGenerator` - 随机数和测试数据生成
- **并发处理**：`CompletableFutures` - 异步任务组合和执行工具
- **多线程执行器**：`ListMutilThreadExecutor` - 集合元素多线程并发处理
- **进度跟踪器**：`ProgressTracker` - 提供精确的进度跟踪和里程碑检查
- **雪花ID**：`SnowFlakes` - 分布式唯一ID生成
- **实体基类**：`BaseEntity` - 通用实体基类
- **记录类型**：`Record` - 键值对记录

### 📄 JSON处理模块 (`jujube-core-json`)
- **JSON处理**：`Jsons` - 基于Jackson的JSON序列化/反序列化

### 🎨 图像处理模块 (`jujube-core-image`)
- **图像处理**：`Images` - 图片处理、缩略图生成、格式转换
- **二维码**：`QrCodes` - 二维码生成和识别

### 📊 办公文档模块 (`jujube-core-office`)
- **Excel处理**：`ExcelReader/Writer` - Excel文档读写
- **Word处理**：`WordReader` - Word文档读取
- **CSV处理**：`CsvReader` - CSV文件处理

### 🔐 安全加密模块 (`jujube-core-security`)
- **加密工具**：`AesUtil` - AES加密解密

### 🌐 Web开发模块 (`jujube-core-web`)
- **网络工具**：`Https`, `Networks` - HTTP请求和网络操作
- **用户代理**：`UserAgentUtil` - User-Agent解析
- **Web工具**：`Controllers`, `Servlets`, `Cookies` - Web开发辅助
- **文件上传**：`WebUploaderUtil` - 文件上传处理

### 🌸 Spring集成模块 (`jujube-core-spring`)
- **上下文持有者**：`ApplicationContextHolder` - Spring上下文访问
- **参数校验**：`BaseMethodParamCheckBeanPostProcessor` - 方法参数自动校验
- **AOP工具**：`Aops` - 面向切面编程辅助
- **SpEL工具**：`SpEls` - Spring表达式语言处理


## 🛠️ 技术栈

各模块采用不同的技术栈，支持按需引入：

### 🏗️ 基础模块 (`jujube-core-base`)
- 零外部依赖，仅依赖JDK 21+
- 可选：SLF4J、Lombok

### 🔧 工具模块 (`jujube-core-utils`)
- **通用工具**：[Apache Commons](https://commons.apache.org/) (Lang3, Text, IO, BeanUtils)
- **字节码处理**：[Javassist](https://www.javassist.org/)
- **模板引擎**：[FreeMarker](https://freemarker.apache.org/)
- **拼音处理**：[pinyin4j](https://github.com/belerweb/pinyin4j)

### 📄 JSON模块 (`jujube-core-json`)
- **JSON处理**：[Jackson](https://github.com/FasterXML/jackson)

### 🎨 图像模块 (`jujube-core-image`)
- **图像处理**：[TwelveMonkeys ImageIO](https://github.com/haraldk/TwelveMonkeys)
- **元数据提取**：[metadata-extractor](https://drewnoakes.com/code/exif/)
- **缩略图生成**：[Thumbnailator](https://github.com/coobird/thumbnailator)
- **二维码**：[ZXing](https://github.com/zxing/zxing)

### 📊 办公文档模块 (`jujube-core-office`)
- **Excel/Word**：[Apache POI](https://poi.apache.org/)
- **CSV处理**：[Apache Commons CSV](https://commons.apache.org/proper/commons-csv/)

### 🔐 安全模块 (`jujube-core-security`)
- 基于JDK内置加密API

### 🌐 Web模块 (`jujube-core-web`)
- **HTTP客户端**：[Unirest](https://github.com/Kong/unirest-java)
- **Servlet API**：Jakarta Servlet

### 🌸 Spring模块 (`jujube-core-spring`)
- **Spring Framework**：提供Spring环境下的增强功能

### 通用工具
- **Lombok**：减少样板代码（所有模块可选依赖）

## 📝 使用示例

### 基础功能示例
```java
// 基础模块 - 环境变量处理
String value = Envs.get("MY_ENV_VAR", "default");

// 工具模块 - 日期处理
Date date = Dates.parseDate("2024-01-01 12:00:00");
String formatted = Dates.format(date, "yyyy/MM/dd");

// 工具模块 - Bean拷贝
UserDTO dto = Beans.copy(user, UserDTO.class);

// 工具模块 - 异步任务组合
String result = CompletableFutures.combine(
    () -> "Hello",
    () -> "World", 
    (s1, s2) -> s1 + " " + s2
);

// 工具模块 - 集合多线程处理
List<String> urls = Arrays.asList("url1", "url2", "url3");
ListMutilThreadExecutor<String> executor = new ListMutilThreadExecutor<>(urls, 5);
executor.start(
    url -> processUrl(url), // 处理每个URL
    result -> System.out.println("处理完成，耗时：" + result.getUseTime() + "秒")
);

// 工具模块 - 进度跟踪
ProgressTracker tracker = new ProgressTracker(100);
tracker.step(25);  // 步进25个单位
System.out.println(tracker.getProgressPercentage());  // "25.00 %"
if (tracker.isPercentMilestone()) {
    System.out.println("达到新的百分比里程碑!");
}
```

### 各模块功能示例
```java
// JSON模块 - JSON操作
User user = new User("John", 25);
String json = Jsons.toJson(user);
User parsed = Jsons.fromJson(json, User.class);

// 图像模块 - 二维码生成
QrCodes.generateQrCode("Hello World", "/path/to/qrcode.png");

// 办公文档模块 - Excel处理
List<User> users = ExcelReader.read("/path/to/users.xlsx", User.class);

// 安全模块 - AES加密
String encrypted = AesUtil.encrypt("plaintext", "secretKey");

// Web模块 - HTTP请求
String response = Https.get("https://api.example.com/data");

// Spring模块 - 获取Spring上下文
ApplicationContext context = ApplicationContextHolder.get();
```

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进这个项目。

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

## 👨‍💻 作者

**xuanyuanli** - [xuanyuanli999@gmail.com](mailto:xuanyuanli999@gmail.com)

## 🔗 相关链接

- [GitHub 仓库](https://github.com/xuanyuanli/jujube-framework)
- [Maven Central](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-core)
- [问题反馈](https://github.com/xuanyuanli/jujube-framework/issues)

