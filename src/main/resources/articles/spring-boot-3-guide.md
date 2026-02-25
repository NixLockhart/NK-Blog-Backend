# Spring Boot 3.x 实战指南

Spring Boot 3.0 是一个重要的里程碑版本，它带来了许多激动人心的新特性和改进。本文将深入探讨 Spring Boot 3.x 的核心特性。

## 核心升级

### Java 17 基线

Spring Boot 3.0 要求 Java 17 作为最低版本，这使得我们可以使用许多现代 Java 特性：

```java
// Records 数据类
public record UserDTO(
    Long id,
    String username,
    String email
) {}

// Pattern Matching
if (obj instanceof String str) {
    System.out.println(str.toUpperCase());
}

// Text Blocks
String json = """
    {
        "name": "John",
        "age": 30
    }
    """;
```

### 原生镜像支持

Spring Boot 3.0 提供了对 GraalVM 原生镜像的一流支持：

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
</plugin>
```

编译原生镜像：

```bash
mvn -Pnative native:compile
```

优势：
- 启动速度快（毫秒级）
- 内存占用少
- 适合云原生和无服务器场景

### Jakarta EE 迁移

Spring Boot 3.0 从 Java EE 迁移到了 Jakarta EE：

```java
// 旧版本 (Spring Boot 2.x)
import javax.servlet.http.HttpServletRequest;
import javax.persistence.Entity;

// 新版本 (Spring Boot 3.x)
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Entity;
```

## 新特性详解

### 可观测性增强

Spring Boot 3.0 大幅增强了可观测性支持：

```java
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    @Observed(name = "user.find")
    public User findUser(@PathVariable Long id) {
        // 自动收集指标和追踪
        return userService.findById(id);
    }
}
```

配置 Micrometer：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
  tracing:
    sampling:
      probability: 1.0
```

### HTTP 接口客户端

新的声明式 HTTP 客户端：

```java
@HttpExchange("/api/users")
public interface UserClient {

    @GetExchange("/{id}")
    User getUser(@PathVariable Long id);

    @PostExchange
    User createUser(@RequestBody User user);

    @DeleteExchange("/{id}")
    void deleteUser(@PathVariable Long id);
}
```

配置：

```java
@Configuration
public class ClientConfig {

    @Bean
    UserClient userClient(WebClient.Builder builder) {
        WebClient client = builder
            .baseUrl("https://api.example.com")
            .build();

        HttpServiceProxyFactory factory =
            HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(client))
                .build();

        return factory.createClient(UserClient.class);
    }
}
```

### Problem Details 支持

RFC 7807 标准的错误响应：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException ex) {
        ProblemDetail detail = ProblemDetail
            .forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("User Not Found");
        detail.setProperty("userId", ex.getUserId());
        return detail;
    }
}
```

响应示例：

```json
{
  "type": "about:blank",
  "title": "User Not Found",
  "status": 404,
  "detail": "User with id 123 not found",
  "userId": 123
}
```

## 性能优化

### 虚拟线程支持

配合 Java 21，可以启用虚拟线程：

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

好处：
- 极大提升并发处理能力
- 简化异步编程
- 降低资源消耗

### AOT 处理

Ahead-of-Time 编译优化：

```java
@Configuration
@RegisterReflectionForBinding(User.class)
public class AppConfig {
    // 配置
}
```

## 迁移指南

### 依赖更新

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>
```

### 主要变更

1. **包名替换**: `javax.*` → `jakarta.*`
2. **最低版本**: Java 17+
3. **废弃移除**: 移除了 2.x 中标记为废弃的 API

## 最佳实践

### 1. 使用 Records

```java
public record PageRequest(int page, int size) {
    public PageRequest {
        if (page < 0) throw new IllegalArgumentException();
        if (size < 1 || size > 100) throw new IllegalArgumentException();
    }
}
```

### 2. 充分利用可观测性

```java
@Service
public class OrderService {

    private final MeterRegistry registry;

    public void createOrder(Order order) {
        Timer.Sample sample = Timer.start(registry);
        try {
            // 业务逻辑
            orderRepository.save(order);
            registry.counter("orders.created").increment();
        } finally {
            sample.stop(registry.timer("order.creation.time"));
        }
    }
}
```

### 3. 原生镜像优化

```java
@ImportRuntimeHints(MyRuntimeHints.class)
@SpringBootApplication
public class Application {
    // ...
}

class MyRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // 注册反射、资源等
        hints.reflection()
            .registerType(MyClass.class,
                MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
```

## 总结

Spring Boot 3.0 是一次重大升级，带来了：

- 更现代的 Java 版本支持
- 更好的云原生特性
- 更强大的可观测性
- 更优秀的性能

虽然迁移需要一些工作，但长期来看绝对值得。建议新项目直接使用 Spring Boot 3.x，老项目也应该规划升级。

## 参考资源

- [Spring Boot 3.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Jakarta EE Migration](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#jakarta-ee)
