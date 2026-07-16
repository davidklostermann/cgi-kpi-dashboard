package com.cgi.kpi.dashboard.kpi.reader;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioTimelineDto;

/**
 * Liefert Portfolio-Gantt-Zeitleistendaten für API und UI (FR-3 / Story 5.1).
 */
public interface PortfolioTimelineReader {

    PortfolioTimelineDto readTimeline(PortfolioFilterCriteria criteria);
}
