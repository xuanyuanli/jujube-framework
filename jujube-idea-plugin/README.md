# Jujube IDEA Plugin

[![Version](https://img.shields.io/badge/version-2025.1.1-blue.svg)](https://github.com/jujube-framework/jujube-idea-plugin)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ-2024.1.7+-orange.svg)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-21-green.svg)](https://openjdk.java.net/)

Jujube Framework 的官方 IntelliJ IDEA 插件，为基于 Jujube Framework 的项目开发提供强大的智能提示、代码导航和快速修复功能。

## 功能特性

### 🔍 智能导航
- **Dao 方法跳转**: 支持从 Dao 接口方法快速跳转到对应的 SQL 文件（Ctrl+B / Ctrl+Alt+B）
- **SQL 反向跳转**: 从 SQL 文件快速跳转到对应的 Dao 接口方法
- **定位搜索**: 支持类和方法的快速定位搜索

### 🔧 代码生成
- **SQL 生成 PO**: 根据 SQL 语句自动生成 PO（Plain Old Java Object）类
- **Map 转 Bean**: 快速将 Map 操作转换为 Bean 操作
- **JPA 方法注释**: 快速为 JPA 方法添加标准注释

### 🔍 代码检查
- **Dao JPA 方法检验**: 检验 Dao 接口中 JPA 方法命名规范
- **Pojos 映射检验**: 检验 POJO 类的映射配置
- **SQL 变量检验**: 检验 SQL 方法中的变量名规范

### 🛠️ 重构支持
- **方法重命名**: 重命名 Dao 方法时，自动同步更新对应的 SQL 文件
- **安全删除**: 删除 Dao 方法时，自动删除对应的 SQL 方法

### 📝 辅助工具
- **Markdown 目录优化**: 自动优化 Markdown 文件的目录编号
- **任务输出控制台**: 提供专用的任务输出窗口

## 安装

1. 打开 IntelliJ IDEA
2. 进入 **File** > **Settings** > **Plugins**
3. 点击 **Marketplace** 标签
4. 搜索 "Jujube-Ext"
5. 点击 **Install** 安装插件
6. 重启 IDE

或者从 [JetBrains Plugin Repository](https://plugins.jetbrains.com/plugin/cn.xuanyuanli.jujube-idea-plugin) 下载安装。

## 使用方法

### 代码导航
- 在 Dao 接口方法上按 `Ctrl+B` 或 `Ctrl+Alt+B` 跳转到对应的 SQL 文件
- 在 SQL 文件中点击行号旁的图标跳转到对应的 Dao 方法

### 代码生成
- 在 SQL 文件中右键选择 **"根据SQL生成PO"**
- 在 Map 操作代码中右键选择 **"转换Map为Bean"**
- 在 Dao 方法中右键选择 **"快捷添加Jpa方法注释"**

### 代码检查
插件会自动检查以下内容并提供修复建议：
- Dao 接口中 JPA 方法的命名规范
- POJO 类的映射配置问题
- SQL 文件中的变量名规范

## 兼容性

- **IntelliJ IDEA**: 2024.1.7+（支持 Community 和 Ultimate 版本）
- **Java 版本**: 17+
- **支持的语言**: Java, SQL, Freemarker

## 依赖插件

- Database Tools and SQL
- FreeMarker Support
- Markdown

## 开发构建

### 环境要求
- Java 21
- Gradle 8.0+

### 构建步骤
```bash
# 克隆项目
git clone https://github.com/jujube-framework/jujube-idea-plugin.git
cd jujube-idea-plugin

# 构建插件
./gradlew buildPlugin

# 运行测试
./gradlew test

# 在开发环境中运行
./gradlew runIde
```

构建完成后，插件包位于 `build/distributions/` 目录下。

### Gradle 输出乱码解决
在 `gradlew.bat` 中修改如下行：
```bash
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m" "-Dfile.encoding=UTF-8"
```

## 问题反馈

如果您在使用过程中遇到问题或有功能建议，请通过以下方式反馈：

- [GitHub Issues](https://github.com/jujube-framework/jujube-idea-plugin/issues)
- 邮箱：xuanyuanli999@gmail.com

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

## 更新日志
### v2025.1.1
- 多语言支持（中英文）

### v2025.1.0
- 支持 IntelliJ IDEA 2024.1.7+
- 优化代码跳转性能
- 增强 SQL 变量检验功能
- 修复已知问题
