package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.comment.CommentCreateRequest;
import com.blog.model.dto.comment.CommentTreeResponse;
import com.blog.service.CommentService;
import com.blog.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论公开API控制器
 */
@Tag(name = "评论接口（公开）", description = "评论查询、发表等公开接口")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取文章评论", description = "获取指定文章的所有评论（树形结构）")
    @GetMapping("/article/{articleId}")
    public Result<List<CommentTreeResponse>> getArticleComments(
            @Parameter(description = "文章ID") @PathVariable Long articleId) {
        List<CommentTreeResponse> comments = commentService.getArticleComments(articleId);
        return Result.success(comments);
    }

    @Operation(summary = "发表评论", description = "发表新评论或回复评论（支持楼中楼）")
    @PostMapping
    public Result<Long> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = IpUtil.getIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Long commentId = commentService.createComment(request, ipAddress, userAgent);
        return Result.success(commentId);
    }
}
