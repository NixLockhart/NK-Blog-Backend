package com.blog.model.dto.widget;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 小工具代码响应DTO（用于编辑）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetCodeResponse {

    /**
     * 小工具ID
     */
    private Long id;

    /**
     * 小工具名称
     */
    private String name;

    /**
     * HTML代码内容
     */
    private String code;

    /**
     * 封面URL
     */
    private String coverUrl;
}
