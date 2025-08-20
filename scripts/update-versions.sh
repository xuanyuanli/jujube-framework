#!/bin/bash

# Jujube Framework 版本批量更新脚本 (Bash)
# 作者: Jujube Framework Team
# 描述: 自动读取当前parent版本，批量更新所有模块版本

set -e  # 遇到错误立即退出

# 获取脚本所在目录的父目录作为项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "项目根目录: $PROJECT_ROOT"

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

# 提示用户输入新版本号
while true; do
    read -p "请输入新版本号: " NEW_VERSION
    if [ -n "$NEW_VERSION" ]; then
        break
    fi
    echo "版本号不能为空，请重新输入"
done

# 确认操作
echo
echo "准备将版本从 $CURRENT_VERSION 更新到 $NEW_VERSION"
read -p "是否继续? (y/N): " CONFIRM
if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "操作已取消"
    exit 0
fi

echo
echo "开始批量更新版本..."

# 更新计数器
UPDATED_FILES=0

# 更新所有 pom.xml 文件
find "$PROJECT_ROOT" -name "pom.xml" -type f | while read file; do
    # 创建临时文件
    TEMP_FILE="${file}.tmp"
    
    # 使用sed进行替换
    sed "s|<groupId>cn\.xuanyuanli</groupId>\s*<artifactId>jujube-parent</artifactId>\s*<version>$CURRENT_VERSION</version>|<groupId>cn.xuanyuanli</groupId>\n        <artifactId>jujube-parent</artifactId>\n        <version>$NEW_VERSION</version>|g" "$file" > "$TEMP_FILE"
    
    # 更新jujube相关依赖版本
    sed -i "s|<groupId>cn\.xuanyuanli</groupId>\s*<artifactId>jujube-\([^<]*\)</artifactId>\s*<version>$CURRENT_VERSION</version>|<groupId>cn.xuanyuanli</groupId>\n                <artifactId>jujube-\1</artifactId>\n                <version>$NEW_VERSION</version>|g" "$TEMP_FILE"
    
    # 更新当前项目版本（在parent后的version标签）
    sed -i "/^[[:space:]]*<\/parent>/,/^[[:space:]]*<version>/ { s|<version>$CURRENT_VERSION</version>|<version>$NEW_VERSION</version>|g; }" "$TEMP_FILE"
    
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
    sed -i "s|cn\.xuanyuanli:jujube-jdbc:$CURRENT_VERSION|cn.xuanyuanli:jujube-jdbc:$NEW_VERSION|g" "$GRADLE_FILE"
    
    # 检查是否有变化
    if ! diff -q "$GRADLE_FILE" "$GRADLE_FILE.bak" > /dev/null; then
        echo "✓ 已更新: jujube-idea-plugin/build.gradle.kts"
        UPDATED_FILES=$((UPDATED_FILES + 1))
        rm "$GRADLE_FILE.bak"
    else
        mv "$GRADLE_FILE.bak" "$GRADLE_FILE"
    fi
fi

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