package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;

@SpringBootTest
@ActiveProfiles("test")
class WorkspaceUserSchemaIntegrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "WORKSPACE",
            "APP_USER",
            "WORKSPACE_MEMBERSHIP",
            "PROJECTS",
            "PROJECT_PHASES",
            "MILESTONES",
            "RISKS",
            "PROJECT_BUDGETS",
            "PROBLEMS",
            "PROJECT_REPORT_SNAPSHOTS",
            "PROJECT_ROLE_CAPACITIES",
            "PROJECT_CAPACITY_SUMMARIES",
            "USER_UI_PREFERENCES");

    @Autowired
    private DataSource dataSource;

    @Test
    void migrationCreatesAuthAndDomainTables() throws SQLException {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
                ResultSet resultSet = connection.getMetaData().getTables(null, null, null, new String[] {"TABLE"})) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("TABLE_NAME").toUpperCase());
            }
        }

        EXPECTED_TABLES.forEach(table -> assertTrue(tables.contains(table), "Missing table: " + table));
    }

    @Test
    void defaultWorkspaceExistsWithoutUsersAndProjectsBackfilled() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT name FROM workspace WHERE id = ?");
                    ) {
                statement.setObject(1, WorkspaceIds.DEFAULT);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertEquals(WorkspaceIds.DEFAULT_NAME, resultSet.getString("name"));
                    assertFalse(resultSet.next());
                }
            }

            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM workspace");
                    ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getLong(1));
            }

            try (PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) FROM app_user");
                    ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(0, resultSet.getLong(1));
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM projects WHERE workspace_id IS NULL");
                    ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals(0, resultSet.getLong(1));
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            "SELECT COUNT(*) FROM projects WHERE workspace_id = ?")) {
                statement.setObject(1, WorkspaceIds.DEFAULT);
                try (ResultSet resultSet = statement.executeQuery()) {
                    assertTrue(resultSet.next());
                    assertTrue(resultSet.getLong(1) >= 20);
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(
                            """
                            SELECT is_nullable
                            FROM information_schema.columns
                            WHERE lower(table_name) = 'projects'
                              AND lower(column_name) = 'workspace_id'
                            """);
                    ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("NO", resultSet.getString("is_nullable").toUpperCase());
            }
        }
    }
}
