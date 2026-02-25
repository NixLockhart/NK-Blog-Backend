package com.blog.controller;

import com.blog.common.response.Result;
import com.blog.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件管理", description = "文件上传和管理接口")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文章图片", description = "上传文章中的图片，支持jpg/png/gif等格式，最大5MB")
    @PostMapping(value = "/article/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadArticleImage(@RequestParam("file") MultipartFile file) {
        log.info("接收到文章图片上传请求: {}", file.getOriginalFilename());
        String relativePath = fileService.uploadArticleImage(file);
        String fileUrl = fileService.getFileUrl(relativePath);

        Map<String, String> result = new HashMap<>();
        result.put("path", relativePath);
        result.put("url", fileUrl);

        return Result.success(result);
    }

    @Operation(summary = "上传头像", description = "上传用户头像，支持jpg/png/gif等格式，最大2MB")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("接收到头像上传请求: {}", file.getOriginalFilename());
        String relativePath = fileService.uploadAvatar(file);
        String fileUrl = fileService.getFileUrl(relativePath);

        Map<String, String> result = new HashMap<>();
        result.put("path", relativePath);
        result.put("url", fileUrl);

        return Result.success(result);
    }

    @Operation(summary = "上传封面图", description = "上传文章封面图，支持jpg/png等格式，最大5MB")
    @PostMapping(value = "/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadCoverImage(@RequestParam("file") MultipartFile file) {
        log.info("接收到封面图上传请求: {}", file.getOriginalFilename());
        String relativePath = fileService.uploadCoverImage(file);
        String fileUrl = fileService.getFileUrl(relativePath);

        Map<String, String> result = new HashMap<>();
        result.put("path", relativePath);
        result.put("url", fileUrl);

        return Result.success(result);
    }

    @Operation(summary = "删除文件", description = "根据文件路径删除文件")
    @DeleteMapping
    public Result<Boolean> deleteFile(@RequestParam String path) {
        log.info("接收到文件删除请求: {}", path);
        boolean success = fileService.deleteFile(path);
        return Result.success(success);
    }
}
