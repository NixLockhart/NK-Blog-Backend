package com.blog.service;

import com.blog.model.dto.category.CategoryResponse;
import com.blog.model.dto.category.CategorySaveRequest;

import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类（按排序权重）
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 根据ID获取分类
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 创建分类
     */
    Long createCategory(CategorySaveRequest request);

    /**
     * 更新分类
     */
    void updateCategory(Long id, CategorySaveRequest request);

    /**
     * 删除分类
     */
    void deleteCategory(Long id, boolean deleteArticles);

    /**
     * 调整分类排序
     */
    void updateCategoriesSort(List<Long> categoryIds);
}
