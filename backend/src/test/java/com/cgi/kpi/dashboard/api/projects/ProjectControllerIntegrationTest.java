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
import com.cgi.kpi.dashboard.security.user.WithDashboardUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithDashboardUser(role = "ADMIN")
class ProjectControllerIntegrationTest {

    private static final UUID KNOWN_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID EPIC_16_CAPACITY_PROJECT_ID =
            UUID.fromString("a0000000-0000-4000-8000-000000000002");
    private static final UUID EMPTY_ISO_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final UUID AGILE_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final UUID HYBRID_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");

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
                .andExpect(jsonPath("$[0].deliveryMethod").value("AGILE"))
                .andExpect(jsonPath("$[0].aiGenerated").doesNotExist())
                .andExpect(jsonPath("$[1].name").value("Cloud Migration Wave 2"))
                .andExpect(jsonPath("$[1].deliveryMethod").value("HYBRID"));
    }

    @Test
    void getProjectByIdReturnsMasterDataBasis() throws Exception {
        mockMvc.perform(get("/api/projects/{id}", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Nexus Analytics Pilot"))
                .andExpect(jsonPath("$.customerName").value("Acme Fabrications GmbH"))
                .andExpect(jsonPath("$.status").value("ON_TRACK"))
                .andExpect(jsonPath("$.deliveryMethod").value("WATERFALL"))
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
    void getProjectKpisReturnsCalculatedManagementMetrics() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/kpis", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.status").value("ON_TRACK"))
                .andExpect(jsonPath("$.statusLabel").value("Auf Kurs"))
                .andExpect(jsonPath("$.progressPercent").value(62))
                .andExpect(jsonPath("$.currentPhaseName").value("Rollout & Betrieb"))
                .andExpect(jsonPath("$.schedule.plannedEndDate").value("2026-06-30"))
                .andExpect(jsonPath("$.schedule.forecastEndDate").value("2026-06-30"))
                .andExpect(jsonPath("$.schedule.deviationDays").value(0))
                .andExpect(jsonPath("$.schedule.timeElapsedPercent").exists())
                .andExpect(jsonPath("$.budget.planned").value(500000.0))
                .andExpect(jsonPath("$.budget.actual").value(475000.0))
                .andExpect(jsonPath("$.budget.utilizationPercent").value(95.0))
                .andExpect(jsonPath("$.budget.deviationPercent").value(-5.0))
                .andExpect(jsonPath("$.budget.remaining").value(25000.0))
                .andExpect(jsonPath("$.budget.forecastAtCompletion").exists())
                .andExpect(jsonPath("$.effort.plannedDays").value(120.0))
                .andExpect(jsonPath("$.effort.actualDays").value(108.0))
                .andExpect(jsonPath("$.effort.deviationPercent").value(-10.0))
                .andExpect(jsonPath("$.risks.openCount").exists())
                .andExpect(jsonPath("$.problems.openCount").exists())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectKpisByUnknownIdReturnsStructuredNotFound() throws Exception {
        UUID unknownId = UUID.fromString("00000000-0000-4000-8000-000000000099");

        mockMvc.perform(get("/api/projects/{id}/kpis", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getProjectMasterDataReturnsHeaderFields() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/master-data", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.name").value("Nexus Analytics Pilot"))
                .andExpect(jsonPath("$.customer").value("Acme Fabrications GmbH"))
                .andExpect(jsonPath("$.projectLead").value("Dr. Anna Keller"))
                .andExpect(jsonPath("$.startDate").value("2025-03-01"))
                .andExpect(jsonPath("$.plannedEndDate").value("2026-06-30"))
                .andExpect(jsonPath("$.forecastEndDate").value("2026-06-30"))
                .andExpect(jsonPath("$.currentPhaseName").value("Rollout & Betrieb"))
                .andExpect(jsonPath("$.status").value("ON_TRACK"))
                .andExpect(jsonPath("$.statusLabel").value("Auf Kurs"))
                .andExpect(jsonPath("$.lastDataUpdate").exists())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectMasterDataByUnknownIdReturnsStructuredNotFound() throws Exception {
        UUID unknownId = UUID.fromString("00000000-0000-4000-8000-000000000099");

        mockMvc.perform(get("/api/projects/{id}/master-data", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getProjectPhasesReturnsStructuredTimelineData() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/phases", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.projectName").value("Nexus Analytics Pilot"))
                .andExpect(jsonPath("$.phases", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.phases[0].name").exists())
                .andExpect(jsonPath("$.phases[0].plannedStartDate").exists())
                .andExpect(jsonPath("$.phases[0].plannedEndDate").exists())
                .andExpect(jsonPath("$.milestones", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.accessibilitySummary").exists())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectInsightsReturnsDeterministicList() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/insights", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.insights").isArray())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectTrendsReturnsSnapshotComparison() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/trends", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.comparisonAvailable").value(true))
                .andExpect(jsonPath("$.previousSnapshotDate").exists())
                .andExpect(jsonPath("$.currentSnapshotDate").exists())
                .andExpect(jsonPath("$.progressDeltaPercent").exists())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectIssuesActionsReturnsOpenProblemsAndRisks() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/issues-actions", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.factsBadge").value("Fakten aus Backend"))
                .andExpect(jsonPath("$.items", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.items[0].severityLabel").exists())
                .andExpect(jsonPath("$.items[0].metrics").isArray())
                .andExpect(jsonPath("$.items[0].actionText").exists())
                .andExpect(jsonPath("$.items[0].cause").exists())
                .andExpect(jsonPath("$.items[0].priority").exists())
                .andExpect(jsonPath("$.items[0].escalationNeeded").exists())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectCapacityReturnsRoleCoverageAndSummary() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/capacity", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.roles", hasSize(4)))
                .andExpect(jsonPath("$.roles[0].roleName").value("Cloud Engineering"))
                .andExpect(jsonPath("$.roles[0].coveragePercent").value(33))
                .andExpect(jsonPath("$.summary.missingFte").value(2.0))
                .andExpect(jsonPath("$.summary.nextAvailabilityDate").value("2026-08-05"))
                .andExpect(jsonPath("$.summary.overloadedRoles").value(1))
                .andExpect(jsonPath("$.summary.externalOptions").value(2))
                .andExpect(jsonPath("$.summary.impactHeadline").value("Kapazitätslücke mit Terminwirkung"))
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectCapacityReturnsEpic16SeedWithoutPersonalDetails() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/capacity", EPIC_16_CAPACITY_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(EPIC_16_CAPACITY_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.roles", hasSize(4)))
                .andExpect(jsonPath("$.roles[0].roleName").value("Cloud Platform Engineering"))
                .andExpect(jsonPath("$.roles[0].requiredFte").value(4.0))
                .andExpect(jsonPath("$.roles[0].availableFte").value(2.6))
                .andExpect(jsonPath("$.roles[0].coveragePercent").value(65))
                .andExpect(jsonPath("$.summary.missingFte").value(3.1))
                .andExpect(jsonPath("$.summary.impactHeadline").value("Rollenabdeckung fuer Cutover unter Ziel"))
                .andExpect(jsonPath("$.roles[0].personName").doesNotExist())
                .andExpect(jsonPath("$.roles[0].salary").doesNotExist())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectCapacityReturnsRemainingSeedForPreviouslyEmptyProject() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/capacity", AGILE_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(AGILE_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.roles", hasSize(4)))
                .andExpect(jsonPath("$.roles[0].roleName").value("Portal Frontend Engineering"))
                .andExpect(jsonPath("$.roles[0].coveragePercent").value(90))
                .andExpect(jsonPath("$.summary.missingFte").value(1.4))
                .andExpect(jsonPath("$.summary.impactHeadline").value("QA und Design leicht unterdeckt"))
                .andExpect(jsonPath("$.roles[0].personName").doesNotExist())
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectIsoManagementReturnsAllCards() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/iso-management", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(KNOWN_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.dataAvailable").value(true))
                .andExpect(jsonPath("$.benefits.expectedBenefit").exists())
                .andExpect(jsonPath("$.benefits.status").value("GREEN"))
                .andExpect(jsonPath("$.benefits.statusLabel").value("Auf Kurs"))
                .andExpect(jsonPath("$.scope.scopeStatus").exists())
                .andExpect(jsonPath("$.scope.trendLabel").value("Stabil"))
                .andExpect(jsonPath("$.changeRequests.total").value(4))
                .andExpect(jsonPath("$.changeRequests.impactScheduleLabel").value("Gering"))
                .andExpect(jsonPath("$.quality.openDefects").value(6))
                .andExpect(jsonPath("$.stakeholders.sponsorCustomer").exists())
                .andExpect(jsonPath("$.stakeholders.lastSteeringDate").value("2026-07-10"))
                .andExpect(jsonPath("$.aiGenerated").doesNotExist());
    }

    @Test
    void getProjectIsoManagementWithoutSeedReturnsEmptyPayload() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/iso-management", EMPTY_ISO_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(EMPTY_ISO_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.dataAvailable").value(false))
                .andExpect(jsonPath("$.emptyReason").value(
                        "Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt."))
                .andExpect(jsonPath("$.benefits").doesNotExist())
                .andExpect(jsonPath("$.scope").doesNotExist())
                .andExpect(jsonPath("$.changeRequests").doesNotExist())
                .andExpect(jsonPath("$.quality").doesNotExist())
                .andExpect(jsonPath("$.stakeholders").doesNotExist());
    }

    @Test
    void getProjectIsoManagementByUnknownIdReturnsStructuredNotFound() throws Exception {
        UUID unknownId = UUID.fromString("00000000-0000-4000-8000-000000000099");

        mockMvc.perform(get("/api/projects/{id}/iso-management", unknownId))
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

    @Test
    void getAgileDeliveryForAgileProjectReturnsSprintsAndKpis() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/agile-delivery", AGILE_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(AGILE_PROJECT_ID.toString()))
                .andExpect(jsonPath("$.deliveryMethod").value("AGILE"))
                .andExpect(jsonPath("$.dataAvailable").value(true))
                .andExpect(jsonPath("$.dataSource").value("INTERNAL_MOCK"))
                .andExpect(jsonPath("$.sprints", hasSize(5)))
                .andExpect(jsonPath("$.sprints[2].current").value(true))
                .andExpect(jsonPath("$.sprints[3].future").value(true))
                .andExpect(jsonPath("$.chart.sprintLabels", hasSize(5)))
                .andExpect(jsonPath("$.chart.plannedStoryPoints", hasSize(5)))
                .andExpect(jsonPath("$.chart.completedStoryPoints", hasSize(5)))
                .andExpect(jsonPath("$.chart.velocityTrend", hasSize(5)))
                .andExpect(jsonPath("$.chart.futureFlags", hasSize(5)))
                .andExpect(jsonPath("$.kpis.openBlockerCount").value(2))
                .andExpect(jsonPath("$.kpis.averageVelocity").exists())
                .andExpect(jsonPath("$.kpis.sprintHealth").exists())
                .andExpect(jsonPath("$.kpis.sprintHealthLabel").exists())
                .andExpect(jsonPath("$.kpis.totalStoryPoints").exists())
                .andExpect(jsonPath("$.kpis.carryOverNextSprint").exists())
                .andExpect(jsonPath("$.forecast").doesNotExist())
                .andExpect(jsonPath("$.risks").doesNotExist())
                .andExpect(jsonPath("$.riskOverview").doesNotExist())
                .andExpect(jsonPath("$.predictedEndDate").doesNotExist());
    }

    @Test
    void getAgileDeliveryForHybridProjectReturnsData() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/agile-delivery", HYBRID_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryMethod").value("HYBRID"))
                .andExpect(jsonPath("$.dataAvailable").value(true))
                .andExpect(jsonPath("$.sprints", hasSize(3)));
    }

    @Test
    void getAgileDeliveryForWaterfallReturnsUnavailablePayload() throws Exception {
        mockMvc.perform(get("/api/projects/{id}/agile-delivery", KNOWN_PROJECT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryMethod").value("WATERFALL"))
                .andExpect(jsonPath("$.dataAvailable").value(false))
                .andExpect(jsonPath("$.emptyReason").exists())
                .andExpect(jsonPath("$.sprints", hasSize(0)));
    }

    @Test
    void getAgileDeliveryByUnknownIdReturnsStructuredNotFound() throws Exception {
        UUID unknownId = UUID.fromString("00000000-0000-4000-8000-000000000099");

        mockMvc.perform(get("/api/projects/{id}/agile-delivery", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
