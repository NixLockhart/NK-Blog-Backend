package com.blog.service.storage;

import com.blog.common.enums.ErrorCode;
import com.blog.config.properties.BlogProperties;
import com.blog.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地存储实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LocalStorageProvider implements ImageStorageProvider {

    private final BlogProperties blogProperties;

    @Override
    public ImageUploadResult upload(MultipartFile file, String category, String fileName) {
        String relativePath = category + "/" + fileName;
        Path targetPath = Paths.get(blogProperties.getData().getPath(), category, fileName);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("本地上传成功: {}", relativePath);
            return new ImageUploadResult(relativePath, null);
        } catch (IOException e) {
            log.error("本地上传失败: {}", relativePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件上传失败");
        }
    }

    @Override
    public boolean delete(String pathOrUrl) {
        try {
            Path basePath = Paths.get(blogProperties.getData().getPath()).normalize().toAbsolutePath();
            Path filePath = basePath.resolve(pathOrUrl).normalize().toAbsolutePath();

            if (!filePath.startsWith(basePath)) {
                log.warn("路径遍历攻击被拦截: {}", pathOrUrl);
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "非法文件路径");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("本地文件删除成功: {}", pathOrUrl);
                return true;
            }
            log.warn("本地文件不存在: {}", pathOrUrl);
            return false;
        } catch (IOException e) {
            log.error("本地文件删除失败: {}", pathOrUrl, e);
            return false;
        }
    }

    @Override
    public boolean canHandle(String pathOrUrl) {
        return pathOrUrl != null
                && !pathOrUrl.startsWith("http://")
                && !pathOrUrl.startsWith("https://");
    }
}
