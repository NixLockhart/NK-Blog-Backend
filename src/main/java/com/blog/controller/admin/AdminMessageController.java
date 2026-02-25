package com.blog.controller.admin;

import com.blog.common.response.PageResult;
import com.blog.common.response.Result;
import com.blog.model.dto.message.MessageResponse;
import com.blog.service.MessageService;
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
 * 留言管理API控制器
 */
@Tag(name = "留言管理接口", description = "留言删除、友链管理等功能")
@RestController
@RequestMapping("/api/admin/messages")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminMessageController {

    private final MessageService messageService;

    @Operation(summary = "获取留言管理列表", description = "分页获取所有留言（包括已删除）")
    @GetMapping
    public Result<PageResult<MessageResponse>> getMessagesForAdmin(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResult<MessageResponse> result = messageService.getMessagesForAdmin(pageable);
        return Result.success(result);
    }

    @Operation(summary = "删除留言", description = "软删除留言")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(
            @Parameter(description = "留言ID") @PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.success(null);
    }

    @Operation(summary = "设置/取消友链标记", description = "切换留言的友情链接标记")
    @PutMapping("/{id}/friend")
    public Result<Void> toggleFriendLink(
            @Parameter(description = "留言ID") @PathVariable Long id) {
        messageService.toggleFriendLink(id);
        return Result.success(null);
    }
}
