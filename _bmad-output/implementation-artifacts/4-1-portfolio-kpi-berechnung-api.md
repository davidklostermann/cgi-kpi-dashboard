---
story_key: 4-1-portfolio-kpi-berechnung-api
epic: 4
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 4.1: Portfolio-KPI-Berechnung API

Status: done

## Story

Als Führungskraft  
möchte ich aggregierte Portfolio-KPIs  
damit ich die Portfolio-Gesundheit sehe.

## Acceptance Criteria

1. **Gegeben** Mock-Portfolio, wenn `GET /api/portfolio/kpis`, dann liefert: aktive Projekte, Ø-Fortschritt, Budgetabweichung, Termintreue, kritische Risiken, Statusverteilung (Zahlen).
2. **Gegeben** leeres Portfolio, dann definierter Leerzustand in Response (`empty: true`, alle Werte 0).
3. **Gegeben** Response, dann stammen alle Werte aus `kpi.*` (Berechnung in `PortfolioKpiCalculator`).

## Tasks / Subtasks

- [x] Task 1: `PortfolioKpiSummaryDto` um Statusverteilung + `empty` erweitern (AC: 1, 2)
- [x] Task 2: `PortfolioKpiCalculator` in `kpi.service` (AC: 3)
- [x] Task 3: `JpaPortfolioKpiReader` ersetzt Stub (AC: 3)
- [x] Task 4: `GET /api/portfolio/kpis` via `PortfolioController` (AC: 1)
- [x] Task 5: Unit- + API-Tests (AC: 1, 2, 3)
- [x] Task 6: Story-Dokumentation aktualisieren

## Dev Notes

- **KPI-Regeln (kanonisch):** [`epic-4-portfolio-kpi-rules.md`](./epic-4-portfolio-kpi-rules.md)
- **AD-3:** Berechnung in `kpi.service.PortfolioKpiCalculator`; Infrastructure lädt nur Daten.

## Dev Agent Record

### Agent Model Used

Composer

### Debug Log References

- `.\mvnw.cmd test` — 35/35 grün (2026-07-15)

### Completion Notes List

- `PortfolioKpiCalculator` — deterministische Aggregation in `kpi.*`.
- `JpaPortfolioKpiReader` — lädt Projekte/Budgets/Risiken, delegiert an Calculator.
- `PortfolioController` — `GET /api/portfolio/kpis`.
- Tests: `PortfolioKpiCalculatorTest` (inkl. Leerzustand), `PortfolioControllerIntegrationTest`, aktualisierter Service-Integrationstest.

### File List

- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioKpiSummaryDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioStatusDistributionDto.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioKpiProjectInput.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/dto/PortfolioKpiRiskInput.java
- backend/src/main/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiCalculator.java
- backend/src/main/java/com/cgi/kpi/dashboard/infrastructure/kpi/JpaPortfolioKpiReader.java
- backend/src/main/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioController.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiCalculatorTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/api/portfolio/PortfolioControllerIntegrationTest.java
- backend/src/test/java/com/cgi/kpi/dashboard/kpi/service/PortfolioKpiServiceIntegrationTest.java
- _bmad-output/implementation-artifacts/4-1-portfolio-kpi-berechnung-api.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 4.1 — Portfolio-KPI-Berechnung API implementiert
