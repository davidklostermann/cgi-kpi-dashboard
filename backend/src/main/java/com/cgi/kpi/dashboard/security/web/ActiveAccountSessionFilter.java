package com.cgi.kpi.dashboard.security.web;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cgi.kpi.dashboard.api.error.ApiErrorResponse;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetails;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Re-validates authenticated sessions against current DB state (Story 11.6).
 * Deactivated users are logged out and receive 401 on the next request.
 */
@Component
public class ActiveAccountSessionFilter extends OncePerRequestFilter {

    private final DashboardUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    public ActiveAccountSessionFilter(
            DashboardUserDetailsService userDetailsService, ObjectMapper objectMapper) {
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof DashboardUserDetails details) {
            try {
                UserDetails refreshed = userDetailsService.loadUserByUsername(details.getUsername());
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(new UsernamePasswordAuthenticationToken(
                        refreshed, null, refreshed.getAuthorities()));
                SecurityContextHolder.setContext(context);
            } catch (DisabledException | UsernameNotFoundException exception) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(
                        response.getOutputStream(),
                        new ApiErrorResponse("UNAUTHORIZED", "Authentication required"));
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
