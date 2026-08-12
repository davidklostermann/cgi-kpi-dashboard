package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectCapacitySummary;
import com.cgi.kpi.dashboard.domain.model.ProjectRoleCapacity;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;

class ProjectIssuesCapacityAssemblerTest {

    private final ProjectIssuesCapacityAssembler assembler = new ProjectIssuesCapacityAssembler();

    @Test
    void assembleIssuesActionsKeepsOnlyOpenItemsSortedBySeverity() {
        Project project = project();
        Problem critical = problem("CRITICAL", "OPEN", "Kapazität");
        Problem closed = problem("HIGH", "CLOSED", "Alt");
        Risk high = risk("HIGH", "OPEN", "API");

        ProjectIssuesActionsDto dto = assembler.assembleIssuesActions(
                project, List.of(critical, closed), List.of(high));

        assertEquals(2, dto.items().size());
        assertEquals("CRITICAL", dto.items().get(0).severity());
        assertEquals("HIGH", dto.items().get(1).severity());
        assertEquals("Fakten aus Backend", dto.factsBadge());
        assertEquals("Laufende Maßnahme", dto.items().get(0).actionLabel());
        assertTrue(dto.items().get(0).metrics().size() >= 1);
        assertEquals("Beschreibung", dto.items().get(0).cause());
        assertTrue(dto.items().get(0).escalationNeeded());
        assertNotNull(dto.items().get(0).requiredDecision());
    }

    @Test
    void assembleIssuesActionsComputesOverdueLabelAgainstReferenceDate() {
        Project project = project();
        Problem overdue = problem("HIGH", "OPEN", "Verspätet");
        overdue.setTargetDate(LocalDate.of(2026, 6, 24));
        overdue.setCountermeasure("Maßnahme ohne Entscheidungssignal");

        ProjectIssuesActionsDto dto = assembler.assembleIssuesActions(project, List.of(overdue), List.of());

        assertEquals(1, dto.items().size());
        assertEquals(7, dto.items().get(0).overdueDays());
        assertEquals("Überfällig seit 7 Tagen", dto.items().get(0).overdueLabel());
        assertTrue(dto.items().get(0).escalationNeeded());
    }

    @Test
    void assembleCapacityMapsRolesAndSummaryWithoutPersonalData() {
        Project project = project();
        ProjectRoleCapacity role = new ProjectRoleCapacity();
        role.setId(UUID.fromString("a1000000-0000-4000-8000-000000000001"));
        role.setRoleName("Cloud Engineering");
        role.setRequiredFte(new BigDecimal("3.00"));
        role.setAvailableFte(new BigDecimal("1.00"));
        role.setCoveragePercent(33);
        role.setSortOrder(1);

        ProjectCapacitySummary summary = new ProjectCapacitySummary();
        summary.setMissingFte(new BigDecimal("2.00"));
        summary.setNextAvailabilityDate(LocalDate.of(2026, 8, 5));
        summary.setOverloadedRoles(1);
        summary.setExternalOptions(2);
        summary.setImpactHeadline("Kapazitätslücke mit Terminwirkung");
        summary.setImpactDetail("Unterdeckung korreliert mit Terminabweichung.");
        summary.setFactsAsOf(Instant.parse("2026-07-10T08:00:00Z"));

        ProjectCapacityDto dto = assembler.assembleCapacity(project, List.of(role), summary);

        assertEquals(1, dto.roles().size());
        assertEquals("Cloud Engineering", dto.roles().get(0).roleName());
        assertEquals(33, dto.roles().get(0).coveragePercent());
        assertNotNull(dto.summary());
        assertEquals(new BigDecimal("2.00"), dto.summary().missingFte());
        assertEquals(LocalDate.of(2026, 8, 5), dto.summary().nextAvailabilityDate());
        assertTrue(dto.factsBadge().contains("10.07.2026"));
    }

    private static Project project() {
        Project project = new Project();
        project.setId(UUID.fromString("a0000000-0000-4000-8000-000000000001"));
        project.setName("Nexus Analytics Pilot");
        project.setCustomerName("Acme");
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2025, 3, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 6, 30));
        project.setProgressPercent(62);
        project.setLastDataUpdate(Instant.parse("2026-07-01T08:00:00Z"));
        return project;
    }

    private static Problem problem(String severity, String status, String title) {
        Problem problem = new Problem();
        problem.setId(UUID.randomUUID());
        problem.setTitle(title);
        problem.setDescription("Beschreibung");
        problem.setSeverity(severity);
        problem.setStatus(status);
        problem.setResponsible("Projektleitung");
        problem.setTargetDate(LocalDate.of(2026, 8, 5));
        problem.setCountermeasure("Steering-Entscheidung einholen");
        problem.setCategory("RESSOURCEN");
        problem.setMetric1Label("Bedarf");
        problem.setMetric1Value("3,0 FTE");
        return problem;
    }

    private static Risk risk(String severity, String status, String title) {
        Risk risk = new Risk();
        risk.setId(UUID.randomUUID());
        risk.setTitle(title);
        risk.setDescription("Beschreibung");
        risk.setSeverity(severity);
        risk.setStatus(status);
        risk.setMitigationMeasure("Gegensteuerung");
        risk.setCategory("INTEGRATION");
        risk.setOwnerName("Schnittstellenverantwortung");
        risk.setDueDate(LocalDate.of(2026, 7, 25));
        risk.setMetric1Label("Wahrscheinlichkeit");
        risk.setMetric1Value("60 %");
        return risk;
    }
}
