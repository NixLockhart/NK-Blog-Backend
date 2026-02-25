package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 网站配置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_site_configs", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class SiteConfig {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置键
     */
    @Column(nullable = false, unique = true, length = 100, name = "config_key")
    private String configKey;

    /**
     * 配置值
     */
    @Column(nullable = false, columnDefinition = "TEXT", name = "config_value")
    private String configValue;

    /**
     * 配置描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 配置类型: string, json, number, boolean
     */
    @Column(length = 50, name = "config_type")
    private String configType;

    /**
     * 是否公开: 0=否(仅管理端), 1=是(前端可访问)
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer isPublic = 1;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
