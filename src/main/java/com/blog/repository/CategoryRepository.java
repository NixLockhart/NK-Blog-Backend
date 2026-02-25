package com.blog.repository;

import com.blog.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 分类Repository接口
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 查询所有分类（按排序权重降序）
     */
    List<Category> findAllByOrderBySortOrderDesc();

    /**
     * 根据名称查询分类
     */
    boolean existsByName(String name);

    /**
     * 根据名称查询分类（排除指定ID）
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 增加文章数量
     */
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount + 1 WHERE c.id = :id")
    void incrementArticleCount(@Param("id") Long id);

    /**
     * 减少文章数量
     */
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = c.articleCount - 1 WHERE c.id = :id AND c.articleCount > 0")
    void decrementArticleCount(@Param("id") Long id);

    /**
     * 更新文章数量
     */
    @Modifying
    @Query("UPDATE Category c SET c.articleCount = :count WHERE c.id = :id")
    void updateArticleCount(@Param("id") Long id, @Param("count") Integer count);
}
