package com.cgi.kpi.dashboard.kpi.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.kpi.dto.ProjectTrendsDto;

/**
 * Compares the two most recent report snapshots (FR-21 / Story 6.7).
 */
@Component
public class ProjectTrendAssembler {

    public ProjectTrendsDto assemble(java.util.UUID projectId, List<ProjectReportSnapshot> snapshots) {
        if (snapshots == null || snapshots.size() < 2) {
            return new ProjectTrendsDto(
                    projectId,
                    false,
                    "Kein vorheriger Berichtsstand vorhanden — Vergleich nicht möglich.",
                    null,
                    snapshots != null && !snapshots.isEmpty()
                            ? snapshots.stream()
                                    .max(Comparator.comparing(ProjectReportSnapshot::getSnapshotDate))
                                    .map(ProjectReportSnapshot::getSnapshotDate)
                                    .orElse(null)
                            : null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        List<ProjectReportSnapshot> ordered = snapshots.stream()
                .sorted(Comparator.comparing(ProjectReportSnapshot::getSnapshotDate))
                .toList();
        ProjectReportSnapshot previous = ordered.get(ordered.size() - 2);
        ProjectReportSnapshot current = ordered.get(ordered.size() - 1);

        return new ProjectTrendsDto(
                projectId,
                true,
                null,
                previous.getSnapshotDate(),
                current.getSnapshotDate(),
                current.getProgressPercent() - previous.getProgressPercent(),
                current.getActualBudget().subtract(previous.getActualBudget()),
                delta(current.getScheduleDeviationDays(), previous.getScheduleDeviationDays()),
                previous.getStatus(),
                PortfolioStatusLabels.toGermanLabel(previous.getStatus()),
                current.getStatus(),
                PortfolioStatusLabels.toGermanLabel(current.getStatus()),
                current.getOpenRiskCount() - previous.getOpenRiskCount());
    }

    private static Integer delta(Integer current, Integer previous) {
        if (current == null || previous == null) {
            return null;
        }
        return current - previous;
    }
}
