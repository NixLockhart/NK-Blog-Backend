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

    /**
     * 检查草稿文件是否存在
     */
    boolean draftExists(Long articleId);

    /**
     * 读取草稿文件内容
     */
    String readDraftFile(Long articleId);

    /**
     * 保存草稿文件（{articleId}_draft.md）
     */
    void saveDraftFile(Long articleId, String content);

    /**
     * 删除草稿文件
     */
    void deleteDraftFile(Long articleId);
}
