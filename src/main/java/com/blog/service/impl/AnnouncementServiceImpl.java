package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.config.properties.BlogProperties;
import com.blog.exception.BusinessException;
import com.blog.model.dto.announcement.AnnouncementResponse;
import com.blog.model.dto.updatelog.UpdateLogRequest;
import com.blog.model.dto.updatelog.UpdateLogResponse;
import com.blog.model.entity.Announcement;
import com.blog.model.entity.UpdateLog;
import com.blog.repository.AnnouncementRepository;
import com.blog.repository.UpdateLogRepository;
import com.blog.service.AnnouncementService;
import com.blog.service.MarkdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 公告和更新日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UpdateLogRepository updateLogRepository;
    private final MarkdownService markdownService;
    private final BlogProperties blogProperties;

    @Override
    @Transactional(readOnly = true)
    public Optional<AnnouncementResponse> getActiveAnnouncement() {
        List<Announcement> announcements = announcementRepository.findActiveAnnouncement(
            LocalDateTime.now(),
            PageRequest.of(0, 1)
        );
        return announcements.isEmpty() ? Optional.empty() : Optional.of(convertAnnouncementToResponse(announcements.get(0)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAllActiveAnnouncements() {
        List<Announcement> announcements = announcementRepository.findActiveAnnouncement(
            LocalDateTime.now(),
            PageRequest.of(0, Integer.MAX_VALUE)  // 获取所有启用的公告
        );
        return announcements.stream()
                .map(this::convertAnnouncementToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> getAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAllByOrderByCreatedAtDesc();
        return announcements.stream()
                .map(this::convertAnnouncementToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createAnnouncement(Announcement announcement) {
        announcement = announcementRepository.save(announcement);
        log.info("创建公告成功: id={}, title={}", announcement.getId(), announcement.getTitle());
        return announcement.getId();
    }

    @Override
    @Transactional
    public void updateAnnouncement(Long id, Announcement updatedAnnouncement) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        announcement.setTitle(updatedAnnouncement.getTitle());
        announcement.setContent(updatedAnnouncement.getContent());
        announcement.setStartTime(updatedAnnouncement.getStartTime());
        announcement.setEndTime(updatedAnnouncement.getEndTime());
        announcement.setEnabled(updatedAnnouncement.getEnabled());

        announcementRepository.save(announcement);
        log.info("更新公告成功: id={}, title={}", id, announcement.getTitle());
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        announcementRepository.delete(announcement);
        log.info("删除公告成功: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpdateLogResponse> getAllUpdateLogs() {
        // 从数据库读取更新日志记录
        List<UpdateLog> logs = updateLogRepository.findAllByOrderByReleaseDateDesc();
        List<UpdateLogResponse> responses = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (UpdateLog updateLog : logs) {
            try {
                UpdateLogResponse response = new UpdateLogResponse();
                response.setId(updateLog.getId());
                response.setVersion(updateLog.getVersion());
                response.setTitle(updateLog.getTitle());
                response.setIsMajor(updateLog.getIsMajor());
                response.setReleaseDate(updateLog.getReleaseDate());
                response.setCreatedAt(updateLog.getCreatedAt());

                // 判断是否为待发布状态
                response.setIsPending(updateLog.getReleaseDate().isAfter(now));

                // 根据 content_path 读取文件内容
                String content = readUpdateLogFile(updateLog.getContentPath());
                response.setContent(content);
                response.setContentHtml(markdownService.markdownToHtml(content));

                responses.add(response);
            } catch (Exception e) {
                log.error("处理更新日志失败: id={}, version={}", updateLog.getId(), updateLog.getVersion(), e);
            }
        }

        log.info("从数据库加载了 {} 个更新日志", responses.size());
        return responses;
    }

    /**
     * 读取更新日志文件内容
     */
    private String readUpdateLogFile(String contentPath) {
        try {
            // contentPath 格式: /update-logs/v2.0.1.md
            String fullPath = blogProperties.getData().getPath() + contentPath;
            Path filePath = Paths.get(fullPath);

            if (!Files.exists(filePath)) {
                log.warn("更新日志文件不存在: {}", fullPath);
                return "文件内容不存在";
            }

            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取更新日志文件失败: {}", contentPath, e);
            return "文件读取失败: " + e.getMessage();
        }
    }

    @Override
    @Transactional
    public Long createUpdateLog(UpdateLogRequest request) {
        try {
            // 检查版本号是否已存在
            if (updateLogRepository.existsByVersion(request.getVersion())) {
                throw new BusinessException(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), "版本号已存在");
            }

            // 生成文件路径
            String contentPath = "/update-logs/" + request.getVersion() + ".md";

            // 创建实体
            UpdateLog updateLog = new UpdateLog();
            updateLog.setVersion(request.getVersion());
            updateLog.setTitle(request.getTitle() != null ? request.getTitle() : "");
            updateLog.setContentPath(contentPath);
            updateLog.setReleaseDate(request.getReleaseDate());
            updateLog.setIsMajor(request.getIsMajor());

            // 先保存到数据库
            UpdateLog saved = updateLogRepository.save(updateLog);

            // 写入文件
            writeUpdateLogFile(contentPath, request.getContent());

            log.info("创建更新日志成功: id={}, version={}", saved.getId(), saved.getVersion());
            return saved.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建更新日志失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "创建更新日志失败: " + e.getMessage());
        }
    }

    /**
     * 写入更新日志文件
     */
    private void writeUpdateLogFile(String contentPath, String content) {
        try {
            // contentPath 格式: /update-logs/v2.0.1.md
            String fullPath = blogProperties.getData().getPath() + contentPath;
            Path filePath = Paths.get(fullPath);

            // 确保目录存在
            Path parentDir = filePath.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 写入文件
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.info("写入更新日志文件成功: {}", fullPath);
        } catch (Exception e) {
            log.error("写入更新日志文件失败: {}", contentPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "写入文件失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateUpdateLog(Long id, UpdateLogRequest request) {
        try {
            // 查找现有记录
            UpdateLog updateLog = updateLogRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

            String oldVersion = updateLog.getVersion();
            String oldContentPath = updateLog.getContentPath();

            // 如果版本号改变，需要检查新版本号是否已存在
            if (!oldVersion.equals(request.getVersion())) {
                if (updateLogRepository.existsByVersion(request.getVersion())) {
                    throw new BusinessException(ErrorCode.RESOURCE_ALREADY_EXISTS.getCode(), "新版本号已存在");
                }

                // 生成新文件路径
                String newContentPath = "/update-logs/" + request.getVersion() + ".md";
                updateLog.setVersion(request.getVersion());
                updateLog.setContentPath(newContentPath);

                // 删除旧文件
                deleteUpdateLogFile(oldContentPath);

                // 写入新文件
                writeUpdateLogFile(newContentPath, request.getContent());
            } else {
                // 版本号没变，直接更新文件内容
                writeUpdateLogFile(oldContentPath, request.getContent());
            }

            // 更新其他字段
            updateLog.setTitle(request.getTitle() != null ? request.getTitle() : "");
            updateLog.setReleaseDate(request.getReleaseDate());
            updateLog.setIsMajor(request.getIsMajor());

            // 保存到数据库
            updateLogRepository.save(updateLog);

            log.info("更新更新日志成功: id={}, version={}", id, updateLog.getVersion());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新更新日志失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "更新更新日志失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteUpdateLog(Long id) {
        try {
            // 查找记录
            UpdateLog updateLog = updateLogRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

            String contentPath = updateLog.getContentPath();

            // 删除文件
            deleteUpdateLogFile(contentPath);

            // 删除数据库记录
            updateLogRepository.delete(updateLog);

            log.info("删除更新日志成功: id={}, version={}", id, updateLog.getVersion());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除更新日志失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "删除更新日志失败: " + e.getMessage());
        }
    }

    /**
     * 删除更新日志文件
     */
    private void deleteUpdateLogFile(String contentPath) {
        try {
            String fullPath = blogProperties.getData().getPath() + contentPath;
            Path filePath = Paths.get(fullPath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除更新日志文件成功: {}", fullPath);
            } else {
                log.warn("更新日志文件不存在: {}", fullPath);
            }
        } catch (Exception e) {
            log.error("删除更新日志文件失败: {}", contentPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "删除文件失败: " + e.getMessage());
        }
    }

    /**
     * 转换公告为响应DTO
     */
    private AnnouncementResponse convertAnnouncementToResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setStartTime(announcement.getStartTime());
        response.setEndTime(announcement.getEndTime());
        response.setEnabled(announcement.getEnabled());
        response.setCreatedAt(announcement.getCreatedAt());
        return response;
    }
}
