package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cgi.kpi.dashboard.domain.model.DeliveryMethod;
import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectSprint;
import com.cgi.kpi.dashboard.domain.model.SprintLifecycle;
import com.cgi.kpi.dashboard.domain.model.WorkItemStatus;

@SpringBootTest
@ActiveProfiles("test")
class AgileDeliverySeedIntegrationTest {

    private static final UUID AGILE_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final UUID HYBRID_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000002");
    private static final UUID WATERFALL_PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final UUID AGILE_SPRINT_S1 = UUID.fromString("a1700000-0000-4000-8000-000000000001");
    private static final UUID AGILE_WORK_ITEM = UUID.fromString("b1700000-0000-4000-8000-000000000005");

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectSprintRepository projectSprintRepository;

    @Autowired
    private ProjectWorkItemRepository projectWorkItemRepository;

    @Test
    void seedAssignsDeliveryMethodsAndSprintCoverage() {
        Project agile = projectRepository.findById(AGILE_PROJECT_ID).orElseThrow();
        Project hybrid = projectRepository.findById(HYBRID_PROJECT_ID).orElseThrow();
        Project waterfall = projectRepository.findById(WATERFALL_PROJECT_ID).orElseThrow();

        assertEquals(DeliveryMethod.AGILE, agile.getDeliveryMethod());
        assertEquals(DeliveryMethod.HYBRID, hybrid.getDeliveryMethod());
        assertEquals(DeliveryMethod.WATERFALL, waterfall.getDeliveryMethod());

        List<ProjectSprint> agileSprints = projectSprintRepository.findByProject_IdOrderBySequenceNoAsc(AGILE_PROJECT_ID);
        List<ProjectSprint> hybridSprints = projectSprintRepository.findByProject_IdOrderBySequenceNoAsc(HYBRID_PROJECT_ID);

        assertEquals(5, agileSprints.size());
        assertEquals(3, hybridSprints.size());
        assertTrue(projectSprintRepository.findById(AGILE_SPRINT_S1).isPresent());
        assertTrue(projectWorkItemRepository.findById(AGILE_WORK_ITEM).isPresent());

        assertLifecycles(agileSprints);
        assertLifecycles(hybridSprints);

        assertTrue(agileSprints.stream().anyMatch(s -> s.getCarryOverPoints() > 0));
        assertTrue(projectWorkItemRepository.countByProject_IdAndBlockerTrueAndStatusNot(
                AGILE_PROJECT_ID, WorkItemStatus.DONE) >= 1);
        assertTrue(projectWorkItemRepository.countByProject_IdAndBlockerTrueAndStatusNot(
                HYBRID_PROJECT_ID, WorkItemStatus.DONE) >= 1);

        long agileCount = projectRepository.findAll().stream()
                .filter(p -> p.getDeliveryMethod() == DeliveryMethod.AGILE)
                .count();
        long hybridCount = projectRepository.findAll().stream()
                .filter(p -> p.getDeliveryMethod() == DeliveryMethod.HYBRID)
                .count();
        long waterfallCount = projectRepository.findAll().stream()
                .filter(p -> p.getDeliveryMethod() == DeliveryMethod.WATERFALL)
                .count();
        assertEquals(14, agileCount);
        assertEquals(2, hybridCount);
        assertEquals(4, waterfallCount);

        long waterfallWithoutSprints = projectRepository.findAll().stream()
                .filter(p -> p.getDeliveryMethod() == DeliveryMethod.WATERFALL)
                .filter(p -> projectSprintRepository.countByProject_Id(p.getId()) == 0)
                .filter(p -> projectWorkItemRepository.countByProject_Id(p.getId()) == 0)
                .count();
        assertEquals(4, waterfallWithoutSprints);

        long agileHybridWithoutSprints = projectRepository.findAll().stream()
                .filter(p -> p.getDeliveryMethod() != DeliveryMethod.WATERFALL)
                .filter(p -> projectSprintRepository.countByProject_Id(p.getId()) == 0)
                .count();
        assertEquals(0, agileHybridWithoutSprints);
    }

    @Test
    void defaultDeliveryMethodRatioIsMostlyAgile() {
        long agileCount = projectRepository.findAll().stream()
                .filter(p -> p.getDeliveryMethod() == DeliveryMethod.AGILE)
                .count();
        long total = projectRepository.count();
        assertEquals(20, total);
        assertEquals(14, agileCount);
        assertTrue(agileCount >= Math.round(total * 0.7));
    }

    private static void assertLifecycles(List<ProjectSprint> sprints) {
        Set<SprintLifecycle> lifecycles = sprints.stream()
                .map(ProjectSprint::getLifecycle)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(SprintLifecycle.class)));
        assertTrue(lifecycles.containsAll(
                EnumSet.of(SprintLifecycle.PAST, SprintLifecycle.ACTIVE, SprintLifecycle.FUTURE)));
    }
}
