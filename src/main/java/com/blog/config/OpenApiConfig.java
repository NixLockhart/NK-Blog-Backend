package com.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI配置类
 */
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final AppVersionProvider appVersionProvider;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("星光小栈 API 文档")
                .description("星光小栈博客系统 RESTful API 文档")
                .version(appVersionProvider.getVersion())
                .contact(new Contact()
                    .name("星光小栈")
                    .url("https://nixstudio.cn"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("请输入JWT Token")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
