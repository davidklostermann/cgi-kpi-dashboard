package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineProjectDto;

class PortfolioTimelineAssemblerTest {

    private static final UUID PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");

    private final PortfolioTimelineAssembler assembler = new PortfolioTimelineAssembler();

    @Test
    void emptyProjectsReturnsEmptyTimeline() {
        PortfolioTimelineDto timeline = assembler.assemble(List.of(), Map.of(), Map.of());

        assertTrue(timeline.empty());
        assertTrue(timeline.projects().isEmpty());
    }

    @Test
    void assemblesProjectWithPhasesMilestonesAndGermanStatusLabel() {
        Project project = new Project();
        project.setId(PROJECT_ID);
        project.setName("Nexus Analytics Pilot");
        project.setCustomerName("Acme GmbH");
        project.setStatus("AT_RISK");
        project.setStartDate(LocalDate.of(2025, 3, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 6, 30));
        project.setPredictedEndDate(LocalDate.of(2026, 7, 15));
        project.setScheduleDeviationDays(12);
        project.setProgressPercent(62);

        ProjectPhase phase = new ProjectPhase();
        phase.setName("Umsetzung");
        phase.setPhaseType("UMSETZUNG");
        phase.setStartDate(LocalDate.of(2025, 9, 1));
        phase.setEndDate(LocalDate.of(2026, 6, 30));
        phase.setSortOrder(2);
        phase.setProject(project);

        Milestone milestone = new Milestone();
        milestone.setName("Pilot-Release");
        milestone.setDueDate(LocalDate.of(2026, 6, 30));
        milestone.setStatus("AT_RISK");
        milestone.setProject(project);

        PortfolioTimelineDto timeline = assembler.assemble(
                List.of(project),
                Map.of(PROJECT_ID, List.of(phase)),
                Map.of(PROJECT_ID, List.of(milestone)));

        assertFalse(timeline.empty());
        assertEquals(1, timeline.projects().size());

        PortfolioTimelineProjectDto row = timeline.projects().getFirst();
        assertEquals("Nexus Analytics Pilot", row.name());
        assertEquals(LocalDate.of(2025, 3, 1), row.startDate());
        assertEquals(LocalDate.of(2026, 6, 30), row.plannedEndDate());
        assertEquals(LocalDate.of(2026, 7, 15), row.forecastEndDate());
        assertEquals(15, row.scheduleDeviationDays());
        assertEquals("AT_RISK", row.status());
        assertEquals("Beobachten", row.statusLabel());
        assertEquals(1, row.phases().size());
        assertEquals("Umsetzung", row.phases().getFirst().name());
        assertEquals(1, row.milestones().size());
        assertEquals("Pilot-Release", row.milestones().getFirst().name());
        assertEquals("Beobachten", row.milestones().getFirst().statusLabel());
    }
}
