package com.blog.model.dto.widget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 小工具响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetResponse {

    /**
     * 小工具ID
     */
    private Long id;

    /**
     * 小工具名称
     */
    private String name;

    /**
     * 代码文件路径
     */
    private String codePath;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 是否应用到博客
     */
    private Boolean isApplied;

    /**
     * 是否系统自带（不可删除）
     */
    private Boolean isSystem;

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
