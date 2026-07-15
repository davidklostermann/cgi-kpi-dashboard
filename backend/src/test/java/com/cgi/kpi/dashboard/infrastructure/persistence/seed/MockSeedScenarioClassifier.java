package com.cgi.kpi.dashboard.infrastructure.persistence.seed;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.domain.model.Risk;

public final class MockSeedScenarioClassifier {

    private MockSeedScenarioClassifier() {
    }

    public static Set<MockPilotScenario> classify(
            Project project,
            ProjectBudget budget,
            List<Risk> risks) {
        EnumSet<MockPilotScenario> scenarios = EnumSet.noneOf(MockPilotScenario.class);

        if (isOnPlan(project, budget, risks)) {
            scenarios.add(MockPilotScenario.ON_PLAN);
        }
        if (hasScheduleDelay(project)) {
            scenarios.add(MockPilotScenario.SCHEDULE_DELAY);
        }
        if (hasBudgetOverrun(budget)) {
            scenarios.add(MockPilotScenario.BUDGET_OVERRUN);
        }
        if (hasOpenRisks(risks)) {
            scenarios.add(MockPilotScenario.OPEN_RISKS);
        }
        if (hasConflictingSignals(project, budget, risks)) {
            scenarios.add(MockPilotScenario.CONFLICTING_SIGNALS);
        }
        if (isCompleted(project)) {
            scenarios.add(MockPilotScenario.COMPLETED);
        }

        return scenarios;
    }

    private static boolean isOnPlan(Project project, ProjectBudget budget, List<Risk> risks) {
        if (!"ON_TRACK".equals(project.getStatus())) {
            return false;
        }
        if (hasScheduleDelay(project) || hasBudgetOverrun(budget)) {
            return false;
        }
        return risks.stream()
                .noneMatch(risk -> "OPEN".equals(risk.getStatus())
                        && ("HIGH".equals(risk.getSeverity()) || "CRITICAL".equals(risk.getSeverity())));
    }

    private static boolean hasScheduleDelay(Project project) {
        Integer deviation = project.getScheduleDeviationDays();
        return deviation != null && deviation > 0;
    }

    private static boolean hasBudgetOverrun(ProjectBudget budget) {
        return budget.getActualBudget().compareTo(budget.getPlannedBudget()) > 0;
    }

    private static boolean hasOpenRisks(List<Risk> risks) {
        return risks.stream().anyMatch(risk -> "OPEN".equals(risk.getStatus()));
    }

    private static boolean hasConflictingSignals(Project project, ProjectBudget budget, List<Risk> risks) {
        if (!"ON_TRACK".equals(project.getStatus())) {
            return false;
        }
        if (hasScheduleDelay(project) || hasBudgetOverrun(budget)) {
            return true;
        }
        return risks.stream()
                .anyMatch(risk -> "OPEN".equals(risk.getStatus())
                        && ("HIGH".equals(risk.getSeverity()) || "CRITICAL".equals(risk.getSeverity())));
    }

    private static boolean isCompleted(Project project) {
        return "COMPLETED".equals(project.getStatus()) && project.getActualEndDate() != null;
    }
}
