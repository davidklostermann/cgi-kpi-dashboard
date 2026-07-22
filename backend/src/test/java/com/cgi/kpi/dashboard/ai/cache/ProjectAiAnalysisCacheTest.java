package com.cgi.kpi.dashboard.ai.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.dto.ProjectAiAnalysisResponseDto;

class ProjectAiAnalysisCacheTest {

    private static final UUID WORKSPACE_A = UUID.fromString("10000000-0000-4000-8000-000000000001");
    private static final UUID WORKSPACE_B = UUID.fromString("20000000-0000-4000-8000-000000000002");
    private static final UUID PROJECT_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
    private static final Instant FACTS_AS_OF = Instant.parse("2026-07-01T08:00:00Z");

    @Test
    void buildKeyIncludesAllScopedSegments() {
        String key = ProjectAiAnalysisCache.buildKey(WORKSPACE_A, PROJECT_ID, FACTS_AS_OF, 3L);

        assertEquals(WORKSPACE_A + "|" + PROJECT_ID + "|" + FACTS_AS_OF + "|3", key);
    }

    @Test
    void differentWorkspacesProduceDifferentKeys() {
        String keyA = ProjectAiAnalysisCache.buildKey(WORKSPACE_A, PROJECT_ID, FACTS_AS_OF, 0L);
        String keyB = ProjectAiAnalysisCache.buildKey(WORKSPACE_B, PROJECT_ID, FACTS_AS_OF, 0L);

        assertEquals(WORKSPACE_A + "|" + PROJECT_ID + "|" + FACTS_AS_OF + "|0", keyA);
        assertEquals(WORKSPACE_B + "|" + PROJECT_ID + "|" + FACTS_AS_OF + "|0", keyB);
    }

    @Test
    void invalidateAllClearsEntries() {
        ProjectAiAnalysisCache cache = new ProjectAiAnalysisCache();
        String key = ProjectAiAnalysisCache.buildKey(WORKSPACE_A, PROJECT_ID, FACTS_AS_OF, 0L);
        cache.put(key, sampleResponse());

        cache.invalidateAll();

        assertNull(cache.get(key));
    }

    private static ProjectAiAnalysisResponseDto sampleResponse() {
        return new ProjectAiAnalysisResponseDto(
                PROJECT_ID,
                FACTS_AS_OF,
                Instant.parse("2026-07-01T09:00:00Z"),
                "OK",
                java.util.List.of(),
                "Summary",
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of(),
                true,
                "Disclaimer");
    }
}
