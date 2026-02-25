package com.blog.repository;

import com.blog.model.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 操作日志Repository接口
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    /**
     * 查询所有操作日志（分页）
     */
    Page<OperationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据操作人查询日志（分页）
     */
    Page<OperationLog> findByOperatorOrderByCreatedAtDesc(String operator, Pageable pageable);

    /**
     * 根据模块查询日志（分页）
     */
    Page<OperationLog> findByModuleOrderByCreatedAtDesc(String module, Pageable pageable);

    /**
     * 根据操作类型查询日志（分页）
     */
    Page<OperationLog> findByOperationTypeOrderByCreatedAtDesc(String operationType, Pageable pageable);

    /**
     * 根据时间范围查询日志（分页）
     */
    @Query("SELECT o FROM OperationLog o WHERE o.createdAt BETWEEN :startTime AND :endTime ORDER BY o.createdAt DESC")
    Page<OperationLog> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime,
                                        Pageable pageable);

    /**
     * 多条件查询（操作人、模块、操作类型、时间范围）
     */
    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:operator IS NULL OR o.operator = :operator) AND " +
           "(:module IS NULL OR o.module = :module) AND " +
           "(:operationType IS NULL OR o.operationType = :operationType) AND " +
           "(:startTime IS NULL OR o.createdAt >= :startTime) AND " +
           "(:endTime IS NULL OR o.createdAt <= :endTime) " +
           "ORDER BY o.createdAt DESC")
    Page<OperationLog> findByConditions(@Param("operator") String operator,
                                         @Param("module") String module,
                                         @Param("operationType") String operationType,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         Pageable pageable);
}
