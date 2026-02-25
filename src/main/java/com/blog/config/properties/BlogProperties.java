package com.blog.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 博客配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "blog")
public class BlogProperties {

    /**
     * 网站基础URL（用于生成文件访问链接）
     * 例如：https://nixstudio.cn 或 http://localhost:8080
     */
    private String baseUrl;

    /**
     * 数据存储配置
     */
    private Data data = new Data();

    /**
     * 文件上传配置
     */
    private Upload upload = new Upload();

    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    /**
     * CORS配置
     */
    private Cors cors = new Cors();

    /**
     * 限流配置
     */
    private RateLimit rateLimit = new RateLimit();

    /**
     * 静态文件服务配置
     */
    private StaticFile staticFile = new StaticFile();

    @lombok.Data
    public static class Data {
        private String path;
        private String articles;
        private String images;
        private String uploads;
        private String avatars;
        private String themes;
    }

    @lombok.Data
    public static class Upload {
        private String maxFileSize;
        private String allowedImageTypes;
        private String allowedFileTypes;
    }

    @lombok.Data
    public static class Jwt {
        private String secret;
        private Long expiration;
        private Long refreshExpiration;
    }

    @lombok.Data
    public static class Cors {
        private String allowedOrigins;
        private String allowedMethods;
        private String allowedHeaders;
        private Boolean allowCredentials;
        private Integer maxAge;
    }

    @lombok.Data
    public static class RateLimit {
        private Integer commentPerMinute;
        private Integer messagePerMinute;
        private Integer loginPerHour;
    }

    @lombok.Data
    public static class StaticFile {
        /**
         * 是否启用Spring Boot静态文件服务
         * 开发环境: true, 生产环境(使用Nginx): false
         */
        private Boolean enabled = true;

        /**
         * 静态文件URL路径前缀
         */
        private String urlPath = "/files";

        /**
         * 缓存时间(秒), 0表示不缓存
         * 开发环境建议: 0, 生产环境建议: 2592000 (30天)
         */
        private Integer cachePeriod = 0;
    }
}
