package com.cgi.kpi.dashboard.kpi.service;

/**
 * German word labels for portfolio/project status codes (UX-DR / Story 5.1).
 */
public final class PortfolioStatusLabels {

    private PortfolioStatusLabels() {
    }

    public static String toGermanLabel(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return "Unbekannt";
        }
        return switch (statusCode.trim().toUpperCase()) {
            case "ON_TRACK" -> "Auf Kurs";
            case "AT_RISK" -> "Beobachten";
            case "CRITICAL" -> "Kritisch";
            case "COMPLETED" -> "Abgeschlossen";
            default -> statusCode;
        };
    }
}
