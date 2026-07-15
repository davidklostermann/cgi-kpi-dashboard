package com.cgi.kpi.dashboard.api.projects;

import com.cgi.kpi.dashboard.api.projects.dto.ProjectDetailDto;
import com.cgi.kpi.dashboard.api.projects.dto.ProjectListItemDto;
import com.cgi.kpi.dashboard.domain.model.Project;

/**
 * Maps domain projects to API DTOs — no entity exposure (AD-3).
 */
public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectListItemDto toListItem(Project project) {
        return new ProjectListItemDto(
                project.getId(),
                project.getName(),
                project.getCustomerName(),
                project.getStatus(),
                project.getProgressPercent(),
                project.getScheduleDeviationDays(),
                formatDate(project.getPlannedEndDate()));
    }

    public static ProjectDetailDto toDetail(Project project) {
        return new ProjectDetailDto(
                project.getId(),
                project.getName(),
                project.getCustomerName(),
                project.getStatus(),
                formatDate(project.getStartDate()),
                formatDate(project.getPlannedEndDate()),
                formatDate(project.getActualEndDate()),
                project.getProgressPercent(),
                project.getScheduleDeviationDays(),
                project.getCreatedAt());
    }

    private static String formatDate(java.time.LocalDate date) {
        return date != null ? date.toString() : null;
    }
}
