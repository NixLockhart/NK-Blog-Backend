package com.blog.model.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员信息更新请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileUpdateRequest {

    /**
     * 昵称（可选，1-50字符）
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 邮箱（可选，有效邮箱格式）
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 头像URL（可选）
     */
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatar;
}
