package com.cgi.kpi.dashboard.infrastructure.persistence.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.domain.model.Risk;

public final class MockSeedInsightClassifier {

    private static final Instant STALE_THRESHOLD = Instant.parse("2026-06-01T00:00:00Z");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private MockSeedInsightClassifier() {
    }

    public static Set<MockManagementInsightType> classify(
            Project project,
            ProjectBudget budget,
            List<Risk> risks,
            List<Milestone> milestones,
            List<ProjectReportSnapshot> snapshots) {
        EnumSet<MockManagementInsightType> types = EnumSet.noneOf(MockManagementInsightType.class);

        if (hasBudgetAheadOfProgress(project, budget)) {
            types.add(MockManagementInsightType.BUDGET_AHEAD_OF_PROGRESS);
        }
        if (hasProgressBehindTime(project)) {
            types.add(MockManagementInsightType.PROGRESS_BEHIND_TIME);
        }
        if (hasForecastEndShifted(project)) {
            types.add(MockManagementInsightType.FORECAST_END_SHIFTED);
        }
        if (hasOverdueMilestone(milestones)) {
            types.add(MockManagementInsightType.OVERDUE_MILESTONE);
        }
        if (hasRiskCluster(risks)) {
            types.add(MockManagementInsightType.RISK_CLUSTER);
        }
        if (hasStatusDegraded(snapshots)) {
            types.add(MockManagementInsightType.STATUS_DEGRADED);
        }
        if (hasStaleData(project)) {
            types.add(MockManagementInsightType.STALE_DATA);
        }
        if (hasConflictingSignals(project, budget, risks)) {
            types.add(MockManagementInsightType.CONFLICTING_SIGNALS);
        }

        return types;
    }

    private static boolean hasBudgetAheadOfProgress(Project project, ProjectBudget budget) {
        if (budget == null || budget.getPlannedBudget().signum() == 0) {
            return false;
        }
        BigDecimal budgetRatio = budget.getActualBudget()
                .divide(budget.getPlannedBudget(), 4, RoundingMode.HALF_UP);
        BigDecimal progressRatio = BigDecimal.valueOf(project.getProgressPercent())
                .divide(HUNDRED, 4, RoundingMode.HALF_UP);
        return budgetRatio.subtract(progressRatio).compareTo(BigDecimal.valueOf(0.15)) > 0;
    }

    private static boolean hasProgressBehindTime(Project project) {
        LocalDate start = project.getStartDate();
        LocalDate end = project.getPlannedEndDate();
        if (start == null || end == null || !end.isAfter(start)) {
            return false;
        }
        long totalDays = ChronoUnit.DAYS.between(start, end);
        long elapsedDays = ChronoUnit.DAYS.between(start, LocalDate.of(2026, 7, 1));
        if (totalDays <= 0 || elapsedDays <= 0) {
            return false;
        }
        double expectedProgress = (double) elapsedDays / totalDays * 100.0;
        return project.getProgressPercent() + 20 < expectedProgress;
    }

    private static boolean hasForecastEndShifted(Project project) {
        LocalDate predicted = project.getPredictedEndDate();
        LocalDate planned = project.getPlannedEndDate();
        return predicted != null && planned != null && predicted.isAfter(planned);
    }

    private static boolean hasOverdueMilestone(List<Milestone> milestones) {
        return milestones.stream().anyMatch(m -> "OVERDUE".equals(m.getStatus()));
    }

    private static boolean hasRiskCluster(List<Risk> risks) {
        return risks.stream().filter(r -> "OPEN".equals(r.getStatus())).count() >= 3;
    }

    private static boolean hasStatusDegraded(List<ProjectReportSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return false;
        }
        List<ProjectReportSnapshot> ordered = snapshots.stream()
                .sorted(Comparator.comparing(ProjectReportSnapshot::getSnapshotDate))
                .toList();
        ProjectReportSnapshot previous = ordered.get(0);
        ProjectReportSnapshot current = ordered.get(ordered.size() - 1);
        return statusRank(current.getStatus()) > statusRank(previous.getStatus());
    }

    private static int statusRank(String status) {
        return switch (status) {
            case "ON_TRACK" -> 0;
            case "AT_RISK" -> 1;
            case "CRITICAL" -> 2;
            case "COMPLETED" -> -1;
            default -> 0;
        };
    }

    private static boolean hasStaleData(Project project) {
        Instant lastUpdate = project.getLastDataUpdate();
        return lastUpdate != null && lastUpdate.isBefore(STALE_THRESHOLD);
    }

    private static boolean hasConflictingSignals(Project project, ProjectBudget budget, List<Risk> risks) {
        if (!"ON_TRACK".equals(project.getStatus())) {
            return false;
        }
        Integer deviation = project.getScheduleDeviationDays();
        if (deviation != null && deviation > 0) {
            return true;
        }
        if (budget != null && budget.getActualBudget().compareTo(budget.getPlannedBudget()) > 0) {
            return true;
        }
        return risks.stream()
                .anyMatch(r -> "OPEN".equals(r.getStatus())
                        && ("HIGH".equals(r.getSeverity()) || "CRITICAL".equals(r.getSeverity())));
    }
}
