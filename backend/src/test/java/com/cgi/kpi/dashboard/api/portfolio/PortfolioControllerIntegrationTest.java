package com.cgi.kpi.dashboard.api.portfolio;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PortfolioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getPortfolioKpisReturnsCalculatedSummaryFromSeed() throws Exception {
        mockMvc.perform(get("/api/portfolio/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjectCount").value(19))
                .andExpect(jsonPath("$.averageProgressPercent").value(greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.budgetDeviationPercent").exists())
                .andExpect(jsonPath("$.scheduleCompliancePercent").value(greaterThanOrEqualTo(0.0)))
                .andExpect(jsonPath("$.criticalRiskCount").value(4))
                .andExpect(jsonPath("$.statusDistribution.onTrack").value(9))
                .andExpect(jsonPath("$.statusDistribution.atRisk").value(6))
                .andExpect(jsonPath("$.statusDistribution.critical").value(4))
                .andExpect(jsonPath("$.statusDistribution.completed").value(0))
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getPortfolioKpisSupportsCombinedFilters() throws Exception {
        mockMvc.perform(get("/api/portfolio/kpis")
                        .param("customer", "Gamma")
                        .param("status", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjectCount").value(1))
                .andExpect(jsonPath("$.statusDistribution.critical").value(1))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    void getPortfolioKpisReturnsEmptyStateForNoMatches() throws Exception {
        mockMvc.perform(get("/api/portfolio/kpis")
                        .param("customer", "NichtVorhanden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjectCount").value(0))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    void getPortfolioKpisWithAllLifecycleIncludesCompletedProjects() throws Exception {
        mockMvc.perform(get("/api/portfolio/kpis").param("lifecycle", "all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProjectCount").value(19))
                .andExpect(jsonPath("$.statusDistribution.completed").value(1))
                .andExpect(jsonPath("$.statusDistribution.onTrack").value(9))
                .andExpect(jsonPath("$.statusDistribution.atRisk").value(6))
                .andExpect(jsonPath("$.statusDistribution.critical").value(4))
                .andExpect(jsonPath("$.empty").value(false));
    }

    @Test
    void getFilterOptionsReturnsDistinctValues() throws Exception {
        mockMvc.perform(get("/api/portfolio/filters/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers").isArray())
                .andExpect(jsonPath("$.projectLeads").isArray())
                .andExpect(jsonPath("$.phases").isArray())
                .andExpect(jsonPath("$.reportMonths").isArray())
                .andExpect(jsonPath("$.statuses[0]").value("ON_TRACK"));
    }
}
