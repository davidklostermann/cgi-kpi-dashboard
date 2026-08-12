package com.cgi.kpi.dashboard.api.error;

import org.springframework.http.HttpStatus;

/**
 * Explicit API error with stable {@code code} for frontend error panels.
 */
public class ApiException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    public ApiException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
