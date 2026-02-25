package com.blog.model.entity;

import com.blog.config.JacksonConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_announcements")
@EntityListeners(AuditingEntityListener.class)
public class Announcement {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 公告标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 公告内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 开始显示时间
     */
    @JsonDeserialize(using = JacksonConfig.FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    /**
     * 结束显示时间
     */
    @JsonDeserialize(using = JacksonConfig.FlexibleLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    /**
     * 是否启用: 0=否, 1=是
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer enabled = 1;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
}
