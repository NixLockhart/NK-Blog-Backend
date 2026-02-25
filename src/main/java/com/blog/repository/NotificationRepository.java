package com.blog.repository;

import com.blog.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统通知Repository接口
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 查询所有通知（分页）
     */
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据已读状态查询通知（分页）
     */
    Page<Notification> findByIsReadOrderByCreatedAtDesc(Integer isRead, Pageable pageable);

    /**
     * 查询未读通知
     */
    List<Notification> findByIsReadOrderByCreatedAtDesc(Integer isRead);

    /**
     * 统计未读通知数量
     */
    long countByIsRead(Integer isRead);

    /**
     * 标记单个通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 1 WHERE n.id = :id")
    void markAsRead(@Param("id") Long id);

    /**
     * 标记所有通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 1 WHERE n.isRead = 0")
    void markAllAsRead();
}
