package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;

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
        assertEquals(4, summary.criticalRiskCount());
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
}
