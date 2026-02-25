package com.blog.repository;

import com.blog.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 留言Repository接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 查询最新留言（分页）
     */
    Page<Message> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 查询所有留言（管理用，分页）
     */
    Page<Message> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 查询友情链接（填写了博客地址的留言）
     */
    List<Message> findByStatusAndIsFriendLinkOrderByCreatedAtDesc(Integer status, Integer isFriendLink);

    /**
     * 统计留言总数（不含已删除）
     */
    long countByStatusNot(Integer status);

    /**
     * 根据状态统计留言数
     */
    long countByStatus(Integer status);

    /**
     * 统计指定时间段的留言数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
