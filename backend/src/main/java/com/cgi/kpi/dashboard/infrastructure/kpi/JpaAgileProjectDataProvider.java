package com.cgi.kpi.dashboard.infrastructure.kpi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.DeliveryMethod;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectSprint;
import com.cgi.kpi.dashboard.domain.model.ProjectWorkItem;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectSprintRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectWorkItemRepository;
import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto;
import com.cgi.kpi.dashboard.kpi.reader.AgileProjectDataProvider;
import com.cgi.kpi.dashboard.kpi.service.AgileDeliveryCalculator;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

@Component
public class JpaAgileProjectDataProvider implements AgileProjectDataProvider {

    private final ProjectRepository projectRepository;
    private final ProjectSprintRepository projectSprintRepository;
    private final ProjectWorkItemRepository projectWorkItemRepository;
    private final AgileDeliveryCalculator agileDeliveryCalculator;
    private final CurrentUserService currentUserService;

    public JpaAgileProjectDataProvider(
            ProjectRepository projectRepository,
            ProjectSprintRepository projectSprintRepository,
            ProjectWorkItemRepository projectWorkItemRepository,
            AgileDeliveryCalculator agileDeliveryCalculator,
            CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.projectSprintRepository = projectSprintRepository;
        this.projectWorkItemRepository = projectWorkItemRepository;
        this.agileDeliveryCalculator = agileDeliveryCalculator;
        this.currentUserService = currentUserService;
    }

    @Override
    public Optional<AgileDeliveryDto> readAgileDelivery(UUID projectId) {
        UUID workspaceId = currentUserService.requireWorkspaceId();
        Optional<Project> projectOpt = projectRepository.findByIdAndWorkspaceId(projectId, workspaceId);
        if (projectOpt.isEmpty()) {
            return Optional.empty();
        }
        Project project = projectOpt.get();
        DeliveryMethod method = project.getDeliveryMethod() != null
                ? project.getDeliveryMethod()
                : DeliveryMethod.WATERFALL;
        if (method == DeliveryMethod.WATERFALL) {
            return Optional.of(agileDeliveryCalculator.assembleWaterfall(project));
        }
        List<ProjectSprint> sprints = projectSprintRepository.findByProject_IdOrderBySequenceNoAsc(projectId);
        List<ProjectWorkItem> workItems = projectWorkItemRepository.findByProject_Id(projectId);
        return Optional.of(agileDeliveryCalculator.assemble(project, sprints, workItems));
    }
}
