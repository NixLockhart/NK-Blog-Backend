package com.blog.aspect;

import com.blog.model.entity.OperationLog;
import com.blog.repository.OperationLogRepository;
import com.blog.service.impl.AsyncLogService;
import com.blog.util.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

/**
 * 操作日志切面
 * 记录所有管理员的操作
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogRepository operationLogRepository;
    private final ObjectMapper objectMapper;
    private final AsyncLogService asyncLogService;

    /**
     * 定义切点：所有管理API（/api/admin/**）
     */
    @Pointcut("execution(* com.blog.controller.admin..*(..))")
    public void adminOperationPointcut() {
    }

    /**
     * 环绕通知：记录操作日志
     */
    @Around("adminOperationPointcut()")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String operator = "anonymous";
        if (authentication != null && authentication.getName() != null) {
            operator = authentication.getName();
        }

        // 创建操作日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setOperator(operator);

        // 设置请求信息
        if (request != null) {
            operationLog.setRequestMethod(request.getMethod());
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setIpAddress(IpUtil.getIpAddress(request));
            operationLog.setUserAgent(request.getHeader("User-Agent"));

            // 获取请求参数
            try {
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    // 过滤掉HttpServletRequest、HttpServletResponse、MultipartFile等
                    StringBuilder params = new StringBuilder();
                    for (Object arg : args) {
                        if (arg != null &&
                            !(arg instanceof HttpServletRequest) &&
                            !(arg instanceof HttpServletResponse) &&
                            !(arg instanceof MultipartFile)) {
                            params.append(objectMapper.writeValueAsString(arg)).append(";");
                        } else if (arg instanceof MultipartFile) {
                            // 对于文件上传，只记录文件名和大小
                            MultipartFile file = (MultipartFile) arg;
                            params.append("{file: '")
                                  .append(file.getOriginalFilename())
                                  .append("', size: ")
                                  .append(file.getSize())
                                  .append("};");
                        }
                    }
                    if (params.length() > 0) {
                        params.setLength(params.length() - 1); // 移除最后的分号
                    }
                    operationLog.setRequestParams(params.toString());
                }
            } catch (Exception e) {
                log.warn("Failed to serialize request params: {}", e.getMessage());
            }
        }

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        // 解析模块和操作类型
        String module = parseModule(className);
        String action = parseAction(methodName, request != null ? request.getMethod() : "");

        operationLog.setModule(module);
        operationLog.setOperationType(action);
        operationLog.setOperationDetail(String.format("%s - %s", module, action));

        // 执行目标方法
        Object result = null;
        try {
            result = joinPoint.proceed();
            operationLog.setResult(1); // 成功
        } catch (Exception e) {
            operationLog.setResult(0); // 失败
            operationLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            // 记录执行时长
            long executionTime = System.currentTimeMillis() - startTime;
            operationLog.setExecutionTime(executionTime);

            // 异步保存日志
            asyncLogService.saveOperationLog(operationLog);
        }

        return result;
    }

    /**
     * 解析模块名称
     */
    private String parseModule(String className) {
        if (className.contains("Article")) {
            return "article";
        } else if (className.contains("Category")) {
            return "category";
        } else if (className.contains("Comment")) {
            return "comment";
        } else if (className.contains("Message")) {
            return "message";
        } else if (className.contains("Config")) {
            return "config";
        } else if (className.contains("Announcement")) {
            return "announcement";
        } else if (className.contains("UpdateLog")) {
            return "update_log";
        } else if (className.contains("Theme")) {
            return "theme";
        } else if (className.contains("Admin")) {
            return "admin";
        } else {
            return "system";
        }
    }

    /**
     * 解析操作类型
     */
    private String parseAction(String methodName, String httpMethod) {
        if (methodName.startsWith("create") || methodName.startsWith("add") || "POST".equals(httpMethod)) {
            return "create";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit") || "PUT".equals(httpMethod)) {
            return "update";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove") || "DELETE".equals(httpMethod)) {
            return "delete";
        } else if (methodName.startsWith("get") || methodName.startsWith("list") || methodName.startsWith("query") || "GET".equals(httpMethod)) {
            return "query";
        } else {
            return "other";
        }
    }
}
