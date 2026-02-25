package com.blog.repository;

import com.blog.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 管理员Repository接口
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * 根据用户名查询
     */
    Optional<Admin> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名和状态查询
     */
    Optional<Admin> findByUsernameAndStatus(String username, Integer status);
}
