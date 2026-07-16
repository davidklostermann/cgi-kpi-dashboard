package com.cgi.kpi.dashboard.kpi.dto;

import java.util.List;

/**
 * Portfolio Gantt timeline payload for API and UI (FR-3 / Story 5.1).
 */
public record PortfolioTimelineDto(
        List<PortfolioTimelineProjectDto> projects,
        boolean empty) {

    public static PortfolioTimelineDto emptyTimeline() {
        return new PortfolioTimelineDto(List.of(), true);
    }
}
