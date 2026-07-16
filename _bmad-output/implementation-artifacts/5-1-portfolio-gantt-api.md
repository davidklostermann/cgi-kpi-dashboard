---
story_key: 5-1-portfolio-gantt-api
epic: 5
baseline_commit: 6368a0db1d44a618f5f15b0fd305cf930fb99e0e
---

# Story 5.1: Portfolio-Gantt API

Status: review

## Story

Als Führungskraft  
möchte ich Zeitleisten-Daten  
damit ich Terminlage im Portfolio sehe.

## Acceptance Criteria

1. **Gegeben** Portfolio, wenn `GET /api/portfolio/timeline`, dann je Projekt: Name, Start, Ende, Phasen, Meilensteine, Plan-Ist-Abweichung, Status (Wort).
2. **Gegeben** Response, dann berechnet in `kpi.*`.
3. **Gegeben** Portfolio-Filter (FR-8), wenn gesetzt, dann gleiche Filterparameter wie KPI-API.

## Tasks / Subtasks

- [x] Task 1: Timeline-DTOs in `kpi.dto` (AC: 1)
- [x] Task 2: `PortfolioTimelineAssembler` in `kpi.service` (AC: 1, 2)
- [x] Task 3: `PortfolioTimelineReader` + JPA-Implementierung (AC: 2)
- [x] Task 4: `GET /api/portfolio/timeline` in `PortfolioController` (AC: 1, 3)
- [x] Task 5: Unit- + API-Tests (AC: 1, 2, 3)
- [x] Task 6: Story-Dokumentation aktualisieren

## Dev Notes

- **KPI-Regeln:** [`epic-4-portfolio-kpi-rules.md`](./epic-4-portfolio-kpi-rules.md) (Filter); Timeline-Assembly in `kpi.service.PortfolioTimelineAssembler`.
- **AD-3:** Infrastructure lädt Rohdaten; Assembly/Labels in `kpi.*`.
- **Status-Wortlabels:** ON_TRACK → „Auf Kurs", AT_RISK → „Beobachten", CRITICAL → „Kritisch", COMPLETED → „Abgeschlossen".
- **Plan-Ist-Abweichung:** `scheduleDeviationDays` (positiv = Verzug); `plannedEndDate`, `forecastEndDate`, `actualEndDate`.
- **Filter:** Wiederverwendung `PortfolioFilterCriteria` + `PortfolioProjectFilter` (Default `lifecycle=active`).
- **Nicht enthalten:** Gantt-UI (Story 5.2), Tabelle (5.3).

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- `GET /api/portfolio/timeline` mit Projektzeilen inkl. Phasen, Meilensteine, Verzug und deutschen Statuslabels.
- Filter-Sync mit KPI-API über gleiche Query-Parameter.
- Backend-Tests: Assembler, Statuslabels, API-Integration, Service-Integration.

### File List

- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioTimelineDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioTimelineProjectDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioTimelinePhaseDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioTimelineMilestoneDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioStatusLabels.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioTimelineAssembler.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/reader/PortfolioTimelineReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/kpi/JpaPortfolioTimelineReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiService.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/DefaultPortfolioKpiService.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioController.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioTimelineAssemblerTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioStatusLabelsTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioControllerIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiServiceIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/PortfolioKpiServiceContractTest.java
- _bmad-output/implementation-artifacts/5-1-portfolio-gantt-api.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-16: Story 5.1 — Portfolio-Gantt API implementiert
