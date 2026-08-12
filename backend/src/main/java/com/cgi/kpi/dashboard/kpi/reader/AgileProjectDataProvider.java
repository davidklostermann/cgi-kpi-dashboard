package com.cgi.kpi.dashboard.kpi.reader;

import java.util.Optional;
import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.AgileDeliveryDto;

/**
 * Exchangeable source for agile project delivery facts (Epic 17 / Story 17.4).
 *
 * <p><strong>Current:</strong> {@code JpaAgileProjectDataProvider} reads PostgreSQL mock seed
 * ({@code dataSource = INTERNAL_MOCK}).
 *
 * <p><strong>Future Jira/PPM handoff:</strong> add a second {@code @Primary}/{@code @ConditionalOn*}
 * implementation that maps external sprint/work-item payloads into {@link AgileDeliveryDto}.
 * {@code ProjectController}, {@code ProjectKpiService}, and the frontend must not change —
 * only this provider boundary and wiring.
 *
 * <p>Workspace isolation remains mandatory: unknown/foreign project IDs return empty → 404 upstream.
 */
public interface AgileProjectDataProvider {

    Optional<AgileDeliveryDto> readAgileDelivery(UUID projectId);
}
