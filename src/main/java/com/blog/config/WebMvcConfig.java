package com.blog.config;

import com.blog.config.properties.BlogProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Web MVC配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final BlogProperties blogProperties;

    /**
     * 配置CORS跨域
     * 注意：当使用通配符 "*" 时，需要使用 allowedOriginPatterns 而非 allowedOrigins
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        BlogProperties.Cors cors = blogProperties.getCors();
        String allowedOrigins = cors.getAllowedOrigins();
        boolean isWildcard = "*".equals(allowedOrigins.trim());

        // 为API端点配置CORS
        var apiMapping = registry.addMapping("/api/**");
        if (isWildcard) {
            apiMapping.allowedOriginPatterns("*");
        } else {
            apiMapping.allowedOrigins(allowedOrigins.split(","));
        }
        apiMapping.allowedMethods(cors.getAllowedMethods().split(","))
                .allowedHeaders(cors.getAllowedHeaders().split(","))
                .allowCredentials(cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());

        // 为静态资源（图片、小工具等）配置CORS
        var filesMapping = registry.addMapping("/files/**");
        if (isWildcard) {
            filesMapping.allowedOriginPatterns("*");
        } else {
            filesMapping.allowedOrigins(allowedOrigins.split(","));
        }
        filesMapping.allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders(cors.getAllowedHeaders().split(","))
                .allowCredentials(cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());

        var dataMapping = registry.addMapping("/data/**");
        if (isWildcard) {
            dataMapping.allowedOriginPatterns("*");
        } else {
            dataMapping.allowedOrigins(allowedOrigins.split(","));
        }
        dataMapping.allowedMethods("GET", "HEAD", "OPTIONS")
                .allowedHeaders(cors.getAllowedHeaders().split(","))
                .allowCredentials(cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());

        log.info("CORS配置已启用: allowedOrigins={}, allowCredentials={}", allowedOrigins, cors.getAllowCredentials());
    }

    /**
     * 提供CorsConfigurationSource Bean供Spring Security使用
     * 这确保CORS在Security过滤器之前被正确处理
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        BlogProperties.Cors cors = blogProperties.getCors();
        String allowedOrigins = cors.getAllowedOrigins();
        boolean isWildcard = "*".equals(allowedOrigins.trim());

        CorsConfiguration configuration = new CorsConfiguration();

        // 处理允许的源
        if (isWildcard) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }

        // 设置允许的方法
        configuration.setAllowedMethods(Arrays.asList(cors.getAllowedMethods().split(",")));

        // 设置允许的头
        String allowedHeaders = cors.getAllowedHeaders();
        if ("*".equals(allowedHeaders.trim())) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        // 设置是否允许凭证
        configuration.setAllowCredentials(cors.getAllowCredentials());

        // 设置预检请求缓存时间
        configuration.setMaxAge(Long.valueOf(cors.getMaxAge()));

        // 暴露响应头（允许前端读取）
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        source.registerCorsConfiguration("/files/**", configuration);
        source.registerCorsConfiguration("/data/**", configuration);

        log.info("CorsConfigurationSource已创建: allowedOrigins={}", allowedOrigins);
        return source;
    }

    /**
     * 配置静态资源处理器
     *
     * 开发环境: 启用Spring Boot静态文件服务
     * 生产环境: 建议使用Nginx,设置 blog.static-file.enabled=false
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        BlogProperties.StaticFile staticFile = blogProperties.getStaticFile();

        // 如果禁用了静态文件服务,则不配置资源处理器(使用Nginx时)
        if (!staticFile.getEnabled()) {
            log.info("静态文件服务已禁用,请确保已配置Nginx或其他Web服务器");
            return;
        }

        String dataPath = blogProperties.getData().getPath();
        String urlPattern = staticFile.getUrlPath() + "/**";
        String resourceLocation = "file:" + Paths.get(dataPath).toAbsolutePath() + "/";

        log.info("配置静态资源处理器: {} -> {}", urlPattern, resourceLocation);

        registry.addResourceHandler(urlPattern)
                .addResourceLocations(resourceLocation)
                .setCachePeriod(staticFile.getCachePeriod());

        // 添加 /data/** 映射，用于小工具等数据文件访问
        String dataUrlPattern = "/data/**";
        log.info("配置数据资源处理器: {} -> {}", dataUrlPattern, resourceLocation);

        registry.addResourceHandler(dataUrlPattern)
                .addResourceLocations(resourceLocation)
                .setCachePeriod(staticFile.getCachePeriod());
    }
}
