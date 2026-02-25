package com.blog.common.enums;

import lombok.Getter;

/**
 * 留言状态枚举
 */
@Getter
public enum MessageStatus {
    DELETED(0, "已删除"),
    VISIBLE(1, "可见");

    private final int value;
    private final String description;

    MessageStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
