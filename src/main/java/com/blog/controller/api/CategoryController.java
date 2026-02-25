package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.category.CategoryResponse;
import com.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类公开API控制器
 */
@Tag(name = "分类接口（公开）", description = "分类查询等公开接口")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类", description = "获取所有分类列表（按排序权重降序）")
    @GetMapping
    public Result<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取分类详情", description = "根据ID获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
}
