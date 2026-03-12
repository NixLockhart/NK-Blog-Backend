package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.service.FileService;
import com.blog.service.ImageStorageConfigService;
import com.blog.service.ImageUrlService;
import com.blog.service.storage.CdnStorageProvider;
import com.blog.service.storage.ImageStorageProvider;
import com.blog.service.storage.LocalStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private final ImageStorageConfigService imageStorageConfigService;
    private final LocalStorageProvider localStorageProvider;
    private final CdnStorageProvider cdnStorageProvider;
    private final ImageUrlService imageUrlService;

    // 允许的图片格式
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "svg"
    );

    // 图片文件最大尺寸: 5MB
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    // 头像文件最大尺寸: 2MB
    private static final long MAX_AVATAR_SIZE = 2 * 1024 * 1024;

    /**
     * 获取当前激活的存储 Provider
     */
    private ImageStorageProvider activeProvider() {
        return imageStorageConfigService.isCdnMode()
                ? cdnStorageProvider : localStorageProvider;
    }

    @Override
    public String uploadArticleImage(MultipartFile file) {
        validateImageFile(file, MAX_IMAGE_SIZE);
        String dateFolder = getDateFolder();
        String fileName = generateFileName(file.getOriginalFilename());
        return activeProvider().upload(file, "images/" + dateFolder, fileName).storedPath();
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file, MAX_AVATAR_SIZE);
        String fileName = generateFileName(file.getOriginalFilename());
        return activeProvider().upload(file, "avatars", fileName).storedPath();
    }

    @Override
    public String uploadCoverImage(MultipartFile file) {
        validateImageFile(file, MAX_IMAGE_SIZE);
        String fileName = generateFileName(file.getOriginalFilename());
        return activeProvider().upload(file, "images/covers", fileName).storedPath();
    }

    @Override
    public boolean deleteFile(String relativePath) {
        // 智能路由：http(s) 开头 → CDN Provider，其他 → 本地 Provider
        if (cdnStorageProvider.canHandle(relativePath)) {
            return cdnStorageProvider.delete(relativePath);
        }
        return localStorageProvider.delete(relativePath);
    }

    @Override
    public String getFileUrl(String relativePath) {
        return imageUrlService.toUrl(relativePath);
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
