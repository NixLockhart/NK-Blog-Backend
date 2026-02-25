package com.blog.common.enums;

import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
public enum CommentStatus {
    DELETED(0, "已删除"),
    APPROVED(1, "已审核"),
    PENDING(2, "待审核");

    private final int value;
    private final String description;

    CommentStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
