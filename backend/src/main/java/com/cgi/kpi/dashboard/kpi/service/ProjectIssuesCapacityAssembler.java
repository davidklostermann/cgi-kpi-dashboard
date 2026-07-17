package com.cgi.kpi.dashboard.kpi.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.Problem;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectCapacitySummary;
import com.cgi.kpi.dashboard.domain.model.ProjectRoleCapacity;
import com.cgi.kpi.dashboard.domain.model.Risk;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto.CapacitySummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto.RoleCapacityItemDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto.IssueActionItemDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto.MetricDto;

/**
 * Assembles operational issues/actions and capacity DTOs for the project detail page.
 */
@Component
public class ProjectIssuesCapacityAssembler {

    private static final String STATUS_OPEN = "OPEN";
    private static final String FACTS_BADGE = "Fakten aus Backend";

    public ProjectIssuesActionsDto assembleIssuesActions(Project project, List<Problem> problems, List<Risk> risks) {
        List<IssueActionItemDto> items = new ArrayList<>();
        for (Problem problem : problems) {
            if (!STATUS_OPEN.equals(problem.getStatus())) {
                continue;
            }
            items.add(fromProblem(problem));
        }
        for (Risk risk : risks) {
            if (!STATUS_OPEN.equals(risk.getStatus())) {
                continue;
            }
            items.add(fromRisk(risk));
        }
        items.sort(Comparator
                .comparingInt((IssueActionItemDto item) -> severityRank(item.severity()))
                .thenComparing(IssueActionItemDto::itemType)
                .thenComparing(IssueActionItemDto::title, String.CASE_INSENSITIVE_ORDER));

        Instant factsAsOf = project.getLastDataUpdate() != null
                ? project.getLastDataUpdate()
                : Instant.parse("2026-07-01T08:00:00Z");

        return new ProjectIssuesActionsDto(project.getId(), FACTS_BADGE, factsAsOf, List.copyOf(items));
    }

    public ProjectCapacityDto assembleCapacity(
            Project project,
            List<ProjectRoleCapacity> roles,
            ProjectCapacitySummary summary) {
        Instant factsAsOf = summary != null && summary.getFactsAsOf() != null
                ? summary.getFactsAsOf()
                : (project.getLastDataUpdate() != null
                        ? project.getLastDataUpdate()
                        : Instant.parse("2026-07-01T08:00:00Z"));

        List<RoleCapacityItemDto> roleItems = roles.stream()
                .sorted(Comparator.comparingInt(ProjectRoleCapacity::getSortOrder))
                .map(role -> new RoleCapacityItemDto(
                        role.getId(),
                        role.getRoleName(),
                        role.getRequiredFte(),
                        role.getAvailableFte(),
                        role.getCoveragePercent()))
                .toList();

        CapacitySummaryDto summaryDto = null;
        if (summary != null) {
            summaryDto = new CapacitySummaryDto(
                    summary.getMissingFte(),
                    summary.getNextAvailabilityDate(),
                    summary.getOverloadedRoles(),
                    summary.getExternalOptions(),
                    summary.getImpactHeadline(),
                    summary.getImpactDetail());
        }

        String badge = "Datenstand "
                + java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                        .withZone(java.time.ZoneOffset.UTC)
                        .format(factsAsOf);

        return new ProjectCapacityDto(project.getId(), factsAsOf, badge, roleItems, summaryDto);
    }

    private static IssueActionItemDto fromProblem(Problem problem) {
        return new IssueActionItemDto(
                problem.getId(),
                "PROBLEM",
                "Problem",
                normalizeCategory(problem.getCategory()),
                problem.getTitle(),
                problem.getDescription(),
                problem.getSeverity(),
                severityLabel(problem.getSeverity()),
                metrics(
                        problem.getMetric1Label(), problem.getMetric1Value(),
                        problem.getMetric2Label(), problem.getMetric2Value(),
                        problem.getMetric3Label(), problem.getMetric3Value(),
                        problem.getMetric4Label(), problem.getMetric4Value()),
                problem.getResponsible(),
                problem.getTargetDate(),
                "COUNTERMEASURE",
                "Laufende Maßnahme",
                problem.getCountermeasure());
    }

    private static IssueActionItemDto fromRisk(Risk risk) {
        return new IssueActionItemDto(
                risk.getId(),
                "RISK",
                "Risiko",
                normalizeCategory(risk.getCategory()),
                risk.getTitle(),
                risk.getDescription(),
                risk.getSeverity(),
                severityLabel(risk.getSeverity()),
                metrics(
                        risk.getMetric1Label(), risk.getMetric1Value(),
                        risk.getMetric2Label(), risk.getMetric2Value(),
                        risk.getMetric3Label(), risk.getMetric3Value(),
                        risk.getMetric4Label(), risk.getMetric4Value()),
                risk.getOwnerName(),
                risk.getDueDate(),
                "MITIGATION",
                "Vorbereitung / Gegensteuerung",
                risk.getMitigationMeasure());
    }

    private static List<MetricDto> metrics(
            String l1, String v1, String l2, String v2, String l3, String v3, String l4, String v4) {
        List<MetricDto> result = new ArrayList<>(4);
        addMetric(result, l1, v1);
        addMetric(result, l2, v2);
        addMetric(result, l3, v3);
        addMetric(result, l4, v4);
        return List.copyOf(result);
    }

    private static void addMetric(List<MetricDto> target, String label, String value) {
        if (label == null || label.isBlank() || value == null || value.isBlank()) {
            return;
        }
        target.add(new MetricDto(label, value));
    }

    private static String normalizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "ALLGEMEIN";
        }
        return category.trim().toUpperCase(Locale.ROOT);
    }

    private static String severityLabel(String severity) {
        if (severity == null) {
            return "Unbekannt";
        }
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> "Kritisch";
            case "HIGH" -> "Hoch";
            case "MEDIUM" -> "Mittel";
            case "LOW" -> "Niedrig";
            default -> severity;
        };
    }

    private static int severityRank(String severity) {
        if (severity == null) {
            return 99;
        }
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> 0;
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            case "LOW" -> 3;
            default -> 50;
        };
    }
}
