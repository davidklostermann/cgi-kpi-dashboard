package com.cgi.kpi.dashboard.kpi.service;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;

/**
 * Application-facing KPI service — exposes DTOs, never domain entities (AD-3).
 */
public interface PortfolioKpiService {

    PortfolioKpiSummaryDto getPortfolioSummary(PortfolioFilterCriteria criteria);

    PortfolioFilterOptionsDto getFilterOptions();

    PortfolioTimelineDto getPortfolioTimeline(PortfolioFilterCriteria criteria);

    PortfolioTableDto getPortfolioTable(PortfolioFilterCriteria criteria);

    PortfolioTrendDto getPortfolioTrends(PortfolioFilterCriteria criteria);
}
