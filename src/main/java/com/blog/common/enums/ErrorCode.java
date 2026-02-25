package com.blog.common.enums;

import lombok.Getter;

/**
 * 错误码枚举
 */
@Getter
public enum ErrorCode {

    // 系统错误 (1xxxx)
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(10000, "系统错误"),
    PARAM_ERROR(10001, "参数错误"),
    VALIDATION_ERROR(10002, "数据校验失败"),

    // 认证授权错误 (2xxxx)
    UNAUTHORIZED(20001, "未授权,请先登录"),
    FORBIDDEN(20002, "无权限访问"),
    TOKEN_EXPIRED(20003, "Token已过期"),
    TOKEN_INVALID(20004, "Token无效"),
    LOGIN_FAILED(20005, "登录失败,用户名或密码错误"),
    USER_NOT_FOUND(20006, "用户不存在"),
    INVALID_CREDENTIALS(20007, "用户名或密码错误"),

    // 资源错误 (3xxxx)
    RESOURCE_NOT_FOUND(30001, "资源不存在"),
    RESOURCE_ALREADY_EXISTS(30002, "资源已存在"),
    ARTICLE_NOT_FOUND(30003, "文章不存在"),
    CATEGORY_NOT_FOUND(30004, "分类不存在"),
    COMMENT_NOT_FOUND(30005, "评论不存在"),
    CONFIG_NOT_FOUND(30006, "配置不存在"),
    WIDGET_NOT_FOUND(30007, "小工具不存在"),
    THEME_NOT_FOUND(30008, "主题不存在"),

    // 业务错误 (4xxxx)
    BUSINESS_ERROR(40000, "业务处理失败"),
    ARTICLE_ALREADY_DELETED(40001, "文章已被删除"),
    CATEGORY_HAS_ARTICLES(40002, "分类下存在文章,无法删除"),
    DUPLICATE_CATEGORY(40003, "分类名称已存在"),
    WIDGET_SYSTEM_CANNOT_DELETE(40004, "系统自带小工具不可删除"),
    WIDGET_CODE_READ_ERROR(40005, "读取小工具代码失败"),
    WIDGET_CODE_SAVE_ERROR(40006, "保存小工具代码失败"),
    INVALID_IMAGE_FORMAT(40007, "图片格式不正确"),
    THEME_OPERATION_NOT_ALLOWED(40008, "主题操作不允许"),

    // 限流错误 (5xxxx)
    RATE_LIMIT_ERROR(50001, "请求过于频繁,请稍后再试"),
    COMMENT_RATE_LIMIT(50002, "评论过于频繁,请稍后再试"),
    MESSAGE_RATE_LIMIT(50003, "留言过于频繁,请稍后再试"),

    // 文件错误 (6xxxx)
    FILE_UPLOAD_ERROR(60001, "文件上传失败"),
    FILE_SIZE_EXCEEDED(60002, "文件大小超出限制"),
    FILE_TYPE_NOT_ALLOWED(60003, "文件类型不支持"),
    FILE_NOT_FOUND(60004, "文件不存在"),
    FILE_SYSTEM_ERROR(60005, "文件系统错误");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
