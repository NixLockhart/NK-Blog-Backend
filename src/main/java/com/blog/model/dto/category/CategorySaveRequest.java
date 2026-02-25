package com.blog.model.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建/更新分类请求DTO
 */
@Data
@Schema(description = "创建/更新分类请求")
public class CategorySaveRequest {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    @Schema(description = "分类名称", example = "技术")
    private String name;

    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    @Schema(description = "分类描述", example = "技术相关文章")
    private String description;

    @Schema(description = "排序权重（越大越靠前）", example = "0")
    private Integer sortOrder = 0;

    @Size(max = 100, message = "分类别名长度不能超过100个字符")
    @Schema(description = "分类别名（URL友好）", example = "tech-articles")
    private String slug;
}
