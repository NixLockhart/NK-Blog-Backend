package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.config.AppVersionProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Tag(name = "系统接口", description = "系统健康检查和基本信息")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SystemController {

    private final AppVersionProvider appVersionProvider;

    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("服务运行正常");
    }

    @Operation(summary = "系统信息", description = "获取系统基本信息")
    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "星光小栈");
        info.put("version", appVersionProvider.getVersion());
        info.put("description", "个人博客系统");
        info.put("springBootVersion", "3.2.5");
        info.put("javaVersion", System.getProperty("java.version"));
        return Result.success(info);
    }
}
