# Jujube JDBC

[![Maven Central](https://img.shields.io/maven-central/v/cn.xuanyuanli/jujube-jdbc.svg)](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-jdbc)
[![License](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java Version](https://img.shields.io/badge/Java-21+-brightgreen.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)

> 🚀 一款简洁高效的 Java ORM 框架，融合了 MyBatis 的灵活性与 Spring JPA 的便捷性

## 📖 简介

Jujube JDBC 是一款基于 Spring JDBC 的轻量级 ORM 框架，旨在简化 Java 持久层开发。它继承了 Spring JPA 的方法名解析功能，同时保持了 MyBatis 的 SQL 灵活性，让开发者能够用最少的代码实现最强大的功能。

### 🌟 核心特性

- 🔍 **智能方法名解析** - 支持 JPA 风格的方法名自动生成查询
- 📝 **灵活 SQL 支持** - 集成 Freemarker 模板引擎，支持动态 SQL
- 🚀 **高性能设计** - 内置多级缓存，优化查询性能
- 🛠️ **代码生成工具** - 自动生成 Entity 和 Dao 代码
- 📦 **Spring Boot 集成** - 开箱即用的 Spring Boot Starter
- 🔄 **分页支持** - 内置分页功能，支持复杂查询分页
- 🎯 **规格查询** - 支持链式调用构建复杂查询条件

## ✨ 最新动态

- **v3.0.0** - 全面升级至 Java 21 + Spring Boot 3.x，性能优化，架构重构
- **v2.1** - 包结构调整，性能大幅优化，增加 @Column 注解实现表字段对应

## 🚀 快速开始

### 📦 Maven 依赖

在 Spring Boot 项目中添加以下依赖：

```xml
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-jdbc-spring-boot-starter</artifactId>
    <version>3.1.0</version>
</dependency>
```

### ⚙️ 配置

在 `application.properties` 中配置：

```properties
# Dao 接口扫描包路径
jujube.jdbc.base-package=com.example.dao
# SQL 文件存放路径
jujube.jdbc.sql-base-package=dao-sql
```

### 🏗️ 定义实体类

```java
public class User {
    private Long id;
    private String name;
    private Integer age;
    private Long departmentId;
    
    // getters and setters...
}
```

### 🔧 创建 Dao 接口

```java
public interface UserDao extends BaseDao<User, Long> {
    
    @Override
    default String getTableName() {
        return "user";
    }
    
    // JPA 风格方法名查询
    User findByName(String name);
    List<User> findByNameLike(String name);
    List<User> findByIdGtOrderByAgeDesc(Integer id);
    int getCountByNameLike(String name);
    
    // 分页查询（需要对应的 SQL 文件）
    Pageable<User> pageForUserList(Map<String, Object> params, PageableRequest request);
}
```

### 📝 编写 SQL 文件

创建 `resources/dao-sql/UserDao.sql`：

```sql
<@pageForUserList>
SELECT u.* FROM user u 
WHERE 1=1
<#if name?has_content>
  AND u.name LIKE '%${name}%'
</#if>
<#if age?? && age gt 0>
  AND u.age > ${age}
</#if>
ORDER BY u.id DESC
</@pageForUserList>
```

### 🎯 使用示例

```java
@Service
public class UserService {
    
    @Autowired
    private UserDao userDao;
    
    public void example() {
        // 基础 CRUD
        User user = new User();
        user.setName("张三");
        userDao.save(user);
        
        // JPA 风格查询
        User found = userDao.findByName("张三");
        List<User> users = userDao.findByNameLike("%张%");
        
        // 分页查询
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张");
        PageableRequest request = new PageableRequest(1, 10);
        Pageable<User> page = userDao.pageForUserList(params, request);
    }
}
```

## 💡 设计理念

Jujube JDBC 的诞生源于对现有 ORM 框架的思考与改进：

### 🤔 为什么选择造轮子？

| 框架              | 优势         | 不足            |
|-----------------|------------|---------------|
| **Spring JDBC** | 简洁轻量       | 大量手写 SQL，效率低下 |
| **Hibernate**   | 强大的 ORM 映射 | 联合查询性能差，学习成本高 |
| **Spring JPA**  | 方法名解析便捷    | 复杂查询支持不足      |
| **MyBatis**     | SQL 灵活性强   | 缺少 JPA 的便捷性   |
| **QueryDSL**    | 强类型查询      | 需要手动编写查询逻辑    |

### 🎯 设计目标

Jujube JDBC 致力于融合各框架的优势，解决痛点：

- ✅ **继承 Spring JPA 的便捷性** - 方法名自动解析查询
- ✅ **保持 MyBatis 的灵活性** - 支持复杂 SQL 和动态条件
- ✅ **优化查询性能** - 避免 `SELECT *`，支持字段选择
- ✅ **简化开发流程** - 最少的配置，最大的功能

## 📚 功能详解

### 🔍 JPA 风格方法名查询

#### 支持的查询关键字

| 关键字         | 说明      | 示例                                             |
|-------------|---------|------------------------------------------------|
| `And`       | 逻辑与     | `findByNameAndAge(String name, Integer age)`   |
| `Or`        | 逻辑或     | `findByNameOrEmail(String name, String email)` |
| `Like`      | 模糊查询    | `findByNameLike(String name)`                  |
| `NotLike`   | 非模糊查询   | `findByNameNotLike(String name)`               |
| `Between`   | 范围查询    | `findByAgeBetween(Integer min, Integer max)`   |
| `Lt/Lte`    | 小于/小于等于 | `findByAgeLt(Integer age)`                     |
| `Gt/Gte`    | 大于/大于等于 | `findByAgeGt(Integer age)`                     |
| `IsNull`    | 空值查询    | `findByEmailIsNull()`                          |
| `IsNotNull` | 非空查询    | `findByEmailIsNotNull()`                       |
| `In/NotIn`  | 包含/不包含  | `findByIdIn(List<Long> ids)`                   |
| `OrderBy`   | 排序      | `findByAgeOrderByNameAsc()`                    |
| `GroupBy`   | 分组      | `findAllGroupByDepartment()`                   |
| `Limit`     | 限制条数    | `findAllLimit10()`                             |

#### 查询方法示例

```java
public interface UserDao extends BaseDao<User, Long> {
    
    // 基础查询
    User findByName(String name);
    List<User> findByNameLike(String name);
    
    // 复合条件
    List<User> findByNameAndAge(String name, Integer age);
    List<User> findByAgeGtAndDepartmentId(Integer age, Long deptId);
    
    // 排序和限制
    List<User> findByIdGtOrderByAgeDesc(Integer id);
    User findAllOrderByIdLimit1();
    
    // 统计查询
    int getCountByNameLike(String name);
    Long getSumOfAgeByDepartmentId(Long deptId);
    
    // 字段查询
    String findNameById(Long id);
    List<String> findNameByDepartmentId(Long deptId);
}
```

### 📄 Freemarker 动态 SQL

对于复杂查询，可以使用 Freemarker 模板引擎编写动态 SQL：

#### SQL 文件格式规则

- **宏定义**：使用 Freemarker 宏语法 `<@methodName>` 和 `</@methodName>` 包围SQL
- **方法对应**：宏名必须与 Dao 接口中的方法名完全一致
- **Union查询**：使用 `#jujube-union` 分隔多个SQL语句实现联合查询

#### 常用 Freemarker 函数

| 函数             | 说明             | 示例                                                          |
|----------------|----------------|-------------------------------------------------------------|
| `??`           | 判断变量是否存在且非null | `<#if name??> AND name = '${name}' </#if>`                  |
| `?has_content` | 判断字符串/集合是否有内容  | `<#if name?has_content> AND name LIKE '%${name}%' </#if>`   |
| `?join(sep)`   | 连接集合元素         | `<#if ids?has_content> AND id IN (${ids?join(',')}) </#if>` |
| `?size`        | 获取集合/字符串长度     | `<#if users?size gt 0> ... </#if>`                          |
| `?length`      | 获取字符串长度        | `<#if name?length gt 5> ... </#if>`                         |
| `?upper_case`  | 转换为大写          | `${name?upper_case}`                                        |
| `?lower_case`  | 转换为小写          | `${name?lower_case}`                                        |
| `?string`      | 转换为字符串         | `${age?string}`                                             |

#### SQL 文件示例

`resources/dao-sql/UserDao.sql`：

```sql
<@complexQuery>
SELECT 
    u.id,
    u.name,
    u.age,
    d.name as departmentName
FROM user u 
LEFT JOIN department d ON u.department_id = d.id
WHERE 1=1
<#if name?has_content>
    AND u.name LIKE '%${name}%'
</#if>
<#if age?? && age gt 0>
    AND u.age > ${age}
</#if>
<#if departmentIds?has_content>
    AND u.department_id IN (${departmentIds?join(',')})
</#if>
<#if orderBy?has_content>
    ORDER BY ${orderBy}
<#else>
    ORDER BY u.id DESC
</#if>
</@complexQuery>

<@unionQuery>
SELECT 'active' as status, u.* FROM user u WHERE u.active = 1
#jujube-union
SELECT 'inactive' as status, u.* FROM user u WHERE u.active = 0
</@unionQuery>
```

### 📄 分页查询

Jujube JDBC 提供强大的分页功能，**注意：分页查询方法必须有对应的 SQL 文件**：

#### 方法定义

```java
public interface UserDao extends BaseDao<User, Long> {
    
    // 分页查询方法签名固定：Map<String, Object> + PageableRequest
    Pageable<User> pageForUserList(Map<String, Object> params, PageableRequest request);
    
    // 支持自定义返回对象
    Pageable<UserVO> pageForUserVO(Map<String, Object> params, PageableRequest request);
}
```

#### 对应的 SQL 文件

**重要**：每个分页方法都必须在 `resources/dao-sql/` 目录下有对应的 SQL 文件：

`resources/dao-sql/UserDao.sql`：
```sql
<@pageForUserList>
SELECT u.id, u.name, u.age, u.create_time
FROM user u 
WHERE 1=1
<#if name?has_content>
    AND u.name LIKE '%${name}%'
</#if>
<#if minAge?? && minAge gt 0>
    AND u.age >= ${minAge}
</#if>
ORDER BY u.id DESC
</@pageForUserList>

<@pageForUserVO>
SELECT 
    u.id,
    u.name,
    u.age,
    d.name as departmentName
FROM user u 
LEFT JOIN department d ON u.department_id = d.id
WHERE 1=1
<#if keyword?has_content>
    AND (u.name LIKE '%${keyword}%' OR d.name LIKE '%${keyword}%')
</#if>
</@pageForUserVO>
```

#### 使用示例

```java
@Service
public class UserService {
    
    public Pageable<User> searchUsers(String name, Integer minAge, int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("minAge", minAge);
        
        PageableRequest request = new PageableRequest(page, size);
        return userDao.pageForUserList(params, request);
    }
}
```

### 🏗️ Entity 字段映射规则

Jujube JDBC 遵循 **约定大于配置** 原则处理数据库表字段和 Entity 字段的映射：

#### 默认映射规则

- **数据库字段**：采用下划线命名（如：`user_name`, `create_time`）
- **Entity 字段**：采用驼峰命名（如：`userName`, `createTime`）
- **自动转换**：框架自动完成两种命名风格的转换

```java
// 数据库表结构
CREATE TABLE user (
    id BIGINT PRIMARY KEY,
    user_name VARCHAR(50),
    email_address VARCHAR(100),
    create_time DATETIME,
    is_active BOOLEAN
);

// 对应的 Entity（自动映射）
public class User {
    private Long id;           // id -> id
    private String userName;   // user_name -> userName  
    private String emailAddress; // email_address -> emailAddress
    private LocalDateTime createTime; // create_time -> createTime
    private Boolean isActive;  // is_active -> isActive
}
```

#### 特殊映射注解

当需要特殊映射时，使用 `@Column` 注解：

```java
public class User {
    @Column("user_id")        // 映射到 user_id 字段
    private Long id;
    
    @Column("full_name")      // 映射到 full_name 字段
    private String name;
    
    @Column("dept_id")        // 映射到 dept_id 字段
    private Long departmentId;
}
```

### 🛠️ 代码生成器

内置强大的代码生成工具，快速生成 Entity 和 Dao：

```java
public class CodeGenerator {
    
    public static void main(String[] args) {
        Config config = new Config(
            "user",                           // 表名
            "D:\\project\\src\\main\\java",   // 输出目录
            "com.example.entity",             // Entity 包名
            "com.example.dao"                 // Dao 包名
        );
        
        config.setForceCoverEntity(true);    // 覆盖 Entity
        config.setForceCoverDao(false);      // 不覆盖 Dao
        config.setCreateDao(true);           // 生成 Dao
        
        EntityGenerator.generateEntity(config);
    }
}
```

生成的代码示例：

```java
// 生成的 Entity（自动遵循约定大于配置）
public class User {
    private Long id;              // 对应 id 字段
    private String userName;      // 对应 user_name 字段  
    private Integer age;          // 对应 age 字段
    private LocalDateTime createTime; // 对应 create_time 字段
    
    // getters and setters...
}

// 生成的 Dao
public interface UserDao extends BaseDao<User, Long> {
    
    @Override
    default String getTableName() {
        return "user";
    }
}
```

## ⚙️ 配置详解

### 🛠️ 开发环境配置

#### IDE 配置（重要）

由于框架需要读取方法参数名，必须在编译时保留参数信息：

**IDEA 配置**：
- `File` → `Settings` → `Build, Execution, Deployment` → `Compiler` → `Java Compiler`
- 在 `Additional command line parameters` 中添加：`-parameters`

**Maven 配置**：
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### 📦 依赖配置

#### Spring Boot 项目

```xml
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-jdbc-spring-boot-starter</artifactId>
    <version>3.1.0</version>
</dependency>
```

配置文件：
```properties
# 必须配置
jujube.jdbc.base-package=com.example.dao
jujube.jdbc.sql-base-package=dao-sql

# 可选配置
jujube.jdbc.enable-cache=true
jujube.jdbc.show-sql=true
```

#### 非 Spring Boot 项目

```xml
<dependency>
    <groupId>cn.xuanyuanli</groupId>
    <artifactId>jujube-jdbc</artifactId>
    <version>3.1.0</version>
</dependency>
```

Java 配置：
```java
@Configuration
public class JujubeJdbcConfig {
    
    @Bean
    public JujubeJdbcConfiguration jujubeJdbcConfiguration() {
        JujubeJdbcConfiguration config = new JujubeJdbcConfiguration();
        config.setBasePackage("com.example.dao");
        config.setSqlBasePackage("dao-sql");
        return config;
    }
}
```

## 🏗️ 架构设计

### 核心架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Dao Interface │ -> │   DaoProxy      │ -> │ QueryStrategy   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │                        │
                               v                        v
                    ┌─────────────────┐    ┌─────────────────┐
                    │   SqlBuilder    │    │   Handler Chain │
                    └─────────────────┘    └─────────────────┘
                               │                        │
                               v                        v
                    ┌─────────────────┐    ┌─────────────────┐
                    │ BaseDaoSupport  │    │  JdbcTemplate   │
                    └─────────────────┘    └─────────────────┘
```

### 🔄 工作流程

1. **接口扫描** - `ClassPathDaoScanner` 扫描并注册 Dao 接口
2. **动态代理** - `DaoProxy` 拦截方法调用
3. **策略路由** - 根据方法名选择合适的查询策略
4. **SQL 构建** - `SqlBuilder` 构建最终的 SQL 语句  
5. **执行查询** - 通过 `JdbcTemplate` 执行数据库操作

### ⚡ 性能优化

#### 多级缓存机制
- **SQL 模板缓存** - 缓存已解析的 Freemarker 模板
- **正则表达式缓存** - 缓存编译后的 Pattern 对象
- **方法解析缓存** - 缓存方法名解析结果
- **查询结果缓存** - 可选的查询结果缓存

#### 智能优化策略
- **延迟初始化** - 按需创建组件，减少启动时间
- **批量操作** - 支持批量插入和更新操作
- **连接池优化** - 合理配置数据库连接池参数

## 🧪 测试与示例

### 单元测试示例

```java
@SpringBootTest
class UserDaoTest {
    
    @Autowired
    private UserDao userDao;
    
    @Test
    void testFindByName() {
        User user = userDao.findByName("张三");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("张三");
    }
    
    @Test
    void testPageQuery() {
        Map<String, Object> params = Map.of("name", "张");
        PageableRequest request = new PageableRequest(1, 10);
        
        Pageable<User> page = userDao.pageForUserList(params, request);
        
        assertThat(page.getTotalElements()).isGreaterThan(0);
        assertThat(page.getData()).isNotEmpty();
    }
}
```

## 🤝 贡献指南

### 参与贡献

1. **Fork 项目** - 点击右上角 Fork 按钮
2. **创建分支** - `git checkout -b feature/your-feature`
3. **提交代码** - `git commit -m 'Add some feature'`
4. **推送分支** - `git push origin feature/your-feature`
5. **提交 PR** - 创建 Pull Request

### 代码规范

- 遵循 Google Java Style Guide
- 添加适当的单元测试
- 更新相关文档
- 保持代码简洁明了

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

## 🔗 相关链接

- 📚 [详细文档](https://github.com/xuanyuanli/jujube-jdbc)
- 🐛 [问题反馈](https://github.com/xuanyuanli/jujube-jdbc/issues)
- 💬 [讨论区](https://github.com/xuanyuanli/jujube-jdbc/discussions)
- 📦 [Maven Central](https://search.maven.org/search?q=g:cn.xuanyuanli%20AND%20a:jujube-jdbc)

## 👨‍💻 作者

**xuanyuanli** - [xuanyuanli999@gmail.com](mailto:xuanyuanli999@gmail.com)

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！
