package com.blog.service;

import com.blog.common.response.PageResult;
import com.blog.model.dto.article.ArticleDetailResponse;
import com.blog.model.dto.article.ArticleListResponse;
import com.blog.model.dto.article.ArticleSaveRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 获取文章列表（公开，分页）
     */
    PageResult<ArticleListResponse> getArticleList(Long categoryId, String keyword, Pageable pageable);

    /**
     * 获取文章详情（公开）
     */
    ArticleDetailResponse getArticleDetail(Long id);

    /**
     * 获取置顶文章
     */
    List<ArticleListResponse> getTopArticles();

    /**
     * 获取热门文章
     */
    List<ArticleListResponse> getHotArticles(int limit);

    /**
     * 获取最新文章
     */
    List<ArticleListResponse> getLatestArticles(int limit);

    /**
     * 增加浏览量
     */
    void incrementViews(Long id);

    /**
     * 增加点赞数
     */
    void incrementLikes(Long id);

    /**
     * 获取文章列表（管理端，分页，支持搜索和筛选）
     */
    PageResult<ArticleListResponse> getArticleListForAdmin(Pageable pageable, String keyword, Long categoryId, Integer status);

    /**
     * 获取文章详情（管理端，不限制状态）
     */
    ArticleDetailResponse getArticleDetailForAdmin(Long id);

    /**
     * 创建文章
     */
    ArticleDetailResponse createArticle(ArticleSaveRequest request);

    /**
     * 更新文章
     */
    ArticleDetailResponse updateArticle(Long id, ArticleSaveRequest request);

    /**
     * 删除文章（软删除）
     */
    void deleteArticle(Long id);

    /**
     * 恢复已删除的文章（变为草稿）
     */
    void restoreArticle(Long id);

    /**
     * 永久删除文章（级联删除评论、访问日志、文件）
     */
    void permanentlyDeleteArticle(Long id);

    /**
     * 置顶/取消置顶文章
     */
    void toggleTop(Long id);
}
