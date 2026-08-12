package com.cgi.kpi.dashboard.security.bootstrap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cgi.kpi.dashboard.domain.model.Workspace;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.infrastructure.persistence.AppUserRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceMembershipRepository;
import com.cgi.kpi.dashboard.infrastructure.persistence.WorkspaceRepository;
import com.cgi.kpi.dashboard.security.config.BootstrapProperties;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminLoggingTest {

    private static final String TEST_PASSWORD = UUID.randomUUID().toString();

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ListAppender<ILoggingEvent> appender;

    private Logger logger;

    @BeforeEach
    void attachLogAppender() {
        logger = (Logger) LoggerFactory.getLogger(BootstrapAdminService.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detachLogAppender() {
        logger.detachAppender(appender);
    }

    @Test
    void successLogDoesNotContainBootstrapPassword() {
        BootstrapProperties properties = new BootstrapProperties();
        properties.setAdminUsername("log-test-admin");
        properties.setAdminPassword(TEST_PASSWORD);

        when(appUserRepository.count()).thenReturn(0L);
        when(workspaceRepository.findById(WorkspaceIds.DEFAULT)).thenReturn(Optional.of(new Workspace()));
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("$2a$10$hash");
        when(appUserRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(workspaceMembershipRepository.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

        BootstrapAdminService service = new BootstrapAdminService(
                appUserRepository,
                workspaceRepository,
                workspaceMembershipRepository,
                properties,
                passwordEncoder);

        service.bootstrapIfNeeded();

        List<ILoggingEvent> events = appender.list;
        assertFalse(events.isEmpty());
        for (ILoggingEvent event : events) {
            assertFalse(event.getFormattedMessage().contains(TEST_PASSWORD));
        }
        assertTrue(events.stream().anyMatch(event -> event.getFormattedMessage().contains("Bootstrap admin created")));
    }
}
