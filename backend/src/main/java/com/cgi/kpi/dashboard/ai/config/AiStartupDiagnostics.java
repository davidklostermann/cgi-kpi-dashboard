package com.cgi.kpi.dashboard.ai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Logs AI configuration at startup without exposing secrets.
 */
@Component
public class AiStartupDiagnostics {

    private static final Logger log = LoggerFactory.getLogger(AiStartupDiagnostics.class);

    private final AiProperties aiProperties;

    public AiStartupDiagnostics(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logConfiguration() {
        log.info(
                "AI configuration: enabled={}, provider={}, model={}, apiKeyPresent={}",
                aiProperties.isEnabled(),
                aiProperties.getProvider(),
                aiProperties.getConfiguredModel(),
                aiProperties.hasApiKey());
        if (aiProperties.isGeminiProvider()) {
            if (!aiProperties.hasApiKey()) {
                log.warn("AI provider is gemini but no API key is configured (GEMINI_API_KEY).");
            }
            if (!aiProperties.hasGeminiModel()) {
                log.warn(
                        "AI provider is gemini but APP_AI_MODEL is missing or still set to local-mock. "
                                + "Set APP_AI_MODEL to a valid Gemini model name.");
            }
        }
    }
}
