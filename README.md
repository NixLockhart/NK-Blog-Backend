# 星光小栈 2.0 - 博客后端

星光小栈个人博客系统的后端部分，基于 Spring Boot 3.x 构建的 RESTful API 服务，是对 [1.0 JSP 版本](https://github.com/NixLockhart/personal_blog) 的完全重构。

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Java](https://img.shields.io/badge/Java-17-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 简介

星光小栈 2.0 后端采用前后端分离架构，通过 RESTful API 为 Vue 3 前端提供数据服务。相比 1.0 版本的 Servlet/JSP 单体应用，2.0 实现了 JWT 无状态认证、Flyway 数据库版本管理、AOP 操作日志与限流、Redis 缓存、WebSocket 实时通知等特性。

## 功能特性

### 核心功能

| 功能 | 说明 |
|------|------|
| **文章管理** | Markdown 文件存储，支持分类、置顶、搜索、浏览量/点赞统计 |
| **评论系统** | 树形嵌套评论（楼中楼），XSS 防御，IP/UA 记录 |
| **留言板** | 访客留言与友情链接管理 |
| **站点配置** | 数据库驱动的动态配置（标题、SEO、联系方式、功能开关等） |
| **主题管理** | 主题 CRUD、封面上传、CSS 文件管理、激活切换 |
| **小工具系统** | 侧边栏小工具（日历、诗词等），支持自定义 HTML 代码上传 |
| **公告管理** | 支持定时展示的公告轮播 |
| **统计系统** | 访问日志记录、每日 UV/PV 统计、仪表板数据聚合 |
| **文件管理** | 图片/封面/头像上传，缩略图生成（Thumbnailator） |
| **更新日志** | 版本更新记录，Markdown 内容渲染 |

### 安全与运维

| 功能 | 说明 |
|------|------|
| **JWT 认证** | 无状态 Token 认证，Spring Security 集成 |
| **接口限流** | 基于 Redis 的 IP 限流（评论 3次/分，留言 3次/分，登录 10次/时） |
| **XSS 防御** | OWASP HTML Sanitizer，严格模式/内容模式双策略 |
| **操作日志** | AOP 自动记录管理员操作（模块、类型、耗时、参数、结果） |
| **访问日志** | AOP 自动记录访客访问，异步写入，同一访客同一天去重 |
| **错误日志** | 全局异常处理，错误信息持久化到数据库 |
| **CORS** | 可配置跨域策略，支持通配符和指定域名 |
| **版本号自动注入** | 基于 Git Tag 自动提取版本号（git-commit-id-maven-plugin） |

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Spring Boot | 3.2.5 |
| 语言 | Java | 17 |
| ORM | Spring Data JPA + Hibernate | - |
| 数据库 | MySQL | 8.0+ |
| 数据库迁移 | Flyway | - |
| 缓存 | Redis + Caffeine 双级缓存 | - |
| 认证 | Spring Security + JWT (jjwt) | 0.12.5 |
| 实时通信 | WebSocket (STOMP + SockJS) | - |
| Markdown | CommonMark (GFM Tables, Heading Anchor) | 0.22.0 |
| 图片处理 | Thumbnailator | 0.4.20 |
| XSS 防御 | OWASP Java HTML Sanitizer | 20240325.1 |
| 对象映射 | MapStruct | 1.5.5 |
| API 文档 | SpringDoc OpenAPI (Swagger UI) | 2.3.0 |
| 构建工具 | Maven | 3.6+ |

## 工程结构

```
blog-backend/
├── src/main/java/com/blog/
│   ├── BlogApplication.java                 # 应用入口
│   ├── aspect/                              # AOP 切面
│   │   ├── OperationLogAspect.java          # 管理员操作日志记录
│   │   ├── RateLimitAspect.java             # Redis 接口限流
│   │   └── VisitLogAspect.java              # 访客访问日志记录
│   ├── common/                              # 公共模块
│   │   ├── enums/                           # 枚举（文章状态、评论状态、错误码等）
│   │   └── response/                        # 统一响应封装（Result、PageResult）
│   ├── config/                              # 配置类
│   │   ├── properties/BlogProperties.java   # 自定义配置属性绑定
│   │   ├── SecurityConfig.java              # Spring Security 过滤链
│   │   ├── WebMvcConfig.java                # CORS、静态资源映射
│   │   ├── WebSocketConfig.java             # WebSocket STOMP 端点
│   │   ├── RedisConfig.java                 # Redis 序列化配置
│   │   ├── JacksonConfig.java               # JSON 序列化配置
│   │   ├── OpenApiConfig.java               # Swagger 文档配置
│   │   └── AppVersionProvider.java          # Git Tag 版本号提供
│   ├── controller/                          # 控制器层
│   │   ├── api/                             # 公开 API（无需认证）
│   │   │   ├── ArticleController.java       # 文章列表/详情/热门/最新/浏览/点赞
│   │   │   ├── CategoryController.java      # 分类列表/详情
│   │   │   ├── CommentController.java       # 评论查询/发表
│   │   │   ├── MessageController.java       # 留言查询/发表/友链
│   │   │   ├── AnnouncementController.java  # 公告/更新日志查询
│   │   │   ├── StatsController.java         # 仪表板统计数据
│   │   │   ├── ThemeController.java         # 当前主题查询
│   │   │   ├── WidgetController.java        # 已应用小工具列表
│   │   │   └── SystemController.java        # 健康检查/系统信息
│   │   ├── admin/                           # 管理 API（需要 JWT 认证）
│   │   │   ├── AdminArticleController.java  # 文章 CRUD、置顶
│   │   │   ├── AdminCategoryController.java # 分类 CRUD、排序
│   │   │   ├── AdminCommentController.java  # 评论审核/删除
│   │   │   ├── AdminMessageController.java  # 留言管理/友链标记
│   │   │   ├── AdminAnnouncementController.java # 公告和更新日志 CRUD
│   │   │   ├── AdminConfigController.java   # 站点配置 CRUD
│   │   │   ├── AdminThemeController.java    # 主题 CRUD、激活、文件管理
│   │   │   ├── AdminWidgetController.java   # 小工具 CRUD、启用/禁用、导出
│   │   │   ├── AdminFileController.java     # 封面/文章图片上传
│   │   │   └── AdminProfileController.java  # 管理员资料/密码修改
│   │   ├── auth/AuthController.java         # 登录/Token 验证
│   │   ├── ConfigController.java            # 公开配置/站点信息/联系方式
│   │   └── FileController.java              # 图片/头像/封面上传
│   ├── exception/                           # 异常处理
│   │   ├── GlobalExceptionHandler.java      # 全局异常捕获
│   │   ├── BusinessException.java           # 业务异常
│   │   └── ResourceNotFoundException.java   # 资源不存在异常
│   ├── mapper/                              # MapStruct 对象映射
│   ├── model/                               # 数据模型
│   │   ├── entity/                          # JPA 实体（15 张表）
│   │   └── dto/                             # 请求/响应 DTO
│   ├── repository/                          # Spring Data JPA 仓库
│   ├── security/                            # 安全模块
│   │   ├── SecurityConfig.java              # 过滤链、授权规则
│   │   ├── CustomUserDetailsService.java    # 用户认证加载
│   │   └── jwt/                             # JWT 组件
│   │       ├── JwtTokenProvider.java        # Token 生成/解析/验证
│   │       └── JwtAuthenticationFilter.java # 请求拦截 Token 校验
│   ├── service/                             # 业务接口
│   │   └── impl/                            # 业务实现
│   │       └── AsyncLogService.java         # 异步日志写入
│   └── util/                                # 工具类
│       ├── HtmlSanitizer.java               # XSS 防御（OWASP）
│       └── IpUtil.java                      # 客户端真实 IP 提取
├── src/main/resources/
│   ├── application.yml                      # 主配置（公共）
│   ├── application-dev.yml                  # 开发环境配置
│   ├── application-prod.yml                 # 生产环境配置（已 gitignore）
│   ├── db/migration/                        # Flyway 迁移脚本
│   │   └── V1__init_database.sql            # 初始化表结构和基础数据
│   ├── articles/                            # 示例 Markdown 文章
│   └── update-logs/                         # 更新日志 Markdown
├── application-prod.yml.example             # 生产配置模板
└── pom.xml                                  # Maven 依赖配置
```

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- Maven 3.6+

### 安装步骤

```bash
# 1. 进入项目目录
cd blog-backend

# 2. 创建数据库（Flyway 会自动初始化表结构）
mysql -u root -p -e "CREATE DATABASE db_nix_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 创建数据存储目录
mkdir -p /var/blog-data    # Linux
# mkdir C:\blog-data       # Windows

# 4. 安装依赖并启动（开发环境）
mvn clean install -DskipTests
mvn spring-boot:run
```

启动后访问 `http://localhost:8080/swagger-ui.html` 查看 API 文档。

### 环境变量配置

生产部署时，复制 `application-prod.yml.example` 为 `src/main/resources/application-prod.yml` 并修改：

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: ${REDIS_HOST}
      password: ${REDIS_PASSWORD}

blog:
  base-url: ${BLOG_BASE_URL}
  data:
    path: ${BLOG_DATA_PATH}
```

或直接通过环境变量注入：

```bash
export DB_PASSWORD=your_password
export JWT_SECRET=your-jwt-secret-key
export BLOG_BASE_URL=https://yourdomain.com
export BLOG_DATA_PATH=/var/blog-data
```

### 构建与部署

```bash
# 构建生产 JAR
mvn clean package -DskipTests -Pproduction

# 运行
java -jar target/blog-backend-1.0.0.jar --spring.profiles.active=prod
```

### 默认账户

```
用户名: admin
密码:   admin123
```

> 部署后请立即通过管理面板修改默认密码。

## API 概览

完整文档启动项目后访问 Swagger UI：`http://localhost:8080/swagger-ui.html`

### 公开接口（无需认证）

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/health` | 健康检查 |
| `GET` | `/api/info` | 系统信息（版本号从 Git Tag 自动提取） |
| `GET` | `/api/articles` | 文章列表（分页、分类筛选、关键词搜索） |
| `GET` | `/api/articles/{id}` | 文章详情 |
| `GET` | `/api/articles/top` | 置顶文章 |
| `GET` | `/api/articles/hot` | 热门文章（按浏览量） |
| `GET` | `/api/articles/latest` | 最新文章 |
| `POST` | `/api/articles/{id}/view` | 增加浏览量 |
| `POST` | `/api/articles/{id}/like` | 点赞 |
| `GET` | `/api/categories` | 分类列表 |
| `GET` | `/api/comments/article/{id}` | 文章评论（树形结构） |
| `POST` | `/api/comments` | 发表评论 |
| `GET` | `/api/messages` | 留言列表（分页） |
| `GET` | `/api/messages/friends` | 友情链接 |
| `POST` | `/api/messages` | 发表留言 |
| `GET` | `/api/announcements/active` | 当前生效公告 |
| `GET` | `/api/update-logs` | 更新日志 |
| `GET` | `/api/config/public` | 公开站点配置 |
| `GET` | `/api/config/site-info` | 站点基本信息 |
| `GET` | `/api/config/contact` | 联系方式 |
| `GET` | `/api/config/link` | 社交媒体链接 |
| `GET` | `/api/stats` | 仪表板统计数据 |
| `GET` | `/api/theme/current` | 当前主题 |
| `GET` | `/api/widgets` | 已应用的小工具列表 |
| `POST` | `/api/files/avatar` | 上传头像 |

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/auth/login` | 管理员登录，返回 JWT Token |
| `GET` | `/api/auth/validate` | 验证 Token 有效性 |

### 管理接口（需要 JWT 认证）

| 模块 | 路径前缀 | 支持操作 |
|------|----------|----------|
| 文章管理 | `/api/admin/articles` | 列表、详情、创建、编辑、删除、置顶 |
| 分类管理 | `/api/admin/categories` | 列表、创建、编辑、删除、排序 |
| 评论管理 | `/api/admin/comments` | 列表、删除、状态变更 |
| 留言管理 | `/api/admin/messages` | 列表、删除、友链标记 |
| 公告管理 | `/api/admin/announcements` | 列表、创建、编辑、删除 |
| 更新日志 | `/api/admin/update-logs` | 列表、创建、编辑、删除 |
| 站点配置 | `/api/admin/config` | 列表、查询、创建、更新、批量更新、删除 |
| 主题管理 | `/api/admin/themes` | 列表、详情、创建、编辑、删除、激活、封面上传、文件管理、导出 |
| 小工具管理 | `/api/admin/widgets` | 列表、详情、代码查看、创建、编辑、删除、启用/禁用、导出 |
| 文件管理 | `/api/admin/files` | 封面上传、文章图片上传 |
| 个人资料 | `/api/admin/profile` | 查看资料、修改资料、修改密码 |

## 数据库设计

共 15 张表，由 Flyway 自动初始化：

| 表名 | 说明 |
|------|------|
| `tb_articles` | 文章（标题、摘要、Markdown 路径、分类、状态、浏览量、点赞） |
| `tb_categories` | 分类（名称、slug、排序、文章计数） |
| `tb_comments` | 评论（树形嵌套、IP/UA 记录） |
| `tb_messages` | 留言与友情链接 |
| `tb_announcements` | 公告（定时展示、启用/禁用） |
| `tb_update_logs` | 版本更新日志 |
| `tb_site_configs` | 站点动态配置（键值对，支持公开/私有） |
| `tb_themes` | 主题（CSS 文件路径、激活状态） |
| `tb_gadgets` | 侧边栏小工具（HTML 代码路径、系统/自定义） |
| `tb_admins` | 管理员（BCrypt 密码加密） |
| `tb_statistics` | 每日访问统计（UV/PV） |
| `tb_visit_logs` | 访问日志明细（访客去重） |
| `tb_operation_logs` | 管理员操作日志 |
| `tb_error_logs` | 系统错误日志 |
| `tb_notifications` | 系统通知 |

## 版本历史

- **V2.1.0** (2026-2)
  - 统一后端文件读取接口为`/files`，移除`/data`接口
  - 新增文章硬删除功能

- **V2.0.6** (2026-2)
  - 修复管理员文章筛选功能无效问题(接口不变)

- **V2.0.5** (2026-2)
  - 提高安全性
  - 整理代码结构

- **V2.0.0** (2025-11)
  - Spring Boot 3.x 全面重构
  - 前后端分离架构，RESTful API
  - Spring Security + JWT 无状态认证
  - Flyway 数据库版本管理
  - AOP 操作日志、访问日志、接口限流
  - Redis + Caffeine 双级缓存
  - WebSocket 实时通知
  - OWASP XSS 防御
  - 主题系统与小工具系统
  - SpringDoc OpenAPI 文档

- **V1.0.0** (2025-06)
  - 基于 Java Servlet/JSP 的传统博客系统
  - 仓库地址：[personal_blog](https://github.com/NixLockhart/personal_blog)

## 许可证

MIT License

## 作者

NixStudio (Nix Lockhart)
