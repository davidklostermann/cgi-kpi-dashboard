package com.cgi.kpi.dashboard.ai.client;

/**
 * Low-level Gemini HTTP transport — testable without live API calls.
 */
public interface GeminiApiTransport {

    /**
     * Sends a prompt and returns the model text (expected to be JSON).
     *
     * @throws GeminiTransportException on HTTP/timeout/empty response
     */
    String generateJson(String prompt);
}
