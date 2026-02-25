package com.blog.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 更新日志实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_update_logs")
@EntityListeners(AuditingEntityListener.class)
public class UpdateLog {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 版本号
     */
    @Column(nullable = false, unique = true, length = 20)
    private String version;

    /**
     * 更新标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * Markdown文件路径（相对路径，如 /update-logs/v2.0.1.md）
     */
    @Column(nullable = false, length = 500)
    private String contentPath;

    /**
     * 发布日期
     */
    @Column(nullable = false)
    private LocalDateTime releaseDate;

    /**
     * 是否为重大更新: 0=否, 1=是
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isMajor = 0;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
}
