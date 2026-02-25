package com.blog.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理后台评论响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理后台评论响应")
public class AdminCommentResponse {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "文章标题")
    private String articleTitle;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "回复目标用户昵称")
    private String replyToNickname;

    @Schema(description = "评论者昵称")
    private String nickname;

    @Schema(description = "评论者邮箱")
    private String email;

    @Schema(description = "评论者网站")
    private String website;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "User-Agent")
    private String userAgent;

    @Schema(description = "状态: 1=已审核, 2=待审核")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "子评论列表")
    private List<AdminCommentResponse> children = new ArrayList<>();
}
