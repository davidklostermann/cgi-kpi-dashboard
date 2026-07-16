package com.cgi.kpi.dashboard.kpi.dto;

import java.util.List;

/**
 * Portfolio management table payload (FR-2 / Story 5.3).
 */
public record PortfolioTableDto(
        List<PortfolioTableRowDto> projects,
        boolean empty) {

    public static PortfolioTableDto emptyTable() {
        return new PortfolioTableDto(List.of(), true);
    }
}
