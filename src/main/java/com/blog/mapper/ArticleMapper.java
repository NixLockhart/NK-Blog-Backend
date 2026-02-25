package com.blog.mapper;

import com.blog.model.dto.article.ArticleDetailResponse;
import com.blog.model.dto.article.ArticleListResponse;
import com.blog.model.dto.article.ArticleSaveRequest;
import com.blog.model.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 文章映射器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ArticleMapper {

    /**
     * 实体转列表响应DTO
     */
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ArticleListResponse toListResponse(Article article);

    /**
     * 实体转详情响应DTO
     * 注意：content, markdownContent, toc 由服务层从文件读取后设置
     */
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "markdownContent", ignore = true)
    @Mapping(target = "toc", ignore = true)
    ArticleDetailResponse toDetailResponse(Article article);

    /**
     * 保存请求DTO转实体
     * 注意：id, contentPath, category, views, likes, commentCount, isTop, 时间字段由服务层处理
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentPath", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "isTop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    Article toEntity(ArticleSaveRequest request);

    /**
     * 更新实体
     * 注意：保留统计数据和时间戳
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contentPath", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "likes", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "isTop", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    void updateEntity(ArticleSaveRequest request, @MappingTarget Article article);
}
