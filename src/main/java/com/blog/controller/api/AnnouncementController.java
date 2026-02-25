package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.announcement.AnnouncementResponse;
import com.blog.model.dto.updatelog.UpdateLogResponse;
import com.blog.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 公告和更新日志公开API控制器
 */
@Tag(name = "公告和更新日志接口（公开）", description = "公告和更新日志查询等公开接口")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "获取当前公告", description = "获取当前有效的公告（时间范围内且已启用）")
    @GetMapping("/announcement/active")
    public Result<AnnouncementResponse> getActiveAnnouncement() {
        Optional<AnnouncementResponse> announcement = announcementService.getActiveAnnouncement();
        return Result.success(announcement.orElse(null));
    }

    @Operation(summary = "获取所有当前公告", description = "获取所有当前有效的公告（用于轮播展示）")
    @GetMapping("/announcements/active")
    public Result<List<AnnouncementResponse>> getAllActiveAnnouncements() {
        List<AnnouncementResponse> announcements = announcementService.getAllActiveAnnouncements();
        return Result.success(announcements);
    }

    @Operation(summary = "获取更新日志列表", description = "获取所有更新日志（按时间倒序）")
    @GetMapping("/update-logs")
    public Result<List<UpdateLogResponse>> getAllUpdateLogs() {
        List<UpdateLogResponse> updateLogs = announcementService.getAllUpdateLogs();
        return Result.success(updateLogs);
    }
}
