package com.blog.aspect;

import com.blog.service.impl.AsyncLogService;
import com.blog.util.IpUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * 访问日志切面
 * 记录网站访问情况，实现访问量统计（同一访客同一天只计一次）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class VisitLogAspect {

    private final AsyncLogService asyncLogService;

    private static final String VISITOR_ID_COOKIE = "visitor_id";

    /**
     * 文章详情访问
     */
    @Pointcut("execution(* com.blog.controller.api.ArticleController.getArticleDetail(..))")
    public void articleDetailPointcut() {
    }

    /**
     * 首页访问
     */
    @Pointcut("execution(* com.blog.controller.api.ArticleController.getArticleList(..))")
    public void homePagePointcut() {
    }

    /**
     * 记录文章访问
     */
    @Around("articleDetailPointcut()")
    public Object logArticleVisit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            Long articleId = (Long) joinPoint.getArgs()[0];
            dispatchVisitLog(articleId);
        } catch (Exception e) {
            log.error("Failed to record article visit", e);
        }

        return result;
    }

    /**
     * 记录页面访问
     */
    @Around("homePagePointcut()")
    public Object logPageVisit(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            dispatchVisitLog(null);
        } catch (Exception e) {
            log.error("Failed to record page visit", e);
        }

        return result;
    }

    /**
     * 在请求线程中提取信息，然后异步记录
     */
    private void dispatchVisitLog(Long articleId) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        String visitorId = getVisitorId(request);
        String ipAddress = IpUtil.getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String referer = request.getHeader("Referer");
        String pageUrl = request.getRequestURI();

        asyncLogService.recordVisit(articleId, visitorId, ipAddress, userAgent, referer, pageUrl);
    }

    /**
     * 获取访客ID（从Cookie或生成新ID）
     */
    private String getVisitorId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (VISITOR_ID_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return "visitor_" + UUID.randomUUID().toString().replace("-", "");
    }
}
