package com.cgi.kpi.dashboard.kpi.reader;

import java.util.Optional;
import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectMasterDataDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectTrendsDto;

/** Reads calculated project KPIs (AD-3). */
public interface ProjectKpiReader {

    Optional<ProjectKpiDto> readProjectKpis(UUID projectId);

    Optional<ProjectMasterDataDto> readProjectMasterData(UUID projectId);

    Optional<ProjectPhasesDto> readProjectPhases(UUID projectId);

    Optional<ProjectInsightsDto> readProjectInsights(UUID projectId);

    Optional<ProjectTrendsDto> readProjectTrends(UUID projectId);

    Optional<ProjectIssuesActionsDto> readProjectIssuesActions(UUID projectId);

    Optional<ProjectCapacityDto> readProjectCapacity(UUID projectId);
}
