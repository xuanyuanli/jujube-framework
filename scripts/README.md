# Jujube Framework 版本管理脚本

此目录包含用于批量更新 Jujube Framework 版本号的自动化脚本。

## 脚本说明
- **适用系统**: Linux, macOS, WSL
- **运行环境**: Bash
- **功能**: 自动读取当前parent版本，批量更新所有模块版本

## 使用方法
```bash
# 切换到项目根目录
cd /path/to/jujube-framework

# 运行脚本
bash ./scripts/update-versions.sh
```

## 脚本功能

1. **自动版本检测**: 从 `jujube-parent/pom.xml` 自动读取当前版本号
2. **交互式输入**: 提示用户输入新版本号
3. **确认机制**: 在执行更新前要求用户确认
4. **精确匹配**: 只更新 `cn.xuanyuanli` 组下的 jujube 相关依赖
5. **多文件支持**: 同时更新 Maven POM 文件和 Gradle 构建文件
6. **进度反馈**: 显示更新进度和结果统计

## 更新范围

脚本会更新以下文件中的版本号：

### Maven POM 文件
- 所有 `pom.xml` 文件中的 parent 版本引用
- 所有 `pom.xml` 文件中的当前项目版本
- 所有 `cn.xuanyuanli:jujube-*` 依赖的版本

### Gradle 文件
- `jujube-idea-plugin/build.gradle.kts` 中的 jujube-jdbc 依赖版本

## 安全特性

- **精确匹配**: 使用正则表达式精确匹配，避免误更新其他依赖
- **备份机制**: Bash 脚本会创建临时备份文件
- **错误处理**: 包含完整的错误检查和处理机制
- **确认机制**: 执行前需要用户明确确认

## 使用示例

```
当前parent版本: 3.1.0
请输入新版本号: 3.1.1

准备将版本从 3.1.0 更新到 3.1.1
是否继续? (y/N): y

开始批量更新版本...
✓ 已更新: jujube-parent\pom.xml
✓ 已更新: jujube-core\pom.xml
✓ 已更新: jujube-jdbc\pom.xml
...

版本更新完成!
共更新了 15 个文件
从版本 3.1.0 更新到 3.1.1
```

## 后续操作建议

脚本执行完成后，建议按以下步骤验证和提交更改：

1. **检查更新结果**:
   ```bash
   git diff
   ```

2. **测试构建**:
   ```bash
   mvn clean compile
   ```

3. **提交更改**:
   ```bash
   git add .
   git commit -m "chore: update version to x.x.x"
   ```

## 注意事项

- 运行脚本前请确保工作目录干净（没有未提交的更改）
- 建议在更新前创建分支进行测试
- 更新完成后务必进行构建测试验证
- 如果发现问题，可以使用 `git checkout .` 快速恢复