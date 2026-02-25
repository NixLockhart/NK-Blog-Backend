package com.blog.repository;

import com.blog.model.entity.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 网站配置Repository接口
 */
@Repository
public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {

    /**
     * 根据配置键查询
     */
    Optional<SiteConfig> findByConfigKey(String configKey);

    /**
     * 根据配置键删除
     */
    void deleteByConfigKey(String configKey);

    /**
     * 查询所有公开配置
     */
    List<SiteConfig> findByIsPublic(Integer isPublic);

    /**
     * 根据配置类型查询
     */
    List<SiteConfig> findByConfigType(String configType);

    /**
     * 根据配置类型和公开状态查询配置
     */
    List<SiteConfig> findByConfigTypeAndIsPublic(String configType, Integer isPublic);

    /**
     * 检查配置键是否存在
     */
    boolean existsByConfigKey(String configKey);

    /**
     * 根据配置键前缀查询
     */
    List<SiteConfig> findByConfigKeyStartingWith(String prefix);
}
