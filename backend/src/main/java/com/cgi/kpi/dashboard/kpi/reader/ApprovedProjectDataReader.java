package com.cgi.kpi.dashboard.kpi.reader;

import java.util.Optional;
import java.util.UUID;

import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectContextDto;

/**
 * Exclusive project-data ingress for ai.* (AD-2 / FR-13 / Story 9.1).
 */
public interface ApprovedProjectDataReader {

    Optional<ApprovedProjectContextDto> readApprovedContext(UUID projectId);
}
