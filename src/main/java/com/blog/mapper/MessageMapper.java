package com.blog.mapper;

import com.blog.model.dto.message.MessageResponse;
import com.blog.model.entity.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * 留言映射器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper {

    /**
     * 实体转响应DTO
     */
    @Mapping(source = "website", target = "blogUrl")
    MessageResponse toResponse(Message message);
}
