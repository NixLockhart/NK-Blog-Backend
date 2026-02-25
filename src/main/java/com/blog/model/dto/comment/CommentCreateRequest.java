package com.blog.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求DTO
 */
@Data
@Schema(description = "发表评论请求")
public class CommentCreateRequest {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", example = "1")
    private Long articleId;

    @Schema(description = "父评论ID（回复评论时需要）")
    private Long parentId;

    @Schema(description = "回复目标用户昵称（回复评论时需要）")
    private String replyToNickname;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "评论者昵称", example = "张三")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "评论者邮箱（可选）", example = "zhangsan@example.com")
    private String email;

    @Size(max = 200, message = "网站URL长度不能超过200个字符")
    @Schema(description = "评论者网站")
    private String website;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Schema(description = "头像URL")
    private String avatar;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容长度不能超过1000个字符")
    @Schema(description = "评论内容（支持Markdown和Emoji）")
    private String content;
}
