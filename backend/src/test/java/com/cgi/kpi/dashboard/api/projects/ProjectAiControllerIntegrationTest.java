package com.cgi.kpi.dashboard.api.projects;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.cgi.kpi.dashboard.security.user.WithDashboardUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithDashboardUser(role = "ADMIN")
class ProjectAiControllerIntegrationTest {

    private static final UUID KNOWN_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analysisReturnsStructuredAiResponseWithEvidence() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/ai/analysis", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.priorities", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.priorities[0].evidenceFactIds", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.missingData", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.disclaimer").exists());
    }

    @Test
    void questionUsesOnlyApprovedProjectFacts() throws Exception {
        mockMvc.perform(post("/api/projects/{id}/ai/questions", KNOWN_PROJECT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Wie ist der aktuelle Fortschritt?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aiGenerated").value(true))
                .andExpect(jsonPath("$.answer").exists())
                .andExpect(jsonPath("$.evidenceFactIds", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void insufficientQuestionReturnsClearGapMessage() throws Exception {
        mockMvc.perform(post("/api/projects/{id}/ai/questions", KNOWN_PROJECT_ID)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Welche Websuche-Ergebnisse gibt es zu Marsmissionen?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insufficientEvidence").value(true))
                .andExpect(jsonPath("$.answer").value(
                        "Dazu liegen keine ausreichend konkreten freigegebenen Projektdaten vor."));
    }
}
