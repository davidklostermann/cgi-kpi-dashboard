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
                .andExpect(jsonPath("$.criticalRiskCount").value(5))
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

    @Test
    void getPortfolioTimelineReturnsProjectRowsFromSeed() throws Exception {
        mockMvc.perform(get("/api/portfolio/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.projects.length()").value(19))
                .andExpect(jsonPath("$.projects[0].name").exists())
                .andExpect(jsonPath("$.projects[0].startDate").exists())
                .andExpect(jsonPath("$.projects[0].plannedEndDate").exists())
                .andExpect(jsonPath("$.projects[0].statusLabel").exists())
                .andExpect(jsonPath("$.projects[0].phases").isArray())
                .andExpect(jsonPath("$.projects[0].milestones").isArray())
                .andExpect(jsonPath("$.projects[0].scheduleDeviationDays").exists());
    }

    @Test
    void getPortfolioTimelineSupportsFilters() throws Exception {
        mockMvc.perform(get("/api/portfolio/timeline")
                        .param("customer", "Gamma")
                        .param("status", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.projects.length()").value(1))
                .andExpect(jsonPath("$.projects[0].status").value("CRITICAL"))
                .andExpect(jsonPath("$.projects[0].statusLabel").value("Kritisch"));
    }

    @Test
    void getPortfolioTimelineReturnsEmptyStateForNoMatches() throws Exception {
        mockMvc.perform(get("/api/portfolio/timeline")
                        .param("customer", "NichtVorhanden"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.projects").isEmpty());
    }

    @Test
    void getPortfolioProjectsReturnsManagementTableFromSeed() throws Exception {
        mockMvc.perform(get("/api/portfolio/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.projects.length()").value(19))
                .andExpect(jsonPath("$.projects[0].name").exists())
                .andExpect(jsonPath("$.projects[0].customerName").exists())
                .andExpect(jsonPath("$.projects[0].projectLead").exists())
                .andExpect(jsonPath("$.projects[0].statusLabel").exists())
                .andExpect(jsonPath("$.projects[0].currentPhaseName").exists())
                .andExpect(jsonPath("$.projects[0].progressPercent").exists())
                .andExpect(jsonPath("$.projects[0].plannedEndDate").exists())
                .andExpect(jsonPath("$.projects[0].budgetUtilizationPercent").exists())
                .andExpect(jsonPath("$.projects[0].budgetDeviationPercent").exists())
                .andExpect(jsonPath("$.projects[0].effortDeviationPercent").exists())
                .andExpect(jsonPath("$.projects[0].openRiskCount").exists())
                .andExpect(jsonPath("$.projects[0].criticalIssueCount").exists())
                .andExpect(jsonPath("$.projects[0].lastDataUpdate").exists());
    }

    @Test
    void getPortfolioProjectsSupportsFilters() throws Exception {
        mockMvc.perform(get("/api/portfolio/projects")
                        .param("status", "CRITICAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.projects.length()").value(4))
                .andExpect(jsonPath("$.projects[0].status").value("CRITICAL"));
    }

    @Test
    void getPortfolioTrendsReturnsSeriesFromSeed() throws Exception {
        mockMvc.perform(get("/api/portfolio/trends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empty").value(false))
                .andExpect(jsonPath("$.points.length()").value(12))
                .andExpect(jsonPath("$.points[0].period").value("2025-08"))
                .andExpect(jsonPath("$.points[11].period").value("2026-07"))
                .andExpect(jsonPath("$.points[0].averageProgressPercent").exists())
                .andExpect(jsonPath("$.points[0].totalActualBudget").exists())
                .andExpect(jsonPath("$.statusDistribution.onTrack").value(9))
                .andExpect(jsonPath("$.statusDistribution.atRisk").value(6))
                .andExpect(jsonPath("$.statusDistribution.critical").value(4));
    }
}
