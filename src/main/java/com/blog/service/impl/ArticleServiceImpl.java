package com.blog.service.impl;

import com.blog.common.enums.ArticleStatus;
import com.blog.common.enums.ErrorCode;
import com.blog.common.response.PageResult;
import com.blog.exception.BusinessException;
import com.blog.model.dto.article.ArticleDetailResponse;
import com.blog.model.dto.article.ArticleListResponse;
import com.blog.model.dto.article.ArticleSaveRequest;
import com.blog.model.entity.Article;
import com.blog.model.entity.Category;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategoryRepository;
import com.blog.service.ArticleService;
import com.blog.service.FileService;
import com.blog.service.MarkdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MarkdownService markdownService;
    private final FileService fileService;

    private static final Integer STATUS_PUBLISHED = ArticleStatus.PUBLISHED.getValue();
    private static final Integer STATUS_DRAFT = ArticleStatus.DRAFT.getValue();
    private static final Integer STATUS_DELETED = ArticleStatus.DELETED.getValue();

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> getArticleList(Long categoryId, String keyword, Pageable pageable) {
        Page<Article> page;

        if (keyword != null && !keyword.isEmpty()) {
            // 搜索文章
            page = articleRepository.searchArticles(keyword, STATUS_PUBLISHED, pageable);
        } else if (categoryId != null) {
            // 按分类查询
            page = articleRepository.findByCategoryIdAndStatusOrderByCreatedAtDesc(categoryId, STATUS_PUBLISHED, pageable);
        } else {
            // 查询所有已发布文章
            page = articleRepository.findByStatusOrderByCreatedAtDesc(STATUS_PUBLISHED, pageable);
        }

        List<ArticleListResponse> content = convertToListResponses(page.getContent());

        return PageResult.of(content, page);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailResponse getArticleDetail(Long id) {
        Article article = articleRepository.findByIdAndStatus(id, STATUS_PUBLISHED)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        return convertToDetailResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleListResponse> getTopArticles() {
        List<Article> articles = articleRepository.findByStatusAndIsTopOrderByCreatedAtDesc(STATUS_PUBLISHED, 1);
        return convertToListResponses(articles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleListResponse> getHotArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> articles = articleRepository.findHotArticles(STATUS_PUBLISHED, pageable);
        return convertToListResponses(articles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleListResponse> getLatestArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Article> articles = articleRepository.findLatestArticles(STATUS_PUBLISHED, pageable);
        return convertToListResponses(articles);
    }

    @Override
    @Transactional
    public void incrementViews(Long id) {
        articleRepository.incrementViews(id);
    }

    @Override
    @Transactional
    public void incrementLikes(Long id) {
        // 检查文章是否存在
        if (!articleRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        articleRepository.incrementLikes(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> getArticleListForAdmin(Pageable pageable, String keyword, Long categoryId, Integer status) {
        Specification<Article> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(root.get("title"), pattern),
                cb.like(root.get("summary"), pattern)
            ));
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoryId"), categoryId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Article> page = articleRepository.findAll(spec, pageable);
        List<ArticleListResponse> content = convertToListResponses(page.getContent());

        return PageResult.of(content, page);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDetailResponse getArticleDetailForAdmin(Long id) {
        // 管理端获取文章详情，不限制状态
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        return convertToDetailResponse(article);
    }

    @Override
    @Transactional
    public ArticleDetailResponse createArticle(ArticleSaveRequest request) {
        // 检查分类是否存在（如果提供了分类ID）
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        // 先创建文章实体（临时保存，获取ID）
        Article article = new Article();
        article.setTitle(request.getTitle() == null || request.getTitle().trim().isEmpty()
            ? "未命名文章" : request.getTitle());
        article.setContentPath(""); // 临时空路径
        article.setCategoryId(request.getCategoryId());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());
        article.setIsTop(0);
        article.setViews(0L);
        article.setLikes(0);
        article.setCommentCount(0);

        // 如果是发布状态，设置发布时间
        if (request.getStatus().equals(STATUS_PUBLISHED)) {
            article.setPublishedAt(LocalDateTime.now());
        }

        article = articleRepository.save(article);

        // 使用文章ID保存Markdown文件
        String contentPath = markdownService.saveMarkdownFile(request.getContent(), article.getId() + ".md");
        article.setContentPath(contentPath);
        articleRepository.save(article);

        // 更新分类文章数
        if (request.getStatus().equals(STATUS_PUBLISHED) && request.getCategoryId() != null) {
            categoryRepository.incrementArticleCount(request.getCategoryId());
        }

        log.info("创建文章成功: id={}, title={}", article.getId(), article.getTitle());
        return convertToDetailResponse(article);
    }

    @Override
    @Transactional
    public ArticleDetailResponse updateArticle(Long id, ArticleSaveRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        // 检查分类是否存在（如果提供了分类ID）
        if (request.getCategoryId() != null && !request.getCategoryId().equals(article.getCategoryId())) {
            categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        }

        // 记录原分类和状态
        Long oldCategoryId = article.getCategoryId();
        Integer oldStatus = article.getStatus();

        // 更新Markdown文件，使用文章ID作为文件名
        String contentPath = markdownService.saveMarkdownFile(request.getContent(), id + ".md");

        // 处理封面图片更新
        String oldCoverImage = article.getCoverImage();
        String newCoverImage = request.getCoverImage();

        // 如果封面图片发生变化，删除旧封面
        if (oldCoverImage != null && !oldCoverImage.isEmpty()
            && newCoverImage != null && !newCoverImage.isEmpty()
            && !oldCoverImage.equals(newCoverImage)) {
            try {
                fileService.deleteFile(oldCoverImage);
                log.info("删除旧封面成功: {}", oldCoverImage);
            } catch (Exception e) {
                log.warn("删除旧封面失败: {}", oldCoverImage, e);
            }
        }

        // 更新文章信息
        article.setTitle(request.getTitle() == null || request.getTitle().trim().isEmpty()
            ? "未命名文章" : request.getTitle());
        article.setContentPath(contentPath);
        article.setCategoryId(request.getCategoryId());
        article.setSummary(request.getSummary());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus());

        // 如果从草稿变为发布，设置发布时间
        if (!oldStatus.equals(STATUS_PUBLISHED) && request.getStatus().equals(STATUS_PUBLISHED)) {
            article.setPublishedAt(LocalDateTime.now());
        }

        articleRepository.save(article);

        // 更新分类文章数
        updateCategoryArticleCount(oldCategoryId, oldStatus, request.getCategoryId(), request.getStatus());

        log.info("更新文章成功: id={}, title={}", article.getId(), article.getTitle());
        return convertToDetailResponse(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        // 软删除
        article.setStatus(STATUS_DELETED);
        articleRepository.save(article);

        // 更新分类文章数
        if (article.getCategoryId() != null) {
            categoryRepository.decrementArticleCount(article.getCategoryId());
        }

        log.info("删除文章成功: id={}, title={}", article.getId(), article.getTitle());
    }

    @Override
    @Transactional
    public void toggleTop(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        // 切换置顶状态
        article.setIsTop(article.getIsTop() == 1 ? 0 : 1);
        articleRepository.save(article);

        log.info("切换文章置顶状态: id={}, isTop={}", article.getId(), article.getIsTop());
    }

    /**
     * 批量构建分类名称映射
     */
    private Map<Long, String> buildCategoryNameMap(List<Article> articles) {
        Set<Long> categoryIds = articles.stream()
                .map(Article::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    /**
     * 批量转换为列表响应DTO
     */
    private List<ArticleListResponse> convertToListResponses(List<Article> articles) {
        Map<Long, String> categoryNameMap = buildCategoryNameMap(articles);
        return articles.stream()
                .map(article -> convertToListResponse(article, categoryNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 转换为列表响应DTO
     */
    private ArticleListResponse convertToListResponse(Article article, Map<Long, String> categoryNameMap) {
        ArticleListResponse response = new ArticleListResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        response.setSummary(article.getSummary());
        response.setCoverImage(article.getCoverImage());
        response.setCategoryId(article.getCategoryId());

        if (article.getCategoryId() != null) {
            response.setCategoryName(categoryNameMap.get(article.getCategoryId()));
        }

        response.setViews(article.getViews());
        response.setLikes(article.getLikes());
        response.setCommentCount(article.getCommentCount());
        response.setIsTop(article.getIsTop());
        response.setStatus(article.getStatus());
        response.setPublishedAt(article.getPublishedAt());
        response.setCreatedAt(article.getCreatedAt());

        return response;
    }

    /**
     * 转换为详情响应DTO
     */
    private ArticleDetailResponse convertToDetailResponse(Article article) {
        ArticleDetailResponse response = new ArticleDetailResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());

        // 读取并解析Markdown内容
        String markdownContent = markdownService.readMarkdownFile(article.getContentPath());
        response.setMarkdownContent(markdownContent);
        response.setContent(markdownService.markdownToHtml(markdownContent));
        response.setToc(markdownService.generateToc(markdownContent));

        response.setSummary(article.getSummary());
        response.setCoverImage(article.getCoverImage());
        response.setCategoryId(article.getCategoryId());

        // 获取分类名称（避免懒加载异常，直接通过categoryId查询）
        if (article.getCategoryId() != null) {
            categoryRepository.findById(article.getCategoryId())
                    .ifPresent(category -> response.setCategoryName(category.getName()));
        }

        response.setViews(article.getViews());
        response.setLikes(article.getLikes());
        response.setCommentCount(article.getCommentCount());
        response.setIsTop(article.getIsTop());
        response.setStatus(article.getStatus());
        response.setPublishedAt(article.getPublishedAt());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());

        return response;
    }

    /**
     * 更新分类文章数
     */
    private void updateCategoryArticleCount(Long oldCategoryId, Integer oldStatus,
                                              Long newCategoryId, Integer newStatus) {
        boolean oldPublished = STATUS_PUBLISHED.equals(oldStatus);
        boolean newPublished = STATUS_PUBLISHED.equals(newStatus);

        if (oldPublished && !newPublished) {
            // 从发布变为草稿/删除
            if (oldCategoryId != null) {
                categoryRepository.decrementArticleCount(oldCategoryId);
            }
        } else if (!oldPublished && newPublished) {
            // 从草稿/删除变为发布
            if (newCategoryId != null) {
                categoryRepository.incrementArticleCount(newCategoryId);
            }
        } else if (oldPublished && newPublished && oldCategoryId != null && newCategoryId != null && !oldCategoryId.equals(newCategoryId)) {
            // 更换分类（都是发布状态）
            categoryRepository.decrementArticleCount(oldCategoryId);
            categoryRepository.incrementArticleCount(newCategoryId);
        }
    }
}
