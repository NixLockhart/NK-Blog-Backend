package com.blog.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 留言实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_messages", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_is_friend_link", columnList = "is_friend_link")
})
@EntityListeners(AuditingEntityListener.class)
public class Message {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 留言者昵称
     */
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 留言者邮箱
     */
    @Column(length = 100)
    private String email;

    /**
     * 网站地址
     */
    @Column(length = 200)
    private String website;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatar;

    /**
     * 留言内容
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * IP地址
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * 状态: 0=已删除, 1=已显示, 2=待审核
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /**
     * 是否为友情链接: 0=否, 1=是
     */
    @Column(nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isFriendLink = 0;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
