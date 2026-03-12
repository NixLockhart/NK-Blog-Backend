package com.blog.service.storage;

/**
 * 图片上传结果
 *
 * @param storedPath DB 存储值：本地为相对路径，图床为完整 URL
 * @param deleteKey  图床删除凭据（本地为 null）
 */
public record ImageUploadResult(String storedPath, String deleteKey) {
}
