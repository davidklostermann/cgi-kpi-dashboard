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
                "AI configuration: implementationProvider={}, runtimeConfigSource=database",
                aiProperties.getProvider());
    }
}
