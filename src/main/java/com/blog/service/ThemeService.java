package com.blog.service;

import com.blog.model.dto.theme.ThemeCreateRequest;
import com.blog.model.dto.theme.ThemeResponse;
import com.blog.model.dto.theme.ThemeUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 主题服务接口
 */
public interface ThemeService {

    /**
     * 获取所有主题列表
     */
    List<ThemeResponse> getAllThemes();

    /**
     * 获取当前应用的主题
     */
    ThemeResponse getAppliedTheme();

    /**
     * 根据ID获取主题详情
     */
    ThemeResponse getThemeById(Long id);

    /**
     * 创建主题
     */
    Long createTheme(ThemeCreateRequest request, MultipartFile lightCss, MultipartFile darkCss);

    /**
     * 更新主题
     */
    void updateTheme(Long id, ThemeUpdateRequest request);

    /**
     * 删除主题
     */
    void deleteTheme(Long id);

    /**
     * 应用/取消应用主题
     */
    void toggleThemeApplication(Long id, Boolean isApplied);

    /**
     * 上传主题封面
     */
    String uploadThemeCover(Long id, MultipartFile file);

    /**
     * 导出主题为ZIP文件
     */
    byte[] exportTheme(Long id);

    /**
     * 更新主题文件
     */
    void updateThemeFiles(Long id, MultipartFile lightCss, MultipartFile darkCss);
}
