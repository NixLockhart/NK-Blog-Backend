package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.model.dto.config.ConfigCreateRequest;
import com.blog.model.dto.config.ConfigUpdateRequest;
import com.blog.model.dto.config.SiteConfigResponse;
import com.blog.model.entity.SiteConfig;
import com.blog.repository.SiteConfigRepository;
import com.blog.service.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网站配置服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SiteConfigRepository siteConfigRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Cacheable(value = "config", key = "'public'")
    @Transactional(readOnly = true)
    public Map<String, String> getPublicConfig() {
        List<SiteConfig> configs = siteConfigRepository.findByIsPublic(1);
        Map<String, String> result = new HashMap<>();
        for (SiteConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteConfigResponse> getAllConfig() {
        List<SiteConfig> configs = siteConfigRepository.findAll();
        return configs.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String key) {
        return siteConfigRepository.findByConfigKey(key)
                .map(SiteConfig::getConfigValue)
                .orElse(null);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public SiteConfigResponse createConfig(ConfigCreateRequest request) {
        // 检查配置键是否已存在
        if (siteConfigRepository.findByConfigKey(request.getConfigKey()).isPresent()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "配置键已存在: " + request.getConfigKey());
        }

        // 创建新配置
        SiteConfig config = new SiteConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setConfigType(request.getConfigType());
        config.setDescription(request.getDescription());
        config.setIsPublic(request.getIsPublic() ? 1 : 0);

        // 保存到数据库
        SiteConfig savedConfig = siteConfigRepository.save(config);
        log.info("配置创建成功: {}", request.getConfigKey());

        // 转换为响应DTO
        return convertToResponse(savedConfig);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void updateConfig(String key, String value) {
        SiteConfig config = siteConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(ErrorCode.CONFIG_NOT_FOUND));

        config.setConfigValue(value);
        siteConfigRepository.save(config);
        log.info("配置更新成功: {} = {}", key, value);
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void batchUpdateConfig(ConfigUpdateRequest request) {
        Map<String, String> configs = request.getConfigs();
        if (configs == null || configs.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "配置不能为空");
        }

        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            siteConfigRepository.findByConfigKey(key).ifPresent(config -> {
                config.setConfigValue(value);
                siteConfigRepository.save(config);
            });
        }

        log.info("批量更新配置成功: {} 项", configs.size());
    }

    @Override
    @Transactional
    @CacheEvict(value = "config", allEntries = true)
    public void deleteConfig(String key) {
        siteConfigRepository.findByConfigKey(key).ifPresent(config -> {
            siteConfigRepository.delete(config);
            log.info("配置删除成功: {}", key);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getSiteInfo() {
        Map<String, String> info = new HashMap<>();

        // 获取网站基本信息（使用数据库中的实际config_key）
        info.put("title", getConfigValueOrDefault("site_title", "星光小栈"));
        info.put("subtitle", getConfigValueOrDefault("site_subtitle", "个人博客"));
        info.put("description", getConfigValueOrDefault("site_description", "一个简单的个人博客"));
        info.put("keywords", getConfigValueOrDefault("site_keywords", "博客,技术,分享"));
        info.put("author", getConfigValueOrDefault("site_author", "Admin"));
        info.put("email", getConfigValueOrDefault("contact_email", ""));
        info.put("github", getConfigValueOrDefault("contact_github", ""));
        info.put("beian", getConfigValueOrDefault("site_icp", ""));

        return info;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> getContactConfig() {
        Map<String, Map<String, String>> result = new HashMap<>();

        // 查询所有contact_开头的配置
        List<SiteConfig> contactConfigs = siteConfigRepository.findByConfigKeyStartingWith("contact_");

        // 过滤出config_value有值的记录，并构建返回结果
        for (SiteConfig config : contactConfigs) {
            if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                Map<String, String> contactInfo = new HashMap<>();
                contactInfo.put("description", config.getDescription());
                contactInfo.put("config_value", config.getConfigValue());
                result.put(config.getConfigKey(), contactInfo);
            }
        }

        log.info("获取联系方式配置，共 {} 条", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, String>> getLinkConfig() {
        Map<String, Map<String, String>> result = new HashMap<>();

        // 查询所有link_开头的配置
        List<SiteConfig> linkConfigs = siteConfigRepository.findByConfigKeyStartingWith("link_");

        // 过滤出config_value有值的记录，并构建返回结果
        for (SiteConfig config : linkConfigs) {
            if (config.getConfigValue() != null && !config.getConfigValue().trim().isEmpty()) {
                Map<String, String> linkInfo = new HashMap<>();
                linkInfo.put("description", config.getDescription());
                linkInfo.put("config_value", config.getConfigValue());
                result.put(config.getConfigKey(), linkInfo);
            }
        }

        log.info("获取关注链接配置，共 {} 条", result.size());
        return result;
    }

    /**
     * 获取配置值，如果不存在则返回默认值
     */
    private String getConfigValueOrDefault(String key, String defaultValue) {
        String value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }

    /**
     * 转换为响应DTO
     */
    private SiteConfigResponse convertToResponse(SiteConfig config) {
        SiteConfigResponse response = new SiteConfigResponse();
        response.setId(config.getId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setConfigType(config.getConfigType()); // 使用configType字段
        response.setDescription(config.getDescription());
        response.setIsPublic(config.getIsPublic() == 1); // Integer转Boolean
        response.setCreatedAt(config.getCreatedAt().format(formatter));
        response.setUpdatedAt(config.getUpdatedAt().format(formatter));
        return response;
    }
}
