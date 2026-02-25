package com.blog.service;

import com.blog.model.dto.widget.WidgetCodeResponse;
import com.blog.model.dto.widget.WidgetCreateRequest;
import com.blog.model.dto.widget.WidgetResponse;
import com.blog.model.dto.widget.WidgetUpdateRequest;

import java.util.List;

/**
 * 小工具服务接口
 */
public interface WidgetService {

    /**
     * 获取所有小工具列表
     */
    List<WidgetResponse> getAllWidgets();

    /**
     * 获取已应用的小工具列表
     */
    List<WidgetResponse> getAppliedWidgets();

    /**
     * 根据ID获取小工具详情
     */
    WidgetResponse getWidgetById(Long id);

    /**
     * 获取小工具代码（用于编辑）
     */
    WidgetCodeResponse getWidgetCode(Long id);

    /**
     * 创建小工具
     */
    Long createWidget(WidgetCreateRequest request);

    /**
     * 更新小工具
     */
    void updateWidget(Long id, WidgetUpdateRequest request);

    /**
     * 删除小工具
     */
    void deleteWidget(Long id);

    /**
     * 应用/取消应用小工具
     */
    void toggleWidgetApplication(Long id, Boolean isApplied);

    /**
     * 导出小工具代码
     */
    String exportWidget(Long id);
}
