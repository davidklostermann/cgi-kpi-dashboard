package com.cgi.kpi.dashboard.ai.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.cgi.kpi.dashboard.ai.dto.PortfolioTrendAnalysisResponseDto;
import com.cgi.kpi.dashboard.kpi.dto.PortfolioFilterCriteria;

class PortfolioAiAnalysisCacheTest {

    private static final UUID USER_A = UUID.fromString("30000000-0000-4000-8000-000000000003");
    private static final UUID USER_B = UUID.fromString("40000000-0000-4000-8000-000000000004");
    private static final UUID WORKSPACE_A = UUID.fromString("10000000-0000-4000-8000-000000000001");

    @Test
    void buildKeyIncludesUserWorkspaceCriteriaAndVersion() {
        PortfolioFilterCriteria criteria = new PortfolioFilterCriteria(
                "Acme", null, List.of("ON_TRACK"), null, PortfolioFilterCriteria.LifecycleFilter.ACTIVE, null, null);

        String key = PortfolioAiAnalysisCache.buildKey(USER_A, WORKSPACE_A, criteria, 3L);

        assertEquals(USER_A + "|" + WORKSPACE_A + "|Acme||ON_TRACK||ACTIVE|||3", key);
    }

    @Test
    void differentUsersProduceDifferentKeys() {
        PortfolioFilterCriteria criteria = PortfolioFilterCriteria.empty();
        String keyA = PortfolioAiAnalysisCache.buildKey(USER_A, WORKSPACE_A, criteria, 0L);
        String keyB = PortfolioAiAnalysisCache.buildKey(USER_B, WORKSPACE_A, criteria, 0L);

        assertEquals(USER_A + "|" + WORKSPACE_A + "|||||ACTIVE|||0", keyA);
        assertEquals(USER_B + "|" + WORKSPACE_A + "|||||ACTIVE|||0", keyB);
        assertNotEquals(keyA, keyB);
    }

    @Test
    void invalidateAllClearsEntries() {
        PortfolioAiAnalysisCache cache = new PortfolioAiAnalysisCache();
        String key = PortfolioAiAnalysisCache.buildKey(USER_A, WORKSPACE_A, PortfolioFilterCriteria.empty(), 0L);
        cache.put(key, sampleResponse());

        cache.invalidateAll();

        assertNull(cache.get(key));
    }

    private static PortfolioTrendAnalysisResponseDto sampleResponse() {
        return new PortfolioTrendAnalysisResponseDto(List.of(), true, "Disclaimer", Instant.now());
    }
}
