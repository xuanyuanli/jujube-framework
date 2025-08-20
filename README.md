# Jujube Framework

[![Maven Central](https://img.shields.io/maven-central/v/cn.xuanyuanli/jujube-parent.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21+-green.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

> 🚀 一个完整的企业级 Java 开发工具框架，融合核心工具库、ORM 框架和统一依赖管理于一体

## 📖 简介

**Jujube Framework** 是一个现代化的 Java 开发框架集合，旨在为企业级应用开发提供完整、高效、易用的解决方案。框架采用模块化设计，包含三个核心子项目，每个项目都解决特定的开发痛点。

### 🌟 核心优势

- **🔧 功能完整** - 涵盖工具库、ORM、依赖管理等完整开发生态
- **⚡ 现代技术栈** - 基于 Java 21 + Spring Boot 3.x，拥抱新技术
- **📦 模块化设计** - 支持按需引入，避免依赖臃肿
- **🏢 企业级** - 经过生产环境验证，适合企业项目使用
- **🔄 持续演进** - 活跃维护，跟随技术发展持续更新

## 🏗️ 框架架构

Jujube Framework 由以下三个核心子项目组成：

```
┌─────────────────────────────────────────────────────────────┐
│                    Jujube Framework                         │
├─────────────────┬─────────────────┬─────────────────────────┤
│   Jujube Core   │   Jujube JDBC   │     Jujube Parent       │
│   核心工具库      │   ORM 框架      │     依赖管理             │
└─────────────────┴─────────────────┴─────────────────────────┘
```

## 📦 子项目详解

### 1. 🔧 [Jujube Core](./jujube-core) - 核心工具库

功能丰富的 Java 核心工具库，提供企业级开发所需的各种工具类。

**🎯 核心特性：**
- **模块化架构** - 8个功能模块，支持按需引入
- **零外部依赖** - 基础模块无任何第三方依赖
- **企业级功能** - 集成主流三方库，提供开箱即用的企业功能
- **高性能优化** - 针对常用操作进行性能优化

**📦 功能模块：**

| 模块 | 功能描述 |
|------|----------|
| `jujube-core-base` | 基础核心模块（零依赖） |
| `jujube-core-utils` | 通用工具模块 |
| `jujube-core-json` | JSON处理模块 |
| `jujube-core-image` | 图像处理模块 |
| `jujube-core-office` | 办公文档模块 |
| `jujube-core-security` | 安全加密模块 |
| `jujube-core-web` | Web开发模块 |
| `jujube-core-spring` | Spring集成模块 |

**🚀 快速开始：**
```xml
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-core-all</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 2. 🗄️ [Jujube JDBC](./jujube-jdbc) - 简洁高效的 ORM 框架

融合 MyBatis 灵活性与 Spring JPA 便捷性的轻量级 ORM 框架。

**🎯 核心特性：**
- **智能方法名解析** - 支持 JPA 风格的方法名自动生成查询
- **灵活 SQL 支持** - 集成 Freemarker 模板引擎，支持动态 SQL
- **高性能设计** - 内置多级缓存，优化查询性能
- **代码生成工具** - 自动生成 Entity 和 Dao 代码
- **Spring Boot 集成** - 开箱即用的 Spring Boot Starter

**🔍 支持的查询方式：**
```java
// JPA 风格方法名查询
User findByName(String name);
List<User> findByNameLike(String name);
List<User> findByAgeGtOrderByNameAsc(Integer age);

// 复杂分页查询（需要对应 SQL 文件）
Pageable<User> pageForUserList(Map<String, Object> params, PageableRequest request);
```

**🚀 快速开始：**
```xml
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-jdbc-spring-boot-starter</artifactId>
    <version>3.1.0</version>
</dependency>
```

### 3. 📋 [Jujube Parent](./jujube-parent) - 统一依赖管理

Maven 父 POM，为整个框架提供统一的依赖管理和构建规范。

**🎯 核心职责：**
- **统一依赖管理** - 管理 200+ 第三方库版本，确保依赖一致性
- **标准化构建** - 提供统一的 Maven 插件配置和构建流程
- **版本控制** - 集中管理 Jujube 系列模块的版本号
- **质量保证** - 配置代码质量检查和测试标准

**🛠️ 主要依赖管理：**
- Spring Boot 3.4.5
- Apache Commons 系列
- Apache POI 5.3.0
- Jackson、Lombok 等主流库

**🚀 快速开始：**
```xml
<parent>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-parent</artifactId>
    <version>3.1.0</version>
</parent>
```

## 🚀 快速开始

### 📋 环境要求

- **Java 21+** - 采用现代 Java 特性
- **Maven 3.5.0+** - 项目构建工具
- **Spring Boot 3.x** - 推荐的应用框架

### 🏗️ 项目搭建

#### 1. 创建项目结构
```xml
<parent>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-parent</artifactId>
    <version>3.1.0</version>
</parent>

<dependencies>
    <!-- 核心工具库 -->
    <dependency>
        <groupId>cn.xuanyuanli</groupId>
        <artifactId>jujube-core-all</artifactId>
    </dependency>
    
    <!-- ORM 框架 -->
    <dependency>
        <groupId>cn.xuanyuanli</groupId>
        <artifactId>jujube-jdbc-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

#### 2. 配置应用
```properties
# 数据源配置
spring.datasource.url=jdbc:h2:mem:test
spring.datasource.driver-class-name=org.h2.Driver

# Jujube JDBC 配置
jujube.jdbc.base-package=com.example.dao
jujube.jdbc.sql-base-package=dao-sql
```

#### 3. 开发示例

**实体类：**
```java
public class User {
    private Long id;
    private String name;
    private Integer age;
    private LocalDateTime createTime;
    
    // getters and setters...
}
```

**数据访问层：**
```java
public interface UserDao extends BaseDao<User, Long> {
    
    @Override
    default String getTableName() {
        return "user";
    }
    
    // JPA 风格查询
    User findByName(String name);
    List<User> findByNameLike(String name);
    
    // 分页查询
    Pageable<User> pageForUserList(Map<String, Object> params, PageableRequest request);
}
```

**业务层：**
```java
@Service
public class UserService {
    
    @Autowired
    private UserDao userDao;
    
    public void example() {
        // 使用核心工具库
        String id = SnowFlakes.nextIdStr();
        Date date = Dates.parseDate("2024-01-01");
        
        // 创建用户
        User user = new User();
        user.setName("张三");
        user.setCreateTime(LocalDateTime.now());
        userDao.save(user);
        
        // JPA 风格查询
        User found = userDao.findByName("张三");
        
        // 分页查询
        Map<String, Object> params = Map.of("name", "张");
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<User> page = userDao.pageForUserList(params, request);
    }
}
```

## 💡 设计理念

### 🎯 解决的核心问题

| 传统痛点 | Jujube Framework 解决方案 |
|----------|------------------------|
| **工具类分散** | 统一的核心工具库，模块化设计 |
| **ORM 选择困难** | 融合 JPA 便捷性与 MyBatis 灵活性 |
| **依赖版本冲突** | 统一的依赖管理，版本兼容性保证 |
| **企业级功能缺失** | 内置企业级功能，开箱即用 |
| **学习成本高** | 简单一致的 API 设计 |

### 🏗️ 架构设计原则

- **约定大于配置** - 遵循最佳实践，减少配置复杂度
- **模块化设计** - 功能模块独立，支持按需引入
- **向后兼容** - 保持 API 稳定性，渐进式升级
- **性能优先** - 内置缓存和优化策略
- **企业级标准** - 符合企业应用开发规范

## 📊 性能特性

### ⚡ 核心工具库性能
- **Bean 拷贝** - 基于 Javassist 的高性能拷贝，比反射快 10+ 倍
- **日期处理** - 优化的日期格式化，比 SimpleDateFormat 快 5+ 倍
- **JSON 序列化** - 基于 Jackson 的高性能 JSON 处理
- **雪花ID生成** - 高并发场景下的分布式 ID 生成

### 🗄️ ORM 框架性能
- **多级缓存** - SQL 模板、方法解析、查询结果多级缓存
- **智能优化** - 避免 `SELECT *`，支持字段选择优化
- **批量操作** - 支持批量插入和更新操作
- **连接池优化** - 合理的数据库连接池配置

## 🧪 测试与质量

### 测试覆盖
- **单元测试覆盖率 > 80%** - 保证代码质量
- **集成测试** - 完整的功能测试用例
- **性能基准测试** - JMH 基准测试验证性能

### 代码质量
- **静态代码分析** - 遵循 Google Java Style Guide
- **自动化CI/CD** - GitHub Actions 自动构建和发布
- **版本管理** - 语义化版本规范

## 📋 版本说明

### 当前版本：3.1.0

**🆕 新特性：**
- 全面升级到 Java 21
- Spring Boot 3.4.5 支持
- 模块化架构重构
- 性能优化提升

**🔄 版本历史：**
- **3.1.0** - 模块化架构，按需引入支持
- **3.0.0** - Java 21 升级，全面性能优化  
- **2.x** - 包结构调整，功能扩展

## 📚 文档和示例

### 完整文档
- 📖 [Jujube Core 文档](./jujube-core/README.md)
- 📖 [Jujube JDBC 文档](./jujube-jdbc/README.md)
- 📖 [Jujube Parent 文档](./jujube-parent/README.md)

### 示例项目
- 🔍 [JDBC 使用示例](./jujube-jdbc/jujube-jdbc-sample)
- 🛠️ [代码生成示例](./jujube-jdbc/entity-generator)

### 在线资源
- 📦 [Maven Central](https://search.maven.org/search?q=g:cn.xuanyuanli)
- 🐛 [问题反馈](https://github.com/xuanyuanli/jujube-framework/issues)
- 💬 [讨论区](https://github.com/xuanyuanli/jujube-framework/discussions)

## 🤝 贡献指南

我们欢迎社区贡献！请查看每个子项目的贡献指南：

### 参与方式
1. **Fork** 相应的子项目
2. **创建分支** - `git checkout -b feature/your-feature`
3. **提交代码** - `git commit -m 'Add some feature'`
4. **推送分支** - `git push origin feature/your-feature`
5. **创建 PR** - 提交 Pull Request

### 代码规范
- 遵循 Google Java Style Guide
- 添加适当的单元测试
- 更新相关文档
- 保持代码简洁明了

## 🆘 支持与社区

### 获取帮助
- 📚 查看各子项目的详细文档
- 🐛 提交 [Issue](https://github.com/xuanyuanli/jujube-framework/issues)
- 💬 参与 [Discussions](https://github.com/xuanyuanli/jujube-framework/discussions)

### 联系方式
- 📧 **邮箱**：xuanyuanli999@gmail.com
- 🔗 **GitHub**：[@xuanyuanli](https://github.com/xuanyuanli)

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

## 🙏 致谢

感谢以下优秀的开源项目为 Jujube Framework 提供基础支持：

- Spring Framework & Spring Boot
- Apache Commons 系列
- Jackson JSON 处理器
- Apache POI
- 以及其他众多优秀的开源项目

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！

**Jujube Framework - 让 Java 开发更简单、更高效！** 🚀