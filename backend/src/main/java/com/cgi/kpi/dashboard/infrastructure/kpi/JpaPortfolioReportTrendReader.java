package com.cgi.kpi.dashboard.infrastructure.kpi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectReportSnapshot;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectReportSnapshotRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.ProjectRepository;
import com.cgi.kpi.dashboard.kpi.dto.ProjectReportTrendDto;
import com.cgi.kpi.dashboard.kpi.reader.PortfolioReportTrendReader;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

@Component
public class JpaPortfolioReportTrendReader implements PortfolioReportTrendReader {

    private final ProjectReportSnapshotRepository snapshotRepository;
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUserService;

    public JpaPortfolioReportTrendReader(
            ProjectReportSnapshotRepository snapshotRepository,
            ProjectRepository projectRepository,
            CurrentUserService currentUserService) {
        this.snapshotRepository = snapshotRepository;
        this.projectRepository = projectRepository;
        this.currentUserService = currentUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectReportTrendDto> readTrendsForProjects(Collection<UUID> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }

        Set<UUID> workspaceProjectIds = projectRepository
                .findAllByWorkspaceId(currentUserService.requireWorkspaceId())
                .stream()
                .map(Project::getId)
                .collect(Collectors.toSet());
        List<UUID> scopedIds = projectIds.stream().filter(workspaceProjectIds::contains).toList();
        if (scopedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<ProjectReportSnapshot>> byProject = snapshotRepository
                .findByProjectIdsWithProject(scopedIds)
                .stream()
                .collect(Collectors.groupingBy(snapshot -> snapshot.getProject().getId()));

        List<ProjectReportTrendDto> trends = new ArrayList<>();
        for (Map.Entry<UUID, List<ProjectReportSnapshot>> entry : byProject.entrySet()) {
            List<ProjectReportSnapshot> ordered = entry.getValue().stream()
                    .sorted(Comparator.comparing(ProjectReportSnapshot::getSnapshotDate)
                            .thenComparing(ProjectReportSnapshot::getId))
                    .toList();
            if (ordered.size() < 2) {
                continue;
            }
            ProjectReportSnapshot previous = ordered.get(ordered.size() - 2);
            ProjectReportSnapshot current = ordered.get(ordered.size() - 1);
            trends.add(new ProjectReportTrendDto(
                    entry.getKey(),
                    current.getProject().getName(),
                    previous.getSnapshotDate(),
                    current.getSnapshotDate(),
                    previous.getScheduleDeviationDays(),
                    current.getScheduleDeviationDays(),
                    previous.getStatus(),
                    current.getStatus(),
                    previous.getOpenRiskCount(),
                    current.getOpenRiskCount(),
                    previous.getActualBudget(),
                    current.getActualBudget(),
                    previous.getProgressPercent(),
                    current.getProgressPercent()));
        }
        return List.copyOf(trends);
    }
}
