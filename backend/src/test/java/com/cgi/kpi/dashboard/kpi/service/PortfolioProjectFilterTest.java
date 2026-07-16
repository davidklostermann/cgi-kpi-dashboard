package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;

class PortfolioProjectFilterTest {

    private static final UUID PROJECT_A = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID PROJECT_B = UUID.fromString("a0000000-0000-4000-8000-000000000002");

    private final PortfolioProjectFilter filter = new PortfolioProjectFilter();

    @Test
    void combinesCustomerAndStatusFilters() {
        List<PortfolioKpiProjectInput> projects = List.of(
                project(PROJECT_A, "ON_TRACK", "Acme GmbH", "Anna Keller", "Umsetzung", "2026-07"),
                project(PROJECT_B, "CRITICAL", "Beta AG", "Markus Brenner", "Analyse & Konzeption", "2026-07"));

        PortfolioFilterCriteria criteria = new PortfolioFilterCriteria(
                "acme",
                null,
                List.of("ON_TRACK"),
                null,
                PortfolioFilterCriteria.LifecycleFilter.ALL,
                null,
                null);

        List<PortfolioKpiProjectInput> filtered = filter.applyProjects(projects, criteria, filter.projectIdsMatchingRiskSeverity(List.of(), null));

        assertEquals(1, filtered.size());
        assertEquals(PROJECT_A, filtered.getFirst().projectId());
    }

    @Test
    void riskSeverityFilterRequiresOpenRiskOnProject() {
        List<PortfolioKpiProjectInput> projects = List.of(
                project(PROJECT_A, "ON_TRACK", "Acme GmbH", "Anna Keller", "Umsetzung", "2026-07"),
                project(PROJECT_B, "CRITICAL", "Beta AG", "Markus Brenner", "Umsetzung", "2026-07"));
        List<PortfolioKpiRiskInput> risks = List.of(
                new PortfolioKpiRiskInput(PROJECT_B, "HIGH", "OPEN"));

        PortfolioFilterCriteria criteria = new PortfolioFilterCriteria(
                null,
                null,
                List.of(),
                null,
                PortfolioFilterCriteria.LifecycleFilter.ALL,
                null,
                "HIGH");

        List<PortfolioKpiProjectInput> filtered = filter.applyProjects(
                projects,
                criteria,
                filter.projectIdsMatchingRiskSeverity(risks, "HIGH"));

        assertEquals(1, filtered.size());
        assertEquals(PROJECT_B, filtered.getFirst().projectId());
    }

    private static PortfolioKpiProjectInput project(
            UUID id,
            String status,
            String customer,
            String lead,
            String phase,
            String reportMonth) {
        return new PortfolioKpiProjectInput(
                id,
                status,
                customer,
                lead,
                phase,
                YearMonth.parse(reportMonth),
                50,
                0,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100));
    }
}
