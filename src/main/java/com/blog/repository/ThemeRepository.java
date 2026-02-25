package com.blog.repository;

import com.blog.model.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 主题Repository接口
 */
@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    /**
     * 查询当前应用的主题
     */
    Optional<Theme> findByIsActive(Integer isActive);

    /**
     * 根据名称查询主题
     */
    Optional<Theme> findByName(String name);

    /**
     * 根据slug查询主题
     */
    Optional<Theme> findBySlug(String slug);

    /**
     * 查询所有主题（按显示顺序升序）
     */
    List<Theme> findAllByOrderByDisplayOrderAsc();

    /**
     * 检查主题名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查主题标识是否存在
     */
    boolean existsBySlug(String slug);

    /**
     * 检查主题名称是否存在（排除指定ID）
     */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 检查主题标识是否存在（排除指定ID）
     */
    boolean existsBySlugAndIdNot(String slug, Long id);
}
