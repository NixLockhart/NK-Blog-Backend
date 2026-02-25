package com.blog.repository;

import com.blog.model.entity.ErrorLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 错误日志Repository接口
 */
@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    /**
     * 查询所有错误日志（分页）
     */
    Page<ErrorLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据错误级别查询日志（分页）
     */
    Page<ErrorLog> findByErrorLevelOrderByCreatedAtDesc(String errorLevel, Pageable pageable);

    /**
     * 根据时间范围查询日志（分页）
     */
    @Query("SELECT e FROM ErrorLog e WHERE e.createdAt BETWEEN :startTime AND :endTime ORDER BY e.createdAt DESC")
    Page<ErrorLog> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    Pageable pageable);

    /**
     * 搜索错误日志（关键词）
     */
    @Query("SELECT e FROM ErrorLog e WHERE e.errorMessage LIKE %:keyword% OR e.exceptionClass LIKE %:keyword% ORDER BY e.createdAt DESC")
    Page<ErrorLog> searchLogs(@Param("keyword") String keyword, Pageable pageable);
}
