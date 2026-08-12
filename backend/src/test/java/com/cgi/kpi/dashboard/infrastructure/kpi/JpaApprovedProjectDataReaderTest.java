package com.cgi.kpi.dashboard.infrastructure.kpi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.ApprovedProjectFactDto;
import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto.MissingDataItemDto;
import com.cgi.kpi.dashboard.kpi.reader.ApprovedProjectDataReader;
import com.cgi.kpi.dashboard.security.user.WithDashboardUser;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@WithDashboardUser(role = "ADMIN")
class JpaApprovedProjectDataReaderTest {

    private static final UUID PROJECT_WITH_ISO =
            UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID PROJECT_WITHOUT_ISO =
            UUID.fromString("a0000000-0000-4000-8000-000000000006");

    @Autowired
    private ApprovedProjectDataReader approvedProjectDataReader;

    @Test
    void readApprovedContextIncludesIsoFactsForSeededProject() {
        ApprovedProjectContextDto context = approvedProjectDataReader
                .readApprovedContext(PROJECT_WITH_ISO)
                .orElseThrow();

        List<ApprovedProjectFactDto> isoFacts = context.facts().stream()
                .filter(fact -> fact.factId().startsWith("iso."))
                .toList();

        assertTrue(isoFacts.size() >= 8, "expected at least 8 iso.* facts");
        assertTrue(isoFacts.stream().allMatch(fact -> "fact-iso-management".equals(fact.detailAnchor())));
        assertTrue(isoFacts.stream().anyMatch(fact -> "iso.benefits.realizedPercent".equals(fact.factId())));
        assertTrue(isoFacts.stream().anyMatch(fact -> "iso.scope.trend".equals(fact.factId())));
        assertTrue(isoFacts.stream().anyMatch(fact -> "iso.changes.openCount".equals(fact.factId())));
        assertTrue(isoFacts.stream().anyMatch(fact -> "iso.quality.criticalDefects".equals(fact.factId())));
        assertTrue(isoFacts.stream().anyMatch(fact -> "iso.stakeholders.escalationStatus".equals(fact.factId())));

        assertFalse(context.missingData().stream()
                .anyMatch(item -> "QUALITY".equals(item.area())));
    }

    @Test
    void readApprovedContextReportsIsoManagementMissingForProjectWithoutSeed() {
        ApprovedProjectContextDto context = approvedProjectDataReader
                .readApprovedContext(PROJECT_WITHOUT_ISO)
                .orElseThrow();

        List<ApprovedProjectFactDto> isoFacts = context.facts().stream()
                .filter(fact -> fact.factId().startsWith("iso."))
                .toList();
        assertTrue(isoFacts.isEmpty());

        MissingDataItemDto isoMissing = context.missingData().stream()
                .filter(item -> "ISO_MANAGEMENT".equals(item.area()))
                .findFirst()
                .orElseThrow();
        assertEquals(
                "Keine ISO-Steuerungsfelder für dieses Projekt hinterlegt.",
                isoMissing.description());

        assertFalse(context.missingData().stream()
                .anyMatch(item -> "QUALITY".equals(item.area())));
    }
}
