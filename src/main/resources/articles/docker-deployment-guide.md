# Docker容器化部署完全指南

Docker已经成为现代应用部署的标准工具。本文将带你从零开始学习Docker容器化部署。

## Docker基础

### 核心概念

- **镜像(Image)**: 应用的只读模板
- **容器(Container)**: 镜像的运行实例
- **仓库(Registry)**: 存储镜像的地方

### 安装Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | bash

# 启动Docker
sudo systemctl start docker
sudo systemctl enable docker
```

## Dockerfile 编写

### 后端应用示例

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 前端应用示例

```dockerfile
FROM node:18-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
```

## Docker Compose

### 完整应用栈

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: blog
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build: ./blog-backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/blog
      SPRING_REDIS_HOST: redis

  frontend:
    build: ./blog-frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql-data:
```

## 常用命令

```bash
# 构建镜像
docker build -t myapp:latest .

# 运行容器
docker run -d -p 8080:8080 --name myapp myapp:latest

# 查看容器
docker ps

# 查看日志
docker logs -f myapp

# 进入容器
docker exec -it myapp /bin/bash

# 停止容器
docker stop myapp

# 删除容器
docker rm myapp
```

## 生产部署

### 多阶段构建

```dockerfile
# 构建阶段
FROM maven:3.9-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 健康检查

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

## 优化建议

1. **使用.dockerignore**
2. **最小化层数**
3. **使用Alpine镜像**
4. **利用缓存机制**
5. **安全扫描镜像**

## 总结

Docker极大简化了应用部署流程。掌握Docker是现代开发者的必备技能。
