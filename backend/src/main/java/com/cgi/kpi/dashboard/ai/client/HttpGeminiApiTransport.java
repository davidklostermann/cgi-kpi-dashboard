package com.cgi.kpi.dashboard.ai.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.cgi.kpi.dashboard.ai.config.AiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Calls Gemini generateContent over HTTPS. Matches the working HTML test request shape:
 * POST /v1beta/models/{model}:generateContent with x-goog-api-key and contents/parts/text body.
 */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class HttpGeminiApiTransport implements GeminiApiTransport {

    private static final Logger log = LoggerFactory.getLogger(HttpGeminiApiTransport.class);

    private static final Set<String> ALLOWED_GEMINI_HOSTS = Set.of(
            "generativelanguage.googleapis.com",
            "localhost",
            "127.0.0.1");

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public HttpGeminiApiTransport(AiProperties aiProperties, ObjectMapper objectMapper) {
        this(aiProperties, objectMapper, createHttpClient(aiProperties));
    }

    HttpGeminiApiTransport(AiProperties aiProperties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        if (!aiProperties.hasApiKey()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is required when APP_AI_PROVIDER=gemini (or app.ai.provider=gemini).");
        }
        aiProperties.requireGeminiModel();
        validateGeminiBaseUrl(aiProperties.getGeminiApiBaseUrl());
    }

    @Override
    public String generateJson(String prompt) {
        try {
            String model = aiProperties.requireGeminiModel();
            URI uri = buildGenerateContentUri(model);
            String json = objectMapper.writeValueAsString(buildRequestBody(prompt));

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(Math.max(1000, aiProperties.getTimeoutMs())))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", aiProperties.getApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            log.info(
                    "Gemini request: method=POST, url={}, model={}, contentType=application/json, apiKeyHeaderPresent={}",
                    uri,
                    model,
                    aiProperties.hasApiKey());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                ProviderError providerError = parseProviderError(response.body());
                log.warn(
                        "Gemini request failed: httpStatus={}, errorCode={}, errorStatus={}, errorMessage={}, providerReason={}, model={}",
                        response.statusCode(),
                        providerError.code() == null ? "unknown" : providerError.code(),
                        providerError.status() == null ? "unknown" : providerError.status(),
                        providerError.message() == null ? "unknown" : providerError.message(),
                        providerError.reason() == null ? "unknown" : providerError.reason(),
                        model);
                throw new GeminiTransportException(
                        describeHttpFailure(response.statusCode(), providerError),
                        response.statusCode(),
                        providerError.reason());
            }
            return extractText(response.body());
        } catch (GeminiTransportException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Gemini request interrupted: exception={}", ex.getClass().getSimpleName());
            throw new GeminiTransportException("Gemini-Anfrage unterbrochen.", ex);
        } catch (Exception ex) {
            log.warn("Gemini request failed: exception={}", ex.getClass().getSimpleName());
            throw new GeminiTransportException("Gemini-Anfrage fehlgeschlagen.", ex);
        }
    }

    static URI buildGenerateContentUri(String model, String baseUrl) {
        return URI.create(
                normalizeBaseUrl(baseUrl) + "/v1beta/models/" + model + ":generateContent");
    }

    private URI buildGenerateContentUri(String model) {
        String baseUrl = aiProperties.getGeminiApiBaseUrl();
        validateGeminiBaseUrl(baseUrl);
        return buildGenerateContentUri(model, baseUrl);
    }

    static void validateGeminiBaseUrl(String baseUrl) {
        URI uri = URI.create(normalizeBaseUrl(baseUrl));
        String host = uri.getHost();
        if (host == null || !ALLOWED_GEMINI_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
            throw new IllegalStateException(
                    "Unsupported Gemini API base URL host: "
                            + host
                            + ". Allowed hosts: generativelanguage.googleapis.com, localhost, 127.0.0.1.");
        }
        if ("generativelanguage.googleapis.com".equalsIgnoreCase(host)
                && !"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException("Gemini API base URL must use HTTPS.");
        }
    }

    static Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        return Map.of("contents", List.of(content));
    }

    private static HttpClient createHttpClient(AiProperties aiProperties) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, aiProperties.getTimeoutMs())))
                .build();
    }

    static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "https://generativelanguage.googleapis.com";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String describeHttpFailure(int statusCode, ProviderError providerError) {
        if (providerError.message() != null && !providerError.message().isBlank()) {
            return "Gemini HTTP " + statusCode + " — " + providerError.message();
        }
        return "Gemini HTTP " + statusCode + " — Anfrage fehlgeschlagen.";
    }

    private ProviderError parseProviderError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return new ProviderError(null, null, null, null);
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode error = root.path("error");
            Integer code = error.path("code").isInt() ? error.path("code").asInt() : null;
            String message = error.path("message").asText("");
            String status = error.path("status").asText("");
            String reason = null;
            for (JsonNode detail : error.path("details")) {
                if ("type.googleapis.com/google.rpc.ErrorInfo".equals(detail.path("@type").asText())) {
                    reason = detail.path("reason").asText(null);
                    break;
                }
            }
            String combinedMessage;
            if (!message.isBlank() && !status.isBlank()) {
                combinedMessage = status + ": " + message;
            } else {
                combinedMessage = message.isBlank() ? null : message;
            }
            return new ProviderError(code, status.isBlank() ? null : status, reason, combinedMessage);
        } catch (Exception ex) {
            return new ProviderError(null, null, null, null);
        }
    }

    static String extractText(String responseBody, ObjectMapper objectMapper) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.isEmpty()) {
            throw new GeminiTransportException("Gemini lieferte keine Textantwort.");
        }
        StringBuilder combined = new StringBuilder();
        for (JsonNode part : parts) {
            String text = part.path("text").asText("");
            if (!text.isBlank()) {
                combined.append(text);
            }
        }
        if (combined.isEmpty()) {
            throw new GeminiTransportException("Gemini lieferte keine Textantwort.");
        }
        return combined.toString();
    }

    private String extractText(String responseBody) throws Exception {
        return extractText(responseBody, objectMapper);
    }

    private record ProviderError(Integer code, String status, String reason, String message) {
    }
}
