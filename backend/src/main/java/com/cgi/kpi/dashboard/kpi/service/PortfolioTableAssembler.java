package com.cgi.kpi.dashboard.kpi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableRowDto;

/**
 * Assembles portfolio management table rows from domain data (AD-3 / FR-2).
 */
@Component
public class PortfolioTableAssembler {

    private static final String STATUS_OPEN = "OPEN";
    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    public PortfolioTableDto assemble(
            List<Project> projects,
            Map<UUID, ProjectBudget> budgetsByProjectId,
            Map<UUID, String> currentPhaseByProjectId,
            Map<UUID, List<Risk>> risksByProjectId,
            Map<UUID, List<Problem>> problemsByProjectId) {
        if (projects == null || projects.isEmpty()) {
            return PortfolioTableDto.emptyTable();
        }

        List<PortfolioTableRowDto> rows = projects.stream()
                .map(project -> toRow(
                        project,
                        budgetsByProjectId.get(project.getId()),
                        currentPhaseByProjectId.get(project.getId()),
                        risksByProjectId.getOrDefault(project.getId(), List.of()),
                        problemsByProjectId.getOrDefault(project.getId(), List.of())))
                .toList();

        return new PortfolioTableDto(rows, false);
    }

    private PortfolioTableRowDto toRow(
            Project project,
            ProjectBudget budget,
            String currentPhaseName,
            List<Risk> risks,
            List<Problem> problems) {
        return new PortfolioTableRowDto(
                project.getId(),
                project.getName(),
                project.getCustomerName(),
                project.getProjectLead(),
                project.getStatus(),
                PortfolioStatusLabels.toGermanLabel(project.getStatus()),
                currentPhaseName,
                project.getProgressPercent(),
                project.getPlannedEndDate(),
                project.getPredictedEndDate(),
                ScheduleDeviationResolver.resolve(
                        project.getPlannedEndDate(),
                        project.getPredictedEndDate(),
                        project.getScheduleDeviationDays()),
                budgetUtilizationPercent(budget),
                budgetDeviationPercent(budget),
                effortDeviationPercent(budget),
                countOpenRisks(risks),
                countCriticalIssues(risks, problems),
                project.getLastDataUpdate());
    }

    private static Double budgetUtilizationPercent(ProjectBudget budget) {
        if (budget == null || budget.getPlannedBudget().signum() <= 0) {
            return null;
        }
        return budget.getActualBudget()
                .divide(budget.getPlannedBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static Double budgetDeviationPercent(ProjectBudget budget) {
        if (budget == null || budget.getPlannedBudget().signum() <= 0) {
            return null;
        }
        return budget.getActualBudget()
                .subtract(budget.getPlannedBudget())
                .divide(budget.getPlannedBudget(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static Double effortDeviationPercent(ProjectBudget budget) {
        if (budget == null || budget.getPlannedEffortDays().signum() <= 0) {
            return null;
        }
        return budget.getActualEffortDays()
                .subtract(budget.getPlannedEffortDays())
                .divide(budget.getPlannedEffortDays(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static int countOpenRisks(List<Risk> risks) {
        return (int) risks.stream()
                .filter(risk -> STATUS_OPEN.equals(risk.getStatus()))
                .count();
    }

    private static int countCriticalIssues(List<Risk> risks, List<Problem> problems) {
        long criticalRisks = risks.stream()
                .filter(risk -> STATUS_OPEN.equals(risk.getStatus()))
                .filter(risk -> SEVERITY_HIGH.equals(risk.getSeverity()) || SEVERITY_CRITICAL.equals(risk.getSeverity()))
                .count();
        long criticalProblems = problems.stream()
                .filter(problem -> STATUS_OPEN.equals(problem.getStatus()))
                .filter(problem -> SEVERITY_HIGH.equals(problem.getSeverity()) || SEVERITY_CRITICAL.equals(problem.getSeverity()))
                .count();
        return (int) (criticalRisks + criticalProblems);
    }
}
