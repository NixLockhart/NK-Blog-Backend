package com.blog.service;

import com.blog.common.response.PageResult;
import com.blog.model.dto.comment.AdminCommentResponse;
import com.blog.model.dto.comment.CommentCreateRequest;
import com.blog.model.dto.comment.CommentTreeResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 获取文章的评论列表（树形结构）
     */
    List<CommentTreeResponse> getArticleComments(Long articleId);

    /**
     * 发表评论
     */
    Long createComment(CommentCreateRequest request, String ipAddress, String userAgent);

    /**
     * 获取评论管理列表（分页）
     */
    PageResult<AdminCommentResponse> getCommentsForAdmin(Pageable pageable, Long articleId, Integer status);

    /**
     * 删除评论（级联删除子评论）
     */
    void deleteComment(Long id);

    /**
     * 审核评论
     */
    void approveComment(Long id, Integer status);
}
