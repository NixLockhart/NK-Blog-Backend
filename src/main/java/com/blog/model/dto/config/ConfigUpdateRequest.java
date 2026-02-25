package com.blog.model.dto.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 配置更新请求DTO
 */
@Data
@Schema(description = "配置批量更新请求")
public class ConfigUpdateRequest {

    @Schema(description = "配置键值对", example = "{\"site_title\":\"星光小栈\",\"site_description\":\"个人博客\"}")
    private Map<String, String> configs;
}
