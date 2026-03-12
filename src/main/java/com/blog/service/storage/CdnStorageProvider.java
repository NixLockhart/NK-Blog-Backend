package com.blog.service.storage;

import com.blog.exception.BusinessException;
import com.blog.model.dto.config.ImageStorageConfig;
import com.blog.service.ImageStorageConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

/**
 * 图床存储实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CdnStorageProvider implements ImageStorageProvider {

    private final ImageStorageConfigService configService;
    private final ObjectMapper objectMapper;

    @Override
    public ImageUploadResult upload(MultipartFile file, String category, String fileName) {
        ImageStorageConfig.CdnConfig cdn = configService.getConfig().getCdn();

        if (cdn == null || cdn.getUploadUrl() == null || cdn.getUploadUrl().isEmpty()) {
            throw new BusinessException("图床未配置上传 API 地址");
        }

        try {
            // 构建 multipart 请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            byte[] fileBytes = file.getBytes();
            body.add(cdn.getFileField(), new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            if (cdn.getExtraParams() != null) {
                cdn.getExtraParams().forEach(body::add);
            }

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (cdn.getHeaders() != null) {
                cdn.getHeaders().forEach(headers::set);
            }

            // 发送请求（含重试）
            RestTemplate restTemplate = buildRestTemplate(cdn.getTimeout());
            HttpMethod method = HttpMethod.valueOf(cdn.getMethod().toUpperCase());
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = executeWithRetry(
                    restTemplate, cdn.getUploadUrl(), method, requestEntity,
                    cdn.getMaxRetries() != null ? cdn.getMaxRetries() : 2);

            // 解析响应
            JsonNode root = objectMapper.readTree(response.getBody());
            String imageUrl = extractField(root, cdn.getResponseUrlField());

            String deleteKey = null;
            if (cdn.getResponseDeleteField() != null && !cdn.getResponseDeleteField().isEmpty()) {
                deleteKey = extractField(root, cdn.getResponseDeleteField());
            }

            log.info("图床上传成功: {} -> {}", fileName, imageUrl);
            return new ImageUploadResult(imageUrl, deleteKey);

        } catch (IOException e) {
            log.error("图床上传失败: {}", fileName, e);
            throw new BusinessException("图床上传失败: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(String pathOrUrl) {
        ImageStorageConfig.CdnConfig cdn = configService.getConfig().getCdn();

        if (cdn == null || cdn.getDeleteUrlTemplate() == null || cdn.getDeleteUrlTemplate().isEmpty()) {
            log.info("图床未配置删除接口，跳过删除: {}", pathOrUrl);
            return true;
        }

        // 本实现不存储 deleteKey，仅在有直接删除 URL 时尝试
        // 若需支持 deleteKey，可后续扩展 tb_image_delete_keys 表
        log.info("图床文件删除跳过（暂未实现 deleteKey 存储）: {}", pathOrUrl);
        return true;
    }

    @Override
    public boolean canHandle(String pathOrUrl) {
        return pathOrUrl != null
                && (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://"));
    }

    /**
     * 用点号分隔路径从 JSON 中提取值
     */
    private String extractField(JsonNode root, String dotPath) {
        if (dotPath == null || dotPath.isEmpty()) {
            throw new BusinessException("JSON 字段路径不能为空");
        }
        JsonNode node = root;
        for (String key : dotPath.split("\\.")) {
            node = node.get(key);
            if (node == null) {
                throw new BusinessException("图床响应中未找到字段: " + dotPath + "，响应内容: " + root);
            }
        }
        return node.asText();
    }

    private RestTemplate buildRestTemplate(Integer timeoutSeconds) {
        int timeout = timeoutSeconds != null ? timeoutSeconds : 30;
        return new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(timeout))
                .setReadTimeout(Duration.ofSeconds(timeout))
                .build();
    }

    private ResponseEntity<String> executeWithRetry(
            RestTemplate restTemplate, String url, HttpMethod method,
            HttpEntity<MultiValueMap<String, Object>> requestEntity, int maxRetries) {

        RestClientException lastException = null;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    return response;
                }
                throw new BusinessException("图床返回非成功状态码: " + response.getStatusCode());
            } catch (RestClientException e) {
                lastException = e;
                if (i < maxRetries) {
                    log.warn("图床上传第 {} 次失败，准备重试: {}", i + 1, e.getMessage());
                }
            }
        }
        throw new BusinessException("图床上传失败（已重试 " + maxRetries + " 次）: " + lastException.getMessage());
    }
}
