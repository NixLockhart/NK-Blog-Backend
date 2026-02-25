package com.blog.repository;

import com.blog.model.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 公告Repository接口
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    /**
     * 查询当前有效的公告（时间范围内且已启用）
     * 注意: 使用Pageable限制只返回第一个结果
     */
    @Query("SELECT a FROM Announcement a WHERE a.enabled = 1 AND a.startTime <= :now AND a.endTime >= :now ORDER BY a.createdAt DESC")
    List<Announcement> findActiveAnnouncement(@Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    /**
     * 查询所有公告（按创建时间倒序）
     */
    List<Announcement> findAllByOrderByCreatedAtDesc();

    /**
     * 根据启用状态查询公告
     */
    List<Announcement> findByEnabledOrderByCreatedAtDesc(Integer enabled);
}
