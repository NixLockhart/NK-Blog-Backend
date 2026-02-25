package com.blog.model.dto.theme;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建主题请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCreateRequest {

    /**
     * 主题名称
     */
    @NotBlank(message = "主题名称不能为空")
    @Size(max = 50, message = "主题名称长度不能超过50个字符")
    private String name;

    /**
     * 主题描述
     */
    @Size(max = 200, message = "主题描述长度不能超过200个字符")
    private String description;

    /**
     * 作者
     */
    @Size(max = 50, message = "作者名称长度不能超过50个字符")
    private String author;

    /**
     * 版本号
     */
    @Size(max = 20, message = "版本号长度不能超过20个字符")
    private String version;

    /**
     * 封面图片（Base64编码）
     */
    private String coverImage;

    /**
     * 显示顺序
     */
    private Integer displayOrder;
}
