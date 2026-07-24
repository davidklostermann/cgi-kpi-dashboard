package com.cgi.kpi.dashboard.infrastructure.persistence;

import com.cgi.kpi.dashboard.admin.ai.AiProviderConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiProviderConfigRepository extends JpaRepository<AiProviderConfig, UUID> {
    // We only have one config in v1, but we might have more later.
    // For now, we can just find the first one or find by provider.
    Optional<AiProviderConfig> findByUserIdAndProvider(UUID userId, String provider);
}
