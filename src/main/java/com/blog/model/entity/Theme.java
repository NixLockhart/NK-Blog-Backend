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
 * 主题实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_themes")
@EntityListeners(AuditingEntityListener.class)
public class Theme {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 主题名称
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 主题标识
     */
    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    /**
     * 主题描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 作者
     */
    @Column(length = 50)
    private String author;

    /**
     * 版本号
     */
    @Column(length = 20)
    private String version;

    /**
     * 主题文件夹路径
     */
    @Column(nullable = false, length = 500)
    private String themePath;

    /**
     * 预览图路径
     */
    @Column(length = 500)
    private String previewImage;

    /**
     * 封面图片路径
     */
    @Column(length = 255)
    private String coverPath;

    /**
     * 是否应用到博客（0=否, 1=是）
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isActive = 0;

    /**
     * 是否为默认主题（0=否, 1=是，不可删除）
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDefault = 0;

    /**
     * 显示顺序
     */
    @Column(nullable = false)
    private Integer displayOrder = 0;

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
