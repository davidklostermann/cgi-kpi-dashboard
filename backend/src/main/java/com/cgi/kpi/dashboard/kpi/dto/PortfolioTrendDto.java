package com.cgi.kpi.dashboard.kpi.dto;

import java.util.List;

/**
 * Portfolio trend and status visualization data (FR-3 / Story 5.4).
 */
public record PortfolioTrendDto(
        List<PortfolioTrendPointDto> points,
        PortfolioStatusDistributionDto statusDistribution,
        boolean empty) {

    public static PortfolioTrendDto emptyTrend() {
        return new PortfolioTrendDto(
                List.of(),
                new PortfolioStatusDistributionDto(0, 0, 0, 0),
                true);
    }
}
