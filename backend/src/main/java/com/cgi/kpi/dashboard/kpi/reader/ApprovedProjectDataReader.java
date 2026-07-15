package com.cgi.kpi.dashboard.kpi.reader;

import com.cgi.kpi.dashboard.kpi.dto.ApprovedProjectDataDto;

import java.util.Optional;
import java.util.UUID;

/**
 * Liefert freigegebene Projektdaten für {@code ai.*} — einzige AI-Datenquelle auf Projektebene (AD-2).
 */
public interface ApprovedProjectDataReader {

    Optional<ApprovedProjectDataDto> readApprovedProjectData(UUID projectId);
}
