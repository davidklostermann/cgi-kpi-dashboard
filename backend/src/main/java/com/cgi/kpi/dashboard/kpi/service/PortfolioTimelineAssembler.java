package com.cgi.kpi.dashboard.kpi.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineMilestoneDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelinePhaseDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineProjectDto;

/**
 * Assembles portfolio Gantt timeline DTOs from domain data (AD-3).
 */
@Component
public class PortfolioTimelineAssembler {

    public PortfolioTimelineDto assemble(
            List<Project> projects,
            Map<UUID, List<ProjectPhase>> phasesByProjectId,
            Map<UUID, List<Milestone>> milestonesByProjectId) {
        if (projects == null || projects.isEmpty()) {
            return PortfolioTimelineDto.emptyTimeline();
        }

        List<PortfolioTimelineProjectDto> rows = projects.stream()
                .sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER))
                .map(project -> toProjectRow(
                        project,
                        phasesByProjectId.getOrDefault(project.getId(), List.of()),
                        milestonesByProjectId.getOrDefault(project.getId(), List.of())))
                .toList();

        return new PortfolioTimelineDto(rows, false);
    }

    private PortfolioTimelineProjectDto toProjectRow(
            Project project,
            List<ProjectPhase> phases,
            List<Milestone> milestones) {
        return new PortfolioTimelineProjectDto(
                project.getId(),
                project.getName(),
                project.getStartDate(),
                project.getPlannedEndDate(),
                project.getPredictedEndDate(),
                project.getActualEndDate(),
                ScheduleDeviationResolver.resolve(
                        project.getPlannedEndDate(),
                        project.getPredictedEndDate(),
                        project.getScheduleDeviationDays()),
                project.getStatus(),
                PortfolioStatusLabels.toGermanLabel(project.getStatus()),
                toPhaseDtos(phases),
                toMilestoneDtos(milestones));
    }

    private static List<PortfolioTimelinePhaseDto> toPhaseDtos(List<ProjectPhase> phases) {
        return phases.stream()
                .sorted(Comparator.comparingInt(ProjectPhase::getSortOrder))
                .map(phase -> new PortfolioTimelinePhaseDto(
                        phase.getName(),
                        phase.getPhaseType(),
                        phase.getStartDate(),
                        phase.getEndDate(),
                        phase.getSortOrder()))
                .toList();
    }

    private static List<PortfolioTimelineMilestoneDto> toMilestoneDtos(List<Milestone> milestones) {
        return milestones.stream()
                .sorted(Comparator.comparing(Milestone::getDueDate))
                .map(milestone -> new PortfolioTimelineMilestoneDto(
                        milestone.getName(),
                        milestone.getDueDate(),
                        milestone.getCompletedDate(),
                        milestone.getStatus(),
                        PortfolioStatusLabels.toGermanLabel(milestone.getStatus())))
                .toList();
    }
}
