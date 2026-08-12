package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DomainRepositoryIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectPhaseRepository projectPhaseRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private ProjectBudgetRepository projectBudgetRepository;

    @Test
    void persistsProjectGraphWithUuidPrimaryKeys() {
        long projectsBefore = projectRepository.count();
        long phasesBefore = projectPhaseRepository.count();
        long milestonesBefore = milestoneRepository.count();
        long risksBefore = riskRepository.count();
        long budgetsBefore = projectBudgetRepository.count();

        Project project = new Project();
        project.setWorkspaceId(WorkspaceIds.DEFAULT);
        project.setName("Pilot Alpha");
        project.setCustomerName("Kunde Intern A");
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 12, 31));
        project.setProgressPercent(45);
        project.setScheduleDeviationDays(14);

        ProjectPhase phase = new ProjectPhase();
        phase.setName("Umsetzung");
        phase.setPhaseType("UMSETZUNG");
        phase.setStartDate(LocalDate.of(2026, 4, 1));
        phase.setEndDate(LocalDate.of(2026, 9, 30));
        phase.setSortOrder(3);
        project.addPhase(phase);

        Milestone milestone = new Milestone();
        milestone.setName("M3 Pilot");
        milestone.setDueDate(LocalDate.of(2026, 8, 15));
        milestone.setStatus("AT_RISK");
        project.addMilestone(milestone);

        Risk risk = new Risk();
        risk.setTitle("Ressourcenengpass");
        risk.setDescription("Fehlende Kapazität im Kernteam");
        risk.setSeverity("HIGH");
        risk.setStatus("OPEN");
        project.addRisk(risk);

        ProjectBudget budget = new ProjectBudget();
        budget.setPlannedBudget(new BigDecimal("500000.00"));
        budget.setActualBudget(new BigDecimal("460000.00"));
        budget.setPlannedEffortDays(new BigDecimal("120.00"));
        budget.setActualEffortDays(new BigDecimal("98.50"));
        project.setBudget(budget);

        Project saved = projectRepository.saveAndFlush(project);

        assertNotNull(saved.getId());
        assertEquals(WorkspaceIds.DEFAULT, saved.getWorkspaceId());
        assertEquals(projectsBefore + 1, projectRepository.count());
        assertEquals(phasesBefore + 1, projectPhaseRepository.count());
        assertEquals(milestonesBefore + 1, milestoneRepository.count());
        assertEquals(risksBefore + 1, riskRepository.count());
        assertEquals(budgetsBefore + 1, projectBudgetRepository.count());

        assertTrue(projectRepository.findById(saved.getId()).isPresent());
        assertEquals(
                WorkspaceIds.DEFAULT,
                projectRepository.findById(saved.getId()).orElseThrow().getWorkspaceId());
        assertTrue(projectPhaseRepository.findAll().stream().allMatch(phaseEntity -> phaseEntity.getId() != null));
        assertTrue(milestoneRepository.findAll().stream().allMatch(m -> m.getId() != null));
        assertTrue(riskRepository.findAll().stream().allMatch(r -> r.getId() != null));
        assertTrue(projectBudgetRepository.findAll().stream().allMatch(b -> b.getId() != null));
    }
}
