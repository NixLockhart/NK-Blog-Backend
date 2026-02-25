package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.widget.WidgetResponse;
import com.blog.service.WidgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 小工具API控制器（前端展示）
 */
@Tag(name = "小工具接口", description = "前端展示已应用的小工具")
@RestController
@RequestMapping("/api/widgets")
@RequiredArgsConstructor
public class WidgetController {

    private final WidgetService widgetService;

    @Operation(summary = "获取已应用的小工具列表", description = "获取所有已应用到博客的小工具")
    @GetMapping
    public Result<List<WidgetResponse>> getAppliedWidgets() {
        List<WidgetResponse> widgets = widgetService.getAppliedWidgets();
        return Result.success(widgets);
    }
}
