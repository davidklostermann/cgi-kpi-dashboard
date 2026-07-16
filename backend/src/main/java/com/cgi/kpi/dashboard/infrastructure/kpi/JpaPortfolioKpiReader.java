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
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectBudgetRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectPhaseRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.RiskRepository;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioKpiReader;
import com.cgi.kpi.dashboard.kpi.service.PortfolioKpiCalculator;
import com.cgi.kpi.dashboard.kpi.service.PortfolioProjectFilter;

/**
 * Loads portfolio data from persistence and delegates KPI calculation to {@code kpi.*}.
 */
@Component
public class JpaPortfolioKpiReader implements PortfolioKpiReader {

    private static final LocalDate CURRENT_PHASE_REFERENCE_DATE = LocalDate.of(2026, 7, 1);

    private final ProjectRepository projectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectPhaseRepository projectPhaseRepository;
    private final RiskRepository riskRepository;
    private final PortfolioKpiCalculator portfolioKpiCalculator;
    private final PortfolioProjectFilter portfolioProjectFilter;

    public JpaPortfolioKpiReader(
            ProjectRepository projectRepository,
            ProjectBudgetRepository projectBudgetRepository,
            ProjectPhaseRepository projectPhaseRepository,
            RiskRepository riskRepository,
            PortfolioKpiCalculator portfolioKpiCalculator,
            PortfolioProjectFilter portfolioProjectFilter) {
        this.projectRepository = projectRepository;
        this.projectBudgetRepository = projectBudgetRepository;
        this.projectPhaseRepository = projectPhaseRepository;
        this.riskRepository = riskRepository;
        this.portfolioKpiCalculator = portfolioKpiCalculator;
        this.portfolioProjectFilter = portfolioProjectFilter;
    }

    @Override
    public PortfolioKpiSummaryDto readPortfolioSummary(PortfolioFilterCriteria criteria) {
        PortfolioFilterCriteria effectiveCriteria = criteria != null ? criteria : PortfolioFilterCriteria.empty();

        List<Project> projects = projectRepository.findAll();
        Map<UUID, ProjectBudget> budgetsByProjectId = projectBudgetRepository.findAll().stream()
                .collect(Collectors.toMap(b -> b.getProject().getId(), b -> b));
        Map<UUID, List<ProjectPhase>> phasesByProjectId = projectPhaseRepository.findAll().stream()
                .collect(Collectors.groupingBy(phase -> phase.getProject().getId()));
        List<PortfolioKpiRiskInput> riskInputs = riskRepository.findAll().stream()
                .map(this::toRiskInput)
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

        List<PortfolioKpiProjectInput> filteredProjects = portfolioProjectFilter.applyProjects(
                projectInputs,
                effectiveCriteria,
                projectIdsWithRiskSeverity);
        List<PortfolioKpiRiskInput> filteredRisks = portfolioProjectFilter.risksForProjects(riskInputs, filteredProjects);

        return portfolioKpiCalculator.calculate(filteredProjects, filteredRisks);
    }

    @Override
    public PortfolioFilterOptionsDto readFilterOptions() {
        List<Project> projects = projectRepository.findAll();
        Map<UUID, List<ProjectPhase>> phasesByProjectId = projectPhaseRepository.findAll().stream()
                .collect(Collectors.groupingBy(phase -> phase.getProject().getId()));

        List<String> customers = projects.stream()
                .map(Project::getCustomerName)
                .distinct()
                .sorted()
                .toList();
        List<String> projectLeads = projects.stream()
                .map(Project::getProjectLead)
                .filter(lead -> lead != null && !lead.isBlank())
                .distinct()
                .sorted()
                .toList();
        List<String> phases = phasesByProjectId.values().stream()
                .flatMap(projectPhases -> projectPhases.stream().map(ProjectPhase::getName))
                .distinct()
                .sorted()
                .toList();
        List<String> reportMonths = projects.stream()
                .map(Project::getLastDataUpdate)
                .filter(update -> update != null)
                .map(this::toReportMonth)
                .filter(month -> month != null)
                .map(YearMonth::toString)
                .distinct()
                .sorted()
                .toList();

        return new PortfolioFilterOptionsDto(
                customers,
                projectLeads,
                phases,
                reportMonths,
                List.of("ON_TRACK", "AT_RISK", "CRITICAL", "COMPLETED"),
                List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"));
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

    private PortfolioKpiRiskInput toRiskInput(Risk risk) {
        return new PortfolioKpiRiskInput(risk.getProject().getId(), risk.getSeverity(), risk.getStatus());
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
