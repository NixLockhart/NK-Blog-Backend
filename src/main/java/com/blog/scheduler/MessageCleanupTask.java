package com.blog.scheduler;

import com.blog.common.enums.MessageStatus;
import com.blog.model.entity.Message;
import com.blog.repository.MessageRepository;
import com.blog.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言定时清理任务
 * 每天凌晨3点检查已删除超过30天的留言，执行永久删除
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageCleanupTask {

    private final MessageRepository messageRepository;
    private final MessageService messageService;

    private static final int RETENTION_DAYS = 30;

    @Scheduled(cron = "0 10 3 * * ?")
    public void cleanupDeletedMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        List<Message> expiredMessages = messageRepository.findByStatusAndDeletedAtBefore(
                MessageStatus.DELETED.getValue(), threshold);

        if (expiredMessages.isEmpty()) {
            return;
        }

        log.info("开始清理已删除超过{}天的留言，共{}条", RETENTION_DAYS, expiredMessages.size());

        int successCount = 0;
        for (Message message : expiredMessages) {
            try {
                messageService.permanentlyDeleteMessage(message.getId());
                successCount++;
            } catch (Exception e) {
                log.error("永久删除留言失败: id={}", message.getId(), e);
            }
        }

        log.info("留言清理完成: 成功{}条，失败{}条", successCount, expiredMessages.size() - successCount);
    }
}
