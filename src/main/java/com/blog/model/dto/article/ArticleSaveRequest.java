package com.blog.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建/更新文章请求DTO
 */
@Data
@Schema(description = "创建/更新文章请求")
public class ArticleSaveRequest {

    @Size(max = 200, message = "标题长度不能超过200个字符")
    @Schema(description = "文章标题（可选，为空时自动填充为未命名文章）", example = "Spring Boot 入门教程")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    @Schema(description = "文章内容（Markdown格式）")
    private String content;

    @Schema(description = "分类ID（可选，自动保存时可为空）", example = "1")
    private Long categoryId;

    @Size(max = 500, message = "摘要长度不能超过500个字符")
    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "封面图URL")
    private String coverImage;

    @NotNull(message = "状态不能为空")
    @Schema(description = "状态: 1=发布, 2=草稿", example = "1")
    private Integer status;
}
