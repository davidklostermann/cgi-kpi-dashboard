package com.cgi.kpi.dashboard.kpi.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Agile delivery facts for project detail (Epic 17 / FR-35). No forecast/risk panels.
 */
public record AgileDeliveryDto(
        UUID projectId,
        String deliveryMethod,
        boolean dataAvailable,
        String emptyReason,
        String dataSource,
        Instant factsAsOf,
        List<AgileSprintCardDto> sprints,
        AgileSprintChartDto chart,
        AgileDeliveryKpisDto kpis) {

    public record AgileSprintCardDto(
            UUID id,
            String name,
            int sequenceNo,
            String lifecycle,
            boolean current,
            boolean future,
            String health,
            String healthLabel,
            int progressPercent,
            int storyPointsPlanned,
            int storyPointsCompleted,
            int carryOverPoints,
            LocalDate startDate,
            LocalDate endDate) {
    }

    public record AgileSprintChartDto(
            List<String> sprintLabels,
            List<Integer> plannedStoryPoints,
            List<Integer> completedStoryPoints,
            List<Integer> velocityTrend,
            List<Boolean> futureFlags) {
    }

    public record AgileDeliveryKpisDto(
            String sprintHealth,
            String sprintHealthLabel,
            int totalStoryPoints,
            Double averageVelocity,
            int carryOverNextSprint,
            int openBlockerCount) {
    }
}
