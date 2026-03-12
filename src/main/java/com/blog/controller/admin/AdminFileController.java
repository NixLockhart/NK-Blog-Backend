package com.blog.controller.admin;

import com.blog.common.enums.ErrorCode;
import com.blog.common.response.Result;
import com.blog.config.properties.BlogProperties;
import com.blog.exception.BusinessException;
import com.blog.service.ImageStorageConfigService;
import com.blog.service.storage.CdnStorageProvider;
import com.blog.service.storage.ImageStorageProvider;
import com.blog.service.storage.ImageUploadResult;
import com.blog.service.storage.LocalStorageProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 管理员文件上传控制器
 */
@Slf4j
@Tag(name = "管理员文件管理接口", description = "管理员文件上传、图片管理等功能")
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class AdminFileController {

    private final BlogProperties blogProperties;
    private final ImageStorageConfigService imageStorageConfigService;
    private final LocalStorageProvider localStorageProvider;
    private final CdnStorageProvider cdnStorageProvider;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private ImageStorageProvider activeProvider() {
        return imageStorageConfigService.isCdnMode()
                ? cdnStorageProvider : localStorageProvider;
    }

    /**
     * 上传文章封面（按文章ID命名）
     */
    @Operation(summary = "上传文章封面", description = "上传文章封面图片，以文章ID命名")
    @PostMapping("/covers/{articleId}")
    public Result<String> uploadCoverByArticleId(
            @Parameter(description = "文章ID") @PathVariable Long articleId,
            @Parameter(description = "封面图片文件") @RequestParam("file") MultipartFile file) {

        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = articleId + extension;

        ImageUploadResult result = activeProvider().upload(file, "images/covers", fileName);
        log.info("上传封面成功: articleId={}, path={}", articleId, result.storedPath());

        return Result.success(result.storedPath());
    }

    /**
     * 上传文章封面（临时，使用UUID命名）
     * 用于新建文章时还没有文章ID的情况
     */
    @Operation(summary = "上传临时封面", description = "上传临时封面图片，使用UUID命名")
    @PostMapping("/covers")
    public Result<String> uploadCover(
            @Parameter(description = "封面图片文件") @RequestParam("file") MultipartFile file) {

        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + extension;

        ImageUploadResult result = activeProvider().upload(file, "images/covers", fileName);
        log.info("上传临时封面成功: {}", result.storedPath());

        return Result.success(result.storedPath());
    }

    /**
     * 上传文章图片
     */
    @Operation(summary = "上传文章图片", description = "上传文章内容中的图片")
    @PostMapping("/articles/{articleId}/images")
    public Result<String> uploadArticleImage(
            @Parameter(description = "文章ID") @PathVariable Long articleId,
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {

        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        // 计算下一个序号（仅本地模式需要序号，CDN 模式直接用 UUID）
        String fileName;
        if (imageStorageConfigService.isCdnMode()) {
            fileName = articleId + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
        } else {
            Path imagesDir = Paths.get(blogProperties.getData().getPath(), "images", String.valueOf(articleId));
            try {
                int nextNumber = getNextImageNumber(imagesDir, articleId, extension);
                fileName = articleId + "-" + nextNumber + extension;
            } catch (IOException e) {
                log.error("获取图片序号失败", e);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传文章图片失败");
            }
        }

        ImageUploadResult result = activeProvider().upload(file, "images/" + articleId, fileName);
        log.info("上传文章图片成功: {}", result.storedPath());

        return Result.success(result.storedPath());
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请选择要上传的文件");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                    "文件大小超过限制（最大10MB）");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(),
                    "只支持 JPG、JPEG、PNG 格式的图片");
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

    /**
     * 获取下一个图片序号
     */
    private int getNextImageNumber(Path directory, Long articleId, String extension) throws IOException {
        int maxNumber = 0;

        if (Files.exists(directory)) {
            String prefix = articleId + "-";
            var files = Files.list(directory)
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.startsWith(prefix) && name.endsWith(extension);
                    })
                    .toList();

            for (Path f : files) {
                String name = f.getFileName().toString();
                String numberPart = name.substring(prefix.length(), name.lastIndexOf('.'));
                try {
                    int number = Integer.parseInt(numberPart);
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException e) {
                    // 忽略无法解析的文件名
                }
            }
        }

        return maxNumber + 1;
    }
}
