package com.cgi.kpi.dashboard.ai.service;

import org.springframework.http.HttpStatus;

import com.cgi.kpi.dashboard.ai.client.GeminiTransportException;
import com.cgi.kpi.dashboard.api.error.ApiException;

/**
 * Maps Gemini transport failures to stable API error codes for the frontend.
 */
public final class AiProviderExceptionMapper {

    private AiProviderExceptionMapper() {
    }

    public static ApiException toApiException(GeminiTransportException exception, String unavailableMessage) {
        if ("ACCESS_TOKEN_TYPE_UNSUPPORTED".equals(exception.getProviderReason())) {
            return new ApiException(
                    "AI_PROVIDER_ERROR",
                    "Gemini Auth-Key (AQ.-Format) wird von Google derzeit abgelehnt "
                            + "(ACCESS_TOKEN_TYPE_UNSUPPORTED). "
                            + "In AI Studio einen Standard-Key (AIza…) erzeugen oder Google-Support kontaktieren.",
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
        return switch (exception.getHttpStatus()) {
            case 401, 403 -> new ApiException(
                    "AI_PROVIDER_ERROR",
                    "Gemini-Authentifizierung fehlgeschlagen. API-Key und Berechtigungen prüfen.",
                    HttpStatus.SERVICE_UNAVAILABLE);
            case 404 -> new ApiException(
                    "AI_PROVIDER_ERROR",
                    "Gemini-Modell nicht gefunden. APP_AI_MODEL prüfen.",
                    HttpStatus.SERVICE_UNAVAILABLE);
            case 429 -> new ApiException(
                    "AI_PROVIDER_ERROR",
                    "Gemini-Anfragenlimit erreicht. Bitte später erneut versuchen.",
                    HttpStatus.SERVICE_UNAVAILABLE);
            case 503, 502, 504 -> new ApiException(
                    "AI_UNAVAILABLE",
                    unavailableMessage,
                    HttpStatus.SERVICE_UNAVAILABLE);
            default -> exception.getHttpStatus() >= 400 && exception.getHttpStatus() < 500
                    ? new ApiException(
                            "AI_PROVIDER_ERROR",
                            "Gemini-Anfrage abgelehnt (HTTP " + exception.getHttpStatus() + ").",
                            HttpStatus.SERVICE_UNAVAILABLE)
                    : new ApiException("AI_UNAVAILABLE", unavailableMessage, HttpStatus.SERVICE_UNAVAILABLE);
        };
    }

    public static ApiException toApiException(IllegalStateException exception, String unavailableMessage) {
        return new ApiException("AI_UNAVAILABLE", unavailableMessage, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
