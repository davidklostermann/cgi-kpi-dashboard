package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.infrastructure.persistence.seed.MockManagementInsightType;
import com.cgi.kpi.dashboard.infrastructure.persistence.seed.MockSeedInsightClassifier;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MockSeedExtendedIntegrationTest {

    private static final int EXPECTED_PROJECT_COUNT = 20;
    private static final int SNAPSHOTS_PER_PROJECT = 2;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ProjectReportSnapshotRepository projectReportSnapshotRepository;

    @Autowired
    private ProjectBudgetRepository projectBudgetRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Test
    void seedV4PopulatesProjectLeadsAndMasterData() {
        List<Project> projects = projectRepository.findAll();
        assertEquals(EXPECTED_PROJECT_COUNT, projects.size());

        for (Project project : projects) {
            assertNotNull(project.getProjectLead(), "Missing project lead for " + project.getName());
            assertFalse(project.getProjectLead().isBlank());
            assertNotNull(project.getLastDataUpdate(), "Missing last_data_update for " + project.getName());
            assertNotNull(project.getPredictedEndDate(), "Missing predicted_end_date for " + project.getName());
        }
    }

    @Test
    void seedV4ContainsProblemsSeparateFromRisks() {
        List<Problem> problems = problemRepository.findAll();
        assertTrue(problems.size() >= 8, "Expected seeded problems");

        for (Problem problem : problems) {
            assertNotNull(problem.getTitle());
            assertNotNull(problem.getDescription());
            assertNotNull(problem.getSeverity());
            assertNotNull(problem.getStatus());
        }

        assertTrue(riskRepository.count() > problems.size(), "Risks and problems must remain separate datasets");
    }

    @Test
    void seedV4ProvidesTwoReportSnapshotsPerProject() {
        List<ProjectReportSnapshot> snapshots = projectReportSnapshotRepository.findAll();
        assertEquals(EXPECTED_PROJECT_COUNT * SNAPSHOTS_PER_PROJECT, snapshots.size());

        Map<UUID, Long> byProject = snapshots.stream()
                .collect(Collectors.groupingBy(s -> s.getProject().getId(), Collectors.counting()));

        assertEquals(EXPECTED_PROJECT_COUNT, byProject.size());
        byProject.values().forEach(count -> assertEquals(SNAPSHOTS_PER_PROJECT, count.intValue()));
    }

    @Test
    void seedV4CoversAllManagementInsightTypes() {
        List<Project> projects = projectRepository.findAll();
        Map<UUID, ProjectBudget> budgets = projectBudgetRepository.findAll().stream()
                .collect(Collectors.toMap(b -> b.getProject().getId(), b -> b));
        Map<UUID, List<Risk>> risks = riskRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getProject().getId()));
        Map<UUID, List<Milestone>> milestones = milestoneRepository.findAll().stream()
                .collect(Collectors.groupingBy(m -> m.getProject().getId()));
        Map<UUID, List<ProjectReportSnapshot>> snapshots = projectReportSnapshotRepository.findAll().stream()
                .collect(Collectors.groupingBy(s -> s.getProject().getId()));

        EnumSet<MockManagementInsightType> covered = EnumSet.noneOf(MockManagementInsightType.class);
        for (Project project : projects) {
            covered.addAll(MockSeedInsightClassifier.classify(
                    project,
                    budgets.get(project.getId()),
                    risks.getOrDefault(project.getId(), List.of()),
                    milestones.getOrDefault(project.getId(), List.of()),
                    snapshots.getOrDefault(project.getId(), List.of())));
        }

        for (MockManagementInsightType type : MockManagementInsightType.values()) {
            assertTrue(covered.contains(type), "Missing management insight seed scenario: " + type);
        }
    }
}
