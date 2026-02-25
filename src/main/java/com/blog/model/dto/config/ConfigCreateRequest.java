package com.blog.model.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站配置创建请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "网站配置创建请求")
public class ConfigCreateRequest {

    @NotBlank(message = "配置键不能为空")
    @Schema(description = "配置键", example = "custom_feature_enabled")
    private String configKey;

    @Schema(description = "配置值", example = "true")
    private String configValue;

    @NotBlank(message = "配置类型不能为空")
    @Schema(description = "配置类型(TEXT/NUMBER/BOOLEAN/JSON)", example = "BOOLEAN")
    private String configType;

    @Schema(description = "配置描述", example = "是否启用自定义功能")
    private String description;

    @NotNull(message = "是否公开不能为空")
    @Schema(description = "是否公开(前台可访问)", example = "false")
    private Boolean isPublic;
}
