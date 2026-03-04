package com.blog.scheduler;

import com.blog.common.enums.CommentStatus;
import com.blog.model.entity.Comment;
import com.blog.repository.CommentRepository;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论定时清理任务
 * 每天凌晨3点检查已删除超过30天的评论，执行永久删除
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCleanupTask {

    private final CommentRepository commentRepository;
    private final CommentService commentService;

    private static final int RETENTION_DAYS = 30;

    @Scheduled(cron = "0 5 3 * * ?")
    public void cleanupDeletedComments() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<Comment> expiredComments = commentRepository.findByStatusAndDeletedAtBefore(
                CommentStatus.DELETED.getValue(), threshold);

        if (expiredComments.isEmpty()) {
            return;
        }

        log.info("开始清理已删除超过{}天的评论，共{}条", RETENTION_DAYS, expiredComments.size());

        int successCount = 0;
        for (Comment comment : expiredComments) {
            try {
                commentService.permanentlyDeleteComment(comment.getId());
                successCount++;
            } catch (Exception e) {
                log.error("永久删除评论失败: id={}", comment.getId(), e);
            }
        }

        log.info("评论清理完成: 成功{}条，失败{}条", successCount, expiredComments.size() - successCount);
    }
}
