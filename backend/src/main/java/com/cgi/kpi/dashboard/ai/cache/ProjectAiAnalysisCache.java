package com.cgi.kpi.dashboard.ai.cache;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;

/**
 * In-memory cache for project AI analysis responses (AD-18).
 * Keys are scoped by workspace, project, facts timestamp, and provider config version.
 */
@Component
public class ProjectAiAnalysisCache {

    private final Map<String, ProjectAiAnalysisResponseDto> entries = new ConcurrentHashMap<>();

    public static String buildKey(
            UUID userId,
            UUID workspaceId,
            UUID projectId,
            Instant factsAsOf,
            long providerConfigVersion) {
        return userId + "|" + workspaceId + "|" + projectId + "|" + factsAsOf + "|" + providerConfigVersion;
    }

    public ProjectAiAnalysisResponseDto get(String key) {
        return entries.get(key);
    }

    public void put(String key, ProjectAiAnalysisResponseDto value) {
        entries.put(key, value);
    }

    /** Clears all entries; called when AI provider config changes (Epic 13.4). */
    public void invalidateAll() {
        entries.clear();
    }
}
