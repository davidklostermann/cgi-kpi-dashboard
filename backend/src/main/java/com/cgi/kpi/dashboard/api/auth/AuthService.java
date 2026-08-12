package com.cgi.kpi.dashboard.api.auth;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.api.auth.dto.AuthMeResponseDto;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetails;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final DashboardUserDetailsService dashboardUserDetailsService;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(
            AuthenticationManager authenticationManager,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            DashboardUserDetailsService dashboardUserDetailsService,
            SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.dashboardUserDetailsService = dashboardUserDetailsService;
        this.securityContextRepository = securityContextRepository;
    }

    public AuthMeResponseDto login(
            String username, String password, HttpServletRequest request, HttpServletResponse response) {
        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new ApiException("BAD_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedUsername, password));
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            return toMeResponse(requireDashboardUserDetails(authentication));
        } catch (AuthenticationException exception) {
            if (isDisabled(exception)) {
                throw new ApiException("ACCOUNT_DISABLED", "Account is disabled", HttpStatus.FORBIDDEN);
            }
            throw new ApiException("BAD_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    public AuthMeResponseDto currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return toMeResponse(requireDashboardUserDetails(authentication));
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

    @Transactional
    public void changePassword(
            String currentPassword,
            String newPassword,
            HttpServletRequest request,
            HttpServletResponse response) {
        DashboardUserDetails principal = requireCurrentUser();
        AppUser user = appUserRepository
                .findById(principal.getUserId())
                .orElseThrow(() -> new ApiException("NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ApiException("BAD_CREDENTIALS", "Current password is incorrect", HttpStatus.UNAUTHORIZED);
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new ApiException("BAD_REQUEST", "New password must differ from current password", HttpStatus.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        appUserRepository.save(user);

        DashboardUserDetails refreshed =
                (DashboardUserDetails) dashboardUserDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(refreshed, null, refreshed.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    private DashboardUserDetails requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return requireDashboardUserDetails(authentication);
    }

    private static DashboardUserDetails requireDashboardUserDetails(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof DashboardUserDetails details)) {
            throw new ApiException("UNAUTHORIZED", "Authentication required", HttpStatus.UNAUTHORIZED);
        }
        return details;
    }

    private static AuthMeResponseDto toMeResponse(DashboardUserDetails details) {
        List<String> roles = details.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();
        return new AuthMeResponseDto(
                details.getUserId(),
                details.getWorkspaceId(),
                details.getUsername(),
                roles,
                details.isMustChangePassword());
    }

    private static boolean isDisabled(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return true;
        }
        Throwable cause = exception.getCause();
        return cause instanceof DisabledException;
    }
}
