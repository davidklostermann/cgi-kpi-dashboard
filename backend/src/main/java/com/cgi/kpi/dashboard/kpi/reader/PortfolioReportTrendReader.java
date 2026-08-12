package com.cgi.kpi.dashboard.kpi.reader;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.ProjectReportTrendDto;

/**
 * Freigegebene Berichtsstand-Paare für Portfolio-Muster (AD-2).
 */
public interface PortfolioReportTrendReader {

    List<ProjectReportTrendDto> readTrendsForProjects(Collection<UUID> projectIds);
}
