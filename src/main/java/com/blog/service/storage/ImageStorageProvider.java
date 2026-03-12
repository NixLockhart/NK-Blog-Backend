package com.blog.service.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 图片存储策略接口
 */
public interface ImageStorageProvider {

    /**
     * 上传图片
     *
     * @param file     图片文件
     * @param category 分类路径（如 "images/covers", "avatars"）
     * @param fileName 文件名
     * @return 上传结果
     */
    ImageUploadResult upload(MultipartFile file, String category, String fileName);

    /**
     * 删除图片
     *
     * @param pathOrUrl 本地相对路径或图床 URL
     * @return 是否删除成功
     */
    boolean delete(String pathOrUrl);

    /**
     * 判断是否能处理该路径/URL 的删除
     */
    boolean canHandle(String pathOrUrl);
}
