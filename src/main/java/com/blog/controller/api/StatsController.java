package com.blog.controller.api;

import com.blog.common.response.Result;
import com.blog.model.dto.statistics.DashboardStatsResponse;
import com.blog.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
