package com.blog.model.dto.theme;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 主题响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeResponse {

    /**
     * 主题ID
     */
    private Long id;

    /**
     * 主题名称
     */
    private String name;

    /**
     * 主题标识
     */
    private String slug;

    /**
     * 主题描述
     */
    private String description;

    /**
     * 作者
     */
    private String author;

    /**
     * 版本号
     */
    private String version;

    /**
     * 主题文件夹路径
     */
    private String themePath;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 预览图URL
     */
    private String previewImageUrl;

    /**
     * 是否应用到博客
     */
    private Boolean isApplied;

    /**
     * 是否为默认主题（不可删除）
     */
    private Boolean isDefault;

    /**
     * 显示顺序
     */
    private Integer displayOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
