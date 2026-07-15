package com.cgi.kpi.dashboard.api.error;

/**
 * Uniform API error body — Architecture Spine Consistency Conventions.
 */
public record ApiErrorResponse(String code, String message) {
}
