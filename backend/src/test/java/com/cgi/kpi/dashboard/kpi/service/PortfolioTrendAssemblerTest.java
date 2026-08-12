package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;

class PortfolioTrendAssemblerTest {

    private static final UUID PROJECT_A = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID PROJECT_B = UUID.fromString("a0000000-0000-4000-8000-000000000002");

    private final PortfolioTrendAssembler assembler = new PortfolioTrendAssembler();

    @Test
    void aggregatesProgressAndBudgetByMonth() {
        Project projectA = project(PROJECT_A, "ON_TRACK");
        Project projectB = project(PROJECT_B, "AT_RISK");

        ProjectReportSnapshot juneA = snapshot(projectA, LocalDate.of(2026, 6, 1), 50, 100_000);
        ProjectReportSnapshot julyA = snapshot(projectA, LocalDate.of(2026, 7, 1), 60, 120_000);
        ProjectReportSnapshot juneB = snapshot(projectB, LocalDate.of(2026, 6, 1), 40, 200_000);
        ProjectReportSnapshot julyB = snapshot(projectB, LocalDate.of(2026, 7, 1), 44, 220_000);

        PortfolioTrendDto trend = assembler.assemble(
                List.of(projectA, projectB),
                Map.of(
                        PROJECT_A, List.of(juneA, julyA),
                        PROJECT_B, List.of(juneB, julyB)));

        assertFalse(trend.empty());
        assertEquals(2, trend.points().size());
        assertEquals("2026-06", trend.points().getFirst().period());
        assertEquals(45.0, trend.points().getFirst().averageProgressPercent());
        assertEquals(300_000.0, trend.points().getFirst().totalActualBudget());
        assertEquals("2026-07", trend.points().get(1).period());
        assertEquals(52.0, trend.points().get(1).averageProgressPercent());
        assertEquals(1, trend.statusDistribution().onTrack());
        assertEquals(1, trend.statusDistribution().atRisk());
    }

    private static Project project(UUID id, String status) {
        Project project = new Project();
        project.setId(id);
        project.setName("Project");
        project.setCustomerName("Customer");
        project.setStatus(status);
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 12, 31));
        project.setProgressPercent(50);
        return project;
    }

    private static ProjectReportSnapshot snapshot(
            Project project,
            LocalDate date,
            int progress,
            double budget) {
        ProjectReportSnapshot snapshot = new ProjectReportSnapshot();
        snapshot.setProject(project);
        snapshot.setSnapshotDate(date);
        snapshot.setProgressPercent(progress);
        snapshot.setActualBudget(BigDecimal.valueOf(budget));
        snapshot.setStatus(project.getStatus());
        snapshot.setOpenRiskCount(0);
        return snapshot;
    }
}
