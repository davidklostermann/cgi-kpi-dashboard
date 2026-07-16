package com.cgi.kpi.dashboard.infrastructure.kpi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProblemRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectBudgetRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectPhaseRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.RiskRepository;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.reader.ProjectKpiReader;
import com.cgi.kpi.dashboard.kpi.service.ProjectKpiCalculator;

@Component
public class JpaProjectKpiReader implements ProjectKpiReader {

    private final ProjectRepository projectRepository;
    private final ProjectBudgetRepository projectBudgetRepository;
    private final ProjectPhaseRepository projectPhaseRepository;
    private final RiskRepository riskRepository;
    private final ProblemRepository problemRepository;
    private final ProjectKpiCalculator projectKpiCalculator;

    public JpaProjectKpiReader(
            ProjectRepository projectRepository,
            ProjectBudgetRepository projectBudgetRepository,
            ProjectPhaseRepository projectPhaseRepository,
            RiskRepository riskRepository,
            ProblemRepository problemRepository,
            ProjectKpiCalculator projectKpiCalculator) {
        this.projectRepository = projectRepository;
        this.projectBudgetRepository = projectBudgetRepository;
        this.projectPhaseRepository = projectPhaseRepository;
        this.riskRepository = riskRepository;
        this.problemRepository = problemRepository;
        this.projectKpiCalculator = projectKpiCalculator;
    }

    @Override
    public Optional<ProjectKpiDto> readProjectKpis(UUID projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }

        Project project = projectOpt.get();
        ProjectBudget budget = projectBudgetRepository.findAll().stream()
                .filter(entry -> projectId.equals(entry.getProject().getId()))
                .findFirst()
                .orElse(null);
        List<ProjectPhase> phases = projectPhaseRepository.findAll().stream()
                .filter(phase -> projectId.equals(phase.getProject().getId()))
                .collect(Collectors.toList());
        List<Risk> risks = riskRepository.findAll().stream()
                .filter(risk -> projectId.equals(risk.getProject().getId()))
                .toList();
        List<Problem> problems = problemRepository.findAll().stream()
                .filter(problem -> projectId.equals(problem.getProject().getId()))
                .toList();

        return Optional.of(projectKpiCalculator.calculate(project, budget, phases, risks, problems));
    }
}
