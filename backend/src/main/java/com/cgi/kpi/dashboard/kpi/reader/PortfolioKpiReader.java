package com.cgi.kpi.dashboard.kpi.reader;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterOptionsDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;

/**
 * Liefert aggregierte Portfolio-KPIs für {@code ai.*} (AD-2).
 */
public interface PortfolioKpiReader {

    PortfolioKpiSummaryDto readPortfolioSummary(PortfolioFilterCriteria criteria);

    PortfolioFilterOptionsDto readFilterOptions();
}
