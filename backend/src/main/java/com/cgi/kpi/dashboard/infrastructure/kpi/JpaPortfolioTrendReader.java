package com.cgi.kpi.dashboard.infrastructure.kpi;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectBudgetRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectPhaseRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectReportSnapshotRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.RiskRepository;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTrendReader;
import com.cgi.kpi.dashboard.kpi.service.PortfolioProjectFilter;
import com.cgi.kpi.dashboard.kpi.service.PortfolioTrendAssembler;

@Component
public class JpaPortfolioTrendReader implements PortfolioTrendReader {

    private static final LocalDate CURRENT_PHASE_REFERENCE_DATE = LocalDate.of(2026, 7, 1);

    private final ProjectRepository projectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectPhaseRepository projectPhaseRepository;
    private final ProjectReportSnapshotRepository projectReportSnapshotRepository;
    private final RiskRepository riskRepository;
    private final PortfolioProjectFilter portfolioProjectFilter;
    private final PortfolioTrendAssembler portfolioTrendAssembler;

    public JpaPortfolioTrendReader(
            ProjectRepository projectRepository,
            ProjectBudgetRepository projectBudgetRepository,
            ProjectPhaseRepository projectPhaseRepository,
            ProjectReportSnapshotRepository projectReportSnapshotRepository,
            RiskRepository riskRepository,
            PortfolioProjectFilter portfolioProjectFilter,
            PortfolioTrendAssembler portfolioTrendAssembler) {
        this.projectRepository = projectRepository;
        this.projectBudgetRepository = projectBudgetRepository;
        this.projectPhaseRepository = projectPhaseRepository;
        this.projectReportSnapshotRepository = projectReportSnapshotRepository;
        this.riskRepository = riskRepository;
        this.portfolioProjectFilter = portfolioProjectFilter;
        this.portfolioTrendAssembler = portfolioTrendAssembler;
    }

    @Override
    public PortfolioTrendDto readTrends(PortfolioFilterCriteria criteria) {
        PortfolioFilterCriteria effectiveCriteria = criteria != null ? criteria : PortfolioFilterCriteria.empty();

        List<Project> projects = projectRepository.findAll();
        Map<UUID, ProjectBudget> budgetsByProjectId = projectBudgetRepository.findAll().stream()
                .collect(Collectors.toMap(b -> b.getProject().getId(), b -> b));
        Map<UUID, List<ProjectPhase>> phasesByProjectId = projectPhaseRepository.findAll().stream()
                .collect(Collectors.groupingBy(phase -> phase.getProject().getId()));
        Map<UUID, List<ProjectReportSnapshot>> snapshotsByProjectId = projectReportSnapshotRepository.findAll().stream()
                .collect(Collectors.groupingBy(snapshot -> snapshot.getProject().getId()));

        List<PortfolioKpiRiskInput> riskInputs = riskRepository.findAll().stream()
                .map(risk -> new PortfolioKpiRiskInput(
                        risk.getProject().getId(),
                        risk.getSeverity(),
                        risk.getStatus()))
                .toList();

        List<PortfolioKpiProjectInput> projectInputs = projects.stream()
                .map(project -> toProjectInput(
                        project,
                        budgetsByProjectId.get(project.getId()),
                        phasesByProjectId.getOrDefault(project.getId(), List.of())))
                .toList();

        Set<UUID> projectIdsWithRiskSeverity = portfolioProjectFilter.projectIdsMatchingRiskSeverity(
                riskInputs,
                effectiveCriteria.riskSeverity());

        List<PortfolioKpiProjectInput> filteredProjectInputs = portfolioProjectFilter.applyProjects(
                projectInputs,
                effectiveCriteria,
                projectIdsWithRiskSeverity);

        if (filteredProjectInputs.isEmpty()) {
            return PortfolioTrendDto.emptyTrend();
        }

        Set<UUID> filteredProjectIds = filteredProjectInputs.stream()
                .map(PortfolioKpiProjectInput::projectId)
                .collect(Collectors.toSet());

        List<Project> filteredProjects = projects.stream()
                .filter(project -> filteredProjectIds.contains(project.getId()))
                .toList();

        return portfolioTrendAssembler.assemble(filteredProjects, snapshotsByProjectId);
    }

    private PortfolioKpiProjectInput toProjectInput(
            Project project,
            ProjectBudget budget,
            List<ProjectPhase> phases) {
        return new PortfolioKpiProjectInput(
                project.getId(),
                project.getStatus(),
                project.getCustomerName(),
                project.getProjectLead(),
                resolveCurrentPhaseName(phases),
                toReportMonth(project.getLastDataUpdate()),
                project.getProgressPercent(),
                project.getScheduleDeviationDays(),
                budget != null ? budget.getPlannedBudget() : null,
                budget != null ? budget.getActualBudget() : null);
    }

    private String resolveCurrentPhaseName(List<ProjectPhase> phases) {
        if (phases == null || phases.isEmpty()) {
            return null;
        }
        return phases.stream()
                .filter(phase -> !CURRENT_PHASE_REFERENCE_DATE.isBefore(phase.getStartDate())
                        && !CURRENT_PHASE_REFERENCE_DATE.isAfter(phase.getEndDate()))
                .max(Comparator.comparingInt(ProjectPhase::getSortOrder))
                .map(ProjectPhase::getName)
                .orElseGet(() -> phases.stream()
                        .max(Comparator.comparingInt(ProjectPhase::getSortOrder))
                        .map(ProjectPhase::getName)
                        .orElse(null));
    }

    private YearMonth toReportMonth(Instant lastDataUpdate) {
        if (lastDataUpdate == null) {
            return null;
        }
        return YearMonth.from(lastDataUpdate.atZone(ZoneOffset.UTC).toLocalDate());
    }
}
