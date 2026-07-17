package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationIntegrationTest {

    private static final int EXPECTED_MIGRATION_COUNT = 7;

    @Autowired
    private Flyway flyway;

    @Test
    void appliesAllMigrationsOnEmptyDatabase() {
        MigrationInfo[] applied = flyway.info().applied();

        assertEquals(EXPECTED_MIGRATION_COUNT, applied.length);
        assertEquals("7", flyway.info().current().getVersion().getVersion());
        assertEquals(MigrationState.SUCCESS, flyway.info().current().getState());

        long successCount = java.util.Arrays.stream(flyway.info().all())
                .filter(info -> info.getState() == MigrationState.SUCCESS)
                .count();
        assertEquals(EXPECTED_MIGRATION_COUNT, successCount);
    }

    @Test
    void secondMigrateIsIdempotent() {
        assertNotNull(flyway.info().current());

        MigrateResult secondRun = flyway.migrate();

        assertEquals(0, secondRun.migrationsExecuted);
        assertEquals("7", flyway.info().current().getVersion().getVersion());
        assertTrue(flyway.validateWithResult().validationSuccessful);
    }
}
