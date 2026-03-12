package com.blog.model.dto.config;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图片存储配置 DTO
 */
@Data
public class ImageStorageConfig {

    /**
     * 存储模式: "local" 或 "cdn"
     */
    private String mode = "local";

    /**
     * 图床配置
     */
    private CdnConfig cdn = new CdnConfig();

    public static ImageStorageConfig defaultLocal() {
        return new ImageStorageConfig();
    }

    @Data
    public static class CdnConfig {
        /** 图床上传 API 地址 */
        private String uploadUrl = "";
        /** HTTP 方法 */
        private String method = "POST";
        /** 请求头 */
        private Map<String, String> headers = new LinkedHashMap<>();
        /** 上传文件对应的 multipart 字段名 */
        private String fileField = "file";
        /** 附加表单参数 */
        private Map<String, String> extraParams = new LinkedHashMap<>();
        /** 从 JSON 响应中提取图片 URL 的路径（点号分隔） */
        private String responseUrlField = "data.url";
        /** 从 JSON 响应中提取删除凭据的路径（可选） */
        private String responseDeleteField = "";
        /** 删除 API 地址模板，{deleteKey} 会被替换 */
        private String deleteUrlTemplate = "";
        /** 删除请求的 HTTP 方法 */
        private String deleteMethod = "GET";
        /** 超时时间（秒） */
        private Integer timeout = 30;
        /** 失败重试次数 */
        private Integer maxRetries = 2;
    }
}
