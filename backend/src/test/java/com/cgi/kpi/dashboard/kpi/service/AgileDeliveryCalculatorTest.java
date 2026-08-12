package com.cgi.kpi.dashboard.kpi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.DeliveryMethod;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectSprint;
import com.cgi.kpi.dashboard.domain.model.ProjectWorkItem;
import com.cgi.kpi.dashboard.domain.model.SprintHealth;
import com.cgi.kpi.dashboard.domain.model.SprintLifecycle;
import com.cgi.kpi.dashboard.domain.model.WorkItemStatus;
import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto;

class AgileDeliveryCalculatorTest {

    private final AgileDeliveryCalculator calculator = new AgileDeliveryCalculator();

    @Test
    void progressPercentUsesZeroWhenPlannedIsZero() {
        assertEquals(0, calculator.progressPercent(0, 10));
        assertEquals(50, calculator.progressPercent(40, 20));
    }

    @Test
    void futureSprintIsPlannedHealth() {
        ProjectSprint sprint = sprint(1, SprintLifecycle.FUTURE, 40, 0, 0);
        assertEquals(SprintHealth.PLANNED, calculator.resolveHealth(sprint));
        assertEquals("Geplant", calculator.healthLabel(SprintHealth.PLANNED));
    }

    @Test
    void pastSprintHealthUsesCompletionAndCarryThresholds() {
        assertEquals(SprintHealth.GOOD, calculator.resolveHealth(sprint(1, SprintLifecycle.PAST, 40, 38, 2)));
        assertEquals(SprintHealth.WATCH, calculator.resolveHealth(sprint(1, SprintLifecycle.PAST, 40, 34, 0)));
        assertEquals(SprintHealth.CRITICAL, calculator.resolveHealth(sprint(1, SprintLifecycle.PAST, 40, 20, 0)));
        assertEquals(SprintHealth.CRITICAL, calculator.resolveHealth(sprint(1, SprintLifecycle.PAST, 40, 38, 12)));
    }

    @Test
    void averageVelocityUsesLastUpToThreePastSprints() {
        List<ProjectSprint> sprints = List.of(
                sprint(1, SprintLifecycle.PAST, 40, 30, 0),
                sprint(2, SprintLifecycle.PAST, 40, 40, 0),
                sprint(3, SprintLifecycle.PAST, 40, 50, 0),
                sprint(4, SprintLifecycle.ACTIVE, 45, 20, 0));
        assertEquals(40.0, calculator.averageVelocity(sprints));
    }

    @Test
    void assembleWaterfallMarksUnavailable() {
        Project project = project(DeliveryMethod.WATERFALL);
        AgileDeliveryDto dto = calculator.assembleWaterfall(project);
        assertFalse(dto.dataAvailable());
        assertEquals("WATERFALL", dto.deliveryMethod());
        assertTrue(dto.sprints().isEmpty());
    }

    @Test
    void assembleCountsOpenBlockersAndBuildsChart() {
        Project project = project(DeliveryMethod.AGILE);
        List<ProjectSprint> sprints = List.of(
                sprint(1, SprintLifecycle.PAST, 40, 38, 2),
                sprint(2, SprintLifecycle.ACTIVE, 45, 22, 0),
                sprint(3, SprintLifecycle.FUTURE, 48, 0, 0));
        ProjectWorkItem openBlocker = new ProjectWorkItem();
        openBlocker.setBlocker(true);
        openBlocker.setStatus(WorkItemStatus.TODO);
        ProjectWorkItem doneBlocker = new ProjectWorkItem();
        doneBlocker.setBlocker(true);
        doneBlocker.setStatus(WorkItemStatus.DONE);

        AgileDeliveryDto dto = calculator.assemble(project, sprints, List.of(openBlocker, doneBlocker));

        assertTrue(dto.dataAvailable());
        assertEquals(3, dto.sprints().size());
        assertTrue(dto.sprints().get(1).current());
        assertTrue(dto.sprints().get(2).future());
        assertEquals(1, dto.kpis().openBlockerCount());
        assertEquals(133, dto.kpis().totalStoryPoints());
        assertEquals(38.0, dto.kpis().averageVelocity());
        assertNull(dto.chart().velocityTrend().get(2));
        assertTrue(dto.chart().futureFlags().get(2));
        assertEquals(AgileDeliveryCalculator.DATA_SOURCE_MOCK, dto.dataSource());
    }

    private static Project project(DeliveryMethod method) {
        Project project = new Project();
        project.setId(UUID.fromString("a0000000-0000-4000-8000-000000000006"));
        project.setDeliveryMethod(method);
        return project;
    }

    private static ProjectSprint sprint(
            int sequence, SprintLifecycle lifecycle, int planned, int completed, int carry) {
        ProjectSprint sprint = new ProjectSprint();
        sprint.setId(UUID.randomUUID());
        sprint.setName("S" + sequence);
        sprint.setSequenceNo(sequence);
        sprint.setLifecycle(lifecycle);
        sprint.setStoryPointsPlanned(planned);
        sprint.setStoryPointsCompleted(completed);
        sprint.setCarryOverPoints(carry);
        sprint.setStartDate(LocalDate.of(2026, 1, 1));
        sprint.setEndDate(LocalDate.of(2026, 1, 14));
        return sprint;
    }
}
