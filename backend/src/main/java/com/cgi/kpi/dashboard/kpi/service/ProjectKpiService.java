package com.cgi.kpi.dashboard.kpi.service;

import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectMasterDataDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectTrendsDto;

/** Project KPI facade — DTOs only (AD-3). */
public interface ProjectKpiService {

    ProjectKpiDto getProjectKpis(UUID projectId);

    ProjectMasterDataDto getProjectMasterData(UUID projectId);

    ProjectPhasesDto getProjectPhases(UUID projectId);

    ProjectInsightsDto getProjectInsights(UUID projectId);

    ProjectTrendsDto getProjectTrends(UUID projectId);

    ProjectIssuesActionsDto getProjectIssuesActions(UUID projectId);

    ProjectCapacityDto getProjectCapacity(UUID projectId);
}
