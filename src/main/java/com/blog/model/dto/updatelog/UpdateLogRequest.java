package com.blog.model.dto.updatelog;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新日志请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新日志请求")
public class UpdateLogRequest {

    @Schema(description = "版本号", example = "v2.0.1")
    @NotBlank(message = "版本号不能为空")
    private String version;

    @Schema(description = "更新标题", example = "优化性能和修复已知问题")
    private String title;

    @Schema(description = "更新内容（Markdown格式）")
    @NotBlank(message = "更新内容不能为空")
    private String content;

    @Schema(description = "是否为重大更新: 0=否, 1=是", example = "0")
    @NotNull(message = "是否重大更新不能为空")
    private Integer isMajor;

    @Schema(description = "发布日期", example = "2025-11-20T10:00:00")
    @NotNull(message = "发布日期不能为空")
    private LocalDateTime releaseDate;
}
