# Redis缓存设计模式

Redis作为高性能的内存数据库，广泛应用于缓存场景。本文介绍常见的Redis缓存设计模式和最佳实践。

## 缓存模式

### 1. Cache-Aside（旁路缓存）

最常用的缓存模式：

```java
public User getUser(Long id) {
    // 1. 尝试从缓存获取
    String cacheKey = "user:" + id;
    User user = redisTemplate.opsForValue().get(cacheKey);

    if (user != null) {
        return user;
    }

    // 2. 缓存未命中，从数据库加载
    user = userRepository.findById(id).orElse(null);

    if (user != null) {
        // 3. 写入缓存
        redisTemplate.opsForValue().set(
            cacheKey, user, 30, TimeUnit.MINUTES);
    }

    return user;
}

public void updateUser(User user) {
    // 1. 更新数据库
    userRepository.save(user);

    // 2. 删除缓存
    String cacheKey = "user:" + user.getId();
    redisTemplate.delete(cacheKey);
}
```

### 2. Read-Through / Write-Through

由缓存层自动管理：

```java
@Cacheable(value = "users", key = "#id")
public User getUser(Long id) {
    return userRepository.findById(id).orElse(null);
}

@CachePut(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}

@CacheEvict(value = "users", key = "#id")
public void deleteUser(Long id) {
    userRepository.deleteById(id);
}
```

## 常见问题及解决方案

### 缓存穿透

**问题**: 查询不存在的数据，每次都会访问数据库。

**解决方案1**: 缓存空值

```java
public User getUser(Long id) {
    User user = redisTemplate.opsForValue().get("user:" + id);

    if (user != null) {
        return user.getId() != null ? user : null;
    }

    user = userRepository.findById(id).orElse(new User());

    // 即使为空也缓存（设置较短过期时间）
    redisTemplate.opsForValue().set(
        "user:" + id, user, 5, TimeUnit.MINUTES);

    return user.getId() != null ? user : null;
}
```

**解决方案2**: 布隆过滤器

```java
@Autowired
private RedisBloomFilter bloomFilter;

public User getUser(Long id) {
    // 快速判断是否可能存在
    if (!bloomFilter.mightContain("user:" + id)) {
        return null;
    }

    // 正常缓存逻辑
    return getUserFromCacheOrDb(id);
}
```

### 缓存雪崩

**问题**: 大量缓存同时过期，数据库压力激增。

**解决方案**: 过期时间加随机值

```java
public void cacheUser(User user) {
    int baseExpire = 30 * 60; // 30分钟
    int randomExpire = new Random().nextInt(5 * 60); // 0-5分钟

    redisTemplate.opsForValue().set(
        "user:" + user.getId(),
        user,
        baseExpire + randomExpire,
        TimeUnit.SECONDS
    );
}
```

### 缓存击穿

**问题**: 热点数据过期时，大量请求同时访问数据库。

**解决方案**: 互斥锁

```java
public User getUser(Long id) {
    String cacheKey = "user:" + id;
    String lockKey = "lock:user:" + id;

    // 1. 尝试获取缓存
    User user = redisTemplate.opsForValue().get(cacheKey);
    if (user != null) {
        return user;
    }

    // 2. 尝试获取锁
    Boolean acquired = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);

    if (Boolean.TRUE.equals(acquired)) {
        try {
            // 3. 双重检查
            user = redisTemplate.opsForValue().get(cacheKey);
            if (user != null) {
                return user;
            }

            // 4. 加载数据
            user = userRepository.findById(id).orElse(null);

            if (user != null) {
                redisTemplate.opsForValue().set(
                    cacheKey, user, 30, TimeUnit.MINUTES);
            }

            return user;
        } finally {
            redisTemplate.delete(lockKey);
        }
    } else {
        // 5. 等待后重试
        Thread.sleep(50);
        return getUser(id);
    }
}
```

## 数据结构应用

### String - 简单值

```java
// 计数器
redisTemplate.opsForValue().increment("page:views");

// 分布式锁
Boolean acquired = redisTemplate.opsForValue()
    .setIfAbsent("lock:key", "value", 30, TimeUnit.SECONDS);
```

### Hash - 对象存储

```java
// 存储用户信息
Map<String, String> userMap = new HashMap<>();
userMap.put("name", user.getName());
userMap.put("email", user.getEmail());
redisTemplate.opsForHash().putAll("user:" + id, userMap);

// 获取单个字段
String email = (String) redisTemplate.opsForHash()
    .get("user:" + id, "email");
```

### List - 队列/栈

```java
// 消息队列
redisTemplate.opsForList().rightPush("queue:tasks", task);
Task task = redisTemplate.opsForList().leftPop("queue:tasks");

// 最新文章列表
redisTemplate.opsForList().leftPush("articles:latest", article);
List<Article> latest = redisTemplate.opsForList()
    .range("articles:latest", 0, 9);
```

### Set - 唯一集合

```java
// 点赞用户集合
redisTemplate.opsForSet().add("article:123:likes", userId);
Boolean liked = redisTemplate.opsForSet()
    .isMember("article:123:likes", userId);
Long likeCount = redisTemplate.opsForSet()
    .size("article:123:likes");
```

### ZSet - 排行榜

```java
// 文章热度排行
redisTemplate.opsForZSet()
    .incrementScore("articles:hot", articleId, 1);

// 获取Top 10
Set<ZSetOperations.TypedTuple<String>> top10 =
    redisTemplate.opsForZSet()
        .reverseRangeWithScores("articles:hot", 0, 9);
```

## 最佳实践

1. **合理设置过期时间**
2. **避免大key**
3. **使用Pipeline批量操作**
4. **监控缓存命中率**
5. **定期清理过期数据**

## 总结

Redis缓存能够显著提升系统性能，但需要注意：
- 选择合适的缓存模式
- 处理好缓存一致性
- 防范常见问题
- 合理使用数据结构

掌握这些模式，你就能设计出高效可靠的缓存系统！
