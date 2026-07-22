package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cgi.kpi.dashboard.domain.model.WorkspaceMembership;
import com.cgi.kpi.dashboard.domain.model.WorkspaceRole;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {

    Optional<WorkspaceMembership> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);

    Optional<WorkspaceMembership> findByUserId(UUID userId);

    List<WorkspaceMembership> findAllByRole(WorkspaceRole role);

    @Query("SELECT COUNT(m) FROM WorkspaceMembership m JOIN AppUser u ON m.userId = u.id WHERE m.role = :role AND u.active = true AND u.id <> :excludeUserId")
    long countActiveMembersWithRoleExcept(@Param("role") WorkspaceRole role, @Param("excludeUserId") UUID excludeUserId);
}
