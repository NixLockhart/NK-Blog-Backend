package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.config.properties.BlogProperties;
import com.blog.exception.BusinessException;
import com.blog.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final BlogProperties blogProperties;

    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    // 图片文件最大尺寸: 5MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    // 头像文件最大尺寸: 2MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    @Override
    public String uploadArticleImage(MultipartFile file) {
        validateImageFile(file, MAX_IMAGE_SIZE);
        String dateFolder = getDateFolder();
        String fileName = generateFileName(file.getOriginalFilename());
        Path targetPath = Paths.get(blogProperties.getData().getPath(), "images", dateFolder, fileName);

        return saveFile(file, targetPath, "images/" + dateFolder + "/" + fileName);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file, MAX_AVATAR_SIZE);
        String fileName = generateFileName(file.getOriginalFilename());
        Path targetPath = Paths.get(blogProperties.getData().getPath(), "avatars", fileName);

        return saveFile(file, targetPath, "avatars/" + fileName);
    }

    @Override
    public String uploadCoverImage(MultipartFile file) {
        validateImageFile(file, MAX_IMAGE_SIZE);
        String fileName = generateFileName(file.getOriginalFilename());
        Path targetPath = Paths.get(blogProperties.getData().getPath(), "images", "covers", fileName);

        return saveFile(file, targetPath, "images/covers/" + fileName);
    }

    @Override
    public boolean deleteFile(String relativePath) {
        try {
            Path basePath = Paths.get(blogProperties.getData().getPath()).normalize().toAbsolutePath();
            Path filePath = basePath.resolve(relativePath).normalize().toAbsolutePath();

            // 路径遍历校验：确保目标文件在数据目录内
            if (!filePath.startsWith(basePath)) {
                log.warn("路径遍历攻击被拦截: {}", relativePath);
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "非法文件路径");
            }

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", relativePath);
                return true;
            }
            log.warn("文件不存在: {}", relativePath);
            return false;
        } catch (IOException e) {
            log.error("文件删除失败: {}", relativePath, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String relativePath) {
        // 返回相对路径，让前端自动使用当前协议（HTTP/HTTPS）
        // 这样可以避免混合内容问题
        return "/files/" + relativePath;
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                "文件大小超过限制: " + (maxSize / 1024 / 1024) + "MB");
        }

        // 检查文件扩展名
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                "不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_IMAGE_EXTENSIONS));
        }

        // 检查文件内容类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件内容类型不正确");
        }
    }

    /**
     * 保存文件
     */
    private String saveFile(MultipartFile file, Path targetPath, String relativePath) {
        try {
            // 创建父目录
            Files.createDirectories(targetPath.getParent());

            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("文件上传成功: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            log.error("文件上传失败: {}", relativePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件上传失败");
        }
    }

    /**
     * 生成唯一文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "." + extension;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 获取日期文件夹（格式: yyyy-MM-dd）
     */
    private String getDateFolder() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
