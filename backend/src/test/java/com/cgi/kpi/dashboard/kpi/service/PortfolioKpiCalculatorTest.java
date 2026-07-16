package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;

class PortfolioKpiCalculatorTest {

    private static final UUID PROJECT_A = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID PROJECT_B = UUID.fromString("a0000000-0000-4000-8000-000000000002");
    private static final UUID PROJECT_C = UUID.fromString("a0000000-0000-4000-8000-000000000003");

    private final PortfolioKpiCalculator calculator = new PortfolioKpiCalculator();

    @Test
    void emptyPortfolioReturnsDefinedEmptyState() {
        PortfolioKpiSummaryDto summary = calculator.calculate(List.of(), List.of());

        assertTrue(summary.empty());
        assertEquals(0, summary.activeProjectCount());
    }

    @Test
    void completedProjectsAreIncludedWhenProvided() {
        PortfolioKpiSummaryDto summary = calculator.calculate(
                List.of(project(PROJECT_A, "COMPLETED", 100, 0, 100, 100)),
                List.of());

        assertFalse(summary.empty());
        assertEquals(0, summary.activeProjectCount());
        assertEquals(1, summary.statusDistribution().completed());
        assertEquals(100.0, summary.averageProgressPercent());
    }

    @Test
    void statusDistributionSumEqualsFilteredProjectCount() {
        PortfolioKpiSummaryDto summary = calculator.calculate(
                List.of(
                        project(PROJECT_A, "ON_TRACK", 50, 0, 100, 110),
                        project(PROJECT_B, "AT_RISK", 30, 5, 200, 180),
                        project(PROJECT_C, "CRITICAL", 100, 0, 50, 50),
                        project(UUID.fromString("a0000000-0000-4000-8000-000000000004"), "COMPLETED", 100, 0, 80, 80)),
                List.of());

        assertEquals(3, summary.activeProjectCount());
        assertEquals(1, summary.statusDistribution().completed());
        assertEquals(4, summary.statusDistribution().total());
    }

    @Test
    void calculatesAggregatedKpisForProvidedProjects() {
        PortfolioKpiSummaryDto summary = calculator.calculate(
                List.of(
                        project(PROJECT_A, "ON_TRACK", 50, 0, 100, 110),
                        project(PROJECT_B, "AT_RISK", 30, 5, 200, 180),
                        project(PROJECT_C, "CRITICAL", 100, 0, 50, 50)),
                List.of(
                        risk(PROJECT_A, "CRITICAL", "OPEN"),
                        risk(PROJECT_A, "CRITICAL", "CLOSED"),
                        risk(PROJECT_B, "HIGH", "OPEN")));

        assertFalse(summary.empty());
        assertEquals(3, summary.activeProjectCount());
        assertEquals(60.0, summary.averageProgressPercent());
        assertEquals(2, summary.criticalRiskCount());
        assertEquals(1, summary.statusDistribution().onTrack());
        assertEquals(1, summary.statusDistribution().atRisk());
        assertEquals(1, summary.statusDistribution().critical());
        assertEquals(0, summary.statusDistribution().completed());
        assertEquals(3, summary.statusDistribution().total());
    }

    private static PortfolioKpiProjectInput project(
            UUID id,
            String status,
            int progress,
            int scheduleDeviation,
            int planned,
            int actual) {
        return new PortfolioKpiProjectInput(
                id,
                status,
                "Customer",
                "Lead",
                "Umsetzung",
                YearMonth.of(2026, 7),
                progress,
                scheduleDeviation,
                BigDecimal.valueOf(planned),
                BigDecimal.valueOf(actual));
    }

    private static PortfolioKpiRiskInput risk(UUID projectId, String severity, String status) {
        return new PortfolioKpiRiskInput(projectId, severity, status);
    }
}
