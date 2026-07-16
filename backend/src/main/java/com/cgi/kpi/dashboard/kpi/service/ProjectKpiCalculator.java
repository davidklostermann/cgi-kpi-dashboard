package com.cgi.kpi.dashboard.kpi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto.ProjectBudgetKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto.ProjectEffortKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto.ProjectRiskProblemKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto.ProjectScheduleKpiDto;

/**
 * Deterministic single-project KPI calculation (AD-3 / FR-5 / Story 6.1).
 */
@Component
public class ProjectKpiCalculator {

    private static final String STATUS_OPEN = "OPEN";
    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";
    static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 7, 1);

    public ProjectKpiDto calculate(
            Project project,
            ProjectBudget budget,
            List<ProjectPhase> phases,
            List<Risk> risks,
            List<Problem> problems) {
        Integer deviationDays = ScheduleDeviationResolver.resolve(
                project.getPlannedEndDate(),
                project.getPredictedEndDate(),
                project.getScheduleDeviationDays());

        return new ProjectKpiDto(
                project.getId(),
                project.getStatus(),
                PortfolioStatusLabels.toGermanLabel(project.getStatus()),
                project.getProgressPercent(),
                resolveCurrentPhaseName(phases),
                buildSchedule(project, deviationDays),
                buildBudget(budget, project.getProgressPercent()),
                buildEffort(budget, project.getProgressPercent()),
                buildRiskCounts(risks),
                buildProblemCounts(problems));
    }

    private static ProjectScheduleKpiDto buildSchedule(Project project, Integer deviationDays) {
        return new ProjectScheduleKpiDto(
                timeElapsedPercent(project.getStartDate(), project.getPlannedEndDate(), REFERENCE_DATE),
                deviationDays,
                project.getPlannedEndDate(),
                project.getPredictedEndDate(),
                project.getActualEndDate());
    }

    private static ProjectBudgetKpiDto buildBudget(ProjectBudget budget, int progressPercent) {
        if (budget == null) {
            return new ProjectBudgetKpiDto(null, null, null, null, null, null);
        }
        BigDecimal planned = budget.getPlannedBudget();
        BigDecimal actual = budget.getActualBudget();
        return new ProjectBudgetKpiDto(
                planned,
                actual,
                utilizationPercent(actual, planned),
                deviationPercent(actual, planned),
                remaining(planned, actual),
                forecastAtCompletion(actual, progressPercent));
    }

    private static ProjectEffortKpiDto buildEffort(ProjectBudget budget, int progressPercent) {
        if (budget == null) {
            return new ProjectEffortKpiDto(null, null, null, null, null);
        }
        BigDecimal planned = budget.getPlannedEffortDays();
        BigDecimal actual = budget.getActualEffortDays();
        return new ProjectEffortKpiDto(
                planned,
                actual,
                deviationPercent(actual, planned),
                remaining(planned, actual),
                forecastAtCompletion(actual, progressPercent));
    }

    private static ProjectRiskProblemKpiDto buildRiskCounts(List<Risk> risks) {
        List<Risk> safe = risks == null ? List.of() : risks;
        int open = (int) safe.stream().filter(risk -> STATUS_OPEN.equals(risk.getStatus())).count();
        int critical = (int) safe.stream()
                .filter(risk -> STATUS_OPEN.equals(risk.getStatus()))
                .filter(risk -> SEVERITY_HIGH.equals(risk.getSeverity())
                        || SEVERITY_CRITICAL.equals(risk.getSeverity()))
                .count();
        return new ProjectRiskProblemKpiDto(open, critical);
    }

    private static ProjectRiskProblemKpiDto buildProblemCounts(List<Problem> problems) {
        List<Problem> safe = problems == null ? List.of() : problems;
        int open = (int) safe.stream().filter(problem -> STATUS_OPEN.equals(problem.getStatus())).count();
        int critical = (int) safe.stream()
                .filter(problem -> STATUS_OPEN.equals(problem.getStatus()))
                .filter(problem -> SEVERITY_HIGH.equals(problem.getSeverity())
                        || SEVERITY_CRITICAL.equals(problem.getSeverity()))
                .count();
        return new ProjectRiskProblemKpiDto(open, critical);
    }

    /**
     * Zeitverbrauch = verstrichene Kalendertage seit Start / geplante Gesamtdauer × 100.
     */
    static Double timeElapsedPercent(LocalDate start, LocalDate plannedEnd, LocalDate asOf) {
        if (start == null || plannedEnd == null || asOf == null) {
            return null;
        }
        long plannedDays = ChronoUnit.DAYS.between(start, plannedEnd);
        if (plannedDays <= 0) {
            return null;
        }
        long elapsedDays = ChronoUnit.DAYS.between(start, asOf);
        if (elapsedDays < 0) {
            return 0.0;
        }
        double percent = elapsedDays * 100.0 / plannedDays;
        return BigDecimal.valueOf(percent).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private static Double utilizationPercent(BigDecimal actual, BigDecimal planned) {
        if (actual == null || planned == null || planned.signum() <= 0) {
            return null;
        }
        return actual.divide(planned, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static Double deviationPercent(BigDecimal actual, BigDecimal planned) {
        if (actual == null || planned == null || planned.signum() <= 0) {
            return null;
        }
        return actual.subtract(planned)
                .divide(planned, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static BigDecimal remaining(BigDecimal planned, BigDecimal actual) {
        if (planned == null || actual == null) {
            return null;
        }
        return planned.subtract(actual).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Hochrechnung Endwert = Ist / (Fortschritt/100), wenn Fortschritt &gt; 0.
     */
    private static BigDecimal forecastAtCompletion(BigDecimal actual, int progressPercent) {
        if (actual == null || progressPercent <= 0) {
            return null;
        }
        return actual
                .divide(BigDecimal.valueOf(progressPercent), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    static String resolveCurrentPhaseName(List<ProjectPhase> phases) {
        if (phases == null || phases.isEmpty()) {
            return null;
        }
        return phases.stream()
                .filter(phase -> phase.getStartDate() != null && phase.getEndDate() != null)
                .filter(phase -> !REFERENCE_DATE.isBefore(phase.getStartDate())
                        && !REFERENCE_DATE.isAfter(phase.getEndDate()))
                .max(Comparator.comparingInt(ProjectPhase::getSortOrder))
                .map(ProjectPhase::getName)
                .orElseGet(() -> phases.stream()
                        .max(Comparator.comparingInt(ProjectPhase::getSortOrder))
                        .map(ProjectPhase::getName)
                        .orElse(null));
    }
}
