@echo off
REM 星光小栈后端构建脚本
REM 自动从 git tag 提取版本号，用于 JAR 命名
REM 用法: build.bat [maven参数...]
REM 示例: build.bat -P production
REM        build.bat -P dev

for /f "delims=" %%i in ('git describe --tags --abbrev^=0 2^>nul') do set VERSION=%%i
if not defined VERSION set VERSION=unknown

echo 构建版本: %VERSION%

mvn clean package -Dmaven.test.skip=true -Dgit.closest.tag.name="%VERSION%" %*
