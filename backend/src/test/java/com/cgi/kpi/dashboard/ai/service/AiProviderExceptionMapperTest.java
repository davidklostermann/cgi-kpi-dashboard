package com.cgi.kpi.dashboard.ai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import com.cgi.kpi.dashboard.api.error.ApiException;

class AiProviderExceptionMapperTest {

    @Test
    void mapsAccessTokenTypeUnsupportedToProviderError() {
        ApiException ex = AiProviderExceptionMapper.toApiException(
                new GeminiTransportException("Gemini HTTP 401", 401, "ACCESS_TOKEN_TYPE_UNSUPPORTED"),
                "Unavailable");

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void maps403ToProviderError() {
        ApiException ex = AiProviderExceptionMapper.toApiException(
                new GeminiTransportException("Gemini HTTP 403", 403), "Unavailable");

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
    }

    @Test
    void maps404ToProviderError() {
        ApiException ex = AiProviderExceptionMapper.toApiException(
                new GeminiTransportException("Gemini HTTP 404", 404), "Unavailable");

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
    }

    @Test
    void maps429ToProviderError() {
        ApiException ex = AiProviderExceptionMapper.toApiException(
                new GeminiTransportException("Gemini HTTP 429", 429), "Unavailable");

        assertEquals("AI_PROVIDER_ERROR", ex.getCode());
    }

    @Test
    void maps503ToUnavailable() {
        ApiException ex = AiProviderExceptionMapper.toApiException(
                new GeminiTransportException("Gemini HTTP 503", 503), "Unavailable");

        assertEquals("AI_UNAVAILABLE", ex.getCode());
        assertEquals("Unavailable", ex.getMessage());
    }
}
