package com.blog.service;

import com.blog.config.properties.BlogProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 图像 URL 统一构造服务
 * <p>
 * 所有图像资源的 URL 构造都必须经过此服务，方便后期接入图床（CDN）。
 * 接入 CDN 时只需修改 {@link #toUrl(String)} 的返回逻辑。
 */
@Service
@RequiredArgsConstructor
public class ImageUrlService {

    private final BlogProperties blogProperties;

    /**
     * 将任意格式的路径转为 URL 路径。
     * <p>
     * 兼容所有现有 DB 中的存储格式：
     * <ul>
     *   <li>{@code "images/covers/1.jpg"} → {@code "/files/images/covers/1.jpg"}</li>
     *   <li>{@code "/files/images/covers/1.jpg?v=123"} → {@code "/files/images/covers/1.jpg"}</li>
     *   <li>{@code "http://nixstudio.cn/files/avatars/a.jpg"} → 原样返回</li>
     *   <li>{@code null} 或 {@code ""} → {@code null}</li>
     * </ul>
     *
     * @param pathOrUrl 任意格式的路径或 URL
     * @return URL 路径，如 {@code "/files/images/covers/1.jpg"}
     */
    public String toUrl(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            return null;
        }

        // 完整 URL 直接返回
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return pathOrUrl;
        }

        // 提取相对路径
        String relativePath = toRelativePath(pathOrUrl);

        // 构造 URL
        String urlPrefix = blogProperties.getStaticFile().getUrlPath();
        return urlPrefix + "/" + relativePath;
    }

    /**
     * 将任意格式的路径转为相对路径（相对于 blog.data.path）。
     * 用于文件删除等需要物理路径的操作。
     * <p>
     * 兼容所有现有格式：
     * <ul>
     *   <li>{@code "/files/images/covers/1.jpg?v=123"} → {@code "images/covers/1.jpg"}</li>
     *   <li>{@code "images/covers/1.jpg"} → {@code "images/covers/1.jpg"}</li>
     *   <li>{@code "http://host/files/avatars/a.jpg"} → {@code "avatars/a.jpg"}</li>
     * </ul>
     *
     * @param pathOrUrl 任意格式的路径或 URL
     * @return 相对路径，如 {@code "images/covers/1.jpg"}
     */
    public String toRelativePath(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            return null;
        }

        String path = pathOrUrl;

        // 处理完整 URL：提取路径部分
        if (path.startsWith("http://") || path.startsWith("https://")) {
            int schemeEnd = path.indexOf("://") + 3;
            int pathStart = path.indexOf('/', schemeEnd);
            if (pathStart == -1) {
                return path;
            }
            path = path.substring(pathStart);
        }

        // 去掉查询参数
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }

        // 去掉 URL 前缀（如 /files/）
        String urlPrefix = blogProperties.getStaticFile().getUrlPath();
        if (path.startsWith(urlPrefix + "/")) {
            path = path.substring(urlPrefix.length() + 1);
        } else if (path.startsWith(urlPrefix)) {
            path = path.substring(urlPrefix.length());
        }

        // 去掉前导斜杠
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }
}
