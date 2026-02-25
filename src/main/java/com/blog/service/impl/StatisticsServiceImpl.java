package com.blog.service.impl;

import com.blog.config.AppVersionProvider;
import com.blog.model.dto.statistics.DashboardStatsResponse;
import com.blog.model.entity.Statistics;
import com.blog.repository.*;
import com.blog.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final MessageRepository messageRepository;
    private final CategoryRepository categoryRepository;
    private final VisitLogRepository visitLogRepository;
    private final StatisticsRepository statisticsRepository;
    private final AppVersionProvider appVersionProvider;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // 总访问量
        stats.setTotalViews(visitLogRepository.countTotalVisits());

        // 今日访问量
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        stats.setTodayViews(visitLogRepository.countByDate(startOfToday, endOfToday));

        // 文章总数（仅已发布）
        stats.setTotalArticles(articleRepository.countByStatus(1));

        // 文章总点赞量
        stats.setTotalLikes(articleRepository.getTotalLikes(1));

        // 评论总数
        stats.setTotalComments(commentRepository.countByStatus(1));

        // 留言总数
        stats.setTotalMessages(messageRepository.countByStatus(1));

        // 分类总数
        stats.setTotalCategories(categoryRepository.count());

        // 网站版本号（从 git tag 自动获取）
        stats.setVersion(appVersionProvider.getVersion());

        // 网站运行时长（从第一条访问记录开始计算，或使用固定日期）
        LocalDate startDate = LocalDate.of(2025, 11, 10); // 项目启动日期
        long runningDays = ChronoUnit.DAYS.between(startDate, LocalDate.now());
        stats.setRunningDays(runningDays);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> getVisitTrend(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 一次查出日期范围内每天的访问量
        List<Object[]> results = visitLogRepository.countByDateGrouped(startDate, endDate);
        Map<LocalDate, Long> dbData = new LinkedHashMap<>();
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Long count = (Long) row[1];
            dbData.put(date, count);
        }

        // 补零填充无数据的日期
        Map<LocalDate, Long> trend = new LinkedHashMap<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            trend.put(date, dbData.getOrDefault(date, 0L));
        }

        return trend;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getArticleViewsRanking(int topN) {
        Map<String, Long> ranking = new LinkedHashMap<>();

        // 获取热门文章
        List<Object[]> topArticles = articleRepository.findTopArticlesByViews(topN);

        for (Object[] row : topArticles) {
            String title = (String) row[0];
            Long views = (Long) row[1];
            ranking.put(title, views);
        }

        return ranking;
    }

    @Override
    @Transactional
    public void recordDailyStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Statistics stats = new Statistics();
        stats.setStatDate(yesterday);
        stats.setDailyVisits(visitLogRepository.countByDate(startOfDay, endOfDay));
        stats.setDailyUniqueVisitors((int) visitLogRepository.countUniqueVisitorsByDate(startOfDay, endOfDay));
        stats.setTotalVisits(visitLogRepository.countTotalVisits());
        stats.setTotalUniqueVisitors(visitLogRepository.countTotalUniqueVisitors());

        statisticsRepository.save(stats);
        log.info("Daily statistics recorded for {}", yesterday);
    }
}
