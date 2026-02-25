package com.blog.controller.admin;

import com.blog.common.response.Result;
import com.blog.model.dto.widget.WidgetCodeResponse;
import com.blog.model.dto.widget.WidgetCreateRequest;
import com.blog.model.dto.widget.WidgetResponse;
import com.blog.model.dto.widget.WidgetUpdateRequest;
import com.blog.service.WidgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小工具管理API控制器
 */
@Tag(name = "小工具管理接口", description = "小工具的增删改查、应用管理等功能")
@RestController
@RequestMapping("/api/admin/widgets")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminWidgetController {

    private final WidgetService widgetService;

    @Operation(summary = "获取所有小工具列表", description = "获取所有小工具列表（包括系统自带和自定义）")
    @GetMapping
    public Result<List<WidgetResponse>> getAllWidgets() {
        List<WidgetResponse> widgets = widgetService.getAllWidgets();
        return Result.success(widgets);
    }

    @Operation(summary = "获取小工具详情", description = "根据ID获取小工具详情")
    @GetMapping("/{id}")
    public Result<WidgetResponse> getWidgetById(
            @Parameter(description = "小工具ID") @PathVariable Long id) {
        WidgetResponse widget = widgetService.getWidgetById(id);
        return Result.success(widget);
    }

    @Operation(summary = "获取小工具代码", description = "获取小工具代码用于编辑")
    @GetMapping("/{id}/code")
    public Result<WidgetCodeResponse> getWidgetCode(
            @Parameter(description = "小工具ID") @PathVariable Long id) {
        WidgetCodeResponse code = widgetService.getWidgetCode(id);
        return Result.success(code);
    }

    @Operation(summary = "创建小工具", description = "创建自定义小工具")
    @PostMapping
    public Result<Long> createWidget(@Valid @RequestBody WidgetCreateRequest request) {
        Long id = widgetService.createWidget(request);
        return Result.success(id);
    }

    @Operation(summary = "更新小工具", description = "更新小工具信息和代码")
    @PutMapping("/{id}")
    public Result<Void> updateWidget(
            @Parameter(description = "小工具ID") @PathVariable Long id,
            @Valid @RequestBody WidgetUpdateRequest request) {
        widgetService.updateWidget(id, request);
        return Result.success(null);
    }

    @Operation(summary = "删除小工具", description = "删除自定义小工具（系统自带不可删除）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteWidget(
            @Parameter(description = "小工具ID") @PathVariable Long id) {
        widgetService.deleteWidget(id);
        return Result.success(null);
    }

    @Operation(summary = "应用/取消应用小工具", description = "切换小工具的应用状态")
    @PutMapping("/{id}/toggle")
    public Result<Void> toggleWidgetApplication(
            @Parameter(description = "小工具ID") @PathVariable Long id,
            @Parameter(description = "是否应用") @RequestParam Boolean isApplied) {
        widgetService.toggleWidgetApplication(id, isApplied);
        return Result.success(null);
    }

    @Operation(summary = "导出小工具代码", description = "导出小工具HTML代码")
    @GetMapping("/{id}/export")
    public Result<String> exportWidget(
            @Parameter(description = "小工具ID") @PathVariable Long id) {
        String code = widgetService.exportWidget(id);
        return Result.success(code);
    }
}
