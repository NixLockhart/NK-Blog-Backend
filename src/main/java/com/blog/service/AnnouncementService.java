package com.blog.service;

import com.blog.model.dto.announcement.AnnouncementResponse;
import com.blog.model.dto.updatelog.UpdateLogRequest;
import com.blog.model.dto.updatelog.UpdateLogResponse;
import com.blog.model.entity.Announcement;

import java.util.List;
import java.util.Optional;

/**
 * 公告和更新日志服务接口
 */
public interface AnnouncementService {

    // ========== 公告相关 ==========

    /**
     * 获取当前有效的公告
     */
    Optional<AnnouncementResponse> getActiveAnnouncement();

    /**
     * 获取所有当前有效的公告（用于轮播）
     */
    List<AnnouncementResponse> getAllActiveAnnouncements();

    /**
     * 获取所有公告（管理端）
     */
    List<AnnouncementResponse> getAllAnnouncements();

    /**
     * 创建公告
     */
    Long createAnnouncement(Announcement announcement);

    /**
     * 更新公告
     */
    void updateAnnouncement(Long id, Announcement announcement);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);

    // ========== 更新日志相关 ==========

    /**
     * 获取更新日志列表
     */
    List<UpdateLogResponse> getAllUpdateLogs();

    /**
     * 创建更新日志
     */
    Long createUpdateLog(UpdateLogRequest request);

    /**
     * 更新更新日志
     */
    void updateUpdateLog(Long id, UpdateLogRequest request);

    /**
     * 删除更新日志
     */
    void deleteUpdateLog(Long id);
}
