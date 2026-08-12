package com.cgi.kpi.dashboard.security.bootstrap;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cgi.kpi.dashboard.domain.model.Workspace;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceRepository;
import com.cgi.kpi.dashboard.security.config.BootstrapProperties;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private BootstrapProperties bootstrapProperties;

    private BootstrapAdminService bootstrapAdminService;

    @BeforeEach
    void setUp() {
        bootstrapProperties = new BootstrapProperties();
        bootstrapAdminService = new BootstrapAdminService(
                appUserRepository,
                workspaceRepository,
                workspaceMembershipRepository,
                bootstrapProperties,
                passwordEncoder);
    }

    @Test
    void skipsWhenUsersAlreadyExist() {
        when(appUserRepository.count()).thenReturn(1L);
        bootstrapProperties.setAdminUsername("existing-user");
        bootstrapProperties.setAdminPassword(UUID.randomUUID().toString());

        bootstrapAdminService.bootstrapIfNeeded();

        verify(appUserRepository, never()).saveAndFlush(any());
        verify(workspaceMembershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void skipsWhenCredentialsAreMissing() {
        when(appUserRepository.count()).thenReturn(0L);

        bootstrapAdminService.bootstrapIfNeeded();

        verify(appUserRepository, never()).saveAndFlush(any());
        verify(workspaceMembershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void skipsWhenUsernameIsBlankAfterTrim() {
        when(appUserRepository.count()).thenReturn(0L);
        bootstrapProperties.setAdminUsername("   ");
        bootstrapProperties.setAdminPassword(UUID.randomUUID().toString());

        bootstrapAdminService.bootstrapIfNeeded();

        verify(appUserRepository, never()).saveAndFlush(any());
        verify(workspaceMembershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void skipsWhenUsernameExceedsMaxLength() {
        when(appUserRepository.count()).thenReturn(0L);
        when(workspaceRepository.findById(WorkspaceIds.DEFAULT)).thenReturn(Optional.of(new Workspace()));
        bootstrapProperties.setAdminUsername("a".repeat(101));
        bootstrapProperties.setAdminPassword(UUID.randomUUID().toString());

        bootstrapAdminService.bootstrapIfNeeded();

        verify(appUserRepository, never()).saveAndFlush(any());
        verify(workspaceMembershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void failsWithRollbackWhenDefaultWorkspaceIsMissing() {
        when(appUserRepository.count()).thenReturn(0L);
        when(workspaceRepository.findById(WorkspaceIds.DEFAULT)).thenReturn(Optional.empty());
        bootstrapProperties.setAdminUsername("bootstrap-user");
        bootstrapProperties.setAdminPassword(UUID.randomUUID().toString());

        DefaultWorkspaceMissingException thrown = assertThrows(
                DefaultWorkspaceMissingException.class, () -> bootstrapAdminService.bootstrapIfNeeded());

        assertTrue(thrown.getMessage().contains(WorkspaceIds.DEFAULT.toString()));
        verify(appUserRepository, never()).saveAndFlush(any());
        verify(workspaceMembershipRepository, never()).saveAndFlush(any());
    }

    @Test
    void encodesPasswordWithoutTrimmingWhitespace() {
        String rawPassword = "  " + UUID.randomUUID() + "  ";
        when(appUserRepository.count()).thenReturn(0L);
        when(workspaceRepository.findById(WorkspaceIds.DEFAULT)).thenReturn(Optional.of(new Workspace()));
        when(passwordEncoder.encode(rawPassword)).thenReturn("$2a$10$hash");
        when(appUserRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workspaceMembershipRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        bootstrapProperties.setAdminUsername("bootstrap-user");
        bootstrapProperties.setAdminPassword(rawPassword);

        bootstrapAdminService.bootstrapIfNeeded();

        verify(passwordEncoder).encode(eq(rawPassword));
    }
}
