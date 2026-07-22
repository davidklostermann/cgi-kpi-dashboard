package com.cgi.kpi.dashboard.security.bootstrap;

import java.util.UUID;

import org.springframework.test.context.DynamicPropertyRegistry;

final class IsolatedH2Database {

    private IsolatedH2Database() {
    }

    static void register(DynamicPropertyRegistry registry, String name) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:h2:mem:"
                        + name
                        + "-"
                        + UUID.randomUUID()
                        + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH");
    }

    static void disableBootstrapCredentials(DynamicPropertyRegistry registry) {
        registry.add("app.bootstrap.admin-username", () -> "");
        registry.add("app.bootstrap.admin-password", () -> "");
    }
}
