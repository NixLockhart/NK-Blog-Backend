package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 系统通知实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_notifications", indexes = {
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 通知类型（LIKE/COMMENT/MESSAGE/MILESTONE）
     */
    @Column(nullable = false, length = 20)
    private String notificationType;

    /**
     * 通知标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 关联对象ID
     */
    private Long relatedId;

    /**
     * 关联对象类型（ARTICLE/COMMENT/MESSAGE）
     */
    @Column(length = 20)
    private String relatedType;

    /**
     * 是否已读: 0=未读, 1=已读
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isRead = 0;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
