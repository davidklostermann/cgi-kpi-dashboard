package com.cgi.kpi.dashboard.kpi.dto;

import java.util.UUID;

/**
 * Minimale Risikodaten für Portfolio-KPI-Berechnung in {@code kpi.*}.
 */
public record PortfolioKpiRiskInput(
        UUID projectId,
        String severity,
        String status) {
}
