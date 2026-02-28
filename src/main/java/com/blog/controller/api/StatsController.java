package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.statistics.DashboardStatsResponse;
import com.blog.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * 统计数据API控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "统计数据管理", description = "Statistics API")
public class StatsController {

    private final StatisticsService statisticsService;

    /**
     * 获取仪表板统计数据
     */
    @Operation(summary = "获取仪表板统计数据", description = "获取网站运营的各项统计数据")
    @GetMapping
    public Result<DashboardStatsResponse> getDashboardStats() {
        log.info("获取仪表板统计数据");
        DashboardStatsResponse stats = statisticsService.getDashboardStats();
        return Result.success(stats);
    }

    /**
     * 获取访问量趋势（按天）
     */
    @Operation(summary = "获取访问量趋势", description = "获取近N天的每日访问量数据，用于折线图")
    @GetMapping("/visit-trend")
    public Result<Map<LocalDate, Long>> getVisitTrend(
            @Parameter(description = "天数，默认30") @RequestParam(defaultValue = "30") int days) {
        Map<LocalDate, Long> trend = statisticsService.getVisitTrend(days);
        return Result.success(trend);
    }

    /**
     * 获取文章浏览量排行
     */
    @Operation(summary = "获取文章浏览量排行", description = "获取浏览量最高的N篇文章")
    @GetMapping("/article-ranking")
    public Result<Map<String, Long>> getArticleViewsRanking(
            @Parameter(description = "排行数量，默认10") @RequestParam(defaultValue = "10") int topN) {
        Map<String, Long> ranking = statisticsService.getArticleViewsRanking(topN);
        return Result.success(ranking);
    }
}
