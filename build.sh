#!/bin/bash
# 星光小栈后端构建脚本
# 自动从 git tag 提取版本号，用于 JAR 命名
# 用法: ./build.sh [maven参数...]
# 示例: ./build.sh -P production
#        ./build.sh -P dev

VERSION=$(git describe --tags --abbrev=0 2>/dev/null || echo "unknown")
echo "构建版本: $VERSION"

mvn clean package -Dmaven.test.skip=true -Dgit.closest.tag.name="$VERSION" "$@"
