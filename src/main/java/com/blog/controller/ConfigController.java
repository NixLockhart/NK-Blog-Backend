package com.blog.controller;

import com.blog.common.response.Result;
import com.blog.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 网站配置公开接口控制器
 */
@Slf4j
@Tag(name = "网站配置", description = "网站配置公开接口")
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @Operation(summary = "获取公开配置", description = "获取前台可访问的网站配置信息")
    @GetMapping("/public")
    public Result<Map<String, String>> getPublicConfig() {
        log.info("获取公开配置");
        Map<String, String> config = configService.getPublicConfig();
        return Result.success(config);
    }

    @Operation(summary = "获取网站基本信息", description = "获取网站标题、作者等基本信息")
    @GetMapping("/site-info")
    public Result<Map<String, String>> getSiteInfo() {
        log.info("获取网站基本信息");
        Map<String, String> info = configService.getSiteInfo();
        return Result.success(info);
    }

    @Operation(summary = "获取联系方式配置", description = "获取所有联系方式的配置信息")
    @GetMapping("/contact")
    public Result<Map<String, Map<String, String>>> getContactConfig() {
        log.info("获取联系方式配置");
        Map<String, Map<String, String>> contactConfig = configService.getContactConfig();
        return Result.success(contactConfig);
    }

    @Operation(summary = "获取关注链接配置", description = "获取所有关注链接的配置信息")
    @GetMapping("/link")
    public Result<Map<String, Map<String, String>>> getLinkConfig() {
        log.info("获取关注链接配置");
        Map<String, Map<String, String>> linkConfig = configService.getLinkConfig();
        return Result.success(linkConfig);
    }
}
