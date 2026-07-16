package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;

class PortfolioTableAssemblerTest {

    private static final UUID PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    private final PortfolioTableAssembler assembler = new PortfolioTableAssembler();

    @Test
    void assemblesManagementTableRowWithMetrics() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName("Nexus Analytics Pilot");
        project.setCustomerName("Acme GmbH");
        project.setProjectLead("Dr. Anna Keller");
        project.setStatus("AT_RISK");
        project.setProgressPercent(62);
        project.setStartDate(LocalDate.of(2025, 3, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 6, 30));
        project.setPredictedEndDate(LocalDate.of(2026, 7, 15));
        project.setScheduleDeviationDays(12);
        project.setLastDataUpdate(Instant.parse("2026-07-10T08:00:00Z"));

        ProjectBudget budget = new ProjectBudget();
        budget.setPlannedBudget(BigDecimal.valueOf(400_000));
        budget.setActualBudget(BigDecimal.valueOf(440_000));
        budget.setPlannedEffortDays(BigDecimal.valueOf(200));
        budget.setActualEffortDays(BigDecimal.valueOf(220));

        Risk openRisk = new Risk();
        openRisk.setStatus("OPEN");
        openRisk.setSeverity("HIGH");

        Risk closedRisk = new Risk();
        closedRisk.setStatus("CLOSED");
        closedRisk.setSeverity("CRITICAL");

        Problem criticalProblem = new Problem();
        criticalProblem.setStatus("OPEN");
        criticalProblem.setSeverity("CRITICAL");

        PortfolioTableDto table = assembler.assemble(
                List.of(project),
                Map.of(PROJECT_ID, budget),
                Map.of(PROJECT_ID, "Umsetzung"),
                Map.of(PROJECT_ID, List.of(openRisk, closedRisk)),
                Map.of(PROJECT_ID, List.of(criticalProblem)));

        assertFalse(table.empty());
        assertEquals(1, table.projects().size());
        var row = table.projects().getFirst();
        assertEquals("Nexus Analytics Pilot", row.name());
        assertEquals("Beobachten", row.statusLabel());
        assertEquals("Umsetzung", row.currentPhaseName());
        assertEquals(110.0, row.budgetUtilizationPercent());
        assertEquals(10.0, row.budgetDeviationPercent());
        assertEquals(10.0, row.effortDeviationPercent());
        assertEquals(1, row.openRiskCount());
        assertEquals(2, row.criticalIssueCount());
    }
}
