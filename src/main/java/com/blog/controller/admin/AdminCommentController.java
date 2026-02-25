package com.blog.controller.admin;

import com.blog.common.response.PageResult;
import com.blog.common.response.Result;
import com.blog.model.dto.comment.AdminCommentResponse;
import com.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 评论管理API控制器
 */
@Tag(name = "评论管理接口", description = "评论审核、删除等管理功能")
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminCommentController {

    private final CommentService commentService;

    @Operation(summary = "获取评论管理列表", description = "分页获取所有评论（包括待审核和已删除）")
    @GetMapping
    public Result<PageResult<AdminCommentResponse>> getCommentsForAdmin(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "文章ID筛选") @RequestParam(required = false) Long articleId,
            @Parameter(description = "状态筛选(1=已审核, 2=待审核)") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序方向(asc/desc)") @RequestParam(defaultValue = "desc") String sort) {

        // 构建排序规则
        size = Math.min(size, 100);
        Sort.Direction direction = "asc".equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createdAt"));

        PageResult<AdminCommentResponse> result = commentService.getCommentsForAdmin(pageable, articleId, status);
        return Result.success(result);
    }

    @Operation(summary = "删除评论", description = "删除评论（级联删除所有子评论）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success(null);
    }

    @Operation(summary = "审核评论", description = "审核评论（1=通过, 2=待审核）")
    @PutMapping("/{id}/status")
    public Result<Void> approveComment(
            @Parameter(description = "评论ID") @PathVariable Long id,
            @Parameter(description = "状态: 1=通过, 2=待审核") @RequestParam Integer status) {
        commentService.approveComment(id, status);
        return Result.success(null);
    }
}
