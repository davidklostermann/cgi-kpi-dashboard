package com.cgi.kpi.dashboard.ai.cache;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;

/**
 * In-memory cache for portfolio AI analysis responses (AD-18).
 * Keys are scoped by user, workspace, filter criteria, and provider config version.
 */
@Component
public class PortfolioAiAnalysisCache {

    private final Map<String, PortfolioTrendAnalysisResponseDto> entries = new ConcurrentHashMap<>();

    public static String buildKey(
            UUID userId,
            UUID workspaceId,
            PortfolioFilterCriteria criteria,
            long providerConfigVersion) {
        return userId + "|" + workspaceId + "|" + criteriaKey(criteria) + "|" + providerConfigVersion;
    }

    public PortfolioTrendAnalysisResponseDto get(String key) {
        return entries.get(key);
    }

    public void put(String key, PortfolioTrendAnalysisResponseDto value) {
        entries.put(key, value);
    }

    /** Clears all entries; called when AI provider config changes (Epic 13.4). */
    public void invalidateAll() {
        entries.clear();
    }

    private static String criteriaKey(PortfolioFilterCriteria criteria) {
        PortfolioFilterCriteria safe = criteria == null ? PortfolioFilterCriteria.empty() : criteria;
        List<String> statuses = safe.statuses() == null ? List.of() : safe.statuses();
        return String.join(
                "|",
                blankToEmpty(safe.customer()),
                blankToEmpty(safe.projectLead()),
                String.join(",", statuses),
                blankToEmpty(safe.phase()),
                safe.lifecycle() == null ? PortfolioFilterCriteria.LifecycleFilter.ACTIVE.name() : safe.lifecycle().name(),
                blankToEmpty(safe.reportMonth()),
                blankToEmpty(safe.riskSeverity()));
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
