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
 * 分类实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_categories")
@EntityListeners(AuditingEntityListener.class)
public class Category {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 分类描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 分类别名（URL友好）
     */
    @Column(length = 100)
    private String slug;

    /**
     * 排序权重（越大越靠前）
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sortOrder = 0;

    /**
     * 文章数量
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer articleCount = 0;

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
}
