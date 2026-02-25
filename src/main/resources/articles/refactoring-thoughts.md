# 关于代码重构的一些思考

最近主导了一个老项目的重构工作，积累了一些经验和教训，分享给大家。

## 为什么要重构？

重构的常见原因：

1. **代码难以维护**: 每次修改都胆战心惊
2. **性能问题**: 响应慢，用户体验差
3. **技术债务**: 使用过时的技术栈
4. **扩展困难**: 新需求难以实现
5. **团队效率低**: 开发速度越来越慢

## 重构前的准备

### 1. 评估必要性

不是所有代码都需要重构。问自己：

- 重构能解决什么问题？
- 投入产出比如何？
- 有没有更好的方案？

### 2. 获得支持

- 说服老板/团队
- 制定详细计划
- 评估风险和收益
- 设定明确目标

### 3. 准备工作

```java
// 1. 完善测试用例
@Test
public void testUserService() {
    // 确保重构后功能不变
}

// 2. 梳理依赖关系
// 3. 制定迁移计划
// 4. 准备回滚方案
```

## 重构策略

### 渐进式重构

**推荐方式**：逐步替换，降低风险

```
Old System                New System
    ↓                         ↓
[Module A] ─────────────→ [Module A']
[Module B]                [Module B']
[Module C]                [Module C']
```

步骤：
1. 新老系统并行运行
2. 逐个模块迁移
3. 验证功能
4. 切换流量
5. 下线老系统

### 大爆炸重构

**高风险**：一次性全部重写

⚠️ 不推荐，除非：
- 系统很小
- 有充足测试
- 能承受失败风险

## 重构实践

### 1. 提取方法

**重构前：**

```java
public void processOrder(Order order) {
    // 验证订单
    if (order.getItems().isEmpty()) {
        throw new IllegalArgumentException("订单为空");
    }
    if (order.getTotalAmount() <= 0) {
        throw new IllegalArgumentException("金额无效");
    }

    // 计算折扣
    double discount = 0;
    if (order.getTotalAmount() > 1000) {
        discount = order.getTotalAmount() * 0.1;
    } else if (order.getTotalAmount() > 500) {
        discount = order.getTotalAmount() * 0.05;
    }

    // 保存订单
    order.setDiscount(discount);
    orderRepository.save(order);

    // 发送通知
    emailService.sendOrderConfirmation(order);
    smsService.sendOrderNotification(order);
}
```

**重构后：**

```java
public void processOrder(Order order) {
    validateOrder(order);
    double discount = calculateDiscount(order);
    order.setDiscount(discount);
    saveOrder(order);
    sendNotifications(order);
}

private void validateOrder(Order order) {
    if (order.getItems().isEmpty()) {
        throw new IllegalArgumentException("订单为空");
    }
    if (order.getTotalAmount() <= 0) {
        throw new IllegalArgumentException("金额无效");
    }
}

private double calculateDiscount(Order order) {
    if (order.getTotalAmount() > 1000) {
        return order.getTotalAmount() * 0.1;
    } else if (order.getTotalAmount() > 500) {
        return order.getTotalAmount() * 0.05;
    }
    return 0;
}

private void sendNotifications(Order order) {
    emailService.sendOrderConfirmation(order);
    smsService.sendOrderNotification(order);
}
```

### 2. 提取类

将大类拆分为职责单一的小类：

```java
// 重构前：一个类做太多事
public class UserService {
    public void register() { }
    public void login() { }
    public void updateProfile() { }
    public void sendEmail() { }
    public void validateEmail() { }
    public void encryptPassword() { }
}

// 重构后：职责分离
public class UserService {
    private final AuthService authService;
    private final EmailService emailService;
    private final ProfileService profileService;
}
```

### 3. 使用设计模式

**策略模式替换 if-else：**

```java
// 重构前
public double calculatePrice(String memberType, double amount) {
    if ("VIP".equals(memberType)) {
        return amount * 0.8;
    } else if ("GOLD".equals(memberType)) {
        return amount * 0.9;
    } else {
        return amount;
    }
}

// 重构后
interface DiscountStrategy {
    double calculate(double amount);
}

public class PriceCalculator {
    private Map<String, DiscountStrategy> strategies;

    public double calculatePrice(String memberType, double amount) {
        return strategies.get(memberType).calculate(amount);
    }
}
```

## 常见陷阱

### 1. 过度设计

❌ 不要为了设计模式而设计模式

```java
// 简单功能不需要复杂设计
public interface UserFactory {
    User createUser();
}

public class SimpleUserFactory implements UserFactory {
    public User createUser() {
        return new User();
    }
}

// 直接用就行
public class UserService {
    public User createUser() {
        return new User();
    }
}
```

### 2. 一次改太多

✅ 小步快跑，频繁提交

```bash
# 好的实践
git commit -m "重构：提取validateOrder方法"
git commit -m "重构：提取calculateDiscount方法"

# 不好的实践
git commit -m "重构整个OrderService"
```

### 3. 忽略测试

⚠️ 没有测试的重构是耍流氓

```java
// 重构前后都要有测试
@Test
public void testProcessOrder() {
    Order order = createTestOrder();
    orderService.processOrder(order);
    verify(emailService).sendOrderConfirmation(order);
}
```

## 经验总结

### 成功经验

1. **充分的测试覆盖**
2. **小步迭代，快速验证**
3. **保持代码审查**
4. **及时沟通协调**
5. **做好文档记录**

### 血泪教训

1. 没有测试就开始重构 → 改出了很多bug
2. 一次改动过大 → 难以定位问题
3. 没有充分评估风险 → 项目延期
4. 团队沟通不足 → 重复工作
5. 忽视性能测试 → 上线后性能下降

## 建议

给想要重构的朋友几点建议：

1. **评估必要性**: 不要为了重构而重构
2. **制定计划**: 明确目标和步骤
3. **小步前进**: 降低风险，快速反馈
4. **保证质量**: 测试先行，代码审查
5. **持续改进**: 重构是持续的过程

## 结语

重构是技术人的必修课。好的重构能让代码焕发新生，差的重构可能带来灾难。

谨慎决策，认真执行，持续改进。愿你的重构之路顺利！

---

欢迎在评论区分享你的重构经验！
