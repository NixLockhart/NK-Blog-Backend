package com.blog.service.impl;

import com.blog.exception.BusinessException;
import com.blog.model.dto.admin.AdminProfileResponse;
import com.blog.model.dto.admin.AdminProfileUpdateRequest;
import com.blog.model.dto.admin.PasswordChangeRequest;
import com.blog.model.entity.Admin;
import com.blog.repository.AdminRepository;
import com.blog.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getProfile(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        return convertToResponse(admin);
    }

    @Override
    @Transactional
    public AdminProfileResponse updateProfile(String username, AdminProfileUpdateRequest request) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        // 更新可编辑字段
        if (request.getNickname() != null) {
            admin.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            admin.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            admin.setAvatar(request.getAvatar());
        }

        Admin savedAdmin = adminRepository.save(admin);
        log.info("管理员信息更新成功: {}", username);

        return convertToResponse(savedAdmin);
    }

    @Override
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("新密码与确认密码不一致");
        }

        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        // 检查新密码是否与旧密码相同
        if (passwordEncoder.matches(request.getNewPassword(), admin.getPassword())) {
            throw new BusinessException("新密码不能与旧密码相同");
        }

        // 更新密码
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);

        log.info("管理员密码修改成功: {}", username);
    }

    /**
     * 将Admin实体转换为响应DTO
     */
    private AdminProfileResponse convertToResponse(Admin admin) {
        return AdminProfileResponse.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .email(admin.getEmail())
                .avatar(admin.getAvatar())
                .status(admin.getStatus())
                .lastLoginAt(admin.getLastLoginAt())
                .lastLoginIp(admin.getLastLoginIp())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();
    }
}
