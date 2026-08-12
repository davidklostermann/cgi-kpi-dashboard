package com.cgi.kpi.dashboard.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cgi.kpi.dashboard.domain.model.UserUiPreferences;

public interface UserUiPreferencesRepository extends JpaRepository<UserUiPreferences, UUID> {

    Optional<UserUiPreferences> findByWorkspaceIdAndUserId(UUID workspaceId, UUID userId);
}
