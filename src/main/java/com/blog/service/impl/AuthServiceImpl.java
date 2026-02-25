package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.model.dto.auth.LoginRequest;
import com.blog.model.dto.auth.LoginResponse;
import com.blog.model.entity.Admin;
import com.blog.repository.AdminRepository;
import com.blog.security.jwt.JwtTokenProvider;
import com.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        try {
            // 认证用户
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 生成JWT Token
            String jwt = jwtTokenProvider.generateToken(authentication);

            // 更新最后登录信息
            Admin admin = adminRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            admin.setLastLoginAt(LocalDateTime.now());
            admin.setLastLoginIp(ipAddress != null ? ipAddress : "unknown");
            adminRepository.save(admin);

            log.info("用户登录成功: {}, IP: {}", request.getUsername(), ipAddress);
            return new LoginResponse(jwt, admin.getUsername(), admin.getNickname());

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {}, 原因: {}", request.getUsername(), e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Token无效或已过期");
        }

        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        Admin admin = adminRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());
        return new LoginResponse(newAccessToken, admin.getUsername(), admin.getNickname());
    }

    @Override
    public void logout() {
        log.info("User logged out");
    }
}
