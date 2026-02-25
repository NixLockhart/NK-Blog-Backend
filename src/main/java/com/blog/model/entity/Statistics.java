package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 统计实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_statistics", indexes = {
    @Index(name = "idx_stat_date", columnList = "stat_date", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class Statistics {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 统计日期
     */
    @Column(nullable = false, unique = true)
    private LocalDate statDate;

    /**
     * 当日访问量
     */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long dailyVisits = 0L;

    /**
     * 当日独立访客数
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer dailyUniqueVisitors = 0;

    /**
     * 总访问量
     */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long totalVisits = 0L;

    /**
     * 总独立访客数
     */
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private Long totalUniqueVisitors = 0L;

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
