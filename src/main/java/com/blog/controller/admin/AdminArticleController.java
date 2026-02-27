package com.blog.controller.admin;

import com.blog.common.response.PageResult;
import com.blog.common.response.Result;
import com.blog.model.dto.article.ArticleDetailResponse;
import com.blog.model.dto.article.ArticleListResponse;
import com.blog.model.dto.article.ArticleSaveRequest;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理API控制器
 */
@Tag(name = "文章管理接口", description = "文章的创建、编辑、删除等管理功能")
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "获取文章管理列表", description = "分页获取所有文章（包括草稿和已删除），支持搜索和筛选")
    @GetMapping
    public Result<PageResult<ArticleListResponse>> getArticleListForAdmin(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "搜索关键词（标题/摘要）") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "状态（0=已删除, 1=已发布, 2=草稿）") @RequestParam(required = false) Integer status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResult<ArticleListResponse> result = articleService.getArticleListForAdmin(pageable, keyword, categoryId, status);
        return Result.success(result);
    }

    @Operation(summary = "获取文章详情", description = "获取单篇文章的详细信息（包括草稿和已删除）")
    @GetMapping("/{id}")
    public Result<ArticleDetailResponse> getArticle(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        ArticleDetailResponse article = articleService.getArticleDetailForAdmin(id);
        return Result.success(article);
    }

    @Operation(summary = "创建文章", description = "创建新文章")
    @PostMapping
    public Result<ArticleDetailResponse> createArticle(@Valid @RequestBody ArticleSaveRequest request) {
        ArticleDetailResponse article = articleService.createArticle(request);
        return Result.success(article);
    }

    @Operation(summary = "更新文章", description = "更新已有文章")
    @PutMapping("/{id}")
    public Result<ArticleDetailResponse> updateArticle(
            @Parameter(description = "文章ID") @PathVariable Long id,
            @Valid @RequestBody ArticleSaveRequest request) {
        ArticleDetailResponse article = articleService.updateArticle(id, request);
        return Result.success(article);
    }

    @Operation(summary = "删除文章", description = "软删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success(null);
    }

    @Operation(summary = "置顶/取消置顶文章", description = "切换文章的置顶状态")
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        articleService.toggleTop(id);
        return Result.success(null);
    }
}
