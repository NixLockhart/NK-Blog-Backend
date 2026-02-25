package com.blog.repository;

import com.blog.model.entity.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 统计Repository接口
 */
@Repository
public interface StatisticsRepository extends JpaRepository<Statistics, Long> {

    /**
     * 根据统计日期查询
     */
    Optional<Statistics> findByStatDate(LocalDate statDate);

    /**
     * 查询最新的统计记录
     */
    Optional<Statistics> findTopByOrderByStatDateDesc();

    /**
     * 查询指定日期范围的统计（用于图表）
     */
    @Query("SELECT s FROM Statistics s WHERE s.statDate BETWEEN :startDate AND :endDate ORDER BY s.statDate ASC")
    List<Statistics> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询最近N天的统计
     */
    @Query("SELECT s FROM Statistics s ORDER BY s.statDate DESC")
    List<Statistics> findRecentStatistics(org.springframework.data.domain.Pageable pageable);
}
