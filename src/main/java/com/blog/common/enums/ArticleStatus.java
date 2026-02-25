package com.blog.common.enums;

import lombok.Getter;

/**
 * 文章状态枚举
 */
@Getter
public enum ArticleStatus {
    DELETED(0, "已删除"),
    PUBLISHED(1, "已发布"),
    DRAFT(2, "草稿");

    private final int value;
    private final String description;

    ArticleStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
}
