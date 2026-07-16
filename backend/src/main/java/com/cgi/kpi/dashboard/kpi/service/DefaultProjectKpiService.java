package com.cgi.kpi.dashboard.kpi.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.kpi.dto.ProjectKpiDto;
import com.cgi.kpi.dashboard.kpi.reader.ProjectKpiReader;

@Service
public class DefaultProjectKpiService implements ProjectKpiService {

    private final ProjectKpiReader projectKpiReader;

    public DefaultProjectKpiService(ProjectKpiReader projectKpiReader) {
        this.projectKpiReader = projectKpiReader;
    }

    @Override
    public ProjectKpiDto getProjectKpis(UUID projectId) {
        return projectKpiReader.readProjectKpis(projectId)
                .orElseThrow(() -> new ApiException(
                        "NOT_FOUND",
                        "Project not found",
                        HttpStatus.NOT_FOUND));
    }
}
