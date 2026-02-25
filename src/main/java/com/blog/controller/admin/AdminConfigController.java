package com.blog.controller.admin;

import com.blog.common.response.Result;
import com.blog.model.dto.config.ConfigCreateRequest;
import com.blog.model.dto.config.ConfigUpdateRequest;
import com.blog.model.dto.config.SiteConfigResponse;
import com.blog.service.ConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网站配置管理控制器
 */
@Slf4j
@Tag(name = "网站配置管理", description = "网站配置管理接口(需要管理员权限)")
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class AdminConfigController {

    private final ConfigService configService;

    @Operation(summary = "获取所有配置", description = "获取所有网站配置项")
    @GetMapping
    public Result<List<SiteConfigResponse>> getAllConfig() {
        log.info("管理员获取所有配置");
        List<SiteConfigResponse> configs = configService.getAllConfig();
        return Result.success(configs);
    }

    @Operation(summary = "获取单个配置值", description = "根据配置键获取配置值")
    @GetMapping("/{key}")
    public Result<String> getConfigValue(@PathVariable String key) {
        log.info("管理员获取配置: {}", key);
        String value = configService.getConfigValue(key);
        return Result.success(value);
    }

    @Operation(summary = "创建新配置", description = "创建新的配置项")
    @PostMapping
    public Result<SiteConfigResponse> createConfig(@Valid @RequestBody ConfigCreateRequest request) {
        log.info("管理员创建配置: {}", request.getConfigKey());
        SiteConfigResponse response = configService.createConfig(request);
        return Result.success(response);
    }

    @Operation(summary = "更新单个配置", description = "更新指定配置项的值")
    @PutMapping("/{key}")
    public Result<Void> updateConfig(@PathVariable String key, @RequestBody String value) {
        log.info("管理员更新配置: {} = {}", key, value);
        configService.updateConfig(key, value);
        return Result.success();
    }

    @Operation(summary = "批量更新配置", description = "批量更新多个配置项")
    @PutMapping("/batch")
    public Result<Void> batchUpdateConfig(@RequestBody ConfigUpdateRequest request) {
        log.info("管理员批量更新配置");
        configService.batchUpdateConfig(request);
        return Result.success();
    }

    @Operation(summary = "删除配置", description = "删除指定的配置项")
    @DeleteMapping("/{key}")
    public Result<Void> deleteConfig(@PathVariable String key) {
        log.info("管理员删除配置: {}", key);
        configService.deleteConfig(key);
        return Result.success();
    }
}
