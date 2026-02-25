package com.blog.mapper;

import com.blog.model.dto.category.CategoryResponse;
import com.blog.model.dto.category.CategorySaveRequest;
import com.blog.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 分类映射器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryMapper {

    /**
     * 实体转响应DTO
     */
    CategoryResponse toResponse(Category category);

    /**
     * 保存请求DTO转实体
     * 注意：id, articleCount, 时间字段由服务层处理
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "articleCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Category toEntity(CategorySaveRequest request);

    /**
     * 更新实体
     * 注意：保留ID、统计数据和创建时间
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "articleCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CategorySaveRequest request, @MappingTarget Category category);
}
