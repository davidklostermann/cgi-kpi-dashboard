package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.IsoBenefitsStatus;
import com.cgi.kpi.dashboard.domain.model.IsoImpactLevel;
import com.cgi.kpi.dashboard.domain.model.IsoScopeTrend;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectIsoManagement;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIsoManagementDto;

class ProjectIsoManagementAssemblerTest {

    private final ProjectIsoManagementAssembler assembler = new ProjectIsoManagementAssembler();

    @Test
    void assembleMapsAllCardsWithLabels() {
        Project project = project("a0000000-0000-4000-8000-000000000001");
        ProjectIsoManagement iso = fullIso(project);

        ProjectIsoManagementDto dto = assembler.assemble(project, iso);

        assertTrue(dto.dataAvailable());
        assertNull(dto.emptyReason());
        assertEquals("Reduktion manueller Reporting-Aufwände um 35 %", dto.benefits().expectedBenefit());
        assertEquals("GREEN", dto.benefits().status());
        assertEquals("Auf Kurs", dto.benefits().statusLabel());
        assertEquals(List.of("Abweichung A", "Abweichung B"), dto.scope().deviations());
        assertEquals("DETERIORATING", dto.scope().trend());
        assertEquals("Verschlechternd", dto.scope().trendLabel());
        assertEquals(9, dto.changeRequests().total());
        assertEquals("HIGH", dto.changeRequests().impactSchedule());
        assertEquals("Hoch", dto.changeRequests().impactScheduleLabel());
        assertEquals(14, dto.quality().openDefects());
        assertEquals(LocalDate.parse("2026-07-10"), dto.stakeholders().lastSteeringDate());
    }

    @Test
    void assembleLimitsDeviationsToTwo() {
        Project project = project("a0000000-0000-4000-8000-000000000002");
        ProjectIsoManagement iso = fullIso(project);
        iso.setDeviation1("Eins");
        iso.setDeviation2("Zwei");

        ProjectIsoManagementDto dto = assembler.assemble(project, iso);

        assertEquals(2, dto.scope().deviations().size());
        assertEquals(List.of("Eins", "Zwei"), dto.scope().deviations());
    }

    @Test
    void assembleEmptyReturnsUnavailablePayload() {
        Project project = project("a0000000-0000-4000-8000-000000000006");

        ProjectIsoManagementDto dto = assembler.assembleEmpty(project);

        assertFalse(dto.dataAvailable());
        assertEquals(
                "Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.",
                dto.emptyReason());
        assertNull(dto.benefits());
        assertNull(dto.scope());
        assertNull(dto.changeRequests());
        assertNull(dto.quality());
        assertNull(dto.stakeholders());
    }

    @Test
    void labelHelpersCoverAllEnumValues() {
        assertEquals("Auf Kurs", ProjectIsoManagementAssembler.benefitsStatusLabel(IsoBenefitsStatus.GREEN));
        assertEquals("Beobachten", ProjectIsoManagementAssembler.benefitsStatusLabel(IsoBenefitsStatus.AMBER));
        assertEquals("Kritisch", ProjectIsoManagementAssembler.benefitsStatusLabel(IsoBenefitsStatus.RED));
        assertEquals("Verbessernd", ProjectIsoManagementAssembler.scopeTrendLabel(IsoScopeTrend.IMPROVING));
        assertEquals("Stabil", ProjectIsoManagementAssembler.scopeTrendLabel(IsoScopeTrend.STABLE));
        assertEquals("Verschlechternd", ProjectIsoManagementAssembler.scopeTrendLabel(IsoScopeTrend.DETERIORATING));
        assertEquals("Keine", ProjectIsoManagementAssembler.impactLabel(IsoImpactLevel.NONE));
        assertEquals("Gering", ProjectIsoManagementAssembler.impactLabel(IsoImpactLevel.LOW));
        assertEquals("Mittel", ProjectIsoManagementAssembler.impactLabel(IsoImpactLevel.MEDIUM));
        assertEquals("Hoch", ProjectIsoManagementAssembler.impactLabel(IsoImpactLevel.HIGH));
    }

    private static Project project(String id) {
        Project project = new Project();
        project.setId(java.util.UUID.fromString(id));
        project.setLastDataUpdate(Instant.parse("2026-07-15T08:00:00Z"));
        return project;
    }

    private static ProjectIsoManagement fullIso(Project project) {
        ProjectIsoManagement iso = new ProjectIsoManagement();
        iso.setProject(project);
        iso.setExpectedBenefit("Reduktion manueller Reporting-Aufwände um 35 %");
        iso.setBenefitUnit("Std./Monat");
        iso.setRealizedPercent(68);
        iso.setBenefitsStatus(IsoBenefitsStatus.GREEN);
        iso.setScopeStatus("Im vereinbarten Scope");
        iso.setDeviation1("Abweichung A");
        iso.setDeviation2("Abweichung B");
        iso.setScopeTrend(IsoScopeTrend.DETERIORATING);
        iso.setCrTotal(9);
        iso.setCrOpen(3);
        iso.setCrInReview(2);
        iso.setCrApproved(4);
        iso.setImpactSchedule(IsoImpactLevel.HIGH);
        iso.setImpactCost(IsoImpactLevel.MEDIUM);
        iso.setImpactScope(IsoImpactLevel.LOW);
        iso.setQualityStatus("Qualität beobachten");
        iso.setOpenDefects(14);
        iso.setCriticalDefects(2);
        iso.setTestAcceptanceStatus("Regressionstest 61 %");
        iso.setQualityProgressPercent(61);
        iso.setSponsorCustomer("Acme / Sponsor");
        iso.setStakeholderStatus("Informiert");
        iso.setEscalationStatus("Steering");
        iso.setLastSteeringDate(LocalDate.parse("2026-07-10"));
        iso.setFactsAsOf(Instant.parse("2026-07-15T08:00:00Z"));
        return iso;
    }
}
