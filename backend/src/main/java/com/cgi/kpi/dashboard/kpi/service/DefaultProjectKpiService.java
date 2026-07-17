package com.cgi.kpi.dashboard.kpi.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ProjectCapacityDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectIssuesActionsDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectMasterDataDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectPhasesDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectTrendsDto;
import com.cgi.kpi.dashboard.kpi.reader.ProjectKpiReader;

@Service
public class DefaultProjectKpiService implements ProjectKpiService {

    private final ProjectKpiReader projectKpiReader;

    public DefaultProjectKpiService(ProjectKpiReader projectKpiReader) {
        this.projectKpiReader = projectKpiReader;
    }

    @Override
    public ProjectKpiDto getProjectKpis(UUID projectId) {
        return require(projectKpiReader.readProjectKpis(projectId));
    }

    @Override
    public ProjectMasterDataDto getProjectMasterData(UUID projectId) {
        return require(projectKpiReader.readProjectMasterData(projectId));
    }

    @Override
    public ProjectPhasesDto getProjectPhases(UUID projectId) {
        return require(projectKpiReader.readProjectPhases(projectId));
    }

    @Override
    public ProjectInsightsDto getProjectInsights(UUID projectId) {
        return require(projectKpiReader.readProjectInsights(projectId));
    }

    @Override
    public ProjectTrendsDto getProjectTrends(UUID projectId) {
        return require(projectKpiReader.readProjectTrends(projectId));
    }

    @Override
    public ProjectIssuesActionsDto getProjectIssuesActions(UUID projectId) {
        return require(projectKpiReader.readProjectIssuesActions(projectId));
    }

    @Override
    public ProjectCapacityDto getProjectCapacity(UUID projectId) {
        return require(projectKpiReader.readProjectCapacity(projectId));
    }

    private static <T> T require(java.util.Optional<T> value) {
        return value.orElseThrow(() -> new ApiException(
                "NOT_FOUND",
                "Project not found",
                HttpStatus.NOT_FOUND));
    }
}
