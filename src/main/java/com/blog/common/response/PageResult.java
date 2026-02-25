package com.blog.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应对象
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> content;

    /**
     * 当前页码(从0开始)
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否第一页
     */
    private Boolean first;

    /**
     * 是否最后一页
     */
    private Boolean last;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 构建分页响应
     */
    public static <T> PageResult<T> of(List<T> content, Integer page, Integer size, Long total) {
        PageResult<T> result = new PageResult<>();
        result.setContent(content);
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);

        // 计算总页数
        int totalPages = (int) Math.ceil((double) total / size);
        result.setTotalPages(totalPages);

        // 设置分页状态
        result.setFirst(page == 0);
        result.setLast(page >= totalPages - 1);
        result.setHasPrevious(page > 0);
        result.setHasNext(page < totalPages - 1);

        return result;
    }

    /**
     * 从Spring Data的Page对象构建分页响应
     */
    public static <T, R> PageResult<R> of(List<R> content, Page<T> page) {
        PageResult<R> result = new PageResult<>();
        result.setContent(content);
        result.setPage(page.getNumber());
        result.setSize(page.getSize());
        result.setTotal(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());

        // 设置分页状态
        result.setFirst(page.isFirst());
        result.setLast(page.isLast());
        result.setHasPrevious(page.hasPrevious());
        result.setHasNext(page.hasNext());

        return result;
    }
}
