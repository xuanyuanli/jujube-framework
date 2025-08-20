#!/bin/bash

# Jujube Framework 版本批量更新脚本 (Bash)
# 作者: Jujube Framework Team
# 描述: 自动读取当前parent版本，批量更新所有模块版本，支持回滚功能

set -e  # 遇到错误立即退出

# 获取脚本所在目录的父目录作为项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# 创建备份目录（使用系统临时目录）
BACKUP_DIR="${TMPDIR:-/tmp}/jujube-version-backups"
mkdir -p "$BACKUP_DIR"

echo "项目根目录: $PROJECT_ROOT"

# 检查参数
OPERATION=${1:-"update"}
if [ "$OPERATION" = "--help" ] || [ "$OPERATION" = "-h" ]; then
    echo "用法: $0 [update|rollback] [version]"
    echo "  update   - 更新版本 (默认)"
    echo "  rollback - 回滚到指定版本"
    echo "示例:"
    echo "  $0                    # 交互式更新版本"
    echo "  $0 update 3.1.1      # 直接更新到3.1.1"
    echo "  $0 rollback 3.1.0    # 回滚到3.1.0"
    exit 0
fi

# 读取parent pom.xml获取当前版本
PARENT_POM="$PROJECT_ROOT/jujube-parent/pom.xml"
if [ ! -f "$PARENT_POM" ]; then
    echo "错误: 找不到parent pom.xml文件: $PARENT_POM"
    exit 1
fi

# 从parent pom.xml提取当前版本
CURRENT_VERSION=$(grep -A2 '<artifactId>jujube-parent</artifactId>' "$PARENT_POM" | grep '<version>' | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | tr -d ' ')

if [ -z "$CURRENT_VERSION" ]; then
    echo "错误: 无法从parent pom.xml中提取版本号"
    exit 1
fi

echo "当前parent版本: $CURRENT_VERSION"

# 根据操作类型处理版本号
if [ "$OPERATION" = "rollback" ]; then
    if [ -n "$2" ]; then
        NEW_VERSION="$2"
    else
        # 显示可用的备份版本
        echo "可回滚的版本:"
        if [ -d "$BACKUP_DIR" ] && [ "$(ls -A "$BACKUP_DIR" 2>/dev/null)" ]; then
            ls "$BACKUP_DIR" | sed 's/^/  /'
        else
            echo "  暂无可回滚的版本"
            exit 1
        fi
        
        while true; do
            read -p "请输入要回滚到的版本号: " NEW_VERSION
            if [ -n "$NEW_VERSION" ] && [ -d "$BACKUP_DIR/$NEW_VERSION" ]; then
                break
            elif [ -n "$NEW_VERSION" ]; then
                echo "版本 $NEW_VERSION 的备份不存在，请重新输入"
            else
                echo "版本号不能为空，请重新输入"
            fi
        done
    fi
    
    echo "准备从 $CURRENT_VERSION 回滚到 $NEW_VERSION"
else
    # 更新操作
    if [ -n "$2" ]; then
        NEW_VERSION="$2"
    else
        while true; do
            read -p "请输入新版本号: " NEW_VERSION
            if [ -n "$NEW_VERSION" ]; then
                break
            fi
            echo "版本号不能为空，请重新输入"
        done
    fi
    
    echo "准备将版本从 $CURRENT_VERSION 更新到 $NEW_VERSION"
fi

# 确认操作
echo
read -p "是否继续? (Y/n): " CONFIRM
CONFIRM=${CONFIRM:-y}
if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "操作已取消"
    exit 0
fi

echo
if [ "$OPERATION" = "rollback" ]; then
    echo "开始回滚版本..."
    
    # 检查备份是否存在
    if [ ! -d "$BACKUP_DIR/$NEW_VERSION" ]; then
        echo "错误: 版本 $NEW_VERSION 的备份不存在"
        exit 1
    fi
    
    # 恢复文件
    echo "正在恢复文件..."
    cp -r "$BACKUP_DIR/$NEW_VERSION"/* "$PROJECT_ROOT/"
    
    echo "版本回滚完成!"
    echo "已从 $CURRENT_VERSION 回滚到 $NEW_VERSION"
    exit 0
else
    echo "开始批量更新版本..."
    
    # 创建当前版本的备份
    CURRENT_BACKUP_DIR="$BACKUP_DIR/$CURRENT_VERSION"
    # 如果备份目录已存在则先删除，确保备份的完整性和安全性
    if [ -d "$CURRENT_BACKUP_DIR" ]; then
        echo "删除已存在的备份目录 $CURRENT_VERSION..."
        rm -rf "$CURRENT_BACKUP_DIR"
    fi
    
    echo "正在备份当前版本 $CURRENT_VERSION..."
    mkdir -p "$CURRENT_BACKUP_DIR"
    
    # 备份所有 pom.xml 文件
    find "$PROJECT_ROOT" -name "pom.xml" -type f | while read file; do
        REL_PATH=$(realpath --relative-to="$PROJECT_ROOT" "$file")
        BACKUP_FILE="$CURRENT_BACKUP_DIR/$REL_PATH"
        mkdir -p "$(dirname "$BACKUP_FILE")"
        cp "$file" "$BACKUP_FILE"
    done
    
    # 备份 gradle 文件
    GRADLE_FILE="$PROJECT_ROOT/jujube-idea-plugin/build.gradle.kts"
    if [ -f "$GRADLE_FILE" ]; then
        BACKUP_GRADLE="$CURRENT_BACKUP_DIR/jujube-idea-plugin/build.gradle.kts"
        mkdir -p "$(dirname "$BACKUP_GRADLE")"
        cp "$GRADLE_FILE" "$BACKUP_GRADLE"
    fi
    
    # 备份所有 README.md 文件
    README_FILES=$(find "$PROJECT_ROOT" -name "README.md" -type f)
    for readme_file in $README_FILES; do
        REL_PATH=$(realpath --relative-to="$PROJECT_ROOT" "$readme_file" 2>/dev/null || echo "$readme_file")
        BACKUP_README="$CURRENT_BACKUP_DIR/$REL_PATH"
        mkdir -p "$(dirname "$BACKUP_README")"
        cp "$readme_file" "$BACKUP_README"
    done
    
    echo "✓ 备份完成: $CURRENT_BACKUP_DIR"
    echo "备份位置: $CURRENT_BACKUP_DIR"
fi

# 转义版本号中的点号，避免正则表达式问题  
ESCAPED_CURRENT_VERSION=$(echo "$CURRENT_VERSION" | sed 's/\./\\\./g')

# 更新计数器
UPDATED_FILES=0

# 更新所有 pom.xml 文件
POM_FILES=$(find "$PROJECT_ROOT" -name "pom.xml" -type f)
for file in $POM_FILES; do
    # 创建临时文件
    TEMP_FILE="${file}.tmp"
    
    # 复制原文件到临时文件
    cp "$file" "$TEMP_FILE"
    
    # 更新parent版本引用
    sed -i "s|<groupId>cn\.xuanyuanli</groupId>[[:space:]]*<artifactId>jujube-parent</artifactId>[[:space:]]*<version>$ESCAPED_CURRENT_VERSION</version>|<groupId>cn.xuanyuanli</groupId>\n        <artifactId>jujube-parent</artifactId>\n        <version>$NEW_VERSION</version>|g" "$TEMP_FILE"
    
    # 更新parent段中的版本号
    sed -i "/<parent>/,/<\/parent>/ { /<groupId>cn\.xuanyuanli<\/groupId>/{N;N; s|<groupId>cn\.xuanyuanli</groupId>[[:space:]]*<artifactId>jujube-parent</artifactId>[[:space:]]*<version>$ESCAPED_CURRENT_VERSION</version>|<groupId>cn.xuanyuanli</groupId>\n        <artifactId>jujube-parent</artifactId>\n        <version>$NEW_VERSION</version>|g; } }" "$TEMP_FILE"
    
    # 更新jujube相关依赖版本
    sed -i "s|<groupId>cn\.xuanyuanli</groupId>[[:space:]]*<artifactId>jujube-\([^<]*\)</artifactId>[[:space:]]*<version>$ESCAPED_CURRENT_VERSION</version>|<groupId>cn.xuanyuanli</groupId>\n                <artifactId>jujube-\1</artifactId>\n                <version>$NEW_VERSION</version>|g" "$TEMP_FILE"
    
    # 更新当前项目版本（在parent后的version标签）
    sed -i "/^[[:space:]]*<\/parent>/,/^[[:space:]]*<version>/ { s|<version>$ESCAPED_CURRENT_VERSION</version>|<version>$NEW_VERSION</version>|g; }" "$TEMP_FILE"
    
    # 检查是否有变化
    if ! diff -q "$file" "$TEMP_FILE" > /dev/null 2>&1; then
        mv "$TEMP_FILE" "$file"
        REL_PATH=$(realpath --relative-to="$PROJECT_ROOT" "$file")
        echo "✓ 已更新: $REL_PATH"
        UPDATED_FILES=$((UPDATED_FILES + 1))
    else
        rm "$TEMP_FILE"
    fi
done

# 更新 Gradle 文件
GRADLE_FILE="$PROJECT_ROOT/jujube-idea-plugin/build.gradle.kts"
if [ -f "$GRADLE_FILE" ]; then
    # 备份原文件
    cp "$GRADLE_FILE" "$GRADLE_FILE.bak"
    
    # 更新gradle文件中的版本
    sed -i "s|cn\.xuanyuanli:jujube-jdbc:$ESCAPED_CURRENT_VERSION|cn.xuanyuanli:jujube-jdbc:$NEW_VERSION|g" "$GRADLE_FILE"
    
    # 检查是否有变化
    if ! diff -q "$GRADLE_FILE" "$GRADLE_FILE.bak" > /dev/null; then
        echo "✓ 已更新: jujube-idea-plugin/build.gradle.kts"
        UPDATED_FILES=$((UPDATED_FILES + 1))
        rm "$GRADLE_FILE.bak"
    else
        mv "$GRADLE_FILE.bak" "$GRADLE_FILE"
    fi
fi

# 更新所有 README.md 文件
README_FILES=$(find "$PROJECT_ROOT" -name "README.md" -type f)
for readme_file in $README_FILES; do
    # 创建临时文件
    TEMP_README="${readme_file}.tmp"
    
    # 复制原文件到临时文件
    cp "$readme_file" "$TEMP_README"
    
    # 更新 README.md 中的版本号
    # 更新所有 <version> 标签中的版本号
    sed -i "s|<version>$ESCAPED_CURRENT_VERSION</version>|<version>$NEW_VERSION</version>|g" "$TEMP_README"
    # 更新版本说明部分的当前版本号  
    sed -i "s|### 当前版本：$ESCAPED_CURRENT_VERSION|### 当前版本：$NEW_VERSION|g" "$TEMP_README"
    sed -i "s|当前版本：$ESCAPED_CURRENT_VERSION|当前版本：$NEW_VERSION|g" "$TEMP_README"
    # 注意：不更新版本历史部分，因为那是记录过去版本的历史信息
    
    # 检查是否有变化
    if ! diff -q "$readme_file" "$TEMP_README" > /dev/null 2>&1; then
        mv "$TEMP_README" "$readme_file"
        REL_PATH=$(realpath --relative-to="$PROJECT_ROOT" "$readme_file" 2>/dev/null || echo "$readme_file")
        echo "✓ 已更新: $REL_PATH"
        UPDATED_FILES=$((UPDATED_FILES + 1))
    else
        # 确保删除临时文件
        rm -f "$TEMP_README"
    fi
done

# 清理可能遗留的临时文件
find "$PROJECT_ROOT" -name "*.tmp" -type f -delete 2>/dev/null || true

echo
echo "版本更新完成!"
echo "共更新了 $UPDATED_FILES 个文件"
echo "从版本 $CURRENT_VERSION 更新到 $NEW_VERSION"

# 提示下一步操作
echo
echo "建议的下一步操作:"
echo "1. 检查更新结果: git diff"
echo "2. 测试构建: mvn clean compile"
echo "3. 提交更改: git add . && git commit -m 'chore: update version to $NEW_VERSION'"
echo
echo "回滚操作:"
echo "如需回滚到当前版本，请运行: $0 rollback $CURRENT_VERSION"