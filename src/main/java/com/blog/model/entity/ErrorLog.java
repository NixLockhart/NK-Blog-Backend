package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 错误日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_error_logs", indexes = {
    @Index(name = "idx_error_level", columnList = "error_level"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class ErrorLog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 错误级别（ERROR/WARN）
     */
    @Column(nullable = false, length = 10)
    private String errorLevel;

    /**
     * 错误信息
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 异常类名
     */
    @Column(length = 200)
    private String exceptionClass;

    /**
     * 堆栈跟踪
     */
    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * 请求方法
     */
    @Column(length = 10)
    private String requestMethod;

    /**
     * 请求URL
     */
    @Column(length = 500)
    private String requestUrl;

    /**
     * 请求参数
     */
    @Column(columnDefinition = "TEXT")
    private String requestParams;

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
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
