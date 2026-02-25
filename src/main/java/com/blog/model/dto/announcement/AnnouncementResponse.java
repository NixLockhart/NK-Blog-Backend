package com.blog.model.dto.announcement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 公告响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告响应")
public class AnnouncementResponse {

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "开始显示时间")
    private LocalDateTime startTime;

    @Schema(description = "结束显示时间")
    private LocalDateTime endTime;

    @Schema(description = "是否启用")
    private Integer enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
