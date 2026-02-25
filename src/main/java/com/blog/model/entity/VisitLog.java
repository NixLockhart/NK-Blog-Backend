package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 访问记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_visit_logs", indexes = {
    @Index(name = "idx_visitor_id", columnList = "visitor_id"),
    @Index(name = "idx_article_id", columnList = "article_id"),
    @Index(name = "idx_visit_date", columnList = "visit_date"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class VisitLog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 访客ID（Cookie/指纹）
     */
    @Column(nullable = false, length = 100)
    private String visitorId;

    /**
     * 文章ID（可为空，表示非文章页面访问）
     */
    @Column(name = "article_id")
    private Long articleId;

    /**
     * 访问日期（用于去重，同一访客同一天只计一次）
     */
    @Column(nullable = false, name = "visit_date")
    private LocalDate visitDate;

    /**
     * 访问页面URL
     */
    @Column(nullable = false, length = 500)
    private String pageUrl;

    /**
     * IP地址
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * User Agent
     */
    @Column(length = 500)
    private String userAgent;

    /**
     * 来源URL
     */
    @Column(length = 500)
    private String referer;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
