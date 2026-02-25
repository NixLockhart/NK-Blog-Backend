package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.model.dto.theme.ThemeCreateRequest;
import com.blog.model.dto.theme.ThemeResponse;
import com.blog.model.dto.theme.ThemeUpdateRequest;
import com.blog.model.entity.Theme;
import com.blog.repository.ThemeRepository;
import com.blog.service.ThemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;

/**
 * 主题服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ThemeRepository themeRepository;

    @Value("${blog.data.path}")
    private String dataPath;

    @Value("${blog.base-url}")
    private String baseUrl;

    // 主题文件目录
    private static final String THEMES_DIR = "themes";
    // 主题封面目录
    private static final String COVERS_DIR = "themes/covers";
    // 默认封面文件名
    private static final String DEFAULT_COVER = "theme_default.jpg";

    @Override
    @Transactional(readOnly = true)
    public List<ThemeResponse> getAllThemes() {
        return themeRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeResponse getAppliedTheme() {
        Theme theme = themeRepository.findByIsActive(1)
                .orElse(null);
        if (theme == null) {
            return null;
        }
        return convertToResponse(theme);
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeResponse getThemeById(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));
        return convertToResponse(theme);
    }

    @Override
    @Transactional
    public Long createTheme(ThemeCreateRequest request, MultipartFile lightCss, MultipartFile darkCss) {
        // 生成主题slug
        String slug = generateSlug(request.getName());

        // 检查slug是否已存在
        if (themeRepository.existsBySlug(slug)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "主题标识已存在");
        }

        // 创建主题实体
        Theme theme = new Theme();
        theme.setName(request.getName());
        theme.setSlug(slug);
        theme.setDescription(request.getDescription());
        theme.setAuthor(request.getAuthor());
        theme.setVersion(request.getVersion());
        theme.setThemePath(slug);
        theme.setIsActive(0);
        theme.setIsDefault(0);
        theme.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0);

        // 保存主题文件
        try {
            saveThemeFiles(slug, lightCss, darkCss);
        } catch (IOException e) {
            log.error("保存主题文件失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "保存主题文件失败");
        }

        // 保存封面
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            String coverPath = saveCoverImage(request.getCoverImage(), slug);
            theme.setCoverPath(coverPath);
        } else {
            theme.setCoverPath(DEFAULT_COVER);
        }

        theme = themeRepository.save(theme);
        log.info("创建主题成功: id={}, name={}", theme.getId(), theme.getName());
        return theme.getId();
    }

    @Override
    @Transactional
    public void updateTheme(Long id, ThemeUpdateRequest request) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));

        // 检查是否为默认主题
        if (theme.getIsDefault() == 1) {
            throw new BusinessException(ErrorCode.THEME_OPERATION_NOT_ALLOWED.getCode(), "默认主题不允许修改");
        }

        // 更新基本信息
        theme.setName(request.getName());
        theme.setDescription(request.getDescription());
        theme.setAuthor(request.getAuthor());
        theme.setVersion(request.getVersion());

        if (request.getDisplayOrder() != null) {
            theme.setDisplayOrder(request.getDisplayOrder());
        }

        // 更新封面
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            String coverPath = saveCoverImage(request.getCoverImage(), theme.getSlug());
            theme.setCoverPath(coverPath);
        }

        themeRepository.save(theme);
        log.info("更新主题成功: id={}, name={}", id, theme.getName());
    }

    @Override
    @Transactional
    public void deleteTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));

        // 检查是否为默认主题
        if (theme.getIsDefault() == 1) {
            throw new BusinessException(ErrorCode.THEME_OPERATION_NOT_ALLOWED.getCode(), "默认主题不允许删除");
        }

        // 如果当前应用了该主题，不允许删除
        if (theme.getIsActive() == 1) {
            throw new BusinessException(ErrorCode.THEME_OPERATION_NOT_ALLOWED.getCode(), "当前应用的主题不允许删除");
        }

        // 删除主题文件
        try {
            deleteThemeFiles(theme.getSlug());
        } catch (IOException e) {
            log.warn("删除主题文件失败: {}", theme.getSlug(), e);
        }

        themeRepository.delete(theme);
        log.info("删除主题成功: id={}, name={}", id, theme.getName());
    }

    @Override
    @Transactional
    public void toggleThemeApplication(Long id, Boolean isApplied) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));

        if (isApplied) {
            // 应用该主题：先取消所有其他主题的应用状态
            themeRepository.findByIsActive(1).ifPresent(activeTheme -> {
                activeTheme.setIsActive(0);
                themeRepository.save(activeTheme);
            });

            theme.setIsActive(1);
            log.info("应用主题: id={}, name={}", id, theme.getName());
        } else {
            // 取消应用：切换回默认主题
            theme.setIsActive(0);

            // 应用默认主题
            themeRepository.findBySlug("default").ifPresent(defaultTheme -> {
                defaultTheme.setIsActive(1);
                themeRepository.save(defaultTheme);
            });

            log.info("取消应用主题: id={}, name={}", id, theme.getName());
        }

        themeRepository.save(theme);
    }

    @Override
    public String uploadThemeCover(Long id, MultipartFile file) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));

        try {
            // 验证文件
            validateImageFile(file);

            // 保存封面
            String fileName = theme.getSlug() + getFileExtension(file.getOriginalFilename());
            Path coversPath = Paths.get(dataPath, COVERS_DIR);
            Files.createDirectories(coversPath);

            Path filePath = coversPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 更新主题封面路径
            theme.setCoverPath(fileName);
            themeRepository.save(theme);

            return getCoverUrl(fileName);
        } catch (IOException e) {
            log.error("上传主题封面失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "上传主题封面失败");
        }
    }

    /**
     * 保存主题CSS文件
     */
    private void saveThemeFiles(String slug, MultipartFile lightCss, MultipartFile darkCss) throws IOException {
        Path themePath = Paths.get(dataPath, THEMES_DIR, slug);
        Files.createDirectories(themePath);

        if (lightCss != null && !lightCss.isEmpty()) {
            Path lightPath = themePath.resolve("light.css");
            Files.copy(lightCss.getInputStream(), lightPath, StandardCopyOption.REPLACE_EXISTING);
        }

        if (darkCss != null && !darkCss.isEmpty()) {
            Path darkPath = themePath.resolve("dark.css");
            Files.copy(darkCss.getInputStream(), darkPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * 删除主题文件
     */
    private void deleteThemeFiles(String slug) throws IOException {
        Path themePath = Paths.get(dataPath, THEMES_DIR, slug);
        if (Files.exists(themePath)) {
            Files.walk(themePath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            log.warn("删除文件失败: {}", path, e);
                        }
                    });
        }
    }

    /**
     * 保存封面图片（Base64）
     */
    private String saveCoverImage(String base64Image, String slug) {
        try {
            // 解析Base64
            String[] parts = base64Image.split(",");
            String imageData = parts.length > 1 ? parts[1] : parts[0];
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            // 确定文件扩展名
            String extension = ".jpg";
            if (parts.length > 1 && parts[0].contains("png")) {
                extension = ".png";
            }

            // 保存文件
            String fileName = slug + extension;
            Path coversPath = Paths.get(dataPath, COVERS_DIR);
            Files.createDirectories(coversPath);

            Path filePath = coversPath.resolve(fileName);
            Files.write(filePath, imageBytes);

            return fileName;
        } catch (Exception e) {
            log.error("保存封面图片失败", e);
            return DEFAULT_COVER;
        }
    }

    /**
     * 生成主题slug
     */
    private String generateSlug(String name) {
        // 简单实现：使用UUID
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 验证图片文件
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.contains("image/jpeg") &&
                !contentType.contains("image/png") && !contentType.contains("image/jpg"))) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED.getCode(),
                    "只支持 JPG、JPEG、PNG 格式的图片");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED.getCode(), "文件大小超过限制（最大10MB）");
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
     * 获取封面URL
     */
    private String getCoverUrl(String coverPath) {
        if (coverPath == null || coverPath.isEmpty()) {
            return "/files/themes/covers/" + DEFAULT_COVER;
        }
        return "/files/themes/covers/" + coverPath;
    }

    /**
     * 转换为响应DTO
     */
    private ThemeResponse convertToResponse(Theme theme) {
        ThemeResponse response = new ThemeResponse();
        response.setId(theme.getId());
        response.setName(theme.getName());
        response.setSlug(theme.getSlug());
        response.setDescription(theme.getDescription());
        response.setAuthor(theme.getAuthor());
        response.setVersion(theme.getVersion());
        response.setThemePath(theme.getThemePath());
        response.setCoverUrl(getCoverUrl(theme.getCoverPath()));
        response.setPreviewImageUrl(theme.getPreviewImage());
        response.setIsApplied(theme.getIsActive() == 1);
        response.setIsDefault(theme.getIsDefault() == 1);
        response.setDisplayOrder(theme.getDisplayOrder());
        response.setCreatedAt(theme.getCreatedAt());
        response.setUpdatedAt(theme.getUpdatedAt());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportTheme(Long id) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);

            // 添加light.css
            Path lightCssPath = Paths.get(dataPath, THEMES_DIR, theme.getSlug(), "light.css");
            if (Files.exists(lightCssPath)) {
                ZipEntry lightEntry = new ZipEntry("light.css");
                zos.putNextEntry(lightEntry);
                Files.copy(lightCssPath, zos);
                zos.closeEntry();
            }

            // 添加dark.css
            Path darkCssPath = Paths.get(dataPath, THEMES_DIR, theme.getSlug(), "dark.css");
            if (Files.exists(darkCssPath)) {
                ZipEntry darkEntry = new ZipEntry("dark.css");
                zos.putNextEntry(darkEntry);
                Files.copy(darkCssPath, zos);
                zos.closeEntry();
            }

            // 添加封面图片（如果不是默认封面）
            if (theme.getCoverPath() != null && !theme.getCoverPath().equals(DEFAULT_COVER)) {
                Path coverPath = Paths.get(dataPath, COVERS_DIR, theme.getCoverPath());
                if (Files.exists(coverPath)) {
                    String extension = getFileExtension(theme.getCoverPath());
                    ZipEntry coverEntry = new ZipEntry("cover" + extension);
                    zos.putNextEntry(coverEntry);
                    Files.copy(coverPath, zos);
                    zos.closeEntry();
                }
            }

            zos.close();
            log.info("导出主题成功: id={}, name={}", id, theme.getName());
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("导出主题失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "导出主题失败");
        }
    }

    @Override
    @Transactional
    public void updateThemeFiles(Long id, MultipartFile lightCss, MultipartFile darkCss) {
        Theme theme = themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND.getCode(), "主题不存在"));

        // 检查是否为默认主题
        if (theme.getIsDefault() == 1) {
            throw new BusinessException(ErrorCode.THEME_OPERATION_NOT_ALLOWED.getCode(), "默认主题不允许修改文件");
        }

        try {
            saveThemeFiles(theme.getSlug(), lightCss, darkCss);
            log.info("更新主题文件成功: id={}, name={}", id, theme.getName());
        } catch (IOException e) {
            log.error("更新主题文件失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "更新主题文件失败");
        }
    }
}
