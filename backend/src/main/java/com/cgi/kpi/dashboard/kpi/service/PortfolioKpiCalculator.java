package com.cgi.kpi.dashboard.kpi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiProjectInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiRiskInput;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioKpiSummaryDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioStatusDistributionDto;

/**
 * Deterministic portfolio KPI calculation (AD-3). All KPI numbers originate here.
 */
@Component
public class PortfolioKpiCalculator {

    private static final String STATUS_ON_TRACK = "ON_TRACK";
    private static final String STATUS_AT_RISK = "AT_RISK";
    private static final String STATUS_CRITICAL = "CRITICAL";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String RISK_STATUS_OPEN = "OPEN";
    private static final String RISK_SEVERITY_CRITICAL = "CRITICAL";

    private static final String RISK_SEVERITY_HIGH = "HIGH";

    public PortfolioKpiSummaryDto calculate(
            List<PortfolioKpiProjectInput> projects,
            List<PortfolioKpiRiskInput> risks) {
        if (projects == null || projects.isEmpty()) {
            return PortfolioKpiSummaryDto.emptyPortfolio();
        }

        int onTrack = 0;
        int atRisk = 0;
        int critical = 0;
        int completed = 0;
        int onScheduleCount = 0;
        int progressSum = 0;
        BigDecimal budgetDeviationSum = BigDecimal.ZERO;
        int budgetDeviationCount = 0;

        for (PortfolioKpiProjectInput project : projects) {
            progressSum += project.progressPercent();

            switch (project.status()) {
                case STATUS_ON_TRACK -> onTrack++;
                case STATUS_AT_RISK -> atRisk++;
                case STATUS_CRITICAL -> critical++;
                case STATUS_COMPLETED -> completed++;
                default -> {
                }
            }

            if (isOnSchedule(project.scheduleDeviationDays())) {
                onScheduleCount++;
            }

            if (project.plannedBudget() != null
                    && project.actualBudget() != null
                    && project.plannedBudget().signum() > 0) {
                BigDecimal deviation = project.actualBudget()
                        .subtract(project.plannedBudget())
                        .divide(project.plannedBudget(), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                budgetDeviationSum = budgetDeviationSum.add(deviation);
                budgetDeviationCount++;
            }
        }

        double averageProgress = (double) progressSum / projects.size();
        double budgetDeviation = budgetDeviationCount == 0
                ? 0.0
                : budgetDeviationSum.divide(BigDecimal.valueOf(budgetDeviationCount), 2, RoundingMode.HALF_UP)
                        .doubleValue();
        double scheduleCompliance = (double) onScheduleCount / projects.size() * 100.0;

        int criticalRiskCount = risks == null ? 0 : (int) risks.stream()
                .filter(this::isCriticalRisk)
                .count();

        return new PortfolioKpiSummaryDto(
                onTrack + atRisk + critical,
                roundOneDecimal(averageProgress),
                roundOneDecimal(budgetDeviation),
                roundOneDecimal(scheduleCompliance),
                criticalRiskCount,
                new PortfolioStatusDistributionDto(onTrack, atRisk, critical, completed),
                false);
    }

    private static boolean isOnSchedule(Integer scheduleDeviationDays) {
        return scheduleDeviationDays == null || scheduleDeviationDays <= 0;
    }

    private static double roundOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isCriticalRisk(PortfolioKpiRiskInput risk) {
        if (!RISK_STATUS_OPEN.equals(risk.status())) {
            return false;
        }
        return RISK_SEVERITY_CRITICAL.equals(risk.severity()) || RISK_SEVERITY_HIGH.equals(risk.severity());
    }
}
