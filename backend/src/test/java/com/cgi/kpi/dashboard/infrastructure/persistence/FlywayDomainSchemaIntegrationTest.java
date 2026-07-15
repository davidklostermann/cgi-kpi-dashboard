package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywayDomainSchemaIntegrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "PROJECTS",
            "PROJECT_PHASES",
            "MILESTONES",
            "RISKS",
            "PROJECT_BUDGETS",
            "PROBLEMS",
            "PROJECT_REPORT_SNAPSHOTS");

    @Autowired
    private DataSource dataSource;

    @Test
    void migrationCreatesAllDomainTables() throws SQLException {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
                ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[] {"TABLE"})) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME").toUpperCase());
            }
        }

        EXPECTED_TABLES.forEach(table -> assertTrue(tables.contains(table), "Missing table: " + table));
    }
}
