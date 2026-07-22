package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto.ProjectMilestoneItemDto;

class ProjectPhasesAssemblerTest {

    private final ProjectPhasesAssembler assembler = new ProjectPhasesAssembler();

    @Test
    void toMilestoneSetsDeviationDaysOnlyWhenCompletedDatePresent() {
        Project project = project();
        Milestone completedLate = milestone(project, "Pilot-Release", LocalDate.of(2026, 6, 24), LocalDate.of(2026, 7, 1), "COMPLETED");

        ProjectPhasesDto dto = assembler.assemble(project, List.of(), List.of(completedLate));
        ProjectMilestoneItemDto item = dto.milestones().getFirst();

        assertEquals(7, item.deviationDays());
        assertNull(item.overdueDays());
        assertEquals(false, item.overdue());
    }

    @Test
    void toMilestoneSetsOverdueDaysWhenOverdueWithoutCompletedDate() {
        Project project = project();
        Milestone overdue = milestone(project, "Pilot-Release", LocalDate.of(2026, 6, 24), null, "OVERDUE");

        ProjectPhasesDto dto = assembler.assemble(project, List.of(), List.of(overdue));
        ProjectMilestoneItemDto item = dto.milestones().getFirst();

        assertTrue(item.overdue());
        assertNull(item.deviationDays());
        assertEquals(7, item.overdueDays());
        assertNull(item.actualOrForecastDate());
    }

    @Test
    void toMilestoneLeavesOverdueDaysNullWhenNotOverdue() {
        Project project = project();
        Milestone planned = milestone(project, "Go-Live", LocalDate.of(2026, 9, 1), null, "PLANNED");

        ProjectPhasesDto dto = assembler.assemble(project, List.of(), List.of(planned));
        ProjectMilestoneItemDto item = dto.milestones().getFirst();

        assertEquals(false, item.overdue());
        assertNull(item.deviationDays());
        assertNull(item.overdueDays());
    }

    private static Project project() {
        Project project = new Project();
        project.setId(UUID.fromString("a0000000-0000-4000-8000-000000000001"));
        project.setName("Nexus Analytics Pilot");
        project.setCustomerName("Acme");
        project.setStatus("ON_TRACK");
        project.setStartDate(LocalDate.of(2025, 3, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 6, 30));
        return project;
    }

    private static Milestone milestone(
            Project project, String name, LocalDate dueDate, LocalDate completedDate, String status) {
        Milestone milestone = new Milestone();
        milestone.setProject(project);
        milestone.setName(name);
        milestone.setDueDate(dueDate);
        milestone.setCompletedDate(completedDate);
        milestone.setStatus(status);
        return milestone;
    }
}
