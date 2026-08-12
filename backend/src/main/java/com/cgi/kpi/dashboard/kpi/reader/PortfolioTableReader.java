package com.cgi.kpi.dashboard.kpi.reader;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTableDto;

/**
 * Liefert Portfolio-Management-Tabellendaten (FR-2 / Story 5.3).
 */
public interface PortfolioTableReader {

    PortfolioTableDto readTable(PortfolioFilterCriteria criteria);
}
