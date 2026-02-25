package com.blog.model.dto.widget;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小工具创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetCreateRequest {

    /**
     * 小工具名称
     */
    @NotBlank(message = "小工具名称不能为空")
    private String name;

    /**
     * HTML代码内容
     */
    @NotBlank(message = "代码内容不能为空")
    private String code;

    /**
     * 封面图片（Base64编码）
     */
    private String coverImage;

    /**
     * 显示顺序
     */
    private Integer displayOrder;
}
