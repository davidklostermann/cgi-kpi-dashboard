package com.cgi.kpi.dashboard.kpi.service;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;

/**
 * Application-facing KPI service — exposes DTOs, never domain entities (AD-3).
 */
public interface PortfolioKpiService {

    PortfolioKpiSummaryDto getPortfolioSummary();
}
