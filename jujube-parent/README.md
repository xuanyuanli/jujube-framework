# Jujube Parent

[![Maven Central](https://img.shields.io/maven-central/v/cn.xuanyuanli/jujube-parent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21+-green.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

Jujube Parent 是 Jujube 系列项目的 Maven 父 POM，为所有子模块提供统一的依赖管理、插件配置和构建规范。

## 🎯 项目定位

作为 **Maven 父 POM**，Jujube Parent 主要职责：

- **统一依赖管理**：管理所有第三方库的版本，确保依赖一致性
- **标准化构建**：提供统一的 Maven 插件配置和构建流程
- **版本控制**：集中管理 Jujube 系列模块的版本号
- **质量保证**：配置代码质量检查和测试标准

## 📦 子模块管理

Jujube Parent 管理以下核心模块：

| 模块                                                                                               | 说明                       | 版本    |
|--------------------------------------------------------------------------------------------------|--------------------------|-------|
| [jujube-core](https://github.com/xuanyuanli/jujube-core)                                         | 核心工具库                    | 3.0.0 |
| [jujube-jdbc](https://github.com/xuanyuanli/jujube-jdbc)                                         | JDBC 增强工具                | 3.0.0 |
| [spring-boot-starter-jujube-jdbc](https://github.com/xuanyuanli/spring-boot-starter-jujube-jdbc) | Spring Boot JDBC Starter | 3.0.0 |

## 🛠️ 技术栈

### 基础框架
- **Java 21** - 现代化 Java 运行时
- **Spring Boot 3.4.5** - 企业级应用框架
- **Maven 3.5.0+** - 项目构建工具

### 核心依赖管理

#### 🔧 工具库
- **Apache Commons** - 通用工具集合
  - commons-lang3, commons-text, commons-io
  - commons-csv, commons-compress, commons-collections4
- **Google Guava 33.3.1** - Google 核心库
- **Javassist 3.30.0** - 字节码操作
- **CGLib 3.3.0** - 代码生成库

#### 📄 文档处理
- **Apache POI 5.3.0** - Office 文档处理
- **Apache Tika 3.1.0** - 文档内容提取
- **Flying Saucer PDF 9.11.3** - PDF 生成

#### 🖼️ 图像处理
- **TwelveMonkeys ImageIO 3.12.0** - 图像格式扩展
- **Thumbnailator 0.4.20** - 图像处理和缩略图
- **Metadata Extractor 2.19.0** - 图像元数据读取

#### 🔐 安全与加密
- **ZXing 3.5.3** - 二维码生成与识别
- **Jasypt Spring Boot 3.0.5** - 属性加密

#### 🌐 网络与爬虫
- **Unirest Java 4.4.5** - HTTP 客户端
- **Retrofit 2.11.0** - 类型安全的 HTTP 客户端
- **JSoup 1.18.3** - HTML 解析器
- **Playwright 1.49.0** - 浏览器自动化

#### 💾 数据存储
- **Redisson 3.39.0** - Redis 分布式客户端
- **H2 Database 2.2.220** - 内存数据库
- **JSQLParser 5.0** - SQL 解析器

#### 📊 监控与日志
- **OpenTelemetry 1.26.0** - 可观测性框架
- **Logstash Logback Encoder 8.0** - 结构化日志
- **SpringDoc OpenAPI 2.7.0** - API 文档生成

#### 🧪 测试工具
- **Spring Boot Test** - Spring Boot 测试框架
- **Mockito Inline 5.2.0** - Mock 测试框架
- **JMH 1.37** - Java 微基准测试

#### 🔧 其他工具
- **JavaFaker 1.0.2** - 测试数据生成
- **Pinyin4j 2.5.0** - 中文拼音转换
- **OpenCC4j 1.8.1** - 中文繁简体转换
- **Zip4j 2.11.5** - ZIP 文件处理

## 🚀 使用方式

### 作为父 POM 继承

在你的项目 `pom.xml` 中：

```xml
<parent>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-parent</artifactId>
    <version>3.1.0</version>
</parent>
```

### 使用托管依赖

继承父 POM 后，可直接使用托管的依赖版本：

```xml
<dependencies>
    <!-- 无需指定版本，由父 POM 管理 -->
    <dependency>
        <groupId>cn.xuanyuanli</groupId>
        <artifactId>jujube-core</artifactId>
    </dependency>
    
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-lang3</artifactId>
    </dependency>
</dependencies>
```

### 覆盖依赖版本

如需使用特定版本，可在子项目中覆盖：

```xml
<properties>
    <guava.version>32.1.0-jre</guava.version>
</properties>
```

## ⚙️ 配置说明

### 编译配置
- **Java 版本**：21
- **编码格式**：UTF-8
- **Maven 最低版本**：3.5.0

### 插件配置
- **测试插件**：只运行 `*Test.java` 命名的测试类
- **源码插件**：自动生成源码 JAR
- **文档插件**：自动生成 Javadoc JAR
- **GPG 签名**：支持 Maven Central 发布

### 构建特性
- **资源过滤**：保护二进制文件（如 TTF 字体）不被过滤
- **Lombok 支持**：注解处理器自动配置
- **中央仓库发布**：配置 Sonatype Central 发布插件

## 📋 版本规范

Jujube Parent 遵循语义化版本规范：
- **主版本号**：不兼容的 API 修改
- **次版本号**：向下兼容的功能新增
- **修订号**：向下兼容的问题修正

## 🤝 贡献指南

1. **Fork** 本仓库
2. 创建特性分支：`git checkout -b feature/amazing-feature`
3. 提交改动：`git commit -m 'Add some amazing feature'`
4. 推送到分支：`git push origin feature/amazing-feature`
5. 创建 **Pull Request**

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

## 👨‍💻 作者

**xuanyuanli** - [xuanyuanli999@gmail.com](mailto:xuanyuanli999@gmail.com)

## 🔗 相关链接

- [GitHub 仓库](https://github.com/xuanyuanli/jujube-parent)
- [Maven Central](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-parent)
- [问题反馈](https://github.com/xuanyuanli/jujube-parent/issues)
- [Jujube Core](https://github.com/xuanyuanli/jujube-core) - 核心工具库
- [Jujube JDBC](https://github.com/xuanyuanli/jujube-jdbc) - ORM工具