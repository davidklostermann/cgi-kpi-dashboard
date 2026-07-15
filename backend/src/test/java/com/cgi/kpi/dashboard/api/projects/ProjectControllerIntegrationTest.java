package com.cgi.kpi.dashboard.api.projects;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerIntegrationTest {

    private static final UUID KNOWN_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listProjectsReturnsSeedDataWithRequiredFields() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(20))))
                .andExpect(jsonPath("$[0].name").value("AI Governance Framework"))
                .andExpect(jsonPath("$[0].id").value("a0000000-0000-4000-8000-000000000005"))
                .andExpect(jsonPath("$[0].customerName").value("Gamma Industries KG"))
                .andExpect(jsonPath("$[0].status").value("CRITICAL"))
                .andExpect(jsonPath("$[0].progressPercent").value(78))
                .andExpect(jsonPath("$[0].scheduleDeviationDays").value(5))
                .andExpect(jsonPath("$[0].plannedEndDate").value("2026-05-31"))
                .andExpect(jsonPath("$[0].aiGenerated").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("Cloud Migration Wave 2"));
    }

    @Test
    void getProjectByIdReturnsMasterDataBasis() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Nexus Analytics Pilot"))
                .andExpect(jsonPath("$.customerName").value("Acme Fabrications GmbH"))
                .andExpect(jsonPath("$.status").value("ON_TRACK"))
                .andExpect(jsonPath("$.startDate").value("2025-03-01"))
                .andExpect(jsonPath("$.plannedEndDate").value("2026-06-30"))
                .andExpect(jsonPath("$.progressPercent").value(62))
                .andExpect(jsonPath("$.scheduleDeviationDays").value(0))
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectByUnknownIdReturnsStructuredNotFound() throws Exception {
        UUID unknownId = UUID.fromString("00000000-0000-4000-8000-000000000099");

        mockMvc.perform(get("/api/projects/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getProjectByMalformedUuidReturnsStructuredBadRequest() throws Exception {
        mockMvc.perform(get("/api/projects/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"));
    }
}
