package com.blog.controller.api;

import com.blog.common.response.PageResult;
import com.blog.common.response.Result;
import com.blog.model.dto.article.ArticleDetailResponse;
import com.blog.model.dto.article.ArticleListResponse;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章公开API控制器
 */
@Tag(name = "文章接口（公开）", description = "文章查询、浏览等公开接口")
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "获取文章列表", description = "分页获取已发布的文章列表，支持分类筛选和关键词搜索")
    @GetMapping
    public Result<PageResult<ArticleListResponse>> getArticleList(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResult<ArticleListResponse> result = articleService.getArticleList(categoryId, keyword, pageable);
        return Result.success(result);
    }

    @Operation(summary = "获取文章详情", description = "根据ID获取已发布文章的详细信息")
    @GetMapping("/{id}")
    public Result<ArticleDetailResponse> getArticleDetail(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        ArticleDetailResponse response = articleService.getArticleDetail(id);
        return Result.success(response);
    }

    @Operation(summary = "获取置顶文章", description = "获取所有置顶的文章")
    @GetMapping("/top")
    public Result<List<ArticleListResponse>> getTopArticles() {
        List<ArticleListResponse> articles = articleService.getTopArticles();
        return Result.success(articles);
    }

    @Operation(summary = "获取热门文章", description = "根据浏览量获取热门文章")
    @GetMapping("/hot")
    public Result<List<ArticleListResponse>> getHotArticles(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        List<ArticleListResponse> articles = articleService.getHotArticles(limit);
        return Result.success(articles);
    }

    @Operation(summary = "获取最新文章", description = "根据发布时间获取最新文章")
    @GetMapping("/latest")
    public Result<List<ArticleListResponse>> getLatestArticles(
            @Parameter(description = "数量限制") @RequestParam(defaultValue = "10") int limit) {
        List<ArticleListResponse> articles = articleService.getLatestArticles(limit);
        return Result.success(articles);
    }

    @Operation(summary = "增加文章浏览量", description = "访问文章时调用，增加浏览量计数")
    @PostMapping("/{id}/view")
    public Result<Void> incrementViews(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        articleService.incrementViews(id);
        return Result.success(null);
    }

    @Operation(summary = "点赞文章", description = "为文章点赞")
    @PostMapping("/{id}/like")
    public Result<Void> incrementLikes(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        articleService.incrementLikes(id);
        return Result.success(null);
    }
}
