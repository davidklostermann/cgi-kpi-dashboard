package com.cgi.kpi.dashboard.kpi.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Milestone;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectPhase;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto.ProjectMilestoneItemDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto.ProjectPhaseItemDto;

/**
 * Assembles project phase/milestone detail DTOs (AD-3 / Story 6.4).
 */
@Component
public class ProjectPhasesAssembler {

    private static final LocalDate REFERENCE_DATE = ProjectKpiCalculator.REFERENCE_DATE;

    public ProjectPhasesDto assemble(Project project, List<ProjectPhase> phases, List<Milestone> milestones) {
        List<ProjectPhaseItemDto> phaseItems = phases.stream()
                .sorted(Comparator.comparingInt(ProjectPhase::getSortOrder))
                .map(this::toPhase)
                .toList();
        List<ProjectMilestoneItemDto> milestoneItems = milestones.stream()
                .sorted(Comparator.comparing(Milestone::getDueDate))
                .map(this::toMilestone)
                .toList();

        return new ProjectPhasesDto(
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
                phaseItems,
                milestoneItems,
                buildAccessibilitySummary(phaseItems, milestoneItems));
    }

    private ProjectPhaseItemDto toPhase(ProjectPhase phase) {
        String status = resolvePhaseStatus(phase.getStartDate(), phase.getEndDate());
        return new ProjectPhaseItemDto(
                phase.getName(),
                phase.getPhaseType(),
                status,
                phaseStatusLabel(status),
                phase.getStartDate(),
                phase.getEndDate(),
                null,
                null,
                null,
                null,
                phase.getSortOrder());
    }

    private ProjectMilestoneItemDto toMilestone(Milestone milestone) {
        boolean overdue = "OVERDUE".equalsIgnoreCase(milestone.getStatus())
                || (milestone.getCompletedDate() == null
                        && milestone.getDueDate() != null
                        && milestone.getDueDate().isBefore(REFERENCE_DATE));
        Integer deviationDays = null;
        if (milestone.getCompletedDate() != null && milestone.getDueDate() != null) {
            deviationDays = (int) ChronoUnit.DAYS.between(milestone.getDueDate(), milestone.getCompletedDate());
        } else if (overdue && milestone.getDueDate() != null) {
            deviationDays = (int) ChronoUnit.DAYS.between(milestone.getDueDate(), REFERENCE_DATE);
        }

        return new ProjectMilestoneItemDto(
                milestone.getName(),
                milestone.getStatus(),
                milestoneStatusLabel(milestone.getStatus(), overdue),
                milestone.getDueDate(),
                milestone.getCompletedDate(),
                deviationDays,
                overdue,
                null);
    }

    private static String resolvePhaseStatus(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return "UNKNOWN";
        }
        if (REFERENCE_DATE.isBefore(start)) {
            return "PLANNED";
        }
        if (REFERENCE_DATE.isAfter(end)) {
            return "COMPLETED";
        }
        return "ACTIVE";
    }

    private static String phaseStatusLabel(String status) {
        return switch (status) {
            case "PLANNED" -> "Geplant";
            case "ACTIVE" -> "Aktiv";
            case "COMPLETED" -> "Abgeschlossen";
            default -> "Unbekannt";
        };
    }

    private static String milestoneStatusLabel(String status, boolean overdue) {
        if (overdue) {
            return "Überfällig";
        }
        if (status == null) {
            return "Unbekannt";
        }
        return switch (status.toUpperCase()) {
            case "COMPLETED", "DONE" -> "Erledigt";
            case "OPEN", "PLANNED" -> "Offen";
            case "OVERDUE" -> "Überfällig";
            default -> status;
        };
    }

    private static String buildAccessibilitySummary(
            List<ProjectPhaseItemDto> phases,
            List<ProjectMilestoneItemDto> milestones) {
        String phaseNames = phases.stream().map(ProjectPhaseItemDto::name).collect(Collectors.joining(", "));
        List<String> overdue = milestones.stream()
                .filter(ProjectMilestoneItemDto::overdue)
                .map(ProjectMilestoneItemDto::name)
                .toList();
        String overdueText = overdue.isEmpty()
                ? "Keine überfälligen Meilensteine."
                : "Überfällige Meilensteine: " + String.join(", ", overdue) + ".";
        return "Phasen: " + (phaseNames.isBlank() ? "keine" : phaseNames) + ". " + overdueText;
    }
}
