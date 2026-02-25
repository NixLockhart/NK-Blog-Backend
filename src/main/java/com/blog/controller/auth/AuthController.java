package com.blog.controller.auth;

import com.blog.common.response.Result;
import com.blog.model.dto.auth.LoginRequest;
import com.blog.model.dto.auth.LoginResponse;
import com.blog.security.jwt.JwtTokenProvider;
import com.blog.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证接口", description = "用户登录、登出等认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "管理员登录", description = "使用用户名和密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                        @RequestHeader(value = "X-Real-IP", required = false) String ipAddress) {
        LoginResponse response = authService.login(loginRequest, ipAddress);
        return Result.success(response);
    }

    @Operation(summary = "验证Token", description = "验证JWT Token是否有效")
    @GetMapping("/validate")
    public Result<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            boolean isValid = jwtTokenProvider.validateToken(token);
            return Result.success(isValid);
        } catch (Exception e) {
            return Result.success(false);
        }
    }
}
