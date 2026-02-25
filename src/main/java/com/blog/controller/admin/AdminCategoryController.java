package com.blog.controller.admin;

import com.blog.common.response.Result;
import com.blog.model.dto.category.CategoryResponse;
import com.blog.model.dto.category.CategorySaveRequest;
import com.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理API控制器
 */
@Tag(name = "分类管理接口", description = "分类的创建、编辑、删除等管理功能")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取分类管理列表", description = "获取所有分类（按排序权重降序）")
    @GetMapping
    public Result<List<CategoryResponse>> getAllCategoriesForAdmin() {
        List<CategoryResponse> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "创建分类", description = "创建新分类")
    @PostMapping
    public Result<Long> createCategory(@Valid @RequestBody CategorySaveRequest request) {
        Long categoryId = categoryService.createCategory(request);
        return Result.success(categoryId);
    }

    @Operation(summary = "更新分类", description = "更新已有分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Valid @RequestBody CategorySaveRequest request) {
        categoryService.updateCategory(id, request);
        return Result.success(null);
    }

    @Operation(summary = "删除分类", description = "删除分类，可选择是否同时删除分类下的文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable Long id,
            @Parameter(description = "是否删除分类下的文章") @RequestParam(defaultValue = "false") boolean deleteArticles) {
        categoryService.deleteCategory(id, deleteArticles);
        return Result.success(null);
    }

    @Operation(summary = "调整分类排序", description = "批量更新分类的排序权重")
    @PutMapping("/sort")
    public Result<Void> updateCategoriesSort(
            @Parameter(description = "分类ID列表（按期望顺序排列）") @RequestBody List<Long> categoryIds) {
        categoryService.updateCategoriesSort(categoryIds);
        return Result.success(null);
    }
}
