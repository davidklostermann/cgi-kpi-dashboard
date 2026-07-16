---
story_key: 4-3-portfolio-kpi-sektion
epic: 4
baseline_commit: 462b010608597fbf1f3e7c6604578049deadc6ed
---

# Story 4.3: Portfolio KPI-Sektion

Status: done

## Story

Als Führungskraft  
möchte ich KPI-Karten auf der Portfolio-Seite  
damit ich den Aha-Moment erlebe (NFR-1).

## Acceptance Criteria

1. **Gegeben** Portfolio-Seite, wenn geladen, dann erscheinen KPI-Karten aus Backend-Daten innerhalb von 30s Gesamterlebnis.
2. **Gegeben** API-Fehler KPI, dann Fehlerpanel nur im KPI-Bereich mit Retry.

## Tasks / Subtasks

- [x] Task 1: `PortfolioKpiSummary` Model + `getPortfolioKpis()` API (AC: 1)
- [x] Task 2: `PortfolioKpiSectionComponent` mit Signal-State (AC: 1, 2)
- [x] Task 3: 6 KPI-Karten aus Backend-Response (AC: 1)
- [x] Task 4: Fehlerpanel + Retry isoliert im KPI-Bereich (AC: 2, AD-7)
- [x] Task 5: Portfolio-Seite einbinden (AC: 1)
- [x] Task 6: Component- + API-Tests (AC: 1, 2)
- [x] Task 7: Story-Dokumentation aktualisieren

## Dev Notes

- **KPI-Regeln (kanonisch):** [`epic-4-portfolio-kpi-rules.md`](./epic-4-portfolio-kpi-rules.md)
- **AD-10:** Page injiziert kein HttpClient — API-Zugriff über `PortfolioApiService` in KPI-Sektion.
- **AD-7:** KPI-Ladefehler betrifft nur `app-portfolio-kpi-section`; KI-Panel bleibt unabhängig.
- Ampelverteilung: `Grün: N · Gelb: N · Rot: N` (+ Abgeschlossen wenn > 0). Projektanzahl-Label dynamisch nach Lifecycle-Filter.

## Dev Agent Record

### Agent Model Used

Composer

### Completion Notes List

- `PortfolioKpiSectionComponent` lädt `GET /api/portfolio/kpis` beim Init und bei Retry.
- Sechs `app-kpi-card`-Instanzen für FR-1-Kennzahlen inkl. Ampelverteilung.
- Fehlerpanel mit Backend-`message` oder Fallback-Text.

### File List

- frontend/src/app/shared/models/portfolio-kpi.model.ts
- frontend/src/app/core/api/portfolio-api.service.ts
- frontend/src/app/core/api/portfolio-api.service.spec.ts
- frontend/src/app/features/portfolio/portfolio-kpi-section.component.ts
- frontend/src/app/features/portfolio/portfolio-kpi-section.component.html
- frontend/src/app/features/portfolio/portfolio-kpi-section.component.scss
- frontend/src/app/features/portfolio/portfolio-kpi-section.component.spec.ts
- frontend/src/app/features/portfolio/portfolio-page.component.ts
- frontend/src/app/features/portfolio/portfolio-page.component.html
- frontend/src/app/features/portfolio/portfolio-page.component.spec.ts
- _bmad-output/implementation-artifacts/4-3-portfolio-kpi-sektion.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-07-15: Story 4.3 — Portfolio KPI-Sektion implementiert
