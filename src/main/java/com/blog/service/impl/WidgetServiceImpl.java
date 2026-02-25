package com.blog.service.impl;

import com.blog.common.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.model.dto.widget.WidgetCodeResponse;
import com.blog.model.dto.widget.WidgetCreateRequest;
import com.blog.model.dto.widget.WidgetResponse;
import com.blog.model.dto.widget.WidgetUpdateRequest;
import com.blog.model.entity.Widget;
import com.blog.repository.WidgetRepository;
import com.blog.service.WidgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 小工具服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {

    private final WidgetRepository widgetRepository;

    @Value("${blog.data.path}")
    private String dataPath;

    @Value("${blog.base-url}")
    private String baseUrl;

    // 小工具代码目录
    private static final String WIDGETS_DIR = "gadgets";
    // 小工具封面目录
    private static final String COVERS_DIR = "gadgets/covers";
    // 默认封面文件名
    private static final String DEFAULT_COVER = "default-cover.jpg";

    // HTML模板容器
    private static final String HTML_TEMPLATE =
        "<div class=\"widget-container\" style=\"width: 100%; max-width: 300px; padding: 16px; background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);\">\n" +
        "  <!-- 在下方编写您的小工具代码 -->\n" +
        "%s\n" +
        "</div>";

    @Override
    @Transactional(readOnly = true)
    public List<WidgetResponse> getAllWidgets() {
        return widgetRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WidgetResponse> getAppliedWidgets() {
        return widgetRepository.findByIsAppliedTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetResponse getWidgetById(Long id) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_NOT_FOUND));
        return convertToResponse(widget);
    }

    @Override
    @Transactional(readOnly = true)
    public WidgetCodeResponse getWidgetCode(Long id) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_NOT_FOUND));

        // 读取代码文件
        Path codePath = Paths.get(dataPath, WIDGETS_DIR, widget.getCodePath());
        String code;
        try {
            code = Files.readString(codePath);
        } catch (IOException e) {
            log.error("读取小工具代码文件失败: {}", codePath, e);
            throw new BusinessException(ErrorCode.WIDGET_CODE_READ_ERROR);
        }

        WidgetCodeResponse response = new WidgetCodeResponse();
        response.setId(widget.getId());
        response.setName(widget.getName());
        response.setCode(code);
        response.setCoverUrl(getCoverUrl(widget.getCoverPath()));
        return response;
    }

    @Override
    @Transactional
    public Long createWidget(WidgetCreateRequest request) {
        // 确保目录存在
        ensureDirectoriesExist();

        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString() + ".html";
        Path codePath = Paths.get(dataPath, WIDGETS_DIR, fileName);

        // 保存代码文件
        try {
            Files.writeString(codePath, request.getCode(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            log.error("保存小工具代码文件失败: {}", codePath, e);
            throw new BusinessException(ErrorCode.WIDGET_CODE_SAVE_ERROR);
        }

        // 保存封面图片（如果提供）
        String coverPath = saveCoverImage(request.getCoverImage());

        // 创建小工具实体
        Widget widget = new Widget();
        widget.setName(request.getName());
        widget.setCodePath(fileName);
        widget.setCoverPath(coverPath);
        widget.setIsApplied(false);
        widget.setIsSystem(false);
        widget.setDisplayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : getNextDisplayOrder());

        widget = widgetRepository.save(widget);
        log.info("创建小工具成功: id={}, name={}", widget.getId(), widget.getName());
        return widget.getId();
    }

    @Override
    @Transactional
    public void updateWidget(Long id, WidgetUpdateRequest request) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_NOT_FOUND));

        // 更新代码文件
        Path codePath = Paths.get(dataPath, WIDGETS_DIR, widget.getCodePath());
        try {
            Files.writeString(codePath, request.getCode(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("更新小工具代码文件失败: {}", codePath, e);
            throw new BusinessException(ErrorCode.WIDGET_CODE_SAVE_ERROR);
        }

        // 更新封面图片（如果提供）
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            // 删除旧封面（如果不是默认封面）
            if (widget.getCoverPath() != null && !widget.getCoverPath().equals(DEFAULT_COVER)) {
                deleteFile(Paths.get(dataPath, WIDGETS_DIR, widget.getCoverPath()));
            }
            String coverPath = saveCoverImage(request.getCoverImage());
            widget.setCoverPath(coverPath);
        }

        // 更新小工具信息
        widget.setName(request.getName());
        if (request.getDisplayOrder() != null) {
            widget.setDisplayOrder(request.getDisplayOrder());
        }

        widgetRepository.save(widget);
        log.info("更新小工具成功: id={}, name={}", widget.getId(), widget.getName());
    }

    @Override
    @Transactional
    public void deleteWidget(Long id) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_NOT_FOUND));

        // 系统自带小工具不可删除
        if (widget.getIsSystem()) {
            throw new BusinessException(ErrorCode.WIDGET_SYSTEM_CANNOT_DELETE);
        }

        // 删除代码文件
        Path codePath = Paths.get(dataPath, WIDGETS_DIR, widget.getCodePath());
        deleteFile(codePath);

        // 删除封面文件（如果不是默认封面）
        if (widget.getCoverPath() != null && !widget.getCoverPath().equals(DEFAULT_COVER)) {
            Path coverPath = Paths.get(dataPath, WIDGETS_DIR, widget.getCoverPath());
            deleteFile(coverPath);
        }

        // 删除数据库记录
        widgetRepository.delete(widget);
        log.info("删除小工具成功: id={}, name={}", widget.getId(), widget.getName());
    }

    @Override
    @Transactional
    public void toggleWidgetApplication(Long id, Boolean isApplied) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_NOT_FOUND));

        widget.setIsApplied(isApplied);
        widgetRepository.save(widget);
        log.info("切换小工具应用状态: id={}, isApplied={}", widget.getId(), isApplied);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportWidget(Long id) {
        Widget widget = widgetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WIDGET_NOT_FOUND));

        // 读取代码文件
        Path codePath = Paths.get(dataPath, WIDGETS_DIR, widget.getCodePath());
        try {
            return Files.readString(codePath);
        } catch (IOException e) {
            log.error("读取小工具代码文件失败: {}", codePath, e);
            throw new BusinessException(ErrorCode.WIDGET_CODE_READ_ERROR);
        }
    }

    /**
     * 转换为响应DTO
     */
    private WidgetResponse convertToResponse(Widget widget) {
        WidgetResponse response = new WidgetResponse();
        response.setId(widget.getId());
        response.setName(widget.getName());
        response.setCodePath(widget.getCodePath());
        response.setCoverUrl(getCoverUrl(widget.getCoverPath()));
        response.setIsApplied(widget.getIsApplied());
        response.setIsSystem(widget.getIsSystem());
        response.setDisplayOrder(widget.getDisplayOrder());
        response.setCreatedAt(widget.getCreatedAt());
        response.setUpdatedAt(widget.getUpdatedAt());
        return response;
    }

    /**
     * 获取封面URL
     */
    private String getCoverUrl(String coverPath) {
        if (coverPath == null || coverPath.isEmpty()) {
            coverPath = DEFAULT_COVER;
        }
        // 系统自带的封面从静态资源获取
        if (coverPath.startsWith("system/")) {
            return "/assets/" + coverPath;
        }
        return "/data/gadgets/" + coverPath;
    }

    /**
     * 保存封面图片
     */
    private String saveCoverImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty()) {
            return DEFAULT_COVER;
        }

        ensureDirectoriesExist();

        try {
            // 解析Base64图片（格式：data:image/png;base64,xxxxx）
            String[] parts = base64Image.split(",");
            if (parts.length != 2) {
                throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT);
            }

            String imageData = parts[1];
            byte[] imageBytes = Base64.getDecoder().decode(imageData);

            // 从MIME类型提取文件扩展名
            String mimeType = parts[0].split(":")[1].split(";")[0];
            String extension = mimeType.split("/")[1];

            // 生成唯一文件名
            String fileName = "covers/" + UUID.randomUUID().toString() + "." + extension;
            Path imagePath = Paths.get(dataPath, WIDGETS_DIR, fileName);

            Files.write(imagePath, imageBytes, StandardOpenOption.CREATE);
            return fileName;
        } catch (Exception e) {
            log.error("保存封面图片失败", e);
            return DEFAULT_COVER;
        }
    }

    /**
     * 确保目录存在
     */
    private void ensureDirectoriesExist() {
        try {
            Path widgetsPath = Paths.get(dataPath, WIDGETS_DIR);
            if (!Files.exists(widgetsPath)) {
                Files.createDirectories(widgetsPath);
            }

            Path coversPath = Paths.get(dataPath, COVERS_DIR);
            if (!Files.exists(coversPath)) {
                Files.createDirectories(coversPath);
            }
        } catch (IOException e) {
            log.error("创建目录失败", e);
            throw new BusinessException(ErrorCode.FILE_SYSTEM_ERROR);
        }
    }

    /**
     * 删除文件
     */
    private void deleteFile(Path path) {
        try {
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            log.warn("删除文件失败: {}", path, e);
        }
    }

    /**
     * 获取下一个显示顺序
     */
    private int getNextDisplayOrder() {
        return widgetRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .mapToInt(Widget::getDisplayOrder)
                .max()
                .orElse(0) + 1;
    }
}
