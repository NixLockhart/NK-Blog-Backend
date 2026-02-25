package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.model.dto.category.CategoryResponse;
import com.blog.model.dto.category.CategorySaveRequest;
import com.blog.model.entity.Category;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategoryRepository;
import com.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    private static final Integer STATUS_PUBLISHED = 1;

    @Override
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAllByOrderBySortOrderDesc();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        return convertToResponse(category);
    }

    @Override
    @Transactional
    public Long createCategory(CategorySaveRequest request) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY);
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setArticleCount(0);
        // 使用前端传入的 slug，如果为空则自动生成
        String slug = request.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.getName());
        }
        category.setSlug(slug.trim());

        category = categoryRepository.save(category);

        log.info("创建分类成功: id={}, name={}", category.getId(), category.getName());
        return category.getId();
    }

    @Override
    @Transactional
    public void updateCategory(Long id, CategorySaveRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 检查分类名称是否与其他分类重复
        if (categoryRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_CATEGORY);
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder());
        // 更新 slug（如果提供了新的 slug）
        if (request.getSlug() != null && !request.getSlug().trim().isEmpty()) {
            category.setSlug(request.getSlug().trim());
        }

        categoryRepository.save(category);

        log.info("更新分类成功: id={}, name={}", category.getId(), category.getName());
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, boolean deleteArticles) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 检查分类下是否有文章
        long articleCount = articleRepository.countByCategoryIdAndStatus(id, STATUS_PUBLISHED);
        if (articleCount > 0 && !deleteArticles) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_ARTICLES);
        }

        if (deleteArticles && articleCount > 0) {
            // 删除分类下的所有文章（软删除）
            articleRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(id, STATUS_PUBLISHED,
                    org.springframework.data.domain.Pageable.unpaged())
                    .forEach(article -> {
                        article.setStatus(0); // 软删除
                        articleRepository.save(article);
                    });
        }

        categoryRepository.delete(category);

        log.info("删除分类成功: id={}, name={}, deleteArticles={}", id, category.getName(), deleteArticles);
    }

    @Override
    @Transactional
    public void updateCategoriesSort(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

            // 排序权重：列表中越靠前的权重越大
            category.setSortOrder(categoryIds.size() - i);
            categoryRepository.save(category);
        }

        log.info("更新分类排序成功");
    }

    /**
     * 生成分类别名（URL友好）
     * 简单实现：使用时间戳 + 名称哈希
     */
    private String generateSlug(String name) {
        if (name == null || name.isEmpty()) {
            return "category-" + System.currentTimeMillis();
        }
        // 简单的 slug 生成：使用名称的哈希值 + 时间戳后4位
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        String hash = Integer.toHexString(name.hashCode()).replace("-", "");
        return "cat-" + hash + "-" + timestamp;
    }

    /**
     * 转换为响应DTO
     */
    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setSortOrder(category.getSortOrder());
        response.setArticleCount(category.getArticleCount());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        return response;
    }
}
