package com.blog.model.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表留言请求DTO
 */
@Data
@Schema(description = "发表留言请求")
public class MessageCreateRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    @Schema(description = "留言者昵称", example = "张三")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "留言者邮箱（可选）", example = "zhangsan@example.com")
    private String email;

    @Size(max = 200, message = "博客地址长度不能超过200个字符")
    @Schema(description = "博客地址（填写后可成为友情链接）")
    private String blogUrl;

    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    @Schema(description = "头像URL")
    private String avatar;

    @NotBlank(message = "留言内容不能为空")
    @Size(max = 1000, message = "留言内容长度不能超过1000个字符")
    @Schema(description = "留言内容")
    private String content;
}
