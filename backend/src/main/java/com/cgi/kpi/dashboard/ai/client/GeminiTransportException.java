package com.cgi.kpi.dashboard.ai.client;

/**
 * Transport / provider failure from the Gemini HTTP API.
 */
public class GeminiTransportException extends RuntimeException {

    private final int httpStatus;
    private final String providerReason;

    public GeminiTransportException(String message) {
        this(message, 0, (String) null);
    }

    public GeminiTransportException(String message, int httpStatus) {
        this(message, httpStatus, (String) null);
    }

    public GeminiTransportException(String message, int httpStatus, String providerReason) {
        super(message);
        this.httpStatus = httpStatus;
        this.providerReason = providerReason;
    }

    public GeminiTransportException(String message, Throwable cause) {
        this(message, 0, (String) null, cause);
    }

    public GeminiTransportException(String message, int httpStatus, Throwable cause) {
        this(message, httpStatus, (String) null, cause);
    }

    public GeminiTransportException(String message, int httpStatus, String providerReason, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.providerReason = providerReason;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getProviderReason() {
        return providerReason;
    }
}
