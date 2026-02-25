package com.blog.model.dto.statistics;

import lombok.Data;

/**
 * 仪表板统计数据响应DTO
 */
@Data
public class DashboardStatsResponse {

    /**
     * 网站总访问量
     */
    private Long totalViews;

    /**
     * 今日访问量
     */
    private Long todayViews;

    /**
     * 文章总数
     */
    private Long totalArticles;

    /**
     * 文章总点赞量
     */
    private Long totalLikes;

    /**
     * 评论总数
     */
    private Long totalComments;

    /**
     * 留言总数
     */
    private Long totalMessages;

    /**
     * 分类总数
     */
    private Long totalCategories;

    /**
     * 网站运行时长（天数）
     */
    private Long runningDays;

    /**
     * 当前网站版本号
     */
    private String version;
}
