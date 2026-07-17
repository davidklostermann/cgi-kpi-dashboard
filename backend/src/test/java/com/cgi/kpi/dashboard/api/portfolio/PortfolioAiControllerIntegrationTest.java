package com.cgi.kpi.dashboard.api.portfolio;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
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
class PortfolioAiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void trendAnalysisReturnsTextDisclaimerAndTopProjects() throws Exception {
        mockMvc.perform(get("/api/portfolio/ai/trend-analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").exists())
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.disclaimer").exists())
                .andExpect(jsonPath("$.topProjects", hasSize(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.topProjects", hasSize(org.hamcrest.Matchers.lessThanOrEqualTo(3))));
    }
}
