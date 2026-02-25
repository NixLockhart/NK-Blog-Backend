package com.blog.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章响应DTO（列表用）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章响应（列表）")
public class ArticleListResponse {

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "浏览量")
    private Long views;

    @Schema(description = "点赞数")
    private Integer likes;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "是否置顶")
    private Integer isTop;

    @Schema(description = "状态: 1=已发布, 2=草稿")
    private Integer status;

    @Schema(description = "发布时间")
    private LocalDateTime publishedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
