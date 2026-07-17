package com.cgi.kpi.dashboard.infrastructure.kpi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectCapacitySummary;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.domain.model.ProjectRoleCapacity;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.infrastructure.persistence.MilestoneRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProblemRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectBudgetRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectCapacitySummaryRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectPhaseRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectReportSnapshotRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRoleCapacityRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.RiskRepository;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectMasterDataDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectTrendsDto;
import com.cgi.kpi.dashboard.kpi.insights.ProjectInsightEngine;
import com.cgi.kpi.dashboard.kpi.reader.ProjectKpiReader;
import com.cgi.kpi.dashboard.kpi.service.PortfolioStatusLabels;
import com.cgi.kpi.dashboard.kpi.service.ProjectIssuesCapacityAssembler;
import com.cgi.kpi.dashboard.kpi.service.ProjectKpiCalculator;
import com.cgi.kpi.dashboard.kpi.service.ProjectPhasesAssembler;
import com.cgi.kpi.dashboard.kpi.service.ProjectTrendAssembler;

@Component
public class JpaProjectKpiReader implements ProjectKpiReader {

    private final ProjectRepository projectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectPhaseRepository projectPhaseRepository;
    private final MilestoneRepository milestoneRepository;
    private final RiskRepository riskRepository;
    private final ProblemRepository problemRepository;
    private final ProjectReportSnapshotRepository projectReportSnapshotRepository;
    private final ProjectRoleCapacityRepository projectRoleCapacityRepository;
    private final ProjectCapacitySummaryRepository projectCapacitySummaryRepository;
    private final ProjectKpiCalculator projectKpiCalculator;
    private final ProjectPhasesAssembler projectPhasesAssembler;
    private final ProjectInsightEngine projectInsightEngine;
    private final ProjectTrendAssembler projectTrendAssembler;
    private final ProjectIssuesCapacityAssembler projectIssuesCapacityAssembler;

    public JpaProjectKpiReader(
            ProjectRepository projectRepository,
            ProjectBudgetRepository projectBudgetRepository,
            ProjectPhaseRepository projectPhaseRepository,
            MilestoneRepository milestoneRepository,
            RiskRepository riskRepository,
            ProblemRepository problemRepository,
            ProjectReportSnapshotRepository projectReportSnapshotRepository,
            ProjectRoleCapacityRepository projectRoleCapacityRepository,
            ProjectCapacitySummaryRepository projectCapacitySummaryRepository,
            ProjectKpiCalculator projectKpiCalculator,
            ProjectPhasesAssembler projectPhasesAssembler,
            ProjectInsightEngine projectInsightEngine,
            ProjectTrendAssembler projectTrendAssembler,
            ProjectIssuesCapacityAssembler projectIssuesCapacityAssembler) {
        this.projectRepository = projectRepository;
        this.projectBudgetRepository = projectBudgetRepository;
        this.projectPhaseRepository = projectPhaseRepository;
        this.milestoneRepository = milestoneRepository;
        this.riskRepository = riskRepository;
        this.problemRepository = problemRepository;
        this.projectReportSnapshotRepository = projectReportSnapshotRepository;
        this.projectRoleCapacityRepository = projectRoleCapacityRepository;
        this.projectCapacitySummaryRepository = projectCapacitySummaryRepository;
        this.projectKpiCalculator = projectKpiCalculator;
        this.projectPhasesAssembler = projectPhasesAssembler;
        this.projectInsightEngine = projectInsightEngine;
        this.projectTrendAssembler = projectTrendAssembler;
        this.projectIssuesCapacityAssembler = projectIssuesCapacityAssembler;
    }

    @Override
    public Optional<ProjectKpiDto> readProjectKpis(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }

        Project project = projectOpt.get();
        ProjectBudget budget = findBudget(projectId);
        List<ProjectPhase> phases = findPhases(projectId);
        List<Risk> risks = findRisks(projectId);
        List<Problem> problems = findProblems(projectId);

        return Optional.of(projectKpiCalculator.calculate(project, budget, phases, risks, problems));
    }

    @Override
    public Optional<ProjectMasterDataDto> readProjectMasterData(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }

        Project project = projectOpt.get();
        List<ProjectPhase> phases = findPhases(projectId);

        return Optional.of(new ProjectMasterDataDto(
                project.getId(),
                project.getName(),
                project.getCustomerName(),
                project.getProjectLead(),
                project.getStartDate(),
                project.getPlannedEndDate(),
                project.getPredictedEndDate(),
                ProjectKpiCalculator.resolveCurrentPhaseName(phases),
                project.getStatus(),
                PortfolioStatusLabels.toGermanLabel(project.getStatus()),
                project.getLastDataUpdate()));
    }

    @Override
    public Optional<ProjectPhasesDto> readProjectPhases(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }

        Project project = projectOpt.get();
        return Optional.of(projectPhasesAssembler.assemble(project, findPhases(projectId), findMilestones(projectId)));
    }

    @Override
    public Optional<ProjectInsightsDto> readProjectInsights(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }

        Project project = projectOpt.get();
        return Optional.of(new ProjectInsightsDto(
                projectId,
                projectInsightEngine.evaluate(
                        project,
                        findBudget(projectId),
                        findRisks(projectId),
                        findMilestones(projectId),
                        findSnapshots(projectId))));
    }

    @Override
    public Optional<ProjectTrendsDto> readProjectTrends(UUID projectId) {
        if (projectRepository.findById(projectId).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(projectTrendAssembler.assemble(projectId, findSnapshots(projectId)));
    }

    @Override
    public Optional<ProjectIssuesActionsDto> readProjectIssuesActions(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        Project project = projectOpt.get();
        return Optional.of(projectIssuesCapacityAssembler.assembleIssuesActions(
                project, findProblems(projectId), findRisks(projectId)));
    }

    @Override
    public Optional<ProjectCapacityDto> readProjectCapacity(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        Project project = projectOpt.get();
        List<ProjectRoleCapacity> roles =
                projectRoleCapacityRepository.findByProject_IdOrderBySortOrderAsc(projectId);
        ProjectCapacitySummary summary =
                projectCapacitySummaryRepository.findById(projectId).orElse(null);
        return Optional.of(projectIssuesCapacityAssembler.assembleCapacity(project, roles, summary));
    }

    private ProjectBudget findBudget(UUID projectId) {
        return projectBudgetRepository.findAll().stream()
                .filter(entry -> projectId.equals(entry.getProject().getId()))
                .findFirst()
                .orElse(null);
    }

    private List<ProjectPhase> findPhases(UUID projectId) {
        return projectPhaseRepository.findAll().stream()
                .filter(phase -> projectId.equals(phase.getProject().getId()))
                .collect(Collectors.toList());
    }

    private List<Milestone> findMilestones(UUID projectId) {
        return milestoneRepository.findAll().stream()
                .filter(milestone -> projectId.equals(milestone.getProject().getId()))
                .toList();
    }

    private List<Risk> findRisks(UUID projectId) {
        return riskRepository.findAll().stream()
                .filter(risk -> projectId.equals(risk.getProject().getId()))
                .toList();
    }

    private List<Problem> findProblems(UUID projectId) {
        return problemRepository.findAll().stream()
                .filter(problem -> projectId.equals(problem.getProject().getId()))
                .toList();
    }

    private List<ProjectReportSnapshot> findSnapshots(UUID projectId) {
        return projectReportSnapshotRepository.findAll().stream()
                .filter(snapshot -> projectId.equals(snapshot.getProject().getId()))
                .toList();
    }
}
