package com.blog.service;

import com.blog.common.response.PageResult;
import com.blog.model.dto.message.MessageCreateRequest;
import com.blog.model.dto.message.MessageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 留言服务接口
 */
public interface MessageService {

    /**
     * 获取留言列表（分页）
     */
    PageResult<MessageResponse> getMessageList(Pageable pageable);

    /**
     * 获取友情链接列表
     */
    List<MessageResponse> getFriendLinks();

    /**
     * 发表留言
     */
    Long createMessage(MessageCreateRequest request, String ipAddress, String userAgent);

    /**
     * 获取留言管理列表（分页）
     */
    PageResult<MessageResponse> getMessagesForAdmin(Pageable pageable);

    /**
     * 删除留言
     */
    void deleteMessage(Long id);

    /**
     * 设置/取消友链标记
     */
    void toggleFriendLink(Long id);
}
