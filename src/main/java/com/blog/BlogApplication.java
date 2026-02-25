package com.blog;

import com.blog.config.AppVersionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * 星光小栈 - 博客后端主启动类
 *
 * @author 星光小栈
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }

    @Component
    @RequiredArgsConstructor
    static class StartupBanner implements CommandLineRunner {

        private final AppVersionProvider appVersionProvider;

        @Override
        public void run(String... args) {
            System.out.printf("""

                ========================================
                  星光小栈 %s 后端服务启动成功！
                  Spring Boot Version: 3.2.5
                  API文档: http://localhost:8080/swagger-ui.html
                ========================================
                %n""", appVersionProvider.getVersion());
        }
    }
}
