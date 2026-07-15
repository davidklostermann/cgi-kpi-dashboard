package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;

@SpringBootTest
@ActiveProfiles("test")
class PortfolioKpiServiceIntegrationTest {

    @Autowired
    private PortfolioKpiService portfolioKpiService;

    @Test
    void getPortfolioSummaryReturnsStubReaderDto() {
        PortfolioKpiSummaryDto summary = portfolioKpiService.getPortfolioSummary();

        assertNotNull(summary);
        assertEquals(0, summary.activeProjectCount());
        assertEquals(0.0, summary.averageProgressPercent());
        assertEquals(0.0, summary.budgetDeviationPercent());
        assertEquals(0.0, summary.scheduleCompliancePercent());
        assertEquals(0, summary.criticalRiskCount());
    }
}
