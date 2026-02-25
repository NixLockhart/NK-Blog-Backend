package com.blog.service;

import com.blog.model.dto.config.ConfigCreateRequest;
import com.blog.model.dto.config.SiteConfigResponse;
import com.blog.model.dto.config.ConfigUpdateRequest;

import java.util.List;
import java.util.Map;

/**
 * 网站配置服务接口
 */
public interface ConfigService {

    /**
     * 获取公开配置（前台可访问）
     *
     * @return 公开配置map
     */
    Map<String, String> getPublicConfig();

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<SiteConfigResponse> getAllConfig();

    /**
     * 获取配置值
     *
     * @param key 配置键
     * @return 配置值
     */
    String getConfigValue(String key);

    /**
     * 创建新配置
     *
     * @param request 配置创建请求
     * @return 创建的配置
     */
    SiteConfigResponse createConfig(ConfigCreateRequest request);

    /**
     * 更新配置
     *
     * @param key   配置键
     * @param value 配置值
     */
    void updateConfig(String key, String value);

    /**
     * 批量更新配置
     *
     * @param request 配置更新请求
     */
    void batchUpdateConfig(ConfigUpdateRequest request);

    /**
     * 删除配置
     *
     * @param key 配置键
     */
    void deleteConfig(String key);

    /**
     * 获取网站基本信息
     *
     * @return 网站基本信息
     */
    Map<String, String> getSiteInfo();

    /**
     * 获取联系方式配置
     *
     * @return 联系方式配置（key -> {description, config_value}）
     */
    Map<String, Map<String, String>> getContactConfig();

    /**
     * 获取关注链接配置
     *
     * @return 关注链接配置（key -> {description, config_value}）
     */
    Map<String, Map<String, String>> getLinkConfig();
}
