package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.infrastructure.persistence.seed.MockPilotScenario;
import com.cgi.kpi.dashboard.infrastructure.persistence.seed.MockSeedScenarioClassifier;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MockSeedIntegrationTest {

    private static final int EXPECTED_PROJECT_COUNT = 20;
    private static final int PROJECT_COUNT_TOLERANCE = 2;

    private static final Set<String> FORBIDDEN_CUSTOMER_PATTERNS = Set.of(
            "SAP", "Deutsche Bank", "BMW", "Siemens", "Allianz", "Telekom", "Commerzbank");

    private static final Set<String> ALLOWED_FICTITIOUS_CUSTOMERS = Set.of(
            "Acme Fabrications GmbH",
            "Beta Systems AG",
            "Delta Logistics SE",
            "Epsilon Retail OHG",
            "Gamma Industries KG",
            "Horizon Media GmbH",
            "Ion Trading AG",
            "Kappa Finance SE",
            "Lambda Telecom GmbH",
            "Mu Engineering AG",
            "Nova Services GmbH",
            "Omega Consulting AG",
            "Pi Insurance SE",
            "Quasar Manufacturing OHG",
            "Rho Energy GmbH",
            "Sigma Health AG",
            "Tau Mobility GmbH",
            "Upsilon Pharma SE",
            "Vega Retail AG",
            "Zenith Telecom OHG");

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectBudgetRepository projectBudgetRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Test
    void seedContainsApproximatelyTwentyProjects() {
        long count = projectRepository.count();
        assertTrue(
                count >= EXPECTED_PROJECT_COUNT - PROJECT_COUNT_TOLERANCE
                        && count <= EXPECTED_PROJECT_COUNT + PROJECT_COUNT_TOLERANCE,
                "Expected ~20 projects, found " + count);
        assertEquals(EXPECTED_PROJECT_COUNT, count);
    }

    @Test
    void seedCoversAllPilotScenarios() {
        List<Project> projects = projectRepository.findAll();
        Map<UUID, ProjectBudget> budgetsByProject = projectBudgetRepository.findAll().stream()
                .collect(Collectors.toMap(b -> b.getProject().getId(), b -> b));
        Map<UUID, List<Risk>> risksByProject = riskRepository.findAll().stream()
                .collect(Collectors.groupingBy(r -> r.getProject().getId()));

        EnumSet<MockPilotScenario> covered = EnumSet.noneOf(MockPilotScenario.class);
        for (Project project : projects) {
            ProjectBudget budget = budgetsByProject.get(project.getId());
            List<Risk> risks = risksByProject.getOrDefault(project.getId(), List.of());
            covered.addAll(MockSeedScenarioClassifier.classify(project, budget, risks));
        }

        for (MockPilotScenario scenario : MockPilotScenario.values()) {
            assertTrue(covered.contains(scenario), "Missing pilot scenario: " + scenario);
        }
    }

    @Test
    void seedUsesOnlyFictionalCustomerNames() {
        List<Project> projects = projectRepository.findAll();
        assertFalse(projects.isEmpty());

        for (Project project : projects) {
            assertTrue(
                    ALLOWED_FICTITIOUS_CUSTOMERS.contains(project.getCustomerName()),
                    "Unexpected customer name: " + project.getCustomerName());
            for (String forbidden : FORBIDDEN_CUSTOMER_PATTERNS) {
                assertFalse(
                        project.getCustomerName().contains(forbidden),
                        "Forbidden customer pattern in: " + project.getCustomerName());
            }
        }
    }
}
