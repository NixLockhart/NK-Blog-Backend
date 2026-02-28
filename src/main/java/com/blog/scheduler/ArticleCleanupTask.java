package com.blog.scheduler;

import com.blog.common.enums.ArticleStatus;
import com.blog.model.entity.Article;
import com.blog.repository.ArticleRepository;
import com.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章定时清理任务
 * 每天凌晨3点检查已删除超过30天的文章，执行永久删除
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCleanupTask {

    private final ArticleRepository articleRepository;
    private final ArticleService articleService;

    private static final int RETENTION_DAYS = 30;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupDeletedArticles() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<Article> expiredArticles = articleRepository.findByStatusAndDeletedAtBefore(
                ArticleStatus.DELETED.getValue(), threshold);

        if (expiredArticles.isEmpty()) {
            return;
        }

        log.info("开始清理已删除超过{}天的文章，共{}篇", RETENTION_DAYS, expiredArticles.size());

        int successCount = 0;
        for (Article article : expiredArticles) {
            try {
                articleService.permanentlyDeleteArticle(article.getId());
                successCount++;
            } catch (Exception e) {
                log.error("永久删除文章失败: id={}, title={}", article.getId(), article.getTitle(), e);
            }
        }

        log.info("清理完成: 成功{}篇，失败{}篇", successCount, expiredArticles.size() - successCount);
    }
}
