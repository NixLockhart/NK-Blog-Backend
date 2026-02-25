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
 * 小工具实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_gadgets")
@EntityListeners(AuditingEntityListener.class)
public class Widget {

    /**
     * 小工具ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 小工具名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 代码文件路径
     */
    @Column(nullable = false, length = 255)
    private String codePath;

    /**
     * 封面图片路径
     */
    @Column(length = 255)
    private String coverPath;

    /**
     * 是否应用到博客
     */
    @Column(nullable = false)
    private Boolean isApplied = false;

    /**
     * 是否系统自带（不可删除）
     */
    @Column(nullable = false)
    private Boolean isSystem = false;

    /**
     * 显示顺序
     */
    @Column(nullable = false)
    private Integer displayOrder = 0;

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
