package com.blog.service.impl;

import com.blog.common.enums.CommentStatus;
import com.blog.common.enums.ErrorCode;
import com.blog.common.response.PageResult;
import com.blog.exception.BusinessException;
import com.blog.model.dto.comment.AdminCommentResponse;
import com.blog.model.dto.comment.CommentCreateRequest;
import com.blog.model.dto.comment.CommentTreeResponse;
import com.blog.model.entity.Article;
import com.blog.util.HtmlSanitizer;
import com.blog.model.entity.Comment;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    private static final Integer STATUS_DELETED = CommentStatus.DELETED.getValue();
    private static final Integer STATUS_APPROVED = CommentStatus.APPROVED.getValue();
    private static final Integer STATUS_PENDING = CommentStatus.PENDING.getValue();

    @Override
    @Transactional(readOnly = true)
    public List<CommentTreeResponse> getArticleComments(Long articleId) {
        // 一次查出文章所有已审核评论
        List<Comment> allComments = commentRepository
                .findByArticleIdAndStatusOrderByCreatedAtAsc(articleId, STATUS_APPROVED);

        // 按 parentId 分组
        Map<Long, List<Comment>> childrenMap = allComments.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 构建树形结构（顶级评论 parentId 为 null）
        return allComments.stream()
                .filter(c -> c.getParentId() == null)
                .map(comment -> buildCommentTree(comment, childrenMap))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createComment(CommentCreateRequest request, String ipAddress, String userAgent) {
        // 检查文章是否存在
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

        // 如果是回复评论，检查父评论是否存在
        if (request.getParentId() != null) {
            commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setParentId(request.getParentId());
        comment.setNickname(HtmlSanitizer.sanitizeStrict(request.getNickname()));
        comment.setEmail(request.getEmail());
        comment.setWebsite(HtmlSanitizer.sanitizeStrict(request.getWebsite()));
        comment.setAvatar(request.getAvatar());
        comment.setContent(HtmlSanitizer.sanitizeContent(request.getContent()));
        comment.setIpAddress(ipAddress);
        comment.setUserAgent(userAgent);
        comment.setStatus(STATUS_APPROVED); // 默认自动审核通过

        comment = commentRepository.save(comment);

        // 更新文章评论数
        long commentCount = commentRepository.countByArticleIdAndStatusNot(article.getId(), STATUS_DELETED);
        articleRepository.updateCommentCount(article.getId(), (int) commentCount);

        log.info("发表评论成功: id={}, articleId={}, nickname={}", comment.getId(), request.getArticleId(), request.getNickname());
        return comment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AdminCommentResponse> getCommentsForAdmin(Pageable pageable, Long articleId, Integer status) {
        Page<Comment> page;

        // 根据筛选条件选择合适的查询方法
        if (articleId != null && status != null) {
            // 同时按文章ID和状态筛选
            page = commentRepository.findByArticleIdAndStatus(articleId, status, pageable);
        } else if (articleId != null) {
            // 只按文章ID筛选
            page = commentRepository.findByArticleId(articleId, pageable);
        } else if (status != null) {
            // 只按状态筛选
            page = commentRepository.findByStatus(status, pageable);
        } else {
            // 不筛选，查询所有
            page = commentRepository.findAll(pageable);
        }

        // 获取所有相关文章ID并批量查询文章标题
        Set<Long> articleIds = page.getContent().stream()
                .map(Comment::getArticleId)
                .collect(Collectors.toSet());
        Map<Long, String> articleTitleMap = articleRepository.findAllById(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, Article::getTitle));

        List<AdminCommentResponse> content = page.getContent().stream()
                .map(comment -> convertToAdminResponse(comment, articleTitleMap))
                .collect(Collectors.toList());

        return PageResult.of(content, page);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 获取所有子评论ID
        List<Long> childIds = commentRepository.findIdsByParentId(id);

        // 递归删除所有子评论
        for (Long childId : childIds) {
            deleteComment(childId);
        }

        // 软删除当前评论
        comment.setStatus(STATUS_DELETED);
        commentRepository.save(comment);

        // 更新文章评论数
        long commentCount = commentRepository.countByArticleIdAndStatusNot(comment.getArticleId(), STATUS_DELETED);
        articleRepository.updateCommentCount(comment.getArticleId(), (int) commentCount);

        log.info("删除评论成功: id={}", id);
    }

    @Override
    @Transactional
    public void approveComment(Long id, Integer status) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        comment.setStatus(status);
        commentRepository.save(comment);

        // 更新文章评论数
        long commentCount = commentRepository.countByArticleIdAndStatusNot(comment.getArticleId(), STATUS_DELETED);
        articleRepository.updateCommentCount(comment.getArticleId(), (int) commentCount);

        log.info("审核评论成功: id={}, status={}", id, status);
    }

    /**
     * 构建评论树
     */
    private CommentTreeResponse buildCommentTree(Comment comment, Map<Long, List<Comment>> childrenMap) {
        CommentTreeResponse response = convertToResponse(comment);

        // 获取子评论
        List<Comment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        response.setChildren(children.stream()
                .map(child -> buildCommentTree(child, childrenMap))
                .collect(Collectors.toList()));

        return response;
    }

    /**
     * 转换为响应DTO
     */
    private CommentTreeResponse convertToResponse(Comment comment) {
        CommentTreeResponse response = new CommentTreeResponse();
        response.setId(comment.getId());
        response.setArticleId(comment.getArticleId());
        response.setParentId(comment.getParentId());
        response.setNickname(comment.getNickname());
        response.setEmail(comment.getEmail());
        response.setWebsite(comment.getWebsite());
        response.setAvatar(comment.getAvatar());
        response.setContent(comment.getContent());
        response.setStatus(comment.getStatus());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }

    /**
     * 转换为管理后台响应DTO
     */
    private AdminCommentResponse convertToAdminResponse(Comment comment, Map<Long, String> articleTitleMap) {
        AdminCommentResponse response = new AdminCommentResponse();
        response.setId(comment.getId());
        response.setArticleId(comment.getArticleId());
        response.setArticleTitle(articleTitleMap.getOrDefault(comment.getArticleId(), "未知文章"));
        response.setParentId(comment.getParentId());
        response.setNickname(comment.getNickname());
        response.setEmail(comment.getEmail());
        response.setWebsite(comment.getWebsite());
        response.setAvatar(comment.getAvatar());
        response.setContent(comment.getContent());
        response.setIpAddress(comment.getIpAddress());
        response.setUserAgent(comment.getUserAgent());
        response.setStatus(comment.getStatus());
        response.setCreatedAt(comment.getCreatedAt());
        return response;
    }
}
