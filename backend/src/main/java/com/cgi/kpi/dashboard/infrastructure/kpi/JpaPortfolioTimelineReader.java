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

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.infrastructure.persistence.MilestoneRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectBudgetRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectPhaseRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.RiskRepository;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioTimelineReader;
import com.cgi.kpi.dashboard.kpi.service.PortfolioProjectFilter;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;
import com.cgi.kpi.dashboard.kpi.service.PortfolioTimelineAssembler;

/**
 * Loads portfolio timeline data from persistence and delegates assembly to {@code kpi.*}.
 */
@Component
public class JpaPortfolioTimelineReader implements PortfolioTimelineReader {

    private static final LocalDate CURRENT_PHASE_REFERENCE_DATE = LocalDate.of(2026, 7, 1);

    private final ProjectRepository projectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectPhaseRepository projectPhaseRepository;
    private final MilestoneRepository milestoneRepository;
    private final RiskRepository riskRepository;
    private final PortfolioProjectFilter portfolioProjectFilter;
    private final CurrentUserService currentUserService;
    private final PortfolioTimelineAssembler portfolioTimelineAssembler;

    public JpaPortfolioTimelineReader(
            ProjectRepository projectRepository,
            ProjectBudgetRepository projectBudgetRepository,
            ProjectPhaseRepository projectPhaseRepository,
            MilestoneRepository milestoneRepository,
            RiskRepository riskRepository,
            PortfolioProjectFilter portfolioProjectFilter,
            CurrentUserService currentUserService,
            PortfolioTimelineAssembler portfolioTimelineAssembler) {
        this.projectRepository = projectRepository;
        this.projectBudgetRepository = projectBudgetRepository;
        this.projectPhaseRepository = projectPhaseRepository;
        this.milestoneRepository = milestoneRepository;
        this.riskRepository = riskRepository;
        this.portfolioProjectFilter = portfolioProjectFilter;
        this.currentUserService = currentUserService;
        this.portfolioTimelineAssembler = portfolioTimelineAssembler;
    }

    @Override
    public PortfolioTimelineDto readTimeline(PortfolioFilterCriteria criteria) {
        PortfolioFilterCriteria effectiveCriteria = criteria != null ? criteria : PortfolioFilterCriteria.empty();

        List<Project> projects = projectRepository.findAllByWorkspaceId(currentUserService.requireWorkspaceId());
        Set<UUID> workspaceProjectIds = projects.stream().map(Project::getId).collect(Collectors.toSet());
        Map<UUID, ProjectBudget> budgetsByProjectId = projectBudgetRepository.findAll().stream()
                .filter(b -> workspaceProjectIds.contains(b.getProject().getId()))
                .collect(Collectors.toMap(b -> b.getProject().getId(), b -> b));
        Map<UUID, List<ProjectPhase>> phasesByProjectId = projectPhaseRepository.findAll().stream()
                .filter(phase -> workspaceProjectIds.contains(phase.getProject().getId()))
                .collect(Collectors.groupingBy(phase -> phase.getProject().getId()));
        Map<UUID, List<Milestone>> milestonesByProjectId = milestoneRepository.findAll().stream()
                .filter(milestone -> workspaceProjectIds.contains(milestone.getProject().getId()))
                .collect(Collectors.groupingBy(milestone -> milestone.getProject().getId()));

        List<PortfolioKpiRiskInput> riskInputs = riskRepository.findAll().stream()
                .filter(risk -> workspaceProjectIds.contains(risk.getProject().getId()))
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
            return PortfolioTimelineDto.emptyTimeline();
        }

        Set<UUID> filteredProjectIds = filteredProjectInputs.stream()
                .map(PortfolioKpiProjectInput::projectId)
                .collect(Collectors.toSet());

        List<Project> filteredProjects = projects.stream()
                .filter(project -> filteredProjectIds.contains(project.getId()))
                .toList();

        return portfolioTimelineAssembler.assemble(filteredProjects, phasesByProjectId, milestonesByProjectId);
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
