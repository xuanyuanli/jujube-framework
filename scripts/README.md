# Jujube Framework 版本管理脚本

此目录包含用于批量更新 Jujube Framework 版本号的自动化脚本，支持版本更新和回滚功能。

## 脚本说明
- **适用系统**: Linux, macOS, WSL
- **运行环境**: Bash
- **功能**: 自动读取当前parent版本，批量更新所有模块版本，支持版本回滚

## 使用方法

### 基本用法
```bash
# 切换到项目根目录
cd /path/to/jujube-framework

# 交互式更新版本
bash ./scripts/update-versions.sh

# 直接更新到指定版本
bash ./scripts/update-versions.sh update 3.1.1

# 回滚到指定版本
bash ./scripts/update-versions.sh rollback 3.1.0

# 查看帮助
bash ./scripts/update-versions.sh --help
```

## 脚本功能

### 版本更新功能
1. **自动版本检测**: 从 `jujube-parent/pom.xml` 自动读取当前版本号
2. **交互式输入**: 提示用户输入新版本号或支持命令行参数直接指定
3. **确认机制**: 在执行更新前要求用户确认
4. **精确匹配**: 只更新 `cn.xuanyuanli` 组下的 jujube 相关依赖
5. **多文件支持**: 同时更新 Maven POM 文件和 Gradle 构建文件
6. **进度反馈**: 显示更新进度和结果统计
7. **自动备份**: 更新前自动备份当前版本的所有相关文件

### 版本回滚功能
1. **备份管理**: 自动保存每个版本的备份，支持快速回滚
2. **版本列表**: 显示所有可回滚的版本
3. **安全回滚**: 验证备份存在性后执行回滚操作
4. **完整恢复**: 恢复所有相关的POM和Gradle文件

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
- **版本备份**: 自动创建版本备份，支持完整回滚
- **临时文件**: 使用临时文件处理，确保更新过程安全
- **错误处理**: 包含完整的错误检查和处理机制
- **确认机制**: 执行前需要用户明确确认
- **备份验证**: 回滚前验证备份文件完整性

## 使用示例

### 版本更新示例
```
当前parent版本: 3.1.0
请输入新版本号: 3.1.1

准备将版本从 3.1.0 更新到 3.1.1
是否继续? (y/N): y

开始批量更新版本...
正在备份当前版本 3.1.0...
✓ 备份完成: ./scripts/version-backups/3.1.0
✓ 已更新: jujube-parent\pom.xml
✓ 已更新: jujube-core\pom.xml
✓ 已更新: jujube-jdbc\pom.xml
...

版本更新完成!
共更新了 15 个文件
从版本 3.1.0 更新到 3.1.1

如需回滚到当前版本，请运行: ./scripts/update-versions.sh rollback 3.1.0
```

### 版本回滚示例
```
当前parent版本: 3.1.1
可回滚的版本:
  3.1.0
  3.0.9
请输入要回滚到的版本号: 3.1.0

准备从 3.1.1 回滚到 3.1.0
是否继续? (y/N): y

开始回滚版本...
正在恢复文件...
版本回滚完成!
已从 3.1.1 回滚到 3.1.0
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
- 脚本会自动创建备份，如果发现问题可使用回滚功能快速恢复
- 备份文件存储在系统临时目录下：`${TMPDIR:-/tmp}/jujube-version-backups/`
- 每个版本只会备份一次，重复更新同一版本不会重复备份

## 文件结构

更新脚本会在以下位置创建备份：
```
scripts/
├── update-versions.sh        # 主脚本文件
└── README.md                 # 说明文档

${TMPDIR:-/tmp}/jujube-version-backups/  # 版本备份目录 (系统临时目录)
├── 3.1.0/                   # 版本3.1.0的备份
│   ├── jujube-parent/pom.xml
│   ├── jujube-core/pom.xml
│   └── ...
├── 3.0.9/                   # 版本3.0.9的备份
└── ...
```