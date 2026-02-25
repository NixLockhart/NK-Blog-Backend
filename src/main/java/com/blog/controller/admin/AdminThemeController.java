package com.blog.controller.admin;

import com.blog.common.response.Result;
import com.blog.model.dto.theme.ThemeCreateRequest;
import com.blog.model.dto.theme.ThemeResponse;
import com.blog.model.dto.theme.ThemeUpdateRequest;
import com.blog.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 主题管理API控制器
 */
@Tag(name = "主题管理接口", description = "主题的增删改查、应用管理等功能")
@RestController
@RequestMapping("/api/admin/themes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminThemeController {

    private final ThemeService themeService;

    @Operation(summary = "获取所有主题列表", description = "获取所有主题列表（包括默认主题和自定义主题）")
    @GetMapping
    public Result<List<ThemeResponse>> getAllThemes() {
        List<ThemeResponse> themes = themeService.getAllThemes();
        return Result.success(themes);
    }

    @Operation(summary = "获取主题详情", description = "根据ID获取主题详情")
    @GetMapping("/{id}")
    public Result<ThemeResponse> getThemeById(
            @Parameter(description = "主题ID") @PathVariable Long id) {
        ThemeResponse theme = themeService.getThemeById(id);
        return Result.success(theme);
    }

    @Operation(summary = "创建主题", description = "创建自定义主题（需上传CSS文件）")
    @PostMapping(consumes = "multipart/form-data")
    public Result<Long> createTheme(
            @Parameter(description = "主题名称") @RequestParam String name,
            @Parameter(description = "主题描述") @RequestParam(required = false) String description,
            @Parameter(description = "作者") @RequestParam(required = false) String author,
            @Parameter(description = "版本号") @RequestParam(required = false) String version,
            @Parameter(description = "显示顺序") @RequestParam(required = false) Integer displayOrder,
            @Parameter(description = "封面图片（Base64）") @RequestParam(required = false) String coverImage,
            @Parameter(description = "亮色主题CSS文件") @RequestParam("lightCss") MultipartFile lightCss,
            @Parameter(description = "暗色主题CSS文件") @RequestParam("darkCss") MultipartFile darkCss) {

        ThemeCreateRequest request = new ThemeCreateRequest();
        request.setName(name);
        request.setDescription(description);
        request.setAuthor(author);
        request.setVersion(version);
        request.setDisplayOrder(displayOrder);
        request.setCoverImage(coverImage);

        Long id = themeService.createTheme(request, lightCss, darkCss);
        return Result.success(id);
    }

    @Operation(summary = "更新主题", description = "更新主题信息")
    @PutMapping("/{id}")
    public Result<Void> updateTheme(
            @Parameter(description = "主题ID") @PathVariable Long id,
            @Valid @RequestBody ThemeUpdateRequest request) {
        themeService.updateTheme(id, request);
        return Result.success(null);
    }

    @Operation(summary = "删除主题", description = "删除自定义主题（默认主题不可删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTheme(
            @Parameter(description = "主题ID") @PathVariable Long id) {
        themeService.deleteTheme(id);
        return Result.success(null);
    }

    @Operation(summary = "应用/取消应用主题", description = "切换主题的应用状态")
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleThemeApplication(
            @Parameter(description = "主题ID") @PathVariable Long id,
            @Parameter(description = "是否应用") @RequestParam Boolean isApplied) {
        themeService.toggleThemeApplication(id, isApplied);
        return Result.success(null);
    }

    @Operation(summary = "上传主题封面", description = "上传主题封面图片")
    @PostMapping("/{id}/cover")
    public Result<String> uploadThemeCover(
            @Parameter(description = "主题ID") @PathVariable Long id,
            @Parameter(description = "封面图片文件") @RequestParam("file") MultipartFile file) {
        String coverUrl = themeService.uploadThemeCover(id, file);
        return Result.success(coverUrl);
    }

    @Operation(summary = "导出主题", description = "导出主题为ZIP压缩包")
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportTheme(
            @Parameter(description = "主题ID") @PathVariable Long id) {
        byte[] zipData = themeService.exportTheme(id);

        // 获取主题信息用于文件名
        ThemeResponse theme = themeService.getThemeById(id);
        String filename = theme.getName() + "-" + theme.getSlug() + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipData);
    }

    @Operation(summary = "更新主题文件", description = "重新上传主题CSS文件")
    @PutMapping(value = "/{id}/files", consumes = "multipart/form-data")
    public Result<Void> updateThemeFiles(
            @Parameter(description = "主题ID") @PathVariable Long id,
            @Parameter(description = "亮色主题CSS文件") @RequestParam(value = "lightCss", required = false) MultipartFile lightCss,
            @Parameter(description = "暗色主题CSS文件") @RequestParam(value = "darkCss", required = false) MultipartFile darkCss) {
        themeService.updateThemeFiles(id, lightCss, darkCss);
        return Result.success(null);
    }
}
