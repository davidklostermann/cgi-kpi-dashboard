package com.cgi.kpi.dashboard.security.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class BootstrapPropertiesTest {

    @Test
    void trimsUsernameForCredentialCheck() {
        BootstrapProperties properties = new BootstrapProperties();
        properties.setAdminUsername("  admin  ");
        properties.setAdminPassword(UUID.randomUUID().toString());

        assertEquals("admin", properties.trimmedUsername());
        assertTrue(properties.hasCredentials());
    }

    @Test
    void blankPasswordDisablesCredentials() {
        BootstrapProperties properties = new BootstrapProperties();
        properties.setAdminUsername("admin");
        properties.setAdminPassword("   ");

        assertFalse(properties.hasCredentials());
    }
}
