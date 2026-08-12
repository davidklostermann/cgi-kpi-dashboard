package com.cgi.kpi.dashboard.api.projects;

import com.cgi.kpi.dashboard.api.projects.dto.ProjectDetailDto;
import com.cgi.kpi.dashboard.api.projects.dto.ProjectListItemDto;
import com.cgi.kpi.dashboard.domain.model.DeliveryMethod;
import com.cgi.kpi.dashboard.domain.model.Project;

/**
 * Maps domain projects to API DTOs — no entity exposure (AD-3).
 */
public final class ProjectMapper {

    private ProjectMapper() {
    }

    public static ProjectListItemDto toListItem(Project project) {
        DeliveryMethod method = project.getDeliveryMethod() != null
                ? project.getDeliveryMethod()
                : DeliveryMethod.WATERFALL;
        return new ProjectListItemDto(
                project.getId(),
                project.getName(),
                project.getCustomerName(),
                project.getStatus(),
                method.name(),
                project.getProgressPercent(),
                project.getScheduleDeviationDays(),
                formatDate(project.getPlannedEndDate()));
    }

    public static ProjectDetailDto toDetail(Project project) {
        DeliveryMethod method = project.getDeliveryMethod() != null
                ? project.getDeliveryMethod()
                : DeliveryMethod.WATERFALL;
        return new ProjectDetailDto(
                project.getId(),
                project.getName(),
                project.getCustomerName(),
                project.getStatus(),
                method.name(),
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
