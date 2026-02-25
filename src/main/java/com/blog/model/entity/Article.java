package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_articles", indexes = {
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_is_top", columnList = "is_top"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Article {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文章标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Markdown文件相对路径
     */
    @Column(nullable = false, length = 500)
    private String contentPath;

    /**
     * 分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 分类对象（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    /**
     * 摘要
     */
    @Column(length = 500)
    private String summary;

    /**
     * 封面图URL
     */
    @Column(length = 500)
    private String coverImage;

    /**
     * 状态: 0=已删除, 1=已发布, 2=草稿
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /**
     * 浏览量
     */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long views = 0L;

    /**
     * 点赞数
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer likes = 0;

    /**
     * 评论数
     */
    @Column(name = "comments_count", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer commentCount = 0;

    /**
     * 是否置顶: 0=否, 1=是
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isTop = 0;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 发布时间
     */
    private LocalDateTime publishedAt;
}
