package com.cgi.kpi.dashboard.kpi.service;

import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectMasterDataDto;

/** Project KPI facade — DTOs only (AD-3). */
public interface ProjectKpiService {

    ProjectKpiDto getProjectKpis(UUID projectId);

    ProjectMasterDataDto getProjectMasterData(UUID projectId);
}
