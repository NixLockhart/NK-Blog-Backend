package com.blog.model.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站配置响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "网站配置响应")
public class SiteConfigResponse {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型(string/number/boolean/json)")
    private String configType;

    @Schema(description = "配置描述")
    private String description;

    @Schema(description = "是否公开(前台可访问)")
    private Boolean isPublic;

    @Schema(description = "创建时间")
    private String createdAt;

    @Schema(description = "更新时间")
    private String updatedAt;
}
