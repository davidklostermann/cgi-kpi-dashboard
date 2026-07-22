package com.cgi.kpi.dashboard.security.web;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cgi.kpi.dashboard.api.error.ApiErrorResponse;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MustChangePasswordFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/api/auth/me",
            "/api/auth/logout",
            "/api/auth/change-password");

    private final ObjectMapper objectMapper;

    public MustChangePasswordFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof DashboardUserDetails details
                && details.isMustChangePassword()
                && requiresPasswordChange(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(
                    response.getOutputStream(),
                    new ApiErrorResponse(
                            "PASSWORD_CHANGE_REQUIRED", "Password change required before accessing this resource"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean requiresPasswordChange(HttpServletRequest request) {
        String path = normalizePath(resolvePath(request));
        return !(ALLOWED_PATHS.contains(path) && isAllowedMethod(request.getMethod(), path));
    }

    private static String resolvePath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path != null && !path.isEmpty()) {
            return path;
        }
        path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    private static String normalizePath(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static boolean isAllowedMethod(String method, String path) {
        return switch (path) {
            case "/api/auth/me" -> HttpMethod.GET.matches(method);
            case "/api/auth/logout", "/api/auth/change-password" -> HttpMethod.POST.matches(method);
            default -> false;
        };
    }
}
