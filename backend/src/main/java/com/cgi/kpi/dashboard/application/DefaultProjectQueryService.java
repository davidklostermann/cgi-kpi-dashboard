package com.cgi.kpi.dashboard.application;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.api.projects.ProjectMapper;
import com.cgi.kpi.dashboard.api.projects.dto.ProjectDetailDto;
import com.cgi.kpi.dashboard.api.projects.dto.ProjectListItemDto;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;

@Service
@Transactional(readOnly = true)
public class DefaultProjectQueryService implements ProjectQueryService {

    private final ProjectRepository projectRepository;

    public DefaultProjectQueryService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<ProjectListItemDto> listProjects() {
        return projectRepository.findAll().stream()
                .sorted(Comparator.comparing(Project::getName, String.CASE_INSENSITIVE_ORDER))
                .map(ProjectMapper::toListItem)
                .toList();
    }

    @Override
    public ProjectDetailDto getProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException(
                        "NOT_FOUND",
                        "Project not found",
                        HttpStatus.NOT_FOUND));

        return ProjectMapper.toDetail(project);
    }
}
