package com.blog.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

/**
 * HTML清洗工具类
 * 防止存储型XSS攻击
 */
public class HtmlSanitizer {

    /**
     * 严格策略：移除所有HTML标签，仅保留纯文本
     * 用于昵称、URL等字段
     */
    private static final PolicyFactory STRICT_POLICY = new HtmlPolicyBuilder().toFactory();

    /**
     * 内容策略：保留Markdown渲染后常见的安全HTML标签
     * 用于评论/留言内容
     */
    private static final PolicyFactory CONTENT_POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "em", "strong", "code", "pre", "blockquote",
                    "ul", "ol", "li", "h1", "h2", "h3", "h4", "h5", "h6",
                    "a", "img", "table", "thead", "tbody", "tr", "th", "td",
                    "del", "hr", "sup", "sub", "span")
            .allowUrlProtocols("http", "https")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "alt").onElements("img")
            .allowAttributes("class").onElements("code", "pre", "span")
            .requireRelNofollowOnLinks()
            .toFactory();

    /**
     * 清洗纯文本字段（移除所有HTML标签）
     */
    public static String sanitizeStrict(String input) {
        if (input == null) return null;
        return STRICT_POLICY.sanitize(input);
    }

    /**
     * 清洗内容字段（保留安全的HTML标签）
     */
    public static String sanitizeContent(String input) {
        if (input == null) return null;
        return CONTENT_POLICY.sanitize(input);
    }
}
