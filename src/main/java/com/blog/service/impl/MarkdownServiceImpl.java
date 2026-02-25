package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.config.properties.BlogProperties;
import com.blog.exception.BusinessException;
import com.blog.service.MarkdownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Heading;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Markdown服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownServiceImpl implements MarkdownService {

    private final BlogProperties blogProperties;
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    @Override
    public String markdownToHtml(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    @Override
    public String generateToc(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        Node document = parser.parse(markdown);
        List<TocItem> tocItems = new ArrayList<>();

        // 遍历文档节点，提取标题
        Node node = document.getFirstChild();
        while (node != null) {
            if (node instanceof Heading) {
                Heading heading = (Heading) node;
                int level = heading.getLevel();
                // 只提取H1-H3
                if (level >= 1 && level <= 3) {
                    String text = extractText(heading);
                    String anchor = generateAnchor(text);
                    tocItems.add(new TocItem(level, text, anchor));
                }
            }
            node = node.getNext();
        }

        // 生成TOC HTML
        return buildTocHtml(tocItems);
    }

    @Override
    public String readMarkdownFile(String filePath) {
        try {
            Path path = getAbsolutePath(filePath);
            if (!Files.exists(path)) {
                log.warn("Markdown文件不存在: {}", filePath);
                return "";
            }
            return Files.readString(path);
        } catch (IOException e) {
            log.error("读取Markdown文件失败: {}", filePath, e);
            return "";
        }
    }

    @Override
    public String saveMarkdownFile(String content, String fileName) {
        try {
            // 直接保存到articles目录下
            Path articlesDir = Paths.get(blogProperties.getData().getPath(), "articles");
            Files.createDirectories(articlesDir);

            // 生成文件名（如果没有提供）
            if (fileName == null || fileName.isEmpty()) {
                fileName = UUID.randomUUID().toString() + ".md";
            } else if (!fileName.endsWith(".md")) {
                fileName += ".md";
            }

            // 保存文件
            Path filePath = articlesDir.resolve(fileName);
            Files.writeString(filePath, content);

            // 返回相对路径
            Path basePath = Paths.get(blogProperties.getData().getPath());
            return basePath.relativize(filePath).toString().replace("\\", "/");

        } catch (IOException e) {
            log.error("保存Markdown文件失败: {}", fileName, e);
            throw new BusinessException(ErrorCode.FILE_SYSTEM_ERROR.getCode(), "保存文件失败");
        }
    }

    @Override
    public void deleteMarkdownFile(String filePath) {
        try {
            Path path = getAbsolutePath(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("删除Markdown文件成功: {}", filePath);
            }
        } catch (IOException e) {
            log.error("删除Markdown文件失败: {}", filePath, e);
        }
    }

    /**
     * 获取绝对路径
     */
    private Path getAbsolutePath(String relativePath) {
        return Paths.get(blogProperties.getData().getPath(), relativePath);
    }

    /**
     * 提取标题文本
     */
    private String extractText(Node node) {
        StringBuilder sb = new StringBuilder();
        Node child = node.getFirstChild();
        while (child != null) {
            if (child instanceof org.commonmark.node.Text) {
                sb.append(((org.commonmark.node.Text) child).getLiteral());
            }
            child = child.getNext();
        }
        return sb.toString();
    }

    /**
     * 生成锚点ID
     */
    private String generateAnchor(String text) {
        return "heading-" + text.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * 构建TOC HTML
     */
    private String buildTocHtml(List<TocItem> items) {
        if (items.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<nav class=\"toc\">\n");
        sb.append("<ul>\n");

        for (TocItem item : items) {
            String indent = "  ".repeat(item.level - 1);
            sb.append(indent)
                    .append("<li class=\"toc-level-").append(item.level).append("\">")
                    .append("<a href=\"#").append(item.anchor).append("\">")
                    .append(item.text)
                    .append("</a></li>\n");
        }

        sb.append("</ul>\n");
        sb.append("</nav>");

        return sb.toString();
    }

    /**
     * TOC项
     */
    private static class TocItem {
        int level;
        String text;
        String anchor;

        TocItem(int level, String text, String anchor) {
            this.level = level;
            this.text = text;
            this.anchor = anchor;
        }
    }
}
