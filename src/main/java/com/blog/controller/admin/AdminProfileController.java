package com.blog.controller.admin;

import com.blog.common.response.Result;
import com.blog.exception.BusinessException;
import com.blog.model.dto.admin.AdminProfileResponse;
import com.blog.model.dto.admin.AdminProfileUpdateRequest;
import com.blog.model.dto.admin.PasswordChangeRequest;
import com.blog.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员个人信息管理控制器
 */
@Slf4j
@Tag(name = "管理员信息管理", description = "管理员个人信息查询和修改接口")
@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class AdminProfileController {

    private final AdminService adminService;

    @Operation(summary = "获取当前管理员信息", description = "获取当前登录管理员的详细信息")
    @GetMapping
    public Result<AdminProfileResponse> getProfile() {
        String username = getCurrentUsername();
        log.info("获取管理员信息: {}", username);
        AdminProfileResponse profile = adminService.getProfile(username);
        return Result.success(profile);
    }

    @Operation(summary = "更新管理员信息", description = "更新当前登录管理员的昵称、邮箱、头像等信息")
    @PutMapping
    public Result<AdminProfileResponse> updateProfile(@Valid @RequestBody AdminProfileUpdateRequest request) {
        String username = getCurrentUsername();
        log.info("更新管理员信息: {}", username);
        AdminProfileResponse profile = adminService.updateProfile(username, request);
        return Result.success("信息更新成功", profile);
    }

    @Operation(summary = "修改密码", description = "修改当前登录管理员的密码")
    @PostMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        String username = getCurrentUsername();
        log.info("修改管理员密码: {}", username);
        adminService.changePassword(username, request);
        return Result.success("密码修改成功", null);
    }

    /**
     * 获取当前登录用户的用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("获取认证信息失败: authentication is null");
            throw new BusinessException("用户未登录");
        }

        if (!authentication.isAuthenticated()) {
            log.error("用户未认证: {}", authentication);
            throw new BusinessException("用户未登录");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            String name = (String) principal;
            if ("anonymousUser".equals(name)) {
                log.error("匿名用户访问");
                throw new BusinessException("用户未登录");
            }
            return name;
        }

        log.error("无法获取用户名, principal type: {}", principal.getClass().getName());
        throw new BusinessException("无法获取用户信息");
    }
}
