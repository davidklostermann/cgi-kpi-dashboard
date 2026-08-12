package com.cgi.kpi.dashboard.infrastructure.persistence.seed;

/**
 * Management-Insight-Kandidaten aus Brief-Addendum §5 — Regeln {@code [OFFEN]}.
 */
public enum MockManagementInsightType {
    BUDGET_AHEAD_OF_PROGRESS,
    PROGRESS_BEHIND_TIME,
    FORECAST_END_SHIFTED,
    OVERDUE_MILESTONE,
    RISK_CLUSTER,
    STATUS_DEGRADED,
    STALE_DATA,
    CONFLICTING_SIGNALS
}
