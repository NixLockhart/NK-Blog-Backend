package com.blog.service;

import com.blog.model.dto.auth.LoginRequest;
import com.blog.model.dto.auth.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request, String ipAddress);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 登出
     */
    void logout();
}
