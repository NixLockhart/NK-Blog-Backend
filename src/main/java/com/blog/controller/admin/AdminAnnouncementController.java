package com.blog.controller.admin;

import com.blog.common.response.Result;
import com.blog.model.dto.announcement.AnnouncementResponse;
import com.blog.model.dto.updatelog.UpdateLogRequest;
import com.blog.model.dto.updatelog.UpdateLogResponse;
import com.blog.model.entity.Announcement;
import com.blog.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告和更新日志管理API控制器
 */
@Tag(name = "公告和更新日志管理接口", description = "公告和更新日志的创建、编辑、删除等管理功能")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    // ========== 公告管理 ==========

    @Operation(summary = "获取所有公告", description = "获取所有公告列表（管理端）")
    @GetMapping("/announcements")
    public Result<List<AnnouncementResponse>> getAllAnnouncements() {
        List<AnnouncementResponse> announcements = announcementService.getAllAnnouncements();
        return Result.success(announcements);
    }

    @Operation(summary = "创建公告", description = "创建新公告")
    @PostMapping("/announcements")
    public Result<Long> createAnnouncement(@RequestBody Announcement announcement) {
        Long id = announcementService.createAnnouncement(announcement);
        return Result.success(id);
    }

    @Operation(summary = "更新公告", description = "更新已有公告")
    @PutMapping("/announcements/{id}")
    public Result<Void> updateAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @RequestBody Announcement announcement) {
        announcementService.updateAnnouncement(id, announcement);
        return Result.success(null);
    }

    @Operation(summary = "删除公告", description = "删除公告")
    @DeleteMapping("/announcements/{id}")
    public Result<Void> deleteAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success(null);
    }

    // ========== 更新日志管理 ==========

    @Operation(summary = "获取所有更新日志", description = "获取所有更新日志列表（管理端）")
    @GetMapping("/update-logs")
    public Result<List<UpdateLogResponse>> getAllUpdateLogsForAdmin() {
        List<UpdateLogResponse> updateLogs = announcementService.getAllUpdateLogs();
        return Result.success(updateLogs);
    }

    @Operation(summary = "创建更新日志", description = "创建新更新日志")
    @PostMapping("/update-logs")
    public Result<Long> createUpdateLog(@Valid @RequestBody UpdateLogRequest request) {
        Long id = announcementService.createUpdateLog(request);
        return Result.success(id);
    }

    @Operation(summary = "更新更新日志", description = "更新已有更新日志")
    @PutMapping("/update-logs/{id}")
    public Result<Void> updateUpdateLog(
            @Parameter(description = "更新日志ID") @PathVariable Long id,
            @Valid @RequestBody UpdateLogRequest request) {
        announcementService.updateUpdateLog(id, request);
        return Result.success(null);
    }

    @Operation(summary = "删除更新日志", description = "删除更新日志")
    @DeleteMapping("/update-logs/{id}")
    public Result<Void> deleteUpdateLog(
            @Parameter(description = "更新日志ID") @PathVariable Long id) {
        announcementService.deleteUpdateLog(id);
        return Result.success(null);
    }
}
