package com.blog.mapper;

import com.blog.model.dto.comment.CommentResponse;
import com.blog.model.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * 评论映射器
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CommentMapper {

    /**
     * 实体转响应DTO
     * 注意: articleTitle 和 children 需要在 Service 层单独填充
     */
    @Mapping(target = "articleTitle", ignore = true)
    @Mapping(target = "children", ignore = true)
    CommentResponse toResponse(Comment comment);

    List<CommentResponse> toResponseList(List<Comment> comments);
}
