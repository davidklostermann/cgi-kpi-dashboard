package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;

@SpringBootTest
@ActiveProfiles("test")
class PortfolioKpiServiceIntegrationTest {

    @Autowired
    private PortfolioKpiService portfolioKpiService;

    @Test
    void getPortfolioSummaryReturnsCalculatedKpisFromSeed() {
        PortfolioKpiSummaryDto summary = portfolioKpiService.getPortfolioSummary(PortfolioFilterCriteria.empty());

        assertNotNull(summary);
        assertFalse(summary.empty());
        assertEquals(19, summary.activeProjectCount());
        assertTrue(summary.averageProgressPercent() > 0);
        assertTrue(summary.scheduleCompliancePercent() > 0);
        assertEquals(5, summary.criticalRiskCount());
        assertEquals(9, summary.statusDistribution().onTrack());
        assertEquals(6, summary.statusDistribution().atRisk());
        assertEquals(4, summary.statusDistribution().critical());
        assertEquals(0, summary.statusDistribution().completed());
        assertEquals(19, summary.statusDistribution().total());
    }

    @Test
    void getPortfolioSummaryWithAllLifecycleIncludesCompletedInDistribution() {
        PortfolioKpiSummaryDto summary = portfolioKpiService.getPortfolioSummary(
                new PortfolioFilterCriteria(null, null, List.of(), null, PortfolioFilterCriteria.LifecycleFilter.ALL, null, null));

        assertEquals(19, summary.activeProjectCount());
        assertEquals(1, summary.statusDistribution().completed());
        assertEquals(20, summary.statusDistribution().total());
    }

    @Test
    void getPortfolioTimelineReturnsFilteredProjectsFromSeed() {
        PortfolioTimelineDto timeline = portfolioKpiService.getPortfolioTimeline(PortfolioFilterCriteria.empty());

        assertFalse(timeline.empty());
        assertEquals(19, timeline.projects().size());
        assertTrue(timeline.projects().stream().allMatch(project -> project.statusLabel() != null));
        assertTrue(timeline.projects().stream().anyMatch(project -> !project.phases().isEmpty()));
        assertTrue(timeline.projects().stream().anyMatch(project -> !project.milestones().isEmpty()));
    }

    @Test
    void getPortfolioTableReturnsManagementRowsFromSeed() {
        PortfolioTableDto table = portfolioKpiService.getPortfolioTable(PortfolioFilterCriteria.empty());

        assertFalse(table.empty());
        assertEquals(19, table.projects().size());
        assertTrue(table.projects().stream().allMatch(row -> row.statusLabel() != null));
        assertTrue(table.projects().stream().anyMatch(row -> row.criticalIssueCount() > 0));
    }

    @Test
    void portfolioEndpointsReturnConsistentFilteredProjectCounts() {
        PortfolioFilterCriteria criteria = PortfolioFilterCriteria.empty();

        PortfolioKpiSummaryDto summary = portfolioKpiService.getPortfolioSummary(criteria);
        PortfolioTimelineDto timeline = portfolioKpiService.getPortfolioTimeline(criteria);
        PortfolioTableDto table = portfolioKpiService.getPortfolioTable(criteria);
        PortfolioTrendDto trends = portfolioKpiService.getPortfolioTrends(criteria);

        assertEquals(19, summary.activeProjectCount());
        assertEquals(19, timeline.projects().size());
        assertEquals(19, table.projects().size());
        assertEquals(19, trends.statusDistribution().total());
    }

    @Test
    void getPortfolioTrendsReturnsMonthlySeriesFromSeed() {
        PortfolioTrendDto trends = portfolioKpiService.getPortfolioTrends(PortfolioFilterCriteria.empty());

        assertFalse(trends.empty());
        assertEquals(12, trends.points().size());
        assertEquals("2025-08", trends.points().getFirst().period());
        assertEquals("2026-07", trends.points().getLast().period());
        assertTrue(trends.points().getFirst().averageProgressPercent() > 0);
        assertTrue(trends.points().getFirst().totalActualBudget() > 0);
        assertEquals(19, trends.statusDistribution().total());
    }
}
