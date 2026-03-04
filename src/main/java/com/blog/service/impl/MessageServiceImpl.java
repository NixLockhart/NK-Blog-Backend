package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.common.enums.MessageStatus;
import com.blog.common.response.PageResult;
import com.blog.exception.BusinessException;
import com.blog.util.HtmlSanitizer;
import com.blog.model.dto.message.MessageCreateRequest;
import com.blog.model.dto.message.MessageResponse;
import com.blog.model.entity.Message;
import com.blog.repository.MessageRepository;
import com.blog.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 留言服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    private static final Integer STATUS_DELETED = MessageStatus.DELETED.getValue();
    private static final Integer STATUS_VISIBLE = MessageStatus.VISIBLE.getValue();

    @Override
    @Transactional(readOnly = true)
    public PageResult<MessageResponse> getMessageList(Pageable pageable) {
        Page<Message> page = messageRepository.findByStatusOrderByCreatedAtDesc(STATUS_VISIBLE, pageable);

        List<MessageResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(content, page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getFriendLinks() {
        List<Message> messages = messageRepository.findByStatusAndIsFriendLinkOrderByCreatedAtDesc(STATUS_VISIBLE, 1);
        return messages.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Long createMessage(MessageCreateRequest request, String ipAddress, String userAgent) {
        Message message = new Message();
        message.setNickname(HtmlSanitizer.sanitizeStrict(request.getNickname()));
        message.setEmail(request.getEmail());
        message.setWebsite(HtmlSanitizer.sanitizeStrict(request.getBlogUrl()));
        message.setAvatar(request.getAvatar());
        message.setContent(HtmlSanitizer.sanitizeContent(request.getContent()));
        message.setIpAddress(ipAddress);
        message.setStatus(STATUS_VISIBLE);

        message.setIsFriendLink(0);

        message = messageRepository.save(message);

        log.info("发表留言成功: id={}, nickname={}", message.getId(), request.getNickname());
        return message.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<MessageResponse> getMessagesForAdmin(Pageable pageable) {
        Page<Message> page = messageRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<MessageResponse> content = page.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PageResult.of(content, page);
    }

    @Override
    @Transactional
    public void deleteMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        message.setStatus(STATUS_DELETED);
        // 删除时自动取消友链
        message.setIsFriendLink(0);
        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);

        log.info("删除留言成功: id={}", id);
    }

    @Override
    @Transactional
    public void permanentlyDeleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        messageRepository.deleteById(id);
        log.info("永久删除留言成功: id={}", id);
    }

    @Override
    @Transactional
    public void restoreMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        if (!STATUS_DELETED.equals(message.getStatus())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "留言未处于已删除状态");
        }

        // 恢复为可见状态（不恢复友链标记，删除时已取消）
        message.setStatus(STATUS_VISIBLE);
        message.setDeletedAt(null);
        messageRepository.save(message);

        log.info("恢复留言成功: id={}", id);
    }

    @Override
    @Transactional
    public void toggleFriendLink(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        message.setIsFriendLink(message.getIsFriendLink() == 1 ? 0 : 1);
        messageRepository.save(message);

        log.info("切换友链标记: id={}, isFriendLink={}", id, message.getIsFriendLink());
    }

    /**
     * 转换为响应DTO
     */
    private MessageResponse convertToResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setNickname(message.getNickname());
        response.setEmail(message.getEmail());
        response.setBlogUrl(message.getWebsite());
        response.setAvatar(message.getAvatar());
        response.setContent(message.getContent());
        response.setIsFriendLink(message.getIsFriendLink());
        response.setStatus(message.getStatus());
        response.setIpAddress(message.getIpAddress());
        response.setCreatedAt(message.getCreatedAt());
        response.setDeletedAt(message.getDeletedAt());
        return response;
    }
}
