package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.domain.model.Risk;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DomainExtensionIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private ProjectReportSnapshotRepository projectReportSnapshotRepository;

    @Test
    void persistsExtendedProjectMasterDataFields() {
        Project project = baseProject("Extended Master Data Pilot");
        project.setProjectLead("Alex Schmidt");
        project.setLastDataUpdate(Instant.parse("2026-07-01T10:00:00Z"));
        project.setPredictedEndDate(LocalDate.of(2026, 8, 15));

        Project saved = projectRepository.saveAndFlush(project);

        Project loaded = projectRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Alex Schmidt", loaded.getProjectLead());
        assertEquals(Instant.parse("2026-07-01T10:00:00Z"), loaded.getLastDataUpdate());
        assertEquals(LocalDate.of(2026, 8, 15), loaded.getPredictedEndDate());
    }

    @Test
    void persistsProblemSeparateFromRiskWithFr6MinimumFields() {
        long problemsBefore = problemRepository.count();
        long risksBefore = riskRepository.count();

        Project project = baseProject("Problem Isolation Pilot");

        Problem problem = new Problem();
        problem.setTitle("Lieferverzug Subsystem");
        problem.setDescription("Kritische Schnittstelle nicht termingerecht geliefert");
        problem.setSeverity("HIGH");
        problem.setStatus("OPEN");
        problem.setResponsible("PMO Lead");
        problem.setTargetDate(LocalDate.of(2026, 5, 30));
        problem.setCountermeasure("Eskalation an Lieferant, Parallel-Workaround");
        project.addProblem(problem);

        Risk risk = new Risk();
        risk.setTitle("Integrationsrisiko");
        risk.setDescription("Ungetestete Schnittstelle");
        risk.setSeverity("MEDIUM");
        risk.setStatus("OPEN");
        project.addRisk(risk);

        projectRepository.saveAndFlush(project);

        assertEquals(problemsBefore + 1, problemRepository.count());
        assertEquals(risksBefore + 1, riskRepository.count());

        Problem savedProblem = problemRepository.findAll().stream()
                .filter(p -> "Lieferverzug Subsystem".equals(p.getTitle()))
                .findFirst()
                .orElseThrow();
        assertNotNull(savedProblem.getId());
        assertEquals("PMO Lead", savedProblem.getResponsible());
        assertEquals(LocalDate.of(2026, 5, 30), savedProblem.getTargetDate());
        assertEquals("Eskalation an Lieferant, Parallel-Workaround", savedProblem.getCountermeasure());
    }

    @Test
    void persistsProjectReportSnapshot() {
        long snapshotsBefore = projectReportSnapshotRepository.count();

        Project project = baseProject("Snapshot Pilot");

        ProjectReportSnapshot snapshot = new ProjectReportSnapshot();
        snapshot.setSnapshotDate(LocalDate.of(2026, 6, 30));
        snapshot.setProgressPercent(55);
        snapshot.setActualBudget(new BigDecimal("125000.00"));
        snapshot.setScheduleDeviationDays(7);
        snapshot.setStatus("AT_RISK");
        snapshot.setOpenRiskCount(3);
        project.addReportSnapshot(snapshot);

        projectRepository.saveAndFlush(project);

        assertEquals(snapshotsBefore + 1, projectReportSnapshotRepository.count());
        assertTrue(projectReportSnapshotRepository.findAll().stream()
                .allMatch(s -> s.getId() != null));
    }

    @Test
    void existingSeedProjectsRemainAfterV3Migration() {
        assertTrue(projectRepository.count() >= 20);
    }

    private static Project baseProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setCustomerName("Test Customer GmbH");
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 12, 31));
        project.setProgressPercent(10);
        return project;
    }
}
