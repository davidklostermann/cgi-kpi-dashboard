package com.cgi.kpi.dashboard.kpi.insights;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.domain.model.Project;
import com.cgi.kpi.dashboard.domain.model.ProjectBudget;
import com.cgi.kpi.dashboard.kpi.dto.ProjectInsightsDto.ProjectInsightItemDto;

class ProjectInsightEngineTest {

    private final ProjectInsightEngine engine = new ProjectInsightEngine();

    @Test
    void detectsForecastEndShifted() {
        Project project = new Project();
        project.setId(UUID.fromString("a0000000-0000-4000-8000-000000000001"));
        project.setStatus("AT_RISK");
        project.setProgressPercent(40);
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 6, 30));
        project.setPredictedEndDate(LocalDate.of(2026, 8, 15));

        List<ProjectInsightItemDto> insights = engine.evaluate(project, null, List.of(), List.of(), List.of());

        assertTrue(insights.stream().anyMatch(item -> "FORECAST_END_SHIFTED".equals(item.code())));
        assertTrue(insights.stream().allMatch(item -> "deterministisch".equals(item.type())));
    }

    @Test
    void detectsBudgetAheadOfProgress() {
        Project project = new Project();
        project.setId(UUID.fromString("a0000000-0000-4000-8000-000000000001"));
        project.setStatus("CRITICAL");
        project.setProgressPercent(20);
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setPlannedEndDate(LocalDate.of(2026, 12, 31));

        ProjectBudget budget = new ProjectBudget();
        budget.setPlannedBudget(new BigDecimal("100000"));
        budget.setActualBudget(new BigDecimal("50000"));

        List<ProjectInsightItemDto> insights = engine.evaluate(project, budget, List.of(), List.of(), List.of());

        assertTrue(insights.stream().anyMatch(item -> "BUDGET_AHEAD_OF_PROGRESS".equals(item.code())));
    }
}
