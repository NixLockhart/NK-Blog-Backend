package com.blog.service;

import com.blog.model.dto.statistics.DashboardStatsResponse;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取仪表板统计数据
     */
    DashboardStatsResponse getDashboardStats();

    /**
     * 获取近30天访问量趋势
     */
    Map<LocalDate, Long> getVisitTrend(int days);

    /**
     * 获取文章浏览量排行榜（Top N）
     */
    Map<String, Long> getArticleViewsRanking(int topN);

    /**
     * 记录每日统计（定时任务调用）
     */
    void recordDailyStatistics();
}
