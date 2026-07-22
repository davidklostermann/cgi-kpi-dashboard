package com.cgi.kpi.dashboard.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.cgi.kpi.dashboard.domain.model.AppUser;
import com.cgi.kpi.dashboard.domain.model.Workspace;
import com.cgi.kpi.dashboard.domain.model.WorkspaceIds;
import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

@SpringBootTest
@ActiveProfiles("test")
class WorkspaceMembershipPersistenceIntegrationTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private WorkspaceMembershipRepository workspaceMembershipRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void loadsDefaultWorkspaceAndPersistsMembershipRoles() {
        Workspace workspace = workspaceRepository.findById(WorkspaceIds.DEFAULT).orElseThrow();
        assertEquals(WorkspaceIds.DEFAULT_NAME, workspace.getName());

        AppUser user = new AppUser();
        user.setUsername("story11-user");
        user.setPasswordHash("{noop}not-a-real-login-hash");
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(user.getId());
        membership.setRole(WorkspaceRole.ADMIN);
        workspaceMembershipRepository.saveAndFlush(membership);

        WorkspaceMembership loaded = workspaceMembershipRepository
                .findByWorkspaceIdAndUserId(WorkspaceIds.DEFAULT, user.getId())
                .orElseThrow();
        assertEquals(WorkspaceRole.ADMIN, loaded.getRole());

        AppUser second = new AppUser();
        second.setUsername("story11-user-2");
        second.setPasswordHash("{noop}not-a-real-login-hash");
        appUserRepository.saveAndFlush(second);

        WorkspaceMembership userRole = new WorkspaceMembership();
        userRole.setWorkspaceId(WorkspaceIds.DEFAULT);
        userRole.setUserId(second.getId());
        userRole.setRole(WorkspaceRole.USER);
        workspaceMembershipRepository.saveAndFlush(userRole);

        assertEquals(
                WorkspaceRole.USER,
                workspaceMembershipRepository
                        .findByWorkspaceIdAndUserId(WorkspaceIds.DEFAULT, second.getId())
                        .orElseThrow()
                        .getRole());
    }

    @Test
    @Transactional
    void rejectsDuplicateUsername() {
        AppUser first = new AppUser();
        first.setUsername("story11-dup-user");
        first.setPasswordHash("{noop}not-a-real-login-hash");
        appUserRepository.saveAndFlush(first);

        AppUser duplicate = new AppUser();
        duplicate.setUsername("story11-dup-user");
        duplicate.setPasswordHash("{noop}another-hash");

        DataIntegrityViolationException thrown =
                assertThrows(DataIntegrityViolationException.class, () -> appUserRepository.saveAndFlush(duplicate));
        assertTrue(hasConstraintViolation(thrown, "23505", "uq_app_user_username", "unique"));
    }

    @Test
    @Transactional
    void rejectsDuplicateMembershipForSameWorkspaceAndUser() {
        AppUser user = new AppUser();
        user.setUsername("story11-dup");
        user.setPasswordHash("{noop}not-a-real-login-hash");
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership first = new WorkspaceMembership();
        first.setWorkspaceId(WorkspaceIds.DEFAULT);
        first.setUserId(user.getId());
        first.setRole(WorkspaceRole.USER);
        workspaceMembershipRepository.saveAndFlush(first);

        WorkspaceMembership duplicate = new WorkspaceMembership();
        duplicate.setWorkspaceId(WorkspaceIds.DEFAULT);
        duplicate.setUserId(user.getId());
        duplicate.setRole(WorkspaceRole.ADMIN);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class, () -> workspaceMembershipRepository.saveAndFlush(duplicate));
        assertTrue(hasConstraintViolation(thrown, "23505", "uq_workspace_membership_workspace_user", "unique"));
    }

    @Test
    @Transactional
    void rejectsMembershipWithUnknownWorkspaceId() {
        AppUser user = new AppUser();
        user.setUsername("story11-bad-ws");
        user.setPasswordHash("{noop}not-a-real-login-hash");
        appUserRepository.saveAndFlush(user);

        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(UUID.fromString("00000000-0000-4000-8000-000000000099"));
        membership.setUserId(user.getId());
        membership.setRole(WorkspaceRole.USER);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class, () -> workspaceMembershipRepository.saveAndFlush(membership));
        assertTrue(hasConstraintViolation(thrown, "23503", "fk_workspace_membership_workspace", "foreign key"));
    }

    @Test
    @Transactional
    void rejectsMembershipWithUnknownUserId() {
        WorkspaceMembership membership = new WorkspaceMembership();
        membership.setWorkspaceId(WorkspaceIds.DEFAULT);
        membership.setUserId(UUID.fromString("00000000-0000-4000-8000-000000000099"));
        membership.setRole(WorkspaceRole.USER);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class, () -> workspaceMembershipRepository.saveAndFlush(membership));
        assertTrue(hasConstraintViolation(thrown, "23503", "fk_workspace_membership_user", "foreign key"));
    }

    @Test
    @Transactional
    void rejectsInvalidMembershipRoleAtDatabase() {
        AppUser user = new AppUser();
        user.setUsername("story11-bad-role");
        user.setPasswordHash("{noop}not-a-real-login-hash");
        appUserRepository.saveAndFlush(user);

        PersistenceException thrown = assertThrows(PersistenceException.class, () -> {
            entityManager
                    .createNativeQuery(
                            """
                            INSERT INTO workspace_membership
                                (id, workspace_id, user_id, role, created_at, updated_at, version)
                            VALUES
                                (?1, ?2, ?3, 'SUPERADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                            """)
                    .setParameter(1, UUID.randomUUID())
                    .setParameter(2, WorkspaceIds.DEFAULT)
                    .setParameter(3, user.getId())
                    .executeUpdate();
            entityManager.flush();
        });

        assertTrue(
                hasConstraintViolation(thrown, "23514", "chk_workspace_membership_role", "check constraint")
                        || hasConstraintViolation(thrown, "23513", "chk_workspace_membership_role", "check"),
                "Expected check constraint failure for invalid role");
    }

    private static boolean hasConstraintViolation(Throwable throwable, String sqlState, String constraintHint, String messageHint) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                if (sqlState.equals(sqlException.getSQLState())) {
                    return true;
                }
                String message = sqlException.getMessage();
                if (message != null
                        && (message.toLowerCase().contains(constraintHint.toLowerCase())
                                || message.toLowerCase().contains(messageHint.toLowerCase()))) {
                    return true;
                }
            }
            String message = current.getMessage();
            if (message != null
                    && (message.toLowerCase().contains(constraintHint.toLowerCase())
                            || message.toLowerCase().contains(messageHint.toLowerCase()))) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
