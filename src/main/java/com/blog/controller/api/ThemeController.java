package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.theme.ThemeResponse;
import com.blog.service.ThemeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 主题API控制器（公开接口）
 */
@Tag(name = "主题接口", description = "博客主题相关接口")
@RestController
@RequestMapping("/api/theme")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @Operation(summary = "获取当前应用的主题", description = "获取当前博客使用的主题信息")
    @GetMapping("/current")
    public Result<ThemeResponse> getCurrentTheme() {
        ThemeResponse theme = themeService.getAppliedTheme();
        return Result.success(theme);
    }
}
