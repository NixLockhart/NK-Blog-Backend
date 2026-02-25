package com.blog.repository;

import com.blog.model.entity.Widget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 小工具Repository接口
 */
@Repository
public interface WidgetRepository extends JpaRepository<Widget, Long> {

    /**
     * 查询所有已应用的小工具（按显示顺序排序）
     */
    List<Widget> findByIsAppliedTrueOrderByDisplayOrderAsc();

    /**
     * 查询所有小工具（按显示顺序排序）
     */
    List<Widget> findAllByOrderByDisplayOrderAsc();

    /**
     * 统计已应用的小工具数量
     */
    long countByIsAppliedTrue();
}
