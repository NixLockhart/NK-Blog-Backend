package com.blog.service;

import com.blog.exception.BusinessException;
import com.blog.model.dto.config.ImageStorageConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 图片存储配置服务
 * <p>
 * 从 tb_site_configs 表读取 image_storage 配置（JSON 格式），
 * 提供类型安全的配置访问和更新。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorageConfigService {

    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    private static final String CONFIG_KEY = "image_storage";

    /**
     * 获取图片存储配置
     */
    public ImageStorageConfig getConfig() {
        String json = configService.getConfigValue(CONFIG_KEY);
        if (json == null || json.isEmpty()) {
            return ImageStorageConfig.defaultLocal();
        }
        try {
            return objectMapper.readValue(json, ImageStorageConfig.class);
        } catch (JsonProcessingException e) {
            log.error("解析图片存储配置失败，使用默认配置", e);
            return ImageStorageConfig.defaultLocal();
        }
    }

    /**
     * 更新图片存储配置
     */
    public void updateConfig(ImageStorageConfig config) {
        if ("cdn".equalsIgnoreCase(config.getMode())) {
            if (config.getCdn() == null
                    || config.getCdn().getUploadUrl() == null
                    || config.getCdn().getUploadUrl().trim().isEmpty()) {
                throw new BusinessException("图床模式下必须配置上传 API 地址");
            }
            if (config.getCdn().getResponseUrlField() == null
                    || config.getCdn().getResponseUrlField().trim().isEmpty()) {
                throw new BusinessException("图床模式下必须配置响应 URL 字段路径");
            }
        }

        try {
            String json = objectMapper.writeValueAsString(config);
            configService.updateConfig(CONFIG_KEY, json);
            log.info("图片存储配置已更新: mode={}", config.getMode());
        } catch (JsonProcessingException e) {
            throw new BusinessException("序列化图片存储配置失败: " + e.getMessage());
        }
    }

    /**
     * 是否为 CDN 模式
     */
    public boolean isCdnMode() {
        return "cdn".equalsIgnoreCase(getConfig().getMode());
    }
}
