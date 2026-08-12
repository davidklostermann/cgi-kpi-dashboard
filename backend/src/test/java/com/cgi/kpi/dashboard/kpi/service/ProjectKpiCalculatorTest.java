package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;

class ProjectKpiCalculatorTest {

    private final ProjectKpiCalculator calculator = new ProjectKpiCalculator();

    @Test
    void calculatesScheduleBudgetEffortAndIssueCounts() {
        UUID projectId = UUID.fromString("a0000000-0000-4000-8000-000000000001");
        Project project = new Project();
        project.setId(projectId);
        project.setName("Nexus Analytics Pilot");
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2025, 3, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 6, 30));
        project.setPredictedEndDate(LocalDate.of(2026, 6, 30));
        project.setProgressPercent(62);
        project.setScheduleDeviationDays(0);

        ProjectBudget budget = new ProjectBudget();
        budget.setPlannedBudget(new BigDecimal("500000.00"));
        budget.setActualBudget(new BigDecimal("475000.00"));
        budget.setPlannedEffortDays(new BigDecimal("120.00"));
        budget.setActualEffortDays(new BigDecimal("108.00"));

        ProjectPhase phase = new ProjectPhase();
        phase.setName("Rollout & Betrieb");
        phase.setPhaseType("ROLLOUT");
        phase.setStartDate(LocalDate.of(2025, 12, 1));
        phase.setEndDate(LocalDate.of(2026, 12, 28));
        phase.setSortOrder(3);

        Risk openCritical = new Risk();
        openCritical.setStatus("OPEN");
        openCritical.setSeverity("CRITICAL");
        Risk closed = new Risk();
        closed.setStatus("CLOSED");
        closed.setSeverity("HIGH");

        Problem openHigh = new Problem();
        openHigh.setStatus("OPEN");
        openHigh.setSeverity("HIGH");

        ProjectKpiDto kpi = calculator.calculate(
                project,
                budget,
                List.of(phase),
                List.of(openCritical, closed),
                List.of(openHigh));

        assertEquals(projectId, kpi.projectId());
        assertEquals("ON_TRACK", kpi.status());
        assertEquals("Auf Kurs", kpi.statusLabel());
        assertEquals(62, kpi.progressPercent());
        assertEquals("Rollout & Betrieb", kpi.currentPhaseName());
        assertEquals(0, kpi.schedule().deviationDays());
        assertEquals(LocalDate.of(2026, 6, 30), kpi.schedule().plannedEndDate());
        assertEquals(LocalDate.of(2026, 6, 30), kpi.schedule().forecastEndDate());
        assertEquals(95.0, kpi.budget().utilizationPercent());
        assertEquals(-5.0, kpi.budget().deviationPercent());
        assertEquals(new BigDecimal("25000.00"), kpi.budget().remaining());
        assertEquals(new BigDecimal("766129.03"), kpi.budget().forecastAtCompletion());
        assertEquals(-10.0, kpi.effort().deviationPercent());
        assertEquals(new BigDecimal("12.00"), kpi.effort().remainingDays());
        assertEquals(1, kpi.risks().openCount());
        assertEquals(1, kpi.risks().criticalOpenCount());
        assertEquals(1, kpi.problems().openCount());
        assertEquals(1, kpi.problems().criticalOpenCount());
    }

    @Test
    void timeElapsedPercentUsesReferenceDate() {
        Double elapsed = ProjectKpiCalculator.timeElapsedPercent(
                LocalDate.of(2025, 3, 1),
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 7, 1));
        assertEquals(100.2, elapsed);
    }

    @Test
    void forecastAtCompletionIsNullWhenProgressIsZero() {
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 1, 1));
        project.setProgressPercent(0);

        ProjectBudget budget = new ProjectBudget();
        budget.setPlannedBudget(new BigDecimal("100"));
        budget.setActualBudget(new BigDecimal("10"));
        budget.setPlannedEffortDays(new BigDecimal("10"));
        budget.setActualEffortDays(new BigDecimal("1"));

        ProjectKpiDto kpi = calculator.calculate(project, budget, List.of(), List.of(), List.of());
        assertNull(kpi.budget().forecastAtCompletion());
        assertNull(kpi.effort().forecastAtCompletionDays());
    }
}
