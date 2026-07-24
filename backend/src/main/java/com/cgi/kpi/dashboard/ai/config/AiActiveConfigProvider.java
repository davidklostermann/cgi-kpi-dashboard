package com.cgi.kpi.dashboard.ai.config;

import java.util.Optional;

/**
 * Provider for the currently active AI configuration.
 * Resolves between database-backed config and environment-backed properties.
 */
public interface AiActiveConfigProvider {

    /**
     * Returns the active API key for the given provider.
     */
    String getActiveApiKey(String provider);

    /**
     * Returns the active model for the given provider.
     */
    String getActiveModel(String provider);

    /**
     * Returns the current configuration version (for cache invalidation).
     */
    long getCurrentVersion();

    /**
     * Returns whether the AI provider is enabled (either in DB or Properties).
     */
    boolean isEnabled(String provider);
}
