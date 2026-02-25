package com.blog.aspect;

import com.blog.exception.BusinessException;
import com.blog.util.IpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 防止恶意请求
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redisTemplate;

    // 评论限流：同一IP每分钟最多3次
    private static final int COMMENT_LIMIT = 3;
    private static final int COMMENT_WINDOW = 60; // 秒

    // 留言限流：同一IP每分钟最多3次
    private static final int MESSAGE_LIMIT = 3;
    private static final int MESSAGE_WINDOW = 60; // 秒

    // 登录限流：同一IP每小时最多10次
    private static final int LOGIN_LIMIT = 10;
    private static final int LOGIN_WINDOW = 3600; // 秒

    /**
     * 评论接口限流
     */
    @Pointcut("execution(* com.blog.controller.api.CommentController.createComment(..))")
    public void commentRateLimitPointcut() {
    }

    /**
     * 留言接口限流
     */
    @Pointcut("execution(* com.blog.controller.api.MessageController.createMessage(..))")
    public void messageRateLimitPointcut() {
    }

    /**
     * 登录接口限流
     */
    @Pointcut("execution(* com.blog.controller.auth.AuthController.login(..))")
    public void loginRateLimitPointcut() {
    }

    @Around("commentRateLimitPointcut()")
    public Object limitComment(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getClientIp();
        String key = "rate_limit:comment:" + ip;
        checkRateLimit(key, COMMENT_LIMIT, COMMENT_WINDOW, "评论");
        return joinPoint.proceed();
    }

    @Around("messageRateLimitPointcut()")
    public Object limitMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getClientIp();
        String key = "rate_limit:message:" + ip;
        checkRateLimit(key, MESSAGE_LIMIT, MESSAGE_WINDOW, "留言");
        return joinPoint.proceed();
    }

    @Around("loginRateLimitPointcut()")
    public Object limitLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        String ip = getClientIp();
        String key = "rate_limit:login:" + ip;
        checkRateLimit(key, LOGIN_LIMIT, LOGIN_WINDOW, "登录");
        return joinPoint.proceed();
    }

    /**
     * 检查限流（原子操作）
     */
    private void checkRateLimit(String key, int limit, int windowSeconds, String operation) {
        // INCR 是原子操作，对不存在的 key 返回 1
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return;
        }

        if (count == 1) {
            // 首次请求，设置过期时间
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        if (count > limit) {
            log.warn("Rate limit exceeded for {}: key={}, count={}", operation, key, count);
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return IpUtil.getIpAddress(request);
        }
        return "unknown";
    }
}
