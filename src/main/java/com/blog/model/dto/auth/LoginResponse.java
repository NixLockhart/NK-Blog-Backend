package com.blog.model.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    public LoginResponse(String accessToken, String username, String nickname) {
        this.accessToken = accessToken;
        this.username = username;
        this.nickname = nickname;
    }
}
