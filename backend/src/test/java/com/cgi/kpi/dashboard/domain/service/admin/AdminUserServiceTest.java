package com.cgi.kpi.dashboard.domain.service.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cgi.kpi.dashboard.api.admin.dto.UpdateUserRequestDto;
import com.cgi.kpi.dashboard.api.error.ApiException;
import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.security.user.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(
                appUserRepository,
                workspaceMembershipRepository,
                passwordEncoder,
                currentUserService
        );
    }

    @Test
    void shouldPreventDeactivatingLastAdmin() {
        // Given
        UUID adminId = UUID.randomUUID();
        AppUser admin = new AppUser();
        admin.setId(adminId);
        admin.setActive(true);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setUserId(adminId);
        membership.setRole(WorkspaceRole.ADMIN);

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(workspaceMembershipRepository.findByUserId(adminId)).thenReturn(Optional.of(membership));
        // No other active admins
        when(workspaceMembershipRepository.countActiveMembersWithRoleExcept(WorkspaceRole.ADMIN, adminId)).thenReturn(0L);

        UpdateUserRequestDto request = new UpdateUserRequestDto(false, WorkspaceRole.ADMIN, false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> adminUserService.updateUser(adminId, request));
        assertEquals("LAST_ADMIN_PROTECTION", exception.getCode());
    }

    @Test
    void shouldPreventDowngradingLastAdmin() {
        // Given
        UUID adminId = UUID.randomUUID();
        AppUser admin = new AppUser();
        admin.setId(adminId);
        admin.setActive(true);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setUserId(adminId);
        membership.setRole(WorkspaceRole.ADMIN);

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(workspaceMembershipRepository.findByUserId(adminId)).thenReturn(Optional.of(membership));
        // No other active admins
        when(workspaceMembershipRepository.countActiveMembersWithRoleExcept(WorkspaceRole.ADMIN, adminId)).thenReturn(0L);

        UpdateUserRequestDto request = new UpdateUserRequestDto(true, WorkspaceRole.USER, false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> adminUserService.updateUser(adminId, request));
        assertEquals("LAST_ADMIN_PROTECTION", exception.getCode());
    }

    @Test
    void shouldAllowDeactivatingAdminIfAnotherExists() {
        // Given
        UUID adminId = UUID.randomUUID();
        AppUser admin = new AppUser();
        admin.setId(adminId);
        admin.setActive(true);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setUserId(adminId);
        membership.setRole(WorkspaceRole.ADMIN);

        when(appUserRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(workspaceMembershipRepository.findByUserId(adminId)).thenReturn(Optional.of(membership));
        // One other active admin exists
        when(workspaceMembershipRepository.countActiveMembersWithRoleExcept(WorkspaceRole.ADMIN, adminId)).thenReturn(1L);

        UpdateUserRequestDto request = new UpdateUserRequestDto(false, WorkspaceRole.ADMIN, false);

        // When
        adminUserService.updateUser(adminId, request);

        // Then
        assertFalse(admin.isActive());
        verify(appUserRepository).saveAndFlush(admin);
    }
}
