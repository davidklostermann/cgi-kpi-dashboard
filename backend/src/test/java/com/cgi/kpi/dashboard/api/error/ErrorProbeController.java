package com.cgi.kpi.dashboard.api.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Test-only controller for {@link GlobalExceptionHandlerWebMvcTest}. */
@RestController
class ErrorProbeController {

    @GetMapping("/api/probe/api-error")
    void apiError() {
        throw new ApiException("NOT_FOUND", "Resource missing", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/api/probe/unhandled")
    void unhandled() {
        throw new RuntimeException("probe failure");
    }
}
