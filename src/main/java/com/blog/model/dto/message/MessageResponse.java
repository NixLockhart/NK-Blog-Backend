package com.blog.model.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 留言响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留言响应")
public class MessageResponse {

    @Schema(description = "留言ID")
    private Long id;

    @Schema(description = "留言者昵称")
    private String nickname;

    @Schema(description = "留言者邮箱")
    private String email;

    @Schema(description = "博客地址")
    private String blogUrl;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "留言内容")
    private String content;

    @Schema(description = "是否为友情链接")
    private Integer isFriendLink;

    @Schema(description = "状态: 0=已删除, 1=已显示")
    private Integer status;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
