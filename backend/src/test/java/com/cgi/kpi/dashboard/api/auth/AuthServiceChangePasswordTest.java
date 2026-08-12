package com.cgi.kpi.dashboard.api.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetails;
import com.cgi.kpi.dashboard.security.user.DashboardUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceChangePasswordTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID WORKSPACE_ID = UUID.fromString("22222222-2222-4222-8222-222222222222");

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DashboardUserDetailsService dashboardUserDetailsService;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                null,
                appUserRepository,
                passwordEncoder,
                dashboardUserDetailsService,
                securityContextRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void changePasswordUsesNewPasswordFieldAndClearsMustChangeFlag() {
        authenticate(principal(true));
        AppUser user = user(true, "$2a$10$old-hash");
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-pass", "$2a$10$old-hash")).thenReturn(true);
        when(passwordEncoder.matches("new-pass-9", "$2a$10$old-hash")).thenReturn(false);
        when(passwordEncoder.encode("new-pass-9")).thenReturn("$2a$10$new-hash");
        when(dashboardUserDetailsService.loadUserByUsername("admin"))
                .thenReturn(principal(false));

        authService.changePassword("current-pass", "new-pass-9", httpServletRequest, httpServletResponse);

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(saved.capture());
        assertFalse(saved.getValue().isMustChangePassword());
        assertTrue(saved.getValue().getPasswordHash().startsWith("$2"));
        verify(passwordEncoder).encode(eq("new-pass-9"));
        verify(securityContextRepository).saveContext(any(), eq(httpServletRequest), eq(httpServletResponse));
    }

    @Test
    void changePasswordRejectsWhenNewPasswordEqualsCurrentHash() {
        authenticate(principal(true));
        AppUser user = user(true, "$2a$10$same-hash");
        when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("same-pass9", "$2a$10$same-hash")).thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.changePassword("same-pass9", "same-pass9", httpServletRequest, httpServletResponse));

        assertTrue(exception.getStatus() == HttpStatus.BAD_REQUEST);
        verify(appUserRepository, never()).save(any());
    }

    private void authenticate(DashboardUserDetails details) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    private static DashboardUserDetails principal(boolean mustChangePassword) {
        return new DashboardUserDetails(
                USER_ID,
                WORKSPACE_ID,
                "admin",
                "$2a$10$placeholder",
                true,
                mustChangePassword,
                List.of());
    }

    private static AppUser user(boolean mustChangePassword, String hash) {
        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setUsername("admin");
        user.setPasswordHash(hash);
        user.setActive(true);
        user.setMustChangePassword(mustChangePassword);
        return user;
    }
}
