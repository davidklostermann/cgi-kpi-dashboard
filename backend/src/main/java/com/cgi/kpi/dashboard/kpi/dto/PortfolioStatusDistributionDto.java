package com.cgi.kpi.dashboard.kpi.dto;

/**
 * Statusverteilung der gefilterten Projekte (ON_TRACK / AT_RISK / CRITICAL / COMPLETED) (FR-1).
 */
public record PortfolioStatusDistributionDto(
        int onTrack,
        int atRisk,
        int critical,
        int completed) {

    public int total() {
        return onTrack + atRisk + critical + completed;
    }
}
