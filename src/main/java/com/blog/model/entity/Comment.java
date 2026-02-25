package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 评论实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_comments", indexes = {
    @Index(name = "idx_article_id", columnList = "article_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文章ID
     */
    @Column(nullable = false, name = "article_id")
    private Long articleId;

    /**
     * 父评论ID（楼中楼）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 评论者昵称
     */
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 评论者邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 评论者网站
     */
    @Column(length = 200)
    private String website;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 评论内容（支持Markdown）
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

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
     * 状态: 0=已删除, 1=已审核, 2=待审核
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 2")
    private Integer status = 2;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
