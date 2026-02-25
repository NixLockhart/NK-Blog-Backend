package com.blog.service;

import com.blog.model.dto.admin.AdminProfileResponse;
import com.blog.model.dto.admin.AdminProfileUpdateRequest;
import com.blog.model.dto.admin.PasswordChangeRequest;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /**
     * 获取当前登录管理员信息
     *
     * @param username 用户名
     * @return 管理员信息
     */
    AdminProfileResponse getProfile(String username);

    /**
     * 更新管理员信息
     *
     * @param username 用户名
     * @param request 更新请求
     * @return 更新后的管理员信息
     */
    AdminProfileResponse updateProfile(String username, AdminProfileUpdateRequest request);

    /**
     * 修改密码
     *
     * @param username 用户名
     * @param request 密码修改请求
     */
    void changePassword(String username, PasswordChangeRequest request);
}
