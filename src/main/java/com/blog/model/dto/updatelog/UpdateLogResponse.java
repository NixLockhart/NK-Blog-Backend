package com.blog.model.dto.updatelog;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 更新日志响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新日志响应")
public class UpdateLogResponse {

    @Schema(description = "更新日志ID")
    private Long id;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "更新标题")
    private String title;

    @Schema(description = "更新内容（Markdown）")
    private String content;

    @Schema(description = "更新内容（HTML）")
    private String contentHtml;

    @Schema(description = "是否为重大更新: 0=否, 1=是")
    private Integer isMajor;

    @Schema(description = "发布日期")
    private LocalDateTime releaseDate;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "是否为待发布状态")
    private Boolean isPending;
}
