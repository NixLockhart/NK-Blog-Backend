package com.blog.repository;

import com.blog.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 文章Repository接口
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {

    /**
     * 根据状态查询文章（分页）
     */
    Page<Article> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    /**
     * 根据分类ID和状态查询文章（分页）
     */
    Page<Article> findByCategoryIdAndStatusOrderByCreatedAtDesc(Long categoryId, Integer status, Pageable pageable);

    /**
     * 根据状态查询置顶文章
     */
    List<Article> findByStatusAndIsTopOrderByCreatedAtDesc(Integer status, Integer isTop);

    /**
     * 根据ID和状态查询文章
     */
    Optional<Article> findByIdAndStatus(Long id, Integer status);

    /**
     * 搜索文章（标题或摘要）
     */
    @Query("SELECT a FROM Article a WHERE a.status = :status AND (a.title LIKE %:keyword% OR a.summary LIKE %:keyword%) ORDER BY a.createdAt DESC")
    Page<Article> searchArticles(@Param("keyword") String keyword, @Param("status") Integer status, Pageable pageable);

    /**
     * 查询热门文章（按浏览量排序）
     */
    @Query("SELECT a FROM Article a WHERE a.status = :status ORDER BY a.views DESC, a.createdAt DESC")
    List<Article> findHotArticles(@Param("status") Integer status, Pageable pageable);

    /**
     * 查询最新文章
     */
    @Query("SELECT a FROM Article a WHERE a.status = :status ORDER BY a.publishedAt DESC")
    List<Article> findLatestArticles(@Param("status") Integer status, Pageable pageable);

    /**
     * 增加浏览量
     */
    @Modifying
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(@Param("id") Long id);

    /**
     * 增加点赞数
     */
    @Modifying
    @Query("UPDATE Article a SET a.likes = a.likes + 1 WHERE a.id = :id")
    void incrementLikes(@Param("id") Long id);

    /**
     * 更新评论数
     */
    @Modifying
    @Query("UPDATE Article a SET a.commentCount = :count WHERE a.id = :id")
    void updateCommentCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 统计文章总数
     */
    long countByStatus(Integer status);

    /**
     * 统计分类下的文章数
     */
    long countByCategoryIdAndStatus(Long categoryId, Integer status);

    /**
     * 获取文章总点赞数
     */
    @Query("SELECT COALESCE(SUM(a.likes), 0) FROM Article a WHERE a.status = :status")
    Long getTotalLikes(@Param("status") Integer status);

    /**
     * 获取浏览量最高的文章（用于排行榜）
     */
    @Query("SELECT a.title, a.views FROM Article a WHERE a.status = 1 ORDER BY a.views DESC")
    List<Object[]> findTopArticlesByViews(int limit);
}
