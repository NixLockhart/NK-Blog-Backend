package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_operation_logs", indexes = {
    @Index(name = "idx_operator", columnList = "operator"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OperationLog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作人
     */
    @Column(nullable = false, length = 50)
    private String operator;

    /**
     * 操作模块
     */
    @Column(nullable = false, length = 50)
    private String module;

    /**
     * 操作类型（CREATE/UPDATE/DELETE等）
     */
    @Column(nullable = false, length = 20)
    private String operationType;

    /**
     * 操作对象
     */
    @Column(length = 100)
    private String operationObject;

    /**
     * 操作详情
     */
    @Column(columnDefinition = "TEXT")
    private String operationDetail;

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
     * 执行时长（毫秒）
     */
    private Long executionTime;

    /**
     * 操作结果: 0=失败, 1=成功
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer result = 1;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
