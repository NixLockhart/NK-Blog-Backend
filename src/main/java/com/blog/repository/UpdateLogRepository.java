package com.blog.repository;

import com.blog.model.entity.UpdateLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 更新日志Repository接口
 */
@Repository
public interface UpdateLogRepository extends JpaRepository<UpdateLog, Long> {

    /**
     * 查询所有更新日志（按发布日期倒序，分页）
     */
    Page<UpdateLog> findAllByOrderByReleaseDateDesc(Pageable pageable);

    /**
     * 查询所有更新日志（按发布日期倒序）
     */
    List<UpdateLog> findAllByOrderByReleaseDateDesc();

    /**
     * 查询重大更新日志（按发布日期倒序）
     */
    List<UpdateLog> findByIsMajorOrderByReleaseDateDesc(Integer isMajor);

    /**
     * 检查版本号是否存在
     */
    boolean existsByVersion(String version);
}
