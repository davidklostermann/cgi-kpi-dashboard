package com.cgi.kpi.dashboard.kpi.insights;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto.ProjectInsightItemDto;
import com.cgi.kpi.dashboard.kpi.service.ProjectKpiCalculator;

/**
 * Deterministic insight rules for project management (FR-20).
 * Thresholds are provisional [OFFEN] and aligned with seed classifiers.
 */
@Component
public class ProjectInsightEngine {

    private static final Instant STALE_THRESHOLD = Instant.parse("2026-06-01T00:00:00Z");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String TYPE = "deterministisch";
    private static final String PERIOD = "Aktueller Berichtsstand vs. Plan (Referenz 2026-07-01)";

    public List<ProjectInsightItemDto> evaluate(
            Project project,
            ProjectBudget budget,
            List<Risk> risks,
            List<Milestone> milestones,
            List<ProjectReportSnapshot> snapshots) {
        List<ProjectInsightItemDto> insights = new ArrayList<>();

        if (hasBudgetAheadOfProgress(project, budget)) {
            insights.add(item(
                    "BUDGET_AHEAD_OF_PROGRESS",
                    "Budgetverbrauch liegt deutlich vor dem Fortschritt.",
                    "Ist-Budgetanteil vs. Fortschritt",
                    "Schwelle +15 Prozentpunkte [OFFEN]",
                    "Der relative Budgetverbrauch übersteigt den Fortschritt um mehr als 15 Prozentpunkte."));
        }
        if (hasProgressBehindTime(project)) {
            insights.add(item(
                    "PROGRESS_BEHIND_TIME",
                    "Fortschritt liegt hinter dem erwarteten Zeitverbrauch.",
                    "Fortschritt vs. Zeitverbrauch",
                    "Schwelle −20 Prozentpunkte [OFFEN]",
                    "Der Ist-Fortschritt liegt mehr als 20 Punkte unter dem erwarteten Fortschritt zum Referenzdatum."));
        }
        if (hasForecastEndShifted(project)) {
            insights.add(item(
                    "FORECAST_END_SHIFTED",
                    "Die Terminprognose liegt hinter dem Planende.",
                    "Plan-Ende vs. Prognose-Ende",
                    null,
                    "Das prognostizierte Enddatum ist später als das geplante Enddatum."));
        }
        if (hasOverdueMilestone(milestones)) {
            insights.add(item(
                    "OVERDUE_MILESTONE",
                    "Es gibt überfällige Meilensteine.",
                    "Meilenstein-Status OVERDUE",
                    null,
                    "Mindestens ein Meilenstein ist als überfällig markiert."));
        }
        if (hasRiskCluster(risks)) {
            insights.add(item(
                    "RISK_CLUSTER",
                    "Es liegt ein Cluster offener Risiken vor.",
                    "Anzahl offener Risiken",
                    "Schwelle ≥ 3 [OFFEN]",
                    "Drei oder mehr offene Risiken erhöhen die Management-Aufmerksamkeit."));
        }
        if (hasStatusDegraded(snapshots)) {
            insights.add(item(
                    "STATUS_DEGRADED",
                    "Der Ampelstatus hat sich gegenüber dem früheren Berichtsstand verschlechtert.",
                    "Ampelstatus Verlauf",
                    null,
                    "Der aktuelle Snapshot-Status ist kritischer als der früheste vorliegende Snapshot."));
        }
        if (hasStaleData(project)) {
            insights.add(item(
                    "STALE_DATA",
                    "Die Projektdaten sind veraltet.",
                    "Letzte Datenaktualisierung",
                    "Schwelle vor 2026-06-01 [OFFEN]",
                    "Die letzte Datenaktualisierung liegt vor dem festgelegten Frische-Schwellenwert."));
        }
        if (hasConflictingSignals(project, budget, risks)) {
            insights.add(item(
                    "CONFLICTING_SIGNALS",
                    "Ampelstatus und Faktenlage widersprechen sich.",
                    "Status ON_TRACK vs. Abweichung/Budget/Risiken",
                    null,
                    "Trotz Status „Auf Kurs“ liegen Terminverzug, Budgetüberschreitung oder kritische offene Risiken vor."));
        }

        return List.copyOf(insights);
    }

    private static ProjectInsightItemDto item(
            String code,
            String statement,
            String metrics,
            String comparisonValue,
            String rationale) {
        return new ProjectInsightItemDto(code, statement, metrics, comparisonValue, PERIOD, rationale, TYPE);
    }

    private static boolean hasBudgetAheadOfProgress(Project project, ProjectBudget budget) {
        if (budget == null || budget.getPlannedBudget() == null || budget.getPlannedBudget().signum() == 0) {
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
        long elapsedDays = ChronoUnit.DAYS.between(start, ProjectKpiCalculator.REFERENCE_DATE);
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
        return milestones != null && milestones.stream().anyMatch(m -> "OVERDUE".equals(m.getStatus()));
    }

    private static boolean hasRiskCluster(List<Risk> risks) {
        return risks != null && risks.stream().filter(r -> "OPEN".equals(r.getStatus())).count() >= 3;
    }

    private static boolean hasStatusDegraded(List<ProjectReportSnapshot> snapshots) {
        if (snapshots == null || snapshots.size() < 2) {
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
        if (budget != null
                && budget.getActualBudget() != null
                && budget.getPlannedBudget() != null
                && budget.getActualBudget().compareTo(budget.getPlannedBudget()) > 0) {
            return true;
        }
        return risks != null && risks.stream()
                .anyMatch(r -> "OPEN".equals(r.getStatus())
                        && ("HIGH".equals(r.getSeverity()) || "CRITICAL".equals(r.getSeverity())));
    }
}
