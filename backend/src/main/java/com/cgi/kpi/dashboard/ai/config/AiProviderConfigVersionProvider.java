package com.cgi.kpi.dashboard.ai.config;

import org.springframework.stereotype.Component;

/**
 * Supplies the active AI provider configuration version for cache keying (AD-18).
 * Epic 13.4 will increment version and call {@link com.cgi.kpi.dashboard.ai.cache.ProjectAiAnalysisCache#invalidateAll()}.
 */
@Component
public class AiProviderConfigVersionProvider {

    private final AiProperties aiProperties;

    public AiProviderConfigVersionProvider(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    public long currentVersion() {
        return aiProperties.getProviderConfigVersion();
    }
}
