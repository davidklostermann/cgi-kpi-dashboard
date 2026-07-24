package com.cgi.kpi.dashboard.ai.config;

import org.springframework.stereotype.Component;

/**
 * Supplies the active AI provider configuration version for cache keying (AD-18).
 */
@Component
public class AiProviderConfigVersionProvider {

    private final AiActiveConfigProvider configProvider;

    public AiProviderConfigVersionProvider(AiActiveConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public long currentVersion() {
        return configProvider.getCurrentVersion();
    }
}
