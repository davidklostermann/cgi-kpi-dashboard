package com.cgi.kpi.dashboard.kpi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.domain.model.DeliveryMethod;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectSprint;
import com.cgi.kpi.dashboard.domain.model.ProjectWorkItem;
import com.cgi.kpi.dashboard.domain.model.SprintHealth;
import com.cgi.kpi.dashboard.domain.model.SprintLifecycle;
import com.cgi.kpi.dashboard.domain.model.WorkItemStatus;
import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto;
import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto.AgileDeliveryKpisDto;
import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto.AgileSprintCardDto;
import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto.AgileSprintChartDto;

/**
 * Deterministic agile KPI calculation (AD-3). Health thresholds documented as pilot defaults.
 *
 * <p>Defaults (OFFEN until product confirms):
 * <ul>
 *   <li>FUTURE → PLANNED</li>
 *   <li>PAST: CRITICAL if completion &lt; 70%, WATCH if &lt; 90%, else GOOD;
 *       carry-over &gt; 15% of planned escalates one level, &gt; 25% to CRITICAL</li>
 *   <li>ACTIVE: CRITICAL if completion &lt; 35%, WATCH if &lt; 55%, else GOOD</li>
 *   <li>Ø Velocity: mean of completed SP of last up to 3 PAST sprints</li>
 *   <li>planned=0 → progress 0</li>
 * </ul>
 */
@Component
public class AgileDeliveryCalculator {

    public static final String DATA_SOURCE_MOCK = "INTERNAL_MOCK";
    private static final String EMPTY_WATERFALL = "Für klassische Projekte sind keine Agile-Delivery-Daten vorgesehen.";
    private static final String EMPTY_NO_SPRINTS = "Keine Sprint-Daten für dieses Projekt hinterlegt.";
    private static final int VELOCITY_WINDOW = 3;

    public AgileDeliveryDto assembleEmpty(Project project, String emptyReason) {
        DeliveryMethod method = project.getDeliveryMethod() != null
                ? project.getDeliveryMethod()
                : DeliveryMethod.WATERFALL;
        return new AgileDeliveryDto(
                project.getId(),
                method.name(),
                false,
                emptyReason,
                DATA_SOURCE_MOCK,
                project.getLastDataUpdate(),
                List.of(),
                new AgileSprintChartDto(List.of(), List.of(), List.of(), List.of(), List.of()),
                new AgileDeliveryKpisDto(null, null, 0, null, 0, 0));
    }

    public AgileDeliveryDto assembleWaterfall(Project project) {
        return assembleEmpty(project, EMPTY_WATERFALL);
    }

    public AgileDeliveryDto assembleNoSprints(Project project) {
        return assembleEmpty(project, EMPTY_NO_SPRINTS);
    }

    public AgileDeliveryDto assemble(
            Project project,
            List<ProjectSprint> sprints,
            List<ProjectWorkItem> workItems) {
        DeliveryMethod method = project.getDeliveryMethod() != null
                ? project.getDeliveryMethod()
                : DeliveryMethod.WATERFALL;
        if (method == DeliveryMethod.WATERFALL) {
            return assembleWaterfall(project);
        }
        if (sprints == null || sprints.isEmpty()) {
            return assembleNoSprints(project);
        }

        List<ProjectSprint> ordered = sprints.stream()
                .sorted(Comparator.comparingInt(ProjectSprint::getSequenceNo))
                .toList();

        List<AgileSprintCardDto> cards = ordered.stream().map(this::toCard).toList();
        AgileSprintChartDto chart = toChart(ordered);
        int openBlockers = countOpenBlockers(workItems);
        AgileDeliveryKpisDto kpis = toKpis(ordered, openBlockers);

        return new AgileDeliveryDto(
                project.getId(),
                method.name(),
                true,
                null,
                DATA_SOURCE_MOCK,
                project.getLastDataUpdate(),
                cards,
                chart,
                kpis);
    }

    public int progressPercent(int planned, int completed) {
        if (planned <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round((completed * 100.0) / planned));
    }

    public SprintHealth resolveHealth(ProjectSprint sprint) {
        if (sprint.getLifecycle() == SprintLifecycle.FUTURE) {
            return SprintHealth.PLANNED;
        }
        double ratio = sprint.getStoryPointsPlanned() <= 0
                ? 0.0
                : (double) sprint.getStoryPointsCompleted() / sprint.getStoryPointsPlanned();
        double carryRatio = sprint.getStoryPointsPlanned() <= 0
                ? 0.0
                : (double) sprint.getCarryOverPoints() / sprint.getStoryPointsPlanned();

        SprintHealth health;
        if (sprint.getLifecycle() == SprintLifecycle.ACTIVE) {
            if (ratio < 0.35) {
                health = SprintHealth.CRITICAL;
            } else if (ratio < 0.55) {
                health = SprintHealth.WATCH;
            } else {
                health = SprintHealth.GOOD;
            }
        } else {
            if (ratio < 0.70) {
                health = SprintHealth.CRITICAL;
            } else if (ratio < 0.90) {
                health = SprintHealth.WATCH;
            } else {
                health = SprintHealth.GOOD;
            }
            if (carryRatio > 0.25) {
                health = SprintHealth.CRITICAL;
            } else if (carryRatio > 0.15 && health == SprintHealth.GOOD) {
                health = SprintHealth.WATCH;
            }
        }
        return health;
    }

    public String healthLabel(SprintHealth health) {
        return switch (health) {
            case GOOD -> "Gut";
            case WATCH -> "Achtung";
            case CRITICAL -> "Kritisch";
            case PLANNED -> "Geplant";
        };
    }

    public Double averageVelocity(List<ProjectSprint> orderedSprints) {
        List<Integer> pastCompleted = orderedSprints.stream()
                .filter(s -> s.getLifecycle() == SprintLifecycle.PAST)
                .sorted(Comparator.comparingInt(ProjectSprint::getSequenceNo).reversed())
                .limit(VELOCITY_WINDOW)
                .map(ProjectSprint::getStoryPointsCompleted)
                .toList();
        if (pastCompleted.isEmpty()) {
            return null;
        }
        double sum = pastCompleted.stream().mapToInt(Integer::intValue).sum();
        return Math.round((sum / pastCompleted.size()) * 10.0) / 10.0;
    }

    public int carryOverNextSprint(List<ProjectSprint> orderedSprints) {
        return orderedSprints.stream()
                .filter(s -> s.getLifecycle() == SprintLifecycle.ACTIVE)
                .findFirst()
                .map(ProjectSprint::getCarryOverPoints)
                .orElseGet(() -> orderedSprints.stream()
                        .filter(s -> s.getLifecycle() == SprintLifecycle.PAST)
                        .max(Comparator.comparingInt(ProjectSprint::getSequenceNo))
                        .map(ProjectSprint::getCarryOverPoints)
                        .orElse(0));
    }

    public static String deliveryMethodLabel(DeliveryMethod method) {
        return switch (method) {
            case AGILE -> "Agil";
            case HYBRID -> "Hybrid";
            case WATERFALL -> "Klassisch";
        };
    }

    private AgileSprintCardDto toCard(ProjectSprint sprint) {
        SprintHealth health = resolveHealth(sprint);
        boolean current = sprint.getLifecycle() == SprintLifecycle.ACTIVE;
        boolean future = sprint.getLifecycle() == SprintLifecycle.FUTURE;
        return new AgileSprintCardDto(
                sprint.getId(),
                sprint.getName(),
                sprint.getSequenceNo(),
                sprint.getLifecycle().name(),
                current,
                future,
                health.name(),
                healthLabel(health),
                progressPercent(sprint.getStoryPointsPlanned(), sprint.getStoryPointsCompleted()),
                sprint.getStoryPointsPlanned(),
                sprint.getStoryPointsCompleted(),
                sprint.getCarryOverPoints(),
                sprint.getStartDate(),
                sprint.getEndDate());
    }

    private AgileSprintChartDto toChart(List<ProjectSprint> ordered) {
        List<String> labels = new ArrayList<>();
        List<Integer> planned = new ArrayList<>();
        List<Integer> completed = new ArrayList<>();
        List<Integer> velocityTrend = new ArrayList<>();
        List<Boolean> futureFlags = new ArrayList<>();
        for (ProjectSprint sprint : ordered) {
            labels.add(sprint.getName());
            planned.add(sprint.getStoryPointsPlanned());
            completed.add(sprint.getStoryPointsCompleted());
            velocityTrend.add(sprint.getLifecycle() == SprintLifecycle.FUTURE
                    ? null
                    : sprint.getStoryPointsCompleted());
            futureFlags.add(sprint.getLifecycle() == SprintLifecycle.FUTURE);
        }
        return new AgileSprintChartDto(labels, planned, completed, velocityTrend, futureFlags);
    }

    private AgileDeliveryKpisDto toKpis(List<ProjectSprint> ordered, int openBlockers) {
        ProjectSprint healthSprint = ordered.stream()
                .filter(s -> s.getLifecycle() == SprintLifecycle.ACTIVE)
                .findFirst()
                .orElseGet(() -> ordered.stream()
                        .filter(s -> s.getLifecycle() == SprintLifecycle.PAST)
                        .max(Comparator.comparingInt(ProjectSprint::getSequenceNo))
                        .orElse(ordered.get(ordered.size() - 1)));
        SprintHealth health = resolveHealth(healthSprint);
        int totalPlanned = ordered.stream().mapToInt(ProjectSprint::getStoryPointsPlanned).sum();
        return new AgileDeliveryKpisDto(
                health.name(),
                healthLabel(health),
                totalPlanned,
                averageVelocity(ordered),
                carryOverNextSprint(ordered),
                openBlockers);
    }

    private static int countOpenBlockers(List<ProjectWorkItem> workItems) {
        if (workItems == null) {
            return 0;
        }
        return (int) workItems.stream()
                .filter(ProjectWorkItem::isBlocker)
                .filter(item -> item.getStatus() != WorkItemStatus.DONE)
                .count();
    }
}
