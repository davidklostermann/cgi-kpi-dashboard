package com.cgi.kpi.dashboard.kpi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioStatusDistributionDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendPointDto;

/**
 * Aggregates portfolio trend series from report snapshots (AD-3 / FR-3).
 */
@Component
public class PortfolioTrendAssembler {

    private static final String STATUS_ON_TRACK = "ON_TRACK";
    private static final String STATUS_AT_RISK = "AT_RISK";
    private static final String STATUS_CRITICAL = "CRITICAL";
    private static final String STATUS_COMPLETED = "COMPLETED";

    public PortfolioTrendDto assemble(
            List<Project> filteredProjects,
            Map<UUID, List<ProjectReportSnapshot>> snapshotsByProjectId) {
        if (filteredProjects == null || filteredProjects.isEmpty()) {
            return PortfolioTrendDto.emptyTrend();
        }

        List<UUID> projectIds = filteredProjects.stream().map(Project::getId).toList();
        List<ProjectReportSnapshot> relevantSnapshots = projectIds.stream()
                .flatMap(id -> snapshotsByProjectId.getOrDefault(id, List.of()).stream())
                .toList();

        if (relevantSnapshots.isEmpty()) {
            return new PortfolioTrendDto(
                    List.of(),
                    buildStatusDistribution(filteredProjects),
                    false);
        }

        Map<YearMonth, List<ProjectReportSnapshot>> byMonth = relevantSnapshots.stream()
                .collect(Collectors.groupingBy(snapshot -> YearMonth.from(snapshot.getSnapshotDate())));

        List<PortfolioTrendPointDto> points = byMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toPoint(entry.getKey(), entry.getValue()))
                .toList();

        return new PortfolioTrendDto(points, buildStatusDistribution(filteredProjects), false);
    }

    private static PortfolioTrendPointDto toPoint(YearMonth month, List<ProjectReportSnapshot> snapshots) {
        double averageProgress = snapshots.stream()
                .mapToInt(ProjectReportSnapshot::getProgressPercent)
                .average()
                .orElse(0.0);

        BigDecimal budgetSum = snapshots.stream()
                .map(ProjectReportSnapshot::getActualBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PortfolioTrendPointDto(
                month.toString(),
                roundOneDecimal(averageProgress),
                budgetSum.setScale(2, RoundingMode.HALF_UP).doubleValue());
    }

    private static PortfolioStatusDistributionDto buildStatusDistribution(List<Project> projects) {
        int onTrack = 0;
        int atRisk = 0;
        int critical = 0;
        int completed = 0;

        for (Project project : projects) {
            switch (project.getStatus()) {
                case STATUS_ON_TRACK -> onTrack++;
                case STATUS_AT_RISK -> atRisk++;
                case STATUS_CRITICAL -> critical++;
                case STATUS_COMPLETED -> completed++;
                default -> {
                }
            }
        }

        return new PortfolioStatusDistributionDto(onTrack, atRisk, critical, completed);
    }

    private static double roundOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
