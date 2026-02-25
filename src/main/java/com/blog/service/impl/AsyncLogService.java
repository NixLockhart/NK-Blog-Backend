package com.blog.service.impl;

import com.blog.model.entity.OperationLog;
import com.blog.model.entity.VisitLog;
import com.blog.repository.OperationLogRepository;
import com.blog.repository.VisitLogRepository;
import com.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * 异步日志服务
 * 将日志写入操作从请求线程中解耦
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncLogService {

    private final OperationLogRepository operationLogRepository;
    private final VisitLogRepository visitLogRepository;
    private final ArticleService articleService;

    @Async
    public void saveOperationLog(OperationLog operationLog) {
        try {
            operationLogRepository.save(operationLog);
        } catch (Exception e) {
            log.error("Failed to save operation log", e);
        }
    }

    @Async
    public void recordVisit(Long articleId, String visitorId, String ipAddress, String userAgent, String referer, String pageUrl) {
        try {
            LocalDate today = LocalDate.now();

            // 检查今天是否已经记录过
            boolean exists;
            if (articleId != null) {
                exists = visitLogRepository.existsByVisitorIdAndArticleIdAndVisitDate(visitorId, articleId, today);
            } else {
                exists = visitLogRepository.existsByVisitorIdAndArticleIdIsNullAndVisitDate(visitorId, today);
            }

            if (!exists) {
                VisitLog visitLog = new VisitLog();
                visitLog.setVisitorId(visitorId);
                visitLog.setArticleId(articleId);
                visitLog.setIpAddress(ipAddress);
                visitLog.setUserAgent(userAgent);
                visitLog.setReferer(referer);
                visitLog.setPageUrl(pageUrl);
                visitLog.setVisitDate(today);

                visitLogRepository.save(visitLog);

                // 如果是文章访问，增加浏览量
                if (articleId != null) {
                    try {
                        articleService.incrementViews(articleId);
                    } catch (Exception e) {
                        log.error("Failed to increment article views", e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to record visit log", e);
        }
    }
}
