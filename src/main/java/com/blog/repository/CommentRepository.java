package com.blog.repository;

import com.blog.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论Repository接口
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据文章ID和状态查询评论（树形结构：先查顶级评论）
     */
    List<Comment> findByArticleIdAndStatusAndParentIdIsNullOrderByCreatedAtAsc(Long articleId, Integer status);

    /**
     * 根据文章ID和状态查询所有评论（用于一次性构建评论树）
     */
    List<Comment> findByArticleIdAndStatusOrderByCreatedAtAsc(Long articleId, Integer status);

    /**
     * 根据父评论ID查询子评论
     */
    List<Comment> findByParentIdAndStatusOrderByCreatedAtAsc(Long parentId, Integer status);

    /**
     * 根据文章ID查询所有评论（包括已删除，用于管理）
     */
    Page<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId, Pageable pageable);

    /**
     * 查询所有评论（分页，用于管理）
     */
    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 根据文章ID查询评论（分页，用于管理）
     */
    Page<Comment> findByArticleId(Long articleId, Pageable pageable);

    /**
     * 根据状态查询评论（分页，用于管理）
     */
    Page<Comment> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据文章ID和状态查询评论（分页，用于管理）
     */
    Page<Comment> findByArticleIdAndStatus(Long articleId, Integer status, Pageable pageable);

    /**
     * 根据状态查询评论（分页）
     */
    Page<Comment> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 统计文章的评论数（不含已删除）
     */
    long countByArticleIdAndStatusNot(Long articleId, Integer status);

    /**
     * 统计总评论数（不含已删除）
     */
    long countByStatusNot(Integer status);

    /**
     * 根据状态统计评论数
     */
    long countByStatus(Integer status);

    /**
     * 统计指定时间段的评论数
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询指定父评论下的所有子评论ID（用于级联删除）
     */
    @Query("SELECT c.id FROM Comment c WHERE c.parentId = :parentId")
    List<Long> findIdsByParentId(@Param("parentId") Long parentId);
}
