---
story_key: 4-4-portfolio-filter
epic: 4
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 4.4: Portfolio-Filter

Status: done

## Story

Als Führungskraft  
möchte ich filtern  
damit ich das Portfolio eingrenze.

## Acceptance Criteria

1. **Gegeben** Filter (Kunde, Projektleitung, Ampelstatus, Phase, aktiv/abgeschlossen, Zeitraum, Risikostufe), wenn gesetzt, dann aktualisiert KPI-API-Aufruf konsistent.
2. **Gegeben** Mehrfachfilter, dann sind sie kombinierbar.
3. **Gegeben** gefilterte Leermenge, dann Hinweis + „Filter zurücksetzen".

## Tasks / Subtasks

- [x] Task 1: `PortfolioFilterCriteria` + `PortfolioProjectFilter` in `kpi.*` (AC: 1, 2)
- [x] Task 2: Query-Parameter an `GET /api/portfolio/kpis` (AC: 1)
- [x] Task 3: `GET /api/portfolio/filters/options` für UI-Werte (AC: 1)
- [x] Task 4: `PortfolioFilterService` + `PortfolioFilterBarComponent` (AC: 1, 2, 3)
- [x] Task 5: KPI-Sektion reagiert auf Filter + Leerzustand (AC: 3)
- [x] Task 6: API- + UI-Tests (AC: 1, 2, 3)
- [x] Task 7: Story-Dokumentation aktualisieren

## Dev Notes

- **KPI-Regeln (kanonisch):** [`epic-4-portfolio-kpi-rules.md`](./epic-4-portfolio-kpi-rules.md)
- Filterlogik in `kpi.service.PortfolioProjectFilter` (AD-3); Infrastructure lädt Rohdaten.
- Default `lifecycle=active` — entspricht bisherigem KPI-Verhalten (19 aktive Projekte).
- Berichtsmonat: `YYYY-MM` aus `last_data_update`.
- Risikostufe: Projekte mit offenem Risiko der gewählten Severity.
- KI-Trend-Filter bewusst aus Scope (Epic 8).

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- Backend: filterbare KPI-API, Filter-Options-Endpunkt, Unit-/Integrationstests.
- Frontend: `filter-bar`, shared `PortfolioFilterService`, KPI-Reload bei Filteränderung.
- Leerzustand: „Keine Projekte für diesen Filter" + Reset.

### File List

- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioFilterCriteria.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioFilterOptionsDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioKpiProjectInput.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioKpiRiskInput.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioProjectFilter.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiCalculator.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/kpi/JpaPortfolioKpiReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioController.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioProjectFilterTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioControllerIntegrationTest.java
- frontend/src/app/shared/models/portfolio-filter.model.ts
- frontend/src/app/features/portfolio/portfolio-filter.service.ts
- frontend/src/app/features/portfolio/portfolio-filter-bar.component.*
- frontend/src/app/features/portfolio/portfolio-kpi-section.component.*
- frontend/src/app/features/portfolio/portfolio-page.component.*
- frontend/src/app/core/api/api-client.service.ts
- frontend/src/app/core/api/portfolio-api.service.ts
- _bmad-output/implementation-artifacts/4-4-portfolio-filter.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 4.4 — Portfolio-Filter implementiert
