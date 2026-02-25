# MySQL索引优化实战

数据库性能优化中，索引是最重要的一环。本文通过实际案例讲解MySQL索引优化技巧。

## 索引基础

### 索引类型

1. **B+Tree索引** - 最常用
2. **Hash索引** - 等值查询
3. **全文索引** - 文本搜索
4. **空间索引** - 地理数据

### 创建索引

```sql
-- 单列索引
CREATE INDEX idx_email ON users(email);

-- 联合索引
CREATE INDEX idx_name_age ON users(name, age);

-- 唯一索引
CREATE UNIQUE INDEX idx_username ON users(username);
```

## 优化案例

### 案例1：慢查询优化

**问题SQL:**

```sql
SELECT * FROM orders
WHERE status = 'pending'
  AND created_at > '2024-01-01'
ORDER BY created_at DESC
LIMIT 10;
```

**执行计划:**

```
type: ALL
rows: 1000000
Extra: Using where; Using filesort
```

**优化方案:**

```sql
-- 创建联合索引
CREATE INDEX idx_status_created ON orders(status, created_at DESC);

-- 优化后
type: ref
rows: 100
Extra: Using index condition
```

### 案例2：覆盖索引

```sql
-- 创建覆盖索引
CREATE INDEX idx_cover ON users(name, email, age);

-- 查询直接从索引获取数据
SELECT name, email, age
FROM users
WHERE name = 'John';
```

## 索引设计原则

1. **最左前缀原则**
2. **选择性高的列优先**
3. **避免冗余索引**
4. **考虑索引大小**

## 总结

合理使用索引可以大幅提升数据库性能，但也要避免过度索引。
