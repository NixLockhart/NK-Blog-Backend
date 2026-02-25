package com.blog.service;

/**
 * Markdown服务接口
 */
public interface MarkdownService {

    /**
     * 将Markdown转换为HTML
     */
    String markdownToHtml(String markdown);

    /**
     * 生成目录（基于H1-H3标题）
     */
    String generateToc(String markdown);

    /**
     * 读取Markdown文件
     */
    String readMarkdownFile(String filePath);

    /**
     * 保存Markdown文件
     */
    String saveMarkdownFile(String content, String fileName);

    /**
     * 删除Markdown文件
     */
    void deleteMarkdownFile(String filePath);
}
