package com.cgi.kpi.dashboard.kpi.reader;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTrendDto;

/**
 * Liefert Portfolio-Trend- und Statusdaten für Visualisierungen (FR-3 / Story 5.4).
 */
public interface PortfolioTrendReader {

    PortfolioTrendDto readTrends(PortfolioFilterCriteria criteria);
}
