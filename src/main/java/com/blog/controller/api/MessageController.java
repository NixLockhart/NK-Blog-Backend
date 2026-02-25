package com.blog.controller.api;

import com.blog.common.response.PageResult;
import com.blog.common.response.Result;
import com.blog.model.dto.message.MessageCreateRequest;
import com.blog.model.dto.message.MessageResponse;
import com.blog.service.MessageService;
import com.blog.util.IpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 留言公开API控制器
 */
@Tag(name = "留言接口（公开）", description = "留言查询、发表等公开接口")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "获取留言列表", description = "分页获取留言列表（最新的在前）")
    @GetMapping
    public Result<PageResult<MessageResponse>> getMessageList(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageResult<MessageResponse> result = messageService.getMessageList(pageable);
        return Result.success(result);
    }

    @Operation(summary = "获取友情链接列表", description = "获取所有标记为友情链接的留言")
    @GetMapping("/friends")
    public Result<List<MessageResponse>> getFriendLinks() {
        List<MessageResponse> friendLinks = messageService.getFriendLinks();
        return Result.success(friendLinks);
    }

    @Operation(summary = "发表留言", description = "发表新留言")
    @PostMapping
    public Result<Long> createMessage(
            @Valid @RequestBody MessageCreateRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = IpUtil.getIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        Long messageId = messageService.createMessage(request, ipAddress, userAgent);
        return Result.success(messageId);
    }
}
