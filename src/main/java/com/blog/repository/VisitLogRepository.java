package com.blog.repository;

import com.blog.model.entity.VisitLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 访问记录Repository接口
 */
@Repository
public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    /**
     * 查询所有访问记录（分页）
     */
    Page<VisitLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 检查访客今天是否已访问特定文章
     */
    boolean existsByVisitorIdAndArticleIdAndVisitDate(String visitorId, Long articleId, LocalDate visitDate);

    /**
     * 检查访客今天是否已访问首页
     */
    boolean existsByVisitorIdAndArticleIdIsNullAndVisitDate(String visitorId, LocalDate visitDate);

    /**
     * 检查访客今天是否已访问
     */
    @Query("SELECT COUNT(v) > 0 FROM VisitLog v WHERE v.visitorId = :visitorId AND v.createdAt >= :startOfDay AND v.createdAt < :endOfDay")
    boolean existsByVisitorIdAndToday(@Param("visitorId") String visitorId,
                                       @Param("startOfDay") LocalDateTime startOfDay,
                                       @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计指定日期的访问量
     */
    @Query("SELECT COUNT(v) FROM VisitLog v WHERE v.createdAt >= :startOfDay AND v.createdAt < :endOfDay")
    long countByDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计指定日期的独立访客数
     */
    @Query("SELECT COUNT(DISTINCT v.visitorId) FROM VisitLog v WHERE v.createdAt >= :startOfDay AND v.createdAt < :endOfDay")
    long countUniqueVisitorsByDate(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    /**
     * 统计总访问量
     */
    @Query("SELECT COUNT(v) FROM VisitLog v")
    long countTotalVisits();

    /**
     * 按日期分组统计访问量
     */
    @Query("SELECT v.visitDate, COUNT(v) FROM VisitLog v WHERE v.visitDate BETWEEN :startDate AND :endDate GROUP BY v.visitDate ORDER BY v.visitDate ASC")
    List<Object[]> countByDateGrouped(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计总独立访客数
     */
    @Query("SELECT COUNT(DISTINCT v.visitorId) FROM VisitLog v")
    long countTotalUniqueVisitors();

    /**
     * 删除指定文章的所有访问记录（用于文章永久删除时级联清理）
     */
    void deleteByArticleId(Long articleId);
}
