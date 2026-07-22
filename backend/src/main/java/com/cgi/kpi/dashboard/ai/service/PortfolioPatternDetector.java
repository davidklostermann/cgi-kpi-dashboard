package com.cgi.kpi.dashboard.ai.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.EvidenceDto;
import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto.PortfolioInsightDto;
import com.cgi.kpi.dashboard.kpi.dto.ProjectReportTrendDto;

/**
 * Deterministic portfolio pattern candidates from report-stand pairs.
 * Active types: DETERIORATING_TREND, REPORTING_PATTERN.
 */
@Component
public class PortfolioPatternDetector {

    public List<PortfolioInsightDto> detect(List<ProjectReportTrendDto> trends) {
        if (trends == null || trends.size() < 2) {
            return List.of();
        }
        List<PortfolioInsightDto> insights = new ArrayList<>();
        detectDeterioratingTrend(trends).ifPresent(insights::add);
        detectReportingPattern(trends).ifPresent(insights::add);
        return List.copyOf(insights);
    }

    private static java.util.Optional<PortfolioInsightDto> detectDeterioratingTrend(
            List<ProjectReportTrendDto> trends) {
        List<ProjectReportTrendDto> deteriorating = trends.stream()
                .filter(PortfolioPatternDetector::isDeteriorating)
                .sorted(Comparator.comparingInt(PortfolioPatternDetector::deteriorationScore).reversed())
                .toList();
        if (deteriorating.size() < 2) {
            return java.util.Optional.empty();
        }
        List<ProjectReportTrendDto> affected = deteriorating.stream().limit(5).toList();
        List<UUID> ids = affected.stream().map(ProjectReportTrendDto::projectId).toList();
        List<String> names = affected.stream().map(ProjectReportTrendDto::projectName).toList();
        List<EvidenceDto> evidence = new ArrayList<>();
        for (ProjectReportTrendDto trend : affected) {
            evidence.add(new EvidenceDto(
                    trend.projectName() + " Terminabweichung",
                    formatDeviation(trend.previousScheduleDeviationDays())
                            + " → "
                            + formatDeviation(trend.currentScheduleDeviationDays()),
                    trend.projectId(),
                    trend.currentDate(),
                    "snapshot.scheduleDeviationDays"));
            evidence.add(new EvidenceDto(
                    trend.projectName() + " Status",
                    formatStatus(trend.previousStatus()) + " → " + formatStatus(trend.currentStatus()),
                    trend.projectId(),
                    trend.currentDate(),
                    "snapshot.status"));
        }
        return java.util.Optional.of(new PortfolioInsightDto(
                "deteriorating-trend-" + ids.get(0),
                "DETERIORATING_TREND",
                "Mehrere Projekte verschlechtern sich über zwei Berichtsstände",
                "In "
                        + affected.size()
                        + " Projekten steigt die Terminabweichung und/oder der Ampelstatus verschlechtert sich "
                        + "über die jeweiligen Berichtsstände.",
                "Portfolio-Steuerung muss gebündelt priorisieren, statt Einzelprojekte isoliert zu behandeln.",
                "Gemeinsames Steering zu Terminabweichung und Statusverschlechterung ansetzen.",
                ids,
                names,
                List.copyOf(evidence),
                "HIGH",
                "COMPLETE",
                Instant.now()));
    }

    private static java.util.Optional<PortfolioInsightDto> detectReportingPattern(
            List<ProjectReportTrendDto> trends) {
        List<ProjectReportTrendDto> risingRisks = trends.stream()
                .filter(trend -> trend.currentOpenRiskCount() > trend.previousOpenRiskCount())
                .sorted(Comparator.comparingInt(
                        (ProjectReportTrendDto trend) ->
                                trend.currentOpenRiskCount() - trend.previousOpenRiskCount())
                        .reversed())
                .toList();
        if (risingRisks.size() < 2) {
            return java.util.Optional.empty();
        }
        List<ProjectReportTrendDto> affected = risingRisks.stream().limit(5).toList();
        List<UUID> ids = affected.stream().map(ProjectReportTrendDto::projectId).toList();
        List<String> names = affected.stream().map(ProjectReportTrendDto::projectName).toList();
        List<EvidenceDto> evidence = new ArrayList<>();
        for (ProjectReportTrendDto trend : affected) {
            evidence.add(new EvidenceDto(
                    trend.projectName() + " offene Risiken",
                    trend.previousOpenRiskCount() + " → " + trend.currentOpenRiskCount(),
                    trend.projectId(),
                    trend.currentDate(),
                    "snapshot.openRiskCount"));
            evidence.add(new EvidenceDto(
                    trend.projectName() + " Fortschritt",
                    formatPercent(trend.previousProgressPercent())
                            + " → "
                            + formatPercent(trend.currentProgressPercent()),
                    trend.projectId(),
                    trend.currentDate(),
                    "snapshot.progressPercent"));
        }
        return java.util.Optional.of(new PortfolioInsightDto(
                "reporting-pattern-" + ids.get(0),
                "REPORTING_PATTERN",
                "Gleichgerichteter Anstieg offener Risiken über mehrere Projekte",
                "Mindestens "
                        + affected.size()
                        + " Projekte melden zwischen zwei Berichtsständen mehr offene Risiken — "
                        + "ein wiederkehrendes Berichtsmuster im Portfolio.",
                "Risikoaggregation und Berichtsqualität sollten portfolio-weit geprüft werden.",
                "Portfolio-Risikoreview mit den betroffenen Projektleitungen terminieren.",
                ids,
                names,
                List.copyOf(evidence),
                "MEDIUM",
                "PARTIAL",
                Instant.now()));
    }

    private static boolean isDeteriorating(ProjectReportTrendDto trend) {
        boolean scheduleWorse = isScheduleWorse(
                trend.previousScheduleDeviationDays(), trend.currentScheduleDeviationDays());
        boolean statusWorse = isStatusWorse(trend.previousStatus(), trend.currentStatus());
        return scheduleWorse || statusWorse;
    }

    private static int deteriorationScore(ProjectReportTrendDto trend) {
        int score = 0;
        if (trend.previousScheduleDeviationDays() != null && trend.currentScheduleDeviationDays() != null) {
            score += trend.currentScheduleDeviationDays() - trend.previousScheduleDeviationDays();
        }
        int prevRank = statusRank(trend.previousStatus());
        int currRank = statusRank(trend.currentStatus());
        if (prevRank > 0 && currRank > 0) {
            score += 10 * (currRank - prevRank);
        }
        return score;
    }

    private static boolean isScheduleWorse(Integer previous, Integer current) {
        if (previous == null || current == null) {
            return false;
        }
        return current > previous;
    }

    private static boolean isStatusWorse(String previous, String current) {
        int prevRank = statusRank(previous);
        int currRank = statusRank(current);
        if (prevRank == 0 || currRank == 0) {
            return false;
        }
        return currRank > prevRank;
    }

    private static int statusRank(String status) {
        if (status == null || status.isBlank()) {
            return 0;
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "COMPLETED", "ON_TRACK" -> 1;
            case "AT_RISK" -> 2;
            case "CRITICAL" -> 3;
            default -> 0;
        };
    }

    private static String formatDeviation(Integer days) {
        if (days == null) {
            return "n/a";
        }
        return days + " Tage";
    }

    private static String formatStatus(String status) {
        return status == null || status.isBlank() ? "n/a" : status;
    }

    private static String formatPercent(Integer value) {
        if (value == null) {
            return "n/a";
        }
        return value + " %";
    }
}
