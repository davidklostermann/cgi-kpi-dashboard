package com.cgi.kpi.dashboard.kpi.reader;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;

/**
 * Liefert aggregierte Portfolio-KPIs für {@code ai.*} (AD-2).
 */
public interface PortfolioKpiReader {

    PortfolioKpiSummaryDto readPortfolioSummary();
}
