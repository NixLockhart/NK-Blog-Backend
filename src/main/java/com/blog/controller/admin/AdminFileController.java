package com.blog.controller.admin;

import com.blog.common.enums.ErrorCode;
import com.blog.common.response.Result;
import com.blog.config.properties.BlogProperties;
import com.blog.exception.BusinessException;
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
import java.nio.file.StandardCopyOption;
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

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 上传文章封面（按文章ID命名）
     */
    @Operation(summary = "上传文章封面", description = "上传文章封面图片，以文章ID命名")
    @PostMapping("/covers/{articleId}")
    public Result<String> uploadCoverByArticleId(
            @Parameter(description = "文章ID") @PathVariable Long articleId,
            @Parameter(description = "封面图片文件") @RequestParam("file") MultipartFile file) {

        try {
            validateImageFile(file);

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // 使用文章ID作为文件名
            String fileName = articleId + extension;

            // 确保目录存在
            String coversPath = getCoversPath();
            Path coversDir = Paths.get(coversPath);
            if (!Files.exists(coversDir)) {
                Files.createDirectories(coversDir);
            }

            // 保存文件（如果文件已存在则覆盖）
            Path filePath = coversDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径（相对于 blog.data.path）
            String relativePath = "images/covers/" + fileName;
            log.info("上传封面成功: articleId={}, path={}", articleId, relativePath);

            return Result.success(relativePath);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("上传封面失败: articleId={}", articleId, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传封面失败: " + e.getMessage());
        }
    }

    /**
     * 上传文章封面（临时，使用UUID命名）
     * 用于新建文章时还没有文章ID的情况
     */
    @Operation(summary = "上传临时封面", description = "上传临时封面图片，使用UUID命名")
    @PostMapping("/covers")
    public Result<String> uploadCover(
            @Parameter(description = "封面图片文件") @RequestParam("file") MultipartFile file) {

        try {
            validateImageFile(file);

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // 生成唯一文件名
            String fileName = UUID.randomUUID().toString() + extension;

            // 确保目录存在
            String coversPath = getCoversPath();
            Path coversDir = Paths.get(coversPath);
            if (!Files.exists(coversDir)) {
                Files.createDirectories(coversDir);
            }

            // 保存文件
            Path filePath = coversDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径（相对于 blog.data.path）
            String relativePath = "images/covers/" + fileName;
            log.info("上传临时封面成功: {}", relativePath);

            return Result.success(relativePath);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("上传封面失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传封面失败: " + e.getMessage());
        }
    }

    /**
     * 上传文章图片
     */
    @Operation(summary = "上传文章图片", description = "上传文章内容中的图片")
    @PostMapping("/articles/{articleId}/images")
    public Result<String> uploadArticleImage(
            @Parameter(description = "文章ID") @PathVariable Long articleId,
            @Parameter(description = "图片文件") @RequestParam("file") MultipartFile file) {

        try {
            validateImageFile(file);

            // 获取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);

            // 获取文章图片目录
            String imagesPath = getArticleImagesPath(articleId);
            Path imagesDir = Paths.get(imagesPath);
            if (!Files.exists(imagesDir)) {
                Files.createDirectories(imagesDir);
            }

            // 计算下一个序号
            int nextNumber = getNextImageNumber(imagesDir, articleId, extension);

            // 生成文件名: articleId-序号.扩展名
            String fileName = articleId + "-" + nextNumber + extension;

            // 保存文件
            Path filePath = imagesDir.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径（相对于 blog.data.path）
            String relativePath = "images/" + articleId + "/" + fileName;
            log.info("上传文章图片成功: {}", relativePath);

            return Result.success(relativePath);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("上传文章图片失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传文章图片失败: " + e.getMessage());
        }
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请选择要上传的文件");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                    "文件大小超过限制（最大10MB）");
        }

        // 检查文件类型
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
            return ".jpg"; // 默认
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

    /**
     * 获取封面图片目录路径
     */
    private String getCoversPath() {
        return blogProperties.getData().getPath() + "/images/covers";
    }

    /**
     * 获取文章图片目录路径
     */
    private String getArticleImagesPath(Long articleId) {
        return blogProperties.getData().getPath() + "/images/" + articleId;
    }

    /**
     * 获取下一个图片序号
     */
    private int getNextImageNumber(Path directory, Long articleId, String extension) throws IOException {
        // 查找当前已存在的最大序号
        int maxNumber = 0;

        if (Files.exists(directory)) {
            String prefix = articleId + "-";
            var files = Files.list(directory)
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return fileName.startsWith(prefix) && fileName.endsWith(extension);
                    })
                    .toList();

            for (Path file : files) {
                String fileName = file.getFileName().toString();
                // 提取序号: articleId-序号.extension
                String numberPart = fileName.substring(prefix.length(), fileName.lastIndexOf('.'));
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
