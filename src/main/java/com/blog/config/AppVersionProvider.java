package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 应用版本号提供者
 * 版本号从 git tag 自动提取，由 git-commit-id-maven-plugin 生成到 git.properties
 * 工作流：git tag v2.0.5-alpha → mvn package → 版本号自动为 v2.0.5-alpha
 */
@Component
@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
public class AppVersionProvider {

    @Value("${git.closest.tag.name:unknown}")
    private String version;

    public String getVersion() {
        return version;
    }
}
