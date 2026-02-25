package com.blog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文章图片
     *
     * @param file 图片文件
     * @return 图片相对路径
     */
    String uploadArticleImage(MultipartFile file);

    /**
     * 上传头像
     *
     * @param file 头像文件
     * @return 头像相对路径
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 上传封面图
     *
     * @param file 封面图文件
     * @return 封面图相对路径
     */
    String uploadCoverImage(MultipartFile file);

    /**
     * 删除文件
     *
     * @param relativePath 文件相对路径
     * @return 是否删除成功
     */
    boolean deleteFile(String relativePath);

    /**
     * 获取文件的完整URL
     *
     * @param relativePath 文件相对路径
     * @return 完整URL
     */
    String getFileUrl(String relativePath);
}
